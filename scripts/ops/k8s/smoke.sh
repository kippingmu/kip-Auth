#!/usr/bin/env bash
set -euo pipefail

KUBECTL="${KUBECTL:-kubectl}"
KUBECONFIG_PATH="${KUBECONFIG_PATH:-${KUBECONFIG:-/etc/kubernetes/admin.conf}}"
KUBE_NAMESPACE="${KUBE_NAMESPACE:-kip-poc}"
APP_LABEL="${APP_LABEL:-kip-auth}"
SERVICE_NAME="${SERVICE_NAME:-kip-auth}"
HEALTH_PORT="${HEALTH_PORT:-5001}"

NACOS_ADDR="${NACOS_ADDR:-10.42.0.125:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASS="${NACOS_PASS:-nacos8848}"
NACOS_GROUP="${NACOS_GROUP:-K8S_POC}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-74a3fe73-35c1-474e-ade8-9bc460b3f398}"
VERIFY_NACOS_DISCOVERY="${VERIFY_NACOS_DISCOVERY:-true}"

POD="$("$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" get pod -l "app=${APP_LABEL}" -o jsonpath='{.items[0].metadata.name}')"
if [ -z "$POD" ]; then
  echo "No pod found for app=${APP_LABEL} in namespace ${KUBE_NAMESPACE}" >&2
  exit 1
fi

"$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" exec "$POD" -- \
  curl -fsS "http://127.0.0.1:${HEALTH_PORT}/actuator/health"

"$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" exec "$POD" -- \
  curl -fsS "http://${SERVICE_NAME}:${HEALTH_PORT}/actuator/health"

if [ "$VERIFY_NACOS_DISCOVERY" = "true" ]; then
  ACCESS_TOKEN="$(curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/auth/users/login" \
    -d "username=${NACOS_USER}&password=${NACOS_PASS}" | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])')"

  DISCOVERY_JSON="$(curl -fsS -G "http://${NACOS_ADDR}/nacos/v1/ns/instance/list" \
    --data-urlencode "accessToken=${ACCESS_TOKEN}" \
    --data-urlencode "serviceName=${APP_LABEL}" \
    --data-urlencode "groupName=${NACOS_GROUP}" \
    --data-urlencode "namespaceId=${NACOS_NAMESPACE}")"

  python3 - "$DISCOVERY_JSON" <<'PY'
import json
import sys

payload = json.loads(sys.argv[1])
hosts = payload.get("hosts") or payload.get("serviceInfo", {}).get("hosts") or []
if hosts:
    raise SystemExit(f"expected no Nacos discovery instances, got: {hosts}")
PY
fi
