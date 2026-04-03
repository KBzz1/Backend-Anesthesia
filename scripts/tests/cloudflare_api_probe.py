#!/usr/bin/env python3
import argparse
import json
import os
import socket
import ssl
import sys
import time
import urllib.error
import urllib.parse
import urllib.request


DEFAULT_BASE_URL = os.environ.get("BASE_URL", "http://anesthesia.kbzz1.top:8080").rstrip("/")
DEFAULT_HOST = os.environ.get("EXTERNAL_HOST", urllib.parse.urlparse(DEFAULT_BASE_URL).hostname or "anesthesia.kbzz1.top")
DEFAULT_AUTH_EMAIL = os.environ.get("AUTH_EMAIL", "zhaomin@hospital.com")
DEFAULT_AUTH_PASSWORD = os.environ.get("AUTH_PASSWORD", "zhaomin123")
DEFAULT_USER_AGENT = os.environ.get(
    "USER_AGENT",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
)


def parse_args():
    parser = argparse.ArgumentParser(
        description="Probe external API availability through the Cloudflare-facing domain."
    )
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL, help="External API base URL.")
    parser.add_argument(
        "--host",
        default=DEFAULT_HOST,
        help="External hostname used for auto-detection when --auto-detect is enabled.",
    )
    parser.add_argument(
        "--frontend-origin",
        default=os.environ.get("FRONTEND_ORIGIN"),
        help="Origin header used to simulate the external frontend. Defaults to scheme://host[:port] from base-url.",
    )
    parser.add_argument(
        "--health-path",
        default=os.environ.get("HEALTH_PATH", "/healthz"),
        help="Health endpoint path.",
    )
    parser.add_argument(
        "--cors-path",
        default=os.environ.get("CORS_PATH", "/auth/signin"),
        help="Path used for CORS preflight probing.",
    )
    parser.add_argument(
        "--request-path",
        default=os.environ.get("REQUEST_PATH", "/healthz"),
        help="GET path used for a simple browser-style fetch probe.",
    )
    parser.add_argument(
        "--auth-email",
        default=DEFAULT_AUTH_EMAIL,
        help="Signin email for POST /auth/signin test.",
    )
    parser.add_argument(
        "--auth-password",
        default=DEFAULT_AUTH_PASSWORD,
        help="Signin password for POST /auth/signin test.",
    )
    parser.add_argument(
        "--auth-path",
        default=os.environ.get("AUTH_PATH", "/auth/signin"),
        help="Signin endpoint path.",
    )
    parser.add_argument(
        "--auth-check-path",
        default=os.environ.get("AUTH_CHECK_PATH"),
        help="Optional authenticated GET path. Example: /surgeryArea/mySignature",
    )
    parser.add_argument(
        "--timeout",
        type=float,
        default=float(os.environ.get("TIMEOUT", "12")),
        help="Network timeout in seconds.",
    )
    parser.add_argument(
        "--insecure",
        action="store_true",
        default=os.environ.get("INSECURE", "").lower() in {"1", "true", "yes"},
        help="Disable TLS certificate verification.",
    )
    parser.add_argument(
        "--report-file",
        default=os.environ.get("RESULTS_FILE", ""),
        help="Optional NDJSON report output path. Leave empty to print only.",
    )
    parser.add_argument(
        "--auto-detect",
        action="store_true",
        default=os.environ.get("AUTO_DETECT", "1").lower() not in {"0", "false", "no"},
        help="Auto-detect the working external entrypoint before probing. Enabled by default.",
    )
    return parser.parse_args()


def ensure_origin(base_url, explicit_origin):
    if explicit_origin:
        return explicit_origin.rstrip("/")
    parsed = urllib.parse.urlparse(base_url)
    return f"{parsed.scheme}://{parsed.netloc}"


def build_ssl_context(insecure):
    if insecure:
        return ssl._create_unverified_context()
    return ssl.create_default_context()


def append_report(path, item):
    if not path:
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "a", encoding="utf-8") as fh:
        fh.write(json.dumps(item, ensure_ascii=False) + "\n")


def record(report_file, name, status, detail="", **extra):
    item = {
        "suite": "cloudflare_api_probe",
        "name": name,
        "status": status,
        "detail": detail,
        "ts": int(time.time()),
    }
    item.update(extra)
    append_report(report_file, item)
    symbol = {
        "pass": "[PASS]",
        "fail": "[FAIL]",
        "warn": "[WARN]",
        "skip": "[SKIP]",
    }.get(status, "[INFO]")
    line = f"{symbol} {name}"
    if detail:
        line += f" - {detail}"
    print(line)


def request(base_url, path, method="GET", body=None, headers=None, timeout=12, ssl_context=None):
    url = base_url + path
    data = None
    req_headers = {
        "User-Agent": DEFAULT_USER_AGENT,
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
        "Cache-Control": "no-cache",
        "Pragma": "no-cache",
    }
    if headers:
        req_headers.update(headers)
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        req_headers.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(url=url, data=data, method=method, headers=req_headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
            raw = resp.read().decode("utf-8", errors="replace")
            return resp.getcode(), raw, dict(resp.headers)
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8", errors="replace")
        return exc.code, raw, dict(exc.headers)
    except Exception as exc:
        return None, str(exc), {}


def lower_headers(headers):
    return {str(k).lower(): str(v) for k, v in headers.items()}


def make_args_copy(args, base_url):
    clone = argparse.Namespace(**vars(args))
    clone.base_url = base_url.rstrip("/")
    clone.frontend_origin = ensure_origin(clone.base_url, None)
    return clone


def check_dns(args, parsed):
    try:
        infos = socket.getaddrinfo(parsed.hostname, parsed.port or 443, type=socket.SOCK_STREAM)
        addresses = sorted({info[4][0] for info in infos})
        record(args.report_file, "dns_lookup", "pass", ", ".join(addresses), host=parsed.hostname)
        return True
    except Exception as exc:
        record(args.report_file, "dns_lookup", "fail", str(exc), host=parsed.hostname)
        return False


def check_tcp_tls(args, parsed, ssl_context):
    port = parsed.port or (443 if parsed.scheme == "https" else 80)
    try:
        with socket.create_connection((parsed.hostname, port), timeout=args.timeout) as sock:
            if parsed.scheme == "https":
                with ssl_context.wrap_socket(sock, server_hostname=parsed.hostname) as tls_sock:
                    cert = tls_sock.getpeercert()
                    subject = cert.get("subject", ())
                    cn = None
                    for entry in subject:
                        for key, value in entry:
                            if key == "commonName":
                                cn = value
                                break
                        if cn:
                            break
                    detail = f"TLS OK, CN={cn or 'unknown'}"
                    record(args.report_file, "tcp_tls_handshake", "pass", detail, host=parsed.hostname, port=port)
                    return True
            else:
                record(args.report_file, "tcp_connect", "pass", f"TCP OK to {parsed.hostname}:{port}", host=parsed.hostname, port=port)
                return True
    except Exception as exc:
        detail = str(exc)
        if "WRONG_VERSION_NUMBER" in detail and parsed.scheme == "https":
            detail += " ; target port is likely serving plain HTTP, try http:// host/port instead of https://"
        record(args.report_file, "tcp_tls_handshake", "fail", detail, host=parsed.hostname, port=port)
        return False


def check_health(args, ssl_context):
    code, raw, headers = request(
        args.base_url,
        args.health_path,
        method="GET",
        headers={"Accept": "text/plain"},
        timeout=args.timeout,
        ssl_context=ssl_context,
    )
    ok = code == 200 and "ok" in raw.lower()
    record(
        args.report_file,
        "health_check",
        "pass" if ok else "fail",
        f"HTTP {code}, body={raw.strip()[:120]}",
        path=args.health_path,
        http_status=code,
    )
    return ok


def health_probe(base_url, health_path, timeout, ssl_context):
    code, raw, _ = request(
        base_url.rstrip("/"),
        health_path,
        method="GET",
        headers={"Accept": "text/plain"},
        timeout=timeout,
        ssl_context=ssl_context,
    )
    return code == 200 and "ok" in raw.lower(), code, raw


def candidate_urls(host):
    return [
        f"http://{host}:8080",
        f"http://{host}",
        f"https://{host}",
        f"https://{host}:8080",
    ]


def auto_detect_base_url(args):
    if not args.auto_detect:
        return args.base_url

    parsed = urllib.parse.urlparse(args.base_url)
    host = args.host or parsed.hostname
    if not host:
        return args.base_url

    print("Auto-detecting external entrypoint...")
    tried = []
    reachable_candidate = None
    for base_url in candidate_urls(host):
        probe_ssl = build_ssl_context(args.insecure)
        ok, code, raw = health_probe(base_url, args.health_path, min(args.timeout, 6), probe_ssl)
        detail = f"HTTP {code}" if code is not None else raw
        tried.append((base_url, detail))
        if ok:
            record(args.report_file, "auto_detect_base_url", "pass", f"selected {base_url}")
            return base_url
        if reachable_candidate is None and code is not None:
            reachable_candidate = base_url

    if reachable_candidate:
        joined = "; ".join(f"{url} => {detail[:80]}" for url, detail in tried)
        record(
            args.report_file,
            "auto_detect_base_url",
            "warn",
            f"no healthy candidate found, selected reachable {reachable_candidate}; tried: {joined}",
        )
        return reachable_candidate

    fallback = args.base_url
    joined = "; ".join(f"{url} => {detail[:80]}" for url, detail in tried)
    record(
        args.report_file,
        "auto_detect_base_url",
        "warn",
        f"no healthy candidate found, fallback to {fallback}; tried: {joined}",
    )
    return fallback


def check_browser_get(args, ssl_context):
    headers = {
        "Origin": args.frontend_origin,
        "Referer": args.frontend_origin + "/",
        "Accept": "application/json, text/plain, */*",
    }
    code, raw, resp_headers = request(
        args.base_url,
        args.request_path,
        method="GET",
        headers=headers,
        timeout=args.timeout,
        ssl_context=ssl_context,
    )
    ok = code is not None and 200 <= code < 500
    cors_header = lower_headers(resp_headers).get("access-control-allow-origin")
    detail = f"HTTP {code}, ACAO={cors_header or 'missing'}"
    if code is None:
        detail = raw
    record(
        args.report_file,
        "browser_get_probe",
        "pass" if ok else "fail",
        detail,
        path=args.request_path,
        http_status=code,
    )
    return ok


def check_cors_preflight(args, ssl_context):
    headers = {
        "Origin": args.frontend_origin,
        "Access-Control-Request-Method": "POST",
        "Access-Control-Request-Headers": "authorization,content-type",
    }
    code, raw, resp_headers = request(
        args.base_url,
        args.cors_path,
        method="OPTIONS",
        headers=headers,
        timeout=args.timeout,
        ssl_context=ssl_context,
    )
    normalized = lower_headers(resp_headers)
    allow_origin = normalized.get("access-control-allow-origin")
    allow_methods = normalized.get("access-control-allow-methods", "")
    allow_headers = normalized.get("access-control-allow-headers", "")
    origin_ok = allow_origin in {args.frontend_origin, "*"}
    method_ok = "POST" in allow_methods.upper()
    headers_ok = "authorization" in allow_headers.lower() and "content-type" in allow_headers.lower()
    ok = code in (200, 204) and origin_ok and method_ok and headers_ok
    detail = (
        f"HTTP {code}, ACAO={allow_origin or 'missing'}, "
        f"ACAM={allow_methods or 'missing'}, ACAH={allow_headers or 'missing'}"
    )
    if code is None:
        detail = raw
    record(
        args.report_file,
        "cors_preflight",
        "pass" if ok else "fail",
        detail,
        path=args.cors_path,
        http_status=code,
    )
    return ok


def find_token(payload):
    if isinstance(payload, dict):
        for key, value in payload.items():
            if key.lower() == "token" and isinstance(value, str):
                return value
            nested = find_token(value)
            if nested:
                return nested
    if isinstance(payload, list):
        for item in payload:
            nested = find_token(item)
            if nested:
                return nested
    return None


def try_signin(args, ssl_context):
    if not args.auth_email or not args.auth_password:
        record(
            args.report_file,
            "auth_signin",
            "skip",
            "set AUTH_EMAIL and AUTH_PASSWORD to enable login probing",
            path=args.auth_path,
        )
        return None

    headers = {
        "Origin": args.frontend_origin,
        "Referer": args.frontend_origin + "/",
        "Accept": "application/json, text/plain, */*",
    }
    body = {"email": args.auth_email, "password": args.auth_password}
    code, raw, _ = request(
        args.base_url,
        args.auth_path,
        method="POST",
        body=body,
        headers=headers,
        timeout=args.timeout,
        ssl_context=ssl_context,
    )
    token = None
    try:
        payload = json.loads(raw)
        token = find_token(payload)
    except Exception:
        payload = None
    ok = code == 200 and token is not None
    detail = f"HTTP {code}, token={'yes' if token else 'no'}"
    if code is None:
        detail = raw
    elif not ok:
        detail = f"{detail}, body={raw[:200]}"
    record(
        args.report_file,
        "auth_signin",
        "pass" if ok else "fail",
        detail,
        path=args.auth_path,
        http_status=code,
    )
    return token if ok else None


def check_authenticated_path(args, ssl_context, token):
    if not args.auth_check_path:
        record(
            args.report_file,
            "authenticated_probe",
            "skip",
            "set AUTH_CHECK_PATH to test an authenticated endpoint",
        )
        return True
    if not token:
        record(
            args.report_file,
            "authenticated_probe",
            "skip",
            "signin did not provide a token",
            path=args.auth_check_path,
        )
        return False
    headers = {
        "Origin": args.frontend_origin,
        "Referer": args.frontend_origin + "/",
        "Authorization": f"Bearer {token}",
        "Accept": "application/json, text/plain, */*",
    }
    code, raw, _ = request(
        args.base_url,
        args.auth_check_path,
        method="GET",
        headers=headers,
        timeout=args.timeout,
        ssl_context=ssl_context,
    )
    ok = code is not None and 200 <= code < 500
    detail = f"HTTP {code}"
    if code is None:
        detail = raw
    elif not ok:
        detail = f"{detail}, body={raw[:200]}"
    record(
        args.report_file,
        "authenticated_probe",
        "pass" if ok else "fail",
        detail,
        path=args.auth_check_path,
        http_status=code,
    )
    return ok


def main():
    args = parse_args()
    args.base_url = auto_detect_base_url(args)
    parsed = urllib.parse.urlparse(args.base_url)
    if not parsed.scheme or not parsed.netloc or not parsed.hostname:
        print(f"Invalid --base-url: {args.base_url}", file=sys.stderr)
        return 2

    args.base_url = args.base_url.rstrip("/")
    args.frontend_origin = ensure_origin(args.base_url, args.frontend_origin)
    ssl_context = build_ssl_context(args.insecure)

    print(f"Base URL:        {args.base_url}")
    print(f"Frontend Origin: {args.frontend_origin}")
    print(f"Report File:     {args.report_file or '(disabled, terminal only)'}")

    checks = [
        check_dns(args, parsed),
        check_tcp_tls(args, parsed, ssl_context),
        check_health(args, ssl_context),
        check_browser_get(args, ssl_context),
        check_cors_preflight(args, ssl_context),
    ]

    token = try_signin(args, ssl_context)
    auth_ok = check_authenticated_path(args, ssl_context, token)
    checks.append(auth_ok)

    failures = sum(1 for item in checks if item is False)
    if failures:
        print(f"\nProbe finished with {failures} failed check(s).")
        return 1

    print("\nProbe finished successfully.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
