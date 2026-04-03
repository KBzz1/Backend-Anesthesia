#!/usr/bin/env python3
import base64
import hashlib
import json
import os
import secrets
import socket
import ssl
import struct
import time
import urllib.request
from urllib.parse import urlparse


BASE_URL = os.environ.get("BASE_URL", "https://127.0.0.1:8080").rstrip("/")
REPORT_DIR = os.environ.get("REPORT_DIR", "/home/kbzz1/20260131/backend/reports")
RESULTS_NDJSON = os.path.join(REPORT_DIR, "test-results.ndjson")
CTX = ssl._create_unverified_context()


def record(name, status, detail="", category="websocket"):
    item = {
        "suite": "ws",
        "name": name,
        "status": status,
        "detail": detail,
        "category": category,
        "ts": int(time.time()),
    }
    with open(RESULTS_NDJSON, "a", encoding="utf-8") as f:
        f.write(json.dumps(item, ensure_ascii=False) + "\n")


def http_post(path, body):
    data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(
        url=BASE_URL + path,
        data=data,
        method="POST",
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, context=CTX, timeout=10) as resp:
            return resp.getcode(), resp.read().decode("utf-8", errors="replace")
    except Exception as e:
        return None, str(e)


class RawWebSocket:
    def __init__(self, ws_url):
        self.ws_url = ws_url
        self.sock = None
        self.buffer = ""

    def connect(self):
        u = urlparse(self.ws_url)
        host = u.hostname
        port = u.port or (443 if u.scheme == "wss" else 80)
        path = u.path or "/"

        s = socket.create_connection((host, port), timeout=10)
        if u.scheme == "wss":
            s = ssl._create_unverified_context().wrap_socket(s, server_hostname=host)

        key = base64.b64encode(secrets.token_bytes(16)).decode()
        req = (
            f"GET {path} HTTP/1.1\r\n"
            f"Host: {host}:{port}\r\n"
            "Upgrade: websocket\r\n"
            "Connection: Upgrade\r\n"
            f"Sec-WebSocket-Key: {key}\r\n"
            "Sec-WebSocket-Version: 13\r\n"
            "\r\n"
        )
        s.sendall(req.encode())
        resp = s.recv(4096).decode("utf-8", errors="replace")
        if not resp.startswith("HTTP/1.1 101"):
            raise RuntimeError(f"websocket handshake failed: {resp[:200]}")

        self.sock = s

    def close(self):
        if self.sock:
            try:
                self.sock.close()
            except Exception:
                pass
        self.sock = None

    def send_text(self, text):
        payload = text.encode("utf-8")
        fin_opcode = 0x81
        mask_bit = 0x80
        header = bytearray([fin_opcode])
        length = len(payload)
        if length < 126:
            header.append(mask_bit | length)
        elif length < (1 << 16):
            header.append(mask_bit | 126)
            header.extend(struct.pack("!H", length))
        else:
            header.append(mask_bit | 127)
            header.extend(struct.pack("!Q", length))
        mask = secrets.token_bytes(4)
        header.extend(mask)
        masked = bytes(payload[i] ^ mask[i % 4] for i in range(length))
        self.sock.sendall(header + masked)

    def recv_text(self, timeout=10):
        self.sock.settimeout(timeout)
        first = self.sock.recv(2)
        if len(first) < 2:
            return ""
        b1, b2 = first[0], first[1]
        opcode = b1 & 0x0F
        masked = (b2 & 0x80) != 0
        length = b2 & 0x7F
        if length == 126:
            length = struct.unpack("!H", self.sock.recv(2))[0]
        elif length == 127:
            length = struct.unpack("!Q", self.sock.recv(8))[0]
        mask_key = b""
        if masked:
            mask_key = self.sock.recv(4)
        payload = b""
        while len(payload) < length:
            chunk = self.sock.recv(length - len(payload))
            if not chunk:
                break
            payload += chunk
        if masked:
            payload = bytes(payload[i] ^ mask_key[i % 4] for i in range(len(payload)))
        if opcode == 0x8:
            return ""
        return payload.decode("utf-8", errors="replace")


class StompClient:
    def __init__(self, ws_url):
        self.ws = RawWebSocket(ws_url)
        self.stomp_buf = ""

    def connect(self):
        self.ws.connect()
        frame = "CONNECT\naccept-version:1.2\nheart-beat:0,0\nhost:localhost\n\n\0"
        self.ws.send_text(frame)
        f = self.read_frame(timeout=10)
        if not f.startswith("CONNECTED"):
            raise RuntimeError(f"stomp CONNECT failed: {f[:200]}")

    def close(self):
        try:
            self.ws.send_text("DISCONNECT\n\n\0")
        except Exception:
            pass
        self.ws.close()

    def send(self, destination, body, headers=None):
        headers = headers or {}
        h = [f"destination:{destination}", "content-type:application/json"]
        for k, v in headers.items():
            h.append(f"{k}:{v}")
        frame = "SEND\n" + "\n".join(h) + "\n\n" + body + "\0"
        self.ws.send_text(frame)

    def subscribe(self, destination, sid):
        frame = f"SUBSCRIBE\nid:{sid}\ndestination:{destination}\nack:auto\n\n\0"
        self.ws.send_text(frame)

    def read_frame(self, timeout=8):
        end = time.time() + timeout
        while time.time() < end:
            if "\0" in self.stomp_buf:
                frame, self.stomp_buf = self.stomp_buf.split("\0", 1)
                return frame
            text = self.ws.recv_text(timeout=max(1, int(end - time.time())))
            if not text:
                continue
            self.stomp_buf += text
        return ""


def ws_url_from_base():
    if BASE_URL.startswith("https://"):
        return "wss://" + BASE_URL[len("https://"):] + "/ws"
    return "ws://" + BASE_URL[len("http://"):] + "/ws"


def assert_frame(name, frame, must_contain):
    if frame and must_contain in frame:
        record(name, "pass")
        return True
    record(name, "fail", f"expected '{must_contain}', got '{frame[:200] if frame else 'EMPTY'}'")
    return False


def main():
    os.makedirs(REPORT_DIR, exist_ok=True)
    ws_url = ws_url_from_base()

    try:
        c = StompClient(ws_url)
        c.connect()
        record("ws_handshake_stomp_connect", "pass")
    except Exception as e:
        record("ws_handshake_stomp_connect", "fail", str(e))
        return

    try:
        # area subscribe + call
        c.subscribe("/data/area/b1", "sub-area")
        payload = json.dumps({
            "type": "CALL_NUMBER",
            "fromArea": "c",
            "toArea": "b1",
            "content": {"surgeryId": 1001, "patientName": "测试患者"},
        }, ensure_ascii=False)
        c.send("/data/area/call", payload)
        frame = c.read_frame(timeout=8)
        assert_frame("ws_area_call", frame, "/data/area/b1")

        # status statistics subscribe + trigger
        c.subscribe("/data/patients/status/statistics", "sub-stat")
        http_post("/patients/status", {"surgeryId": 1, "statusCode": 2})
        frame2 = c.read_frame(timeout=8)
        assert_frame("ws_status_statistics", frame2, "/data/patients/status/statistics")

        # mqtt publish ack topic
        c.subscribe("/data/pub/response", "sub-pub-ack")
        c.send("/data/pub/E2:7A:4C:19:B3:65", json.dumps({
            "ecg": 100, "resp": 25, "bo": 95, "hr": 75, "temp": 36.6, "boWave": 90, "respWave": 90,
            "timestamp": int(time.time() * 1000),
        }))
        frame3 = c.read_frame(timeout=8)
        assert_frame("ws_pub_response", frame3, "/data/pub/response")

        # reconnect test
        c.close()
        c2 = StompClient(ws_url)
        c2.connect()
        c2.subscribe("/data/area/a", "sub-reconnect")
        payload2 = json.dumps({
            "type": "CALL_NUMBER",
            "fromArea": "c",
            "toArea": "a",
            "content": {"surgeryId": 1002, "patientName": "重连测试"},
        }, ensure_ascii=False)
        c2.send("/data/area/call", payload2)
        frame4 = c2.read_frame(timeout=8)
        assert_frame("ws_reconnect_resubscribe", frame4, "/data/area/a")
        c2.close()
    except Exception as e:
        record("ws_suite_runtime", "fail", str(e))


if __name__ == "__main__":
    main()
