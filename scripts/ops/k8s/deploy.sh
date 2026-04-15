#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
KUBECTL="${KUBECTL:-kubectl}"
KUBECONFIG_PATH="${KUBECONFIG_PATH:-${KUBECONFIG:-/etc/kubernetes/admin.conf}}"
KUBE_NAMESPACE="${KUBE_NAMESPACE:-kip-poc}"
IMAGE_REPO="${IMAGE_REPO:-registry.cn-hangzhou.aliyuncs.com/kip-app/kip-auth}"
IMAGE_TAG="${IMAGE_TAG:-}"
DEPLOYMENT_NAME="${DEPLOYMENT_NAME:-kip-auth}"
ROLLOUT_TIMEOUT="${ROLLOUT_TIMEOUT:-300s}"

MYSQL_URL="${MYSQL_URL:-}"
MYSQL_USERNAME="${MYSQL_USERNAME:-}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
REDIS_PASSWORD="${REDIS_PASSWORD:-}"
AUTH_JWT_SECRET="${AUTH_JWT_SECRET:-}"

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

require_value() {
  local name="$1"
  local value="$2"
  if [ -z "$value" ]; then
    echo "Missing required environment variable: $name" >&2
    exit 1
  fi
}

using_existing_secret() {
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" get secret kip-auth-secret >/dev/null 2>&1
}

should_render_secret() {
  [ -n "$MYSQL_URL" ] || [ -n "$MYSQL_USERNAME" ] || [ -n "$MYSQL_PASSWORD" ] || [ -n "$REDIS_PASSWORD" ] || [ -n "$AUTH_JWT_SECRET" ]
}

render_deployment() {
  sed \
    -e "s#__IMAGE_REPO__#${IMAGE_REPO}#g" \
    -e "s#__IMAGE_TAG__#${IMAGE_TAG}#g" \
    "$ROOT/deploy/k8s/deployment.yaml" > "$WORK_DIR/deployment.yaml"
}

render_configmap() {
  sed \
    -e "s#__NACOS_SERVER_ADDR__#${NACOS_ADDR:-10.42.0.125:8848}#g" \
    -e "s#__NACOS_NAMESPACE__#${NACOS_NAMESPACE:-74a3fe73-35c1-474e-ade8-9bc460b3f398}#g" \
    -e "s#__NACOS_GROUP__#${NACOS_GROUP:-K8S_POC}#g" \
    "$ROOT/deploy/k8s/configmap.yaml" > "$WORK_DIR/configmap.yaml"
}

render_secret() {
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" create secret generic kip-auth-secret \
    --from-literal=MYSQL_URL="$MYSQL_URL" \
    --from-literal=MYSQL_USERNAME="$MYSQL_USERNAME" \
    --from-literal=MYSQL_PASSWORD="$MYSQL_PASSWORD" \
    --from-literal=REDIS_PASSWORD="$REDIS_PASSWORD" \
    --from-literal=AUTH_JWT_SECRET="$AUTH_JWT_SECRET" \
    --dry-run=client -o yaml > "$WORK_DIR/secret.yaml"
}

apply_manifests() {
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" apply -f "$ROOT/deploy/k8s/namespace.yaml"
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" apply -f "$WORK_DIR/configmap.yaml"
  if [ -f "$WORK_DIR/secret.yaml" ]; then
    "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" apply -f "$WORK_DIR/secret.yaml"
  fi
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" apply -f "$WORK_DIR/deployment.yaml"
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" apply -f "$ROOT/deploy/k8s/service.yaml"
}

clear_legacy_node_selector() {
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" patch deployment "$DEPLOYMENT_NAME" \
    --type merge \
    -p '{"spec":{"template":{"spec":{"nodeSelector":null}}}}' >/dev/null
}

rollout_wait() {
  "$KUBECTL" --kubeconfig "$KUBECONFIG_PATH" -n "$KUBE_NAMESPACE" rollout status "deploy/${DEPLOYMENT_NAME}" --timeout="$ROLLOUT_TIMEOUT"
}

main() {
  require_value IMAGE_TAG "$IMAGE_TAG"
  require_value IMAGE_REPO "$IMAGE_REPO"

  render_deployment
  render_configmap

  if should_render_secret; then
    require_value MYSQL_URL "$MYSQL_URL"
    require_value MYSQL_USERNAME "$MYSQL_USERNAME"
    require_value MYSQL_PASSWORD "$MYSQL_PASSWORD"
    require_value REDIS_PASSWORD "$REDIS_PASSWORD"
    require_value AUTH_JWT_SECRET "$AUTH_JWT_SECRET"
    render_secret
  elif ! using_existing_secret; then
    echo "Missing kip-auth-secret in namespace ${KUBE_NAMESPACE} and no replacement secret values were provided." >&2
    exit 1
  fi

  apply_manifests
  clear_legacy_node_selector
  rollout_wait
}

main "$@"
