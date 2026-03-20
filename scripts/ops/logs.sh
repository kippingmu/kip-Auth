#!/usr/bin/env bash
set -euo pipefail
CONTAINER_NAME="kip-auth"
LINES="${1:-120}"
docker logs --tail "$LINES" "$CONTAINER_NAME"
