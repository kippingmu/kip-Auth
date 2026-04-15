#!/usr/bin/env bash
set -euo pipefail

KUBECTL="${KUBECTL:-kubectl}"
KUBECONFIG_PATH="${KUBECONFIG_PATH:-${KUBECONFIG:-/etc/kubernetes/admin.conf}}"
KUBE_NAMESPACE="${KUBE_NAMESPACE:-kip-poc}"
DEPLOYMENT_NAME="${DEPLOYMENT_NAME:-kip-auth}"

if ! "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" rollout undo "deploy/${DEPLOYMENT_NAME}"; then
  echo "No previous rollout revision was available to undo." >&2
  exit 0
fi
if "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" rollout status "deploy/${DEPLOYMENT_NAME}" --timeout=180s; then
  exit 0
fi

echo "Rollback initiated, but rollout status did not settle cleanly." >&2
exit 1
