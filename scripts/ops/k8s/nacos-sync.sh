#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
MODE="${1:-${NACOS_SYNC_MODE:-publish}}"
DATA_FILE="${2:-$ROOT/deploy/k8s/nacos/kip-auth-k8s.yml}"

NACOS_ADDR="${NACOS_ADDR:-10.42.0.125:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASS="${NACOS_PASS:-nacos8848}"
NACOS_GROUP="${NACOS_GROUP:-K8S_POC}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-74a3fe73-35c1-474e-ade8-9bc460b3f398}"
NACOS_DATA_ID="${NACOS_DATA_ID:-kip-auth-k8s.yml}"

usage() {
  cat <<EOF
Usage: $0 [publish|verify] [config-file]

Environment overrides:
  NACOS_ADDR NACOS_USER NACOS_PASS NACOS_GROUP NACOS_NAMESPACE NACOS_DATA_ID
EOF
}

require_file() {
  if [ ! -f "$DATA_FILE" ]; then
    echo "Config file not found: $DATA_FILE" >&2
    exit 1
  fi
}

verify_remote() {
  local TMP
  local STATUS
  TMP="$(mktemp)"
  NACOS_ADDR="$NACOS_ADDR" \
  NACOS_USER="$NACOS_USER" \
  NACOS_PASS="$NACOS_PASS" \
  NACOS_GROUP="$NACOS_GROUP" \
  NACOS_NAMESPACE="$NACOS_NAMESPACE" \
    NACOS_DATA_ID="$NACOS_DATA_ID" \
      "$ROOT/scripts/ops/nacos-config.sh" get "$NACOS_DATA_ID" > "$TMP"
  if diff -u "$DATA_FILE" "$TMP"; then
    STATUS=0
  else
    STATUS=$?
  fi
  rm -f "$TMP"
  return "$STATUS"
}

case "$MODE" in
  publish)
    require_file
    NACOS_ADDR="$NACOS_ADDR" \
    NACOS_USER="$NACOS_USER" \
    NACOS_PASS="$NACOS_PASS" \
    NACOS_GROUP="$NACOS_GROUP" \
    NACOS_NAMESPACE="$NACOS_NAMESPACE" \
    NACOS_DATA_ID="$NACOS_DATA_ID" \
      "$ROOT/scripts/ops/nacos-config.sh" put "$DATA_FILE"
    for _ in 1 2 3 4 5; do
      if verify_remote; then
        exit 0
      fi
      sleep 3
    done
    echo "Published Nacos config, but the live config never matched the repo file." >&2
    exit 1
    ;;
  verify)
    require_file
    verify_remote
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
