#!/usr/bin/env node

const httpBase = process.argv[2] ?? "http://127.0.0.1:18080";
const wsUrl = process.argv[3] ?? "ws://127.0.0.1:18080/ws";
const surgeryId = process.argv[4] ?? "0";
const targetStatus = Number(process.argv[5] ?? "2");
const timeoutMs = 12000;

function frame(command, headers = {}, body = "") {
  const headerLines = Object.entries(headers).map(([k, v]) => `${k}:${v}`);
  return `${command}\n${headerLines.join("\n")}\n\n${body}\u0000`;
}

function parseFrames(raw) {
  return raw
    .split("\u0000")
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part) => {
      const [headerPart, ...bodyParts] = part.split("\n\n");
      const lines = headerPart.split("\n");
      const command = lines[0];
      const headers = {};
      for (const line of lines.slice(1)) {
        const idx = line.indexOf(":");
        if (idx > -1) {
          headers[line.slice(0, idx)] = line.slice(idx + 1);
        }
      }
      return { command, headers, body: bodyParts.join("\n\n") };
    });
}

async function jsonFetch(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      "content-type": "application/json",
      ...(options.headers ?? {}),
    },
  });
  const text = await response.text();
  return { status: response.status, text };
}

const initialStatusResponse = await jsonFetch(`${httpBase}/patients/status/${surgeryId}`);
if (initialStatusResponse.status !== 200) {
  console.error(`Failed to read initial status: ${initialStatusResponse.status} ${initialStatusResponse.text}`);
  process.exit(1);
}

let initialStatusCode = null;
try {
  initialStatusCode = JSON.parse(initialStatusResponse.text).data?.statusCode ?? null;
} catch (error) {
  console.error(`Failed to parse initial status response: ${error.message}`);
  process.exit(1);
}

const destination = `/data/patients/status/${targetStatus}`;
const ws = new WebSocket(wsUrl);
let timeoutHandle = null;
let completed = false;

function cleanup(exitCode = 0) {
  if (completed) return;
  completed = true;
  if (timeoutHandle) clearTimeout(timeoutHandle);
  try {
    ws.close();
  } catch {
  }
  process.exit(exitCode);
}

async function restoreStatus() {
  if (initialStatusCode == null || initialStatusCode === targetStatus) {
    return;
  }
  await jsonFetch(`${httpBase}/patients/status`, {
    method: "POST",
    body: JSON.stringify({ surgeryId, statusCode: initialStatusCode }),
  });
}

timeoutHandle = setTimeout(async () => {
  await restoreStatus();
  console.error(`Timed out waiting for STOMP MESSAGE on ${destination}`);
  cleanup(1);
}, timeoutMs);

ws.addEventListener("open", () => {
  ws.send(frame("CONNECT", {
    "accept-version": "1.2",
    "heart-beat": "10000,10000",
  }));
});

ws.addEventListener("message", async (event) => {
  const payload = typeof event.data === "string" ? event.data : String(event.data);
  for (const stompFrame of parseFrames(payload)) {
    if (stompFrame.command === "CONNECTED") {
      ws.send(frame("SUBSCRIBE", {
        id: "sub-status",
        destination,
      }));
      const updateResponse = await jsonFetch(`${httpBase}/patients/status`, {
        method: "POST",
        body: JSON.stringify({ surgeryId, statusCode: targetStatus }),
      });
      if (updateResponse.status !== 200) {
        await restoreStatus();
        console.error(`Failed to trigger status update: ${updateResponse.status} ${updateResponse.text}`);
        cleanup(1);
      }
      continue;
    }

    if (stompFrame.command === "MESSAGE" && stompFrame.headers.destination === destination) {
      console.log(stompFrame.body);
      await restoreStatus();
      cleanup(0);
    }
  }
});

ws.addEventListener("error", async (event) => {
  await restoreStatus();
  console.error(`WebSocket/STOMP roundtrip failed: ${event.message ?? "unknown error"}`);
  cleanup(1);
});
