#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-ws://127.0.0.1:18080/ws}"

tmpfile="$(mktemp)"
trap 'rm -f "$tmpfile"' EXIT

printf 'CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\0' > "$tmpfile"
printf 'SUBSCRIBE\nid:sub-0\ndestination:/data/patients/status/1\n\n\0' >> "$tmpfile"

echo "Attempting STOMP handshake against ${BASE_URL}"
cat "$tmpfile" | wscat --no-color --connect "$BASE_URL" --wait 2 || true
