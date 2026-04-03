#!/usr/bin/env python3
import json
import os
import ssl
import time
import base64
import urllib.error
import urllib.request


BASE_URL = os.environ.get("BASE_URL", "https://127.0.0.1:8080").rstrip("/")
AUTH_EMAIL = os.environ.get("AUTH_EMAIL", "zhaomin@hospital.com")
AUTH_PASSWORD = os.environ.get("AUTH_PASSWORD", "zhaomin123")
REPORT_DIR = os.environ.get("REPORT_DIR", "/home/kbzz1/20260131/backend/reports")
RESULTS_NDJSON = os.path.join(REPORT_DIR, "test-results.ndjson")
CTX = ssl._create_unverified_context()


def record(name, status, detail="", method="", path="", http_status=None, biz_code=None, category=""):
    item = {
        "suite": "http",
        "name": name,
        "status": status,
        "detail": detail,
        "method": method,
        "path": path,
        "http_status": http_status,
        "biz_code": biz_code,
        "category": category,
        "ts": int(time.time()),
    }
    with open(RESULTS_NDJSON, "a", encoding="utf-8") as f:
        f.write(json.dumps(item, ensure_ascii=False) + "\n")


def request(method, path, body=None, token=None, extra_headers=None, timeout=20):
    url = BASE_URL + path
    data = None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if extra_headers:
        headers.update(extra_headers)
    if body is not None:
        data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(url=url, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, context=CTX, timeout=timeout) as resp:
            raw = resp.read().decode("utf-8", errors="replace")
            return resp.getcode(), raw
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", errors="replace")
        return e.code, raw
    except Exception as e:
        return None, str(e)


def parse_json(text):
    try:
        return json.loads(text)
    except Exception:
        return None


def find_token(obj):
    if isinstance(obj, dict):
        for k, v in obj.items():
            if k.lower() == "token" and isinstance(v, str):
                return v
            t = find_token(v)
            if t:
                return t
    elif isinstance(obj, list):
        for it in obj:
            t = find_token(it)
            if t:
                return t
    return None


def extract_surgery_id(obj):
    if not isinstance(obj, dict):
        return None
    data = obj.get("data")
    if isinstance(data, int):
        return data
    if isinstance(data, str) and data.isdigit():
        return int(data)
    if isinstance(data, dict):
        for key in ("id", "surgeryId", "treatmentInformationId"):
            v = data.get(key)
            if isinstance(v, int):
                return v
            if isinstance(v, str) and v.isdigit():
                return int(v)
    return None


def decode_jwt_payload(token):
    if not token or token.count(".") < 2:
        return None
    try:
        payload = token.split(".")[1]
        padding = "=" * (-len(payload) % 4)
        data = base64.urlsafe_b64decode(payload + padding)
        return json.loads(data.decode("utf-8"))
    except Exception:
        return None


def assert_result(name, method, path, code, raw, accept_http=(200,), require_code_1=False, category=""):
    obj = parse_json(raw)
    biz_code = obj.get("code") if isinstance(obj, dict) else None
    ok_http = code in accept_http
    ok_biz = (biz_code == 1) if require_code_1 else True
    if ok_http and ok_biz:
        record(name, "pass", method=method, path=path, http_status=code, biz_code=biz_code, category=category)
        return True, obj
    detail = raw[:300] if isinstance(raw, str) else str(raw)
    record(name, "fail", detail=detail, method=method, path=path, http_status=code, biz_code=biz_code, category=category)
    return False, obj


def main():
    os.makedirs(REPORT_DIR, exist_ok=True)
    token = None
    token_has_staff_claim = False
    surgery_id = None
    treatment_id = None
    device_id = f"AA:BB:CC:DD:EE:{int(time.time()) % 90 + 10}"
    now_slot = "2026-12-31 08:00:00"

    # 1) auth
    code, raw = request("POST", "/auth/signin", {"email": AUTH_EMAIL, "password": AUTH_PASSWORD})
    obj = parse_json(raw)
    if code == 200 and obj:
        token = find_token(obj)
    if token:
        claims = decode_jwt_payload(token) or {}
        token_has_staff_claim = claims.get("staff_id") is not None
        record("auth_signin", "pass", method="POST", path="/auth/signin", http_status=code, category="auth")
    else:
        # fallback: try signup and signin again
        signup_payload = {
            "firstName": "Zhao",
            "lastName": "Min",
            "email": AUTH_EMAIL,
            "password": AUTH_PASSWORD,
            "staffId": None
        }
        code_su, raw_su = request("POST", "/auth/signup", signup_payload)
        if code_su in (200, 400, 409):
            record("auth_signup_fallback", "pass", method="POST", path="/auth/signup", http_status=code_su, category="auth")
        else:
            record("auth_signup_fallback", "fail", raw_su[:300], "POST", "/auth/signup", code_su, category="auth")
        code2, raw2 = request("POST", "/auth/signin", {"email": AUTH_EMAIL, "password": AUTH_PASSWORD})
        obj2 = parse_json(raw2)
        token = find_token(obj2) if obj2 else None
        if token and code2 == 200:
            claims = decode_jwt_payload(token) or {}
            token_has_staff_claim = claims.get("staff_id") is not None
            record("auth_signin", "pass", "signin passed after signup fallback", "POST", "/auth/signin", code2, category="auth")
            record("auth_signin_retry", "pass", method="POST", path="/auth/signin", http_status=code2, category="auth")
        else:
            record("auth_signin", "fail", raw[:300], "POST", "/auth/signin", code, category="auth")
            record("auth_signin_retry", "warn", "token not extracted; token-required endpoints may fail", "POST", "/auth/signin", code2, category="auth")

    # 2) create surgery record
    patient_payload = {
        "name": "接口测试患者",
        "gender": "男",
        "age": 30,
        "isSoldier": False,
        "isEmergency": False,
    }
    code, raw = request("POST", "/surgery", patient_payload)
    ok, obj = assert_result("surgery_create", "POST", "/surgery", code, raw, require_code_1=True, category="patients")
    if obj:
        surgery_id = extract_surgery_id(obj)
        treatment_id = surgery_id
    if not surgery_id:
        surgery_id = 1
        treatment_id = 1
        record("surgery_id_fallback", "warn", "fallback surgeryId=1 because create result did not provide id", "POST", "/surgery", code, category="patients")

    # 3) patient + status chain
    chain_tests = [
        ("patient_get", "GET", f"/patients/{surgery_id}", None, "patients", False),
        ("patient_status_update_1", "POST", "/patients/status", {"surgeryId": surgery_id, "statusCode": 1}, "patients", True),
        ("patient_status_get", "GET", f"/patients/status/{surgery_id}", None, "patients", True),
        ("patient_status_by_code", "GET", "/patients/status/statusCode/1", None, "patients", True),
        ("patient_status_statistics", "GET", "/patients/status/statistics", None, "patients", True),
        ("queue_check", "GET", f"/queue/check/{surgery_id}", None, "queue", False),
        ("queue_appointment", "PUT", "/queue/appointment", {"surgeryId": surgery_id, "scheduledTime": now_slot}, "queue", False),
        ("queue_appointment_check", "POST", "/queue/appointment/check", {"scheduledTime": now_slot}, "queue", False),
    ]
    queue_appointment_ok = False
    for name, method, path, body, cat, strict_code in chain_tests:
        code, raw = request(method, path, body=body, token=token)
        ok, _ = assert_result(name, method, path, code, raw, require_code_1=strict_code, category=cat)
        if name == "queue_appointment":
            queue_appointment_ok = ok

    queue_register_ok = False
    if queue_appointment_ok:
        code, raw = request("POST", f"/queue/register/{surgery_id}", token=token)
        ok, obj = assert_result("queue_register", "POST", f"/queue/register/{surgery_id}", code, raw, category="queue")
        if ok and isinstance(obj, dict):
            queue_register_ok = obj.get("code") == 1
    else:
        record("queue_register", "skip", "skipped because queue_appointment failed", "POST", f"/queue/register/{surgery_id}", category="queue")

    if queue_register_ok:
        code, raw = request("POST", f"/queue/miss/{surgery_id}", token=token)
        assert_result("queue_miss", "POST", f"/queue/miss/{surgery_id}", code, raw, category="queue")
    else:
        record("queue_miss", "skip", "skipped because queue_register did not reach 已签到(code=1)", "POST", f"/queue/miss/{surgery_id}", category="queue")

    other_tests = [
        ("device_bind", "POST", "/device/binding", {"macAddress": device_id, "surgeryId": surgery_id}, "device", True),
        ("device_get_patient", "GET", f"/device/binding/device/{device_id}", None, "device", True),
        ("device_get_mac", "GET", f"/device/binding/patient/{surgery_id}", None, "device", True),
        ("device_stats", "GET", "/device/binding/statistics", None, "device", True),
        ("areas_online", "GET", "/areas/online", None, "areas", False),
    ]
    for name, method, path, body, cat, strict_code in other_tests:
        code, raw = request(method, path, body=body, token=token)
        assert_result(name, method, path, code, raw, require_code_1=strict_code, category=cat)

    # 4) anesthesia app endpoints
    app_tests = [
        ("staff_info", "GET", "/staff/information/4", None, False),
        ("surgery_area_get", "GET", f"/surgeryArea/{treatment_id}", None, False),
        ("surgery_area_anesthesiologist", "POST", "/surgeryArea/anesthesiologist", {"surgeryId": treatment_id, "staffId": 4}, False),
        ("surgery_area_record", "POST", f"/surgeryArea/record/{treatment_id}", {"drugRecord": [], "surgeryRecord": []}, False),
        ("paa_create", "POST", "/paa", {"surgeryId": treatment_id, "height": 170, "weight": 65}, False),
        ("recovery_create", "POST", "/recovery", {"treatmentInformationId": treatment_id, "bp": 120, "pBpm": 80, "rBpm": 16, "spo2": 98}, False),
        ("recovery_out", "POST", "/recovery/out", {"treatmentInformationId": treatment_id, "bp": 120, "pBpm": 80, "rBpm": 16, "spo2": 98}, False),
        ("rer_create", "POST", "/rer", {"treatmentInformationId": treatment_id, "intraoperative": [], "complication": [], "monitoring": {}}, False),
        ("ars_get_patients", "GET", "/ARS/getPatients", None, False),
        ("ars_get_summary", "GET", f"/ARS/{treatment_id}", None, False),
        ("waveform_get", "GET", f"/waveform/{treatment_id}", None, False),
    ]
    for name, method, path, body, strict_code in app_tests:
        code, raw = request(method, path, body=body, token=token)
        assert_result(name, method, path, code, raw, require_code_1=strict_code, category="anesthesia_app")

    if token and token_has_staff_claim:
        code, raw = request("POST", "/surgeryArea/anesthesiologist2", {"surgeryId": treatment_id}, token=token)
        assert_result("surgery_area_anesthesiologist2", "POST", "/surgeryArea/anesthesiologist2", code, raw, category="anesthesia_app")
    else:
        record(
            "surgery_area_anesthesiologist2",
            "skip",
            "skipped because auth token has no staff_id claim; endpoint requires JWT staff identity",
            "POST",
            "/surgeryArea/anesthesiologist2",
            category="anesthesia_app",
        )

    # 5) cleanup binding + status
    cleanup_tests = [
        ("device_unbind_patient", "DELETE", f"/device/binding/patient/{surgery_id}", None, False),
        ("patient_status_delete", "DELETE", f"/patients/status/{surgery_id}", None, False),
    ]
    for name, method, path, body, strict_code in cleanup_tests:
        code, raw = request(method, path, body=body, token=token)
        assert_result(name, method, path, code, raw, require_code_1=strict_code, category="cleanup")


if __name__ == "__main__":
    main()
