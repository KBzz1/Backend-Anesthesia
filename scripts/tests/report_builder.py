#!/usr/bin/env python3
import json
import os
from collections import Counter


def main():
    report_dir = os.environ.get("REPORT_DIR", "/home/kbzz1/20260131/backend/reports")
    ndjson = os.environ.get("RESULTS_NDJSON", os.path.join(report_dir, "test-results.ndjson"))
    items = []
    if os.path.exists(ndjson):
        with open(ndjson, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line:
                    items.append(json.loads(line))

    status_counter = Counter(i.get("status") for i in items)
    suite_counter = Counter(i.get("suite") for i in items)
    failures = [i for i in items if i.get("status") == "fail"]

    report = {
        "summary": {
            "total": len(items),
            "pass": status_counter.get("pass", 0),
            "fail": status_counter.get("fail", 0),
            "warn": status_counter.get("warn", 0),
            "skip": status_counter.get("skip", 0),
        },
        "by_suite": dict(suite_counter),
        "failures": failures,
        "results": items,
    }

    os.makedirs(report_dir, exist_ok=True)
    json_path = os.path.join(report_dir, "test-report.json")
    md_path = os.path.join(report_dir, "test-report.md")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    lines = [
        "# Anesthesia Test Report",
        "",
        f"- Total: {report['summary']['total']}",
        f"- Pass: {report['summary']['pass']}",
        f"- Fail: {report['summary']['fail']}",
        f"- Warn: {report['summary']['warn']}",
        f"- Skip: {report['summary']['skip']}",
        "",
        "## Failed Cases",
    ]
    if not failures:
        lines.append("- None")
    else:
        for i in failures:
            lines.append(f"- `{i.get('suite')}` `{i.get('name')}`: {i.get('detail','')}")

    lines.append("")
    lines.append("## All Cases")
    for i in items:
        lines.append(
            f"- [{i.get('status')}] {i.get('suite')} :: {i.get('name')} "
            f"(method={i.get('method','')}, path={i.get('path','')}, http={i.get('http_status','')}, code={i.get('biz_code','')})"
        )

    with open(md_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()

