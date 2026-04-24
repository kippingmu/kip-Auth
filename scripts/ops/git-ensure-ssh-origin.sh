#!/usr/bin/env bash
set -euo pipefail

REMOTE="${KIP_AUTH_REMOTE:-git@github.com:kippingmu/kip-Auth.git}"
REPO_DIR="$(git rev-parse --show-toplevel)"

git -C "${REPO_DIR}" remote set-url origin "${REMOTE}"
git -C "${REPO_DIR}" remote -v
