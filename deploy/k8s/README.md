# kip-auth K8s POC

## Files

- `namespace.yaml`: 创建 `kip-poc` namespace
- `configmap.yaml`: 非敏感环境变量
- `secret.example.yaml`: 敏感变量样例，复制后改成真实 `secret.yaml`
- `deployment.yaml`: `kip-auth` Deployment
- `service.yaml`: `kip-auth` ClusterIP Service
- `nacos/kip-auth-k8s.yml`: 发布到 Nacos `K8S_POC` group 的配置

## Publish Nacos config

```bash
cd /Users/xiaoshichuan/ide/idea/kip-auth
NACOS_GROUP=K8S_POC \
NACOS_NAMESPACE=74a3fe73-35c1-474e-ade8-9bc460b3f398 \
NACOS_DATA_ID=kip-auth-k8s.yml \
scripts/ops/nacos-config.sh put deploy/k8s/nacos/kip-auth-k8s.yml
```

## Build image on `ub` and import into containerd

```bash
mvn -B -pl app/auth-web -am -DskipTests package

ssh ub 'rm -rf /tmp/kip-auth-k8s-build && mkdir -p /tmp/kip-auth-k8s-build'
rsync -az --delete --exclude ".git" --exclude ".idea" --exclude "target" ./ ub:/tmp/kip-auth-k8s-build/
scp app/auth-web/target/auth-web-1.0-SNAPSHOT.jar ub:/tmp/kip-auth-k8s-build/app.jar

ssh ub 'set -euo pipefail
  cd /tmp/kip-auth-k8s-build
  BASE_IMAGE=$(docker ps --format "{{.Image}}" --filter name=kip-auth | head -1)
  test -n "$BASE_IMAGE"
  printf "%s\n" "FROM ${BASE_IMAGE}" "COPY app.jar /app/app.jar" > Dockerfile.k8s-local
  docker build --pull=false -f Dockerfile.k8s-local -t registry.cn-hangzhou.aliyuncs.com/kip-app/kip-auth:k8s-poc-local .
  docker save registry.cn-hangzhou.aliyuncs.com/kip-app/kip-auth:k8s-poc-local | sudo ctr -n k8s.io images import -
'
```

说明：

- `ub` 上直接按原始多阶段 `Dockerfile` 构建时，当前 Docker Hub mirror 可能返回 `403`。
- 上面这套命令基于 `ub` 本机已有的 `kip-auth` 基础镜像重打新 jar，已验证可用。

## Deploy to `kip-poc`

```bash
cd /Users/xiaoshichuan/ide/idea/kip-auth
cp deploy/k8s/secret.example.yaml deploy/k8s/secret.yaml

scp deploy/k8s/secret.yaml ub:/tmp/kip-auth-k8s-build/deploy/k8s/secret.yaml
ssh ub 'sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl apply -f /tmp/kip-auth-k8s-build/deploy/k8s/namespace.yaml && \
  sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl apply -f /tmp/kip-auth-k8s-build/deploy/k8s/configmap.yaml -f /tmp/kip-auth-k8s-build/deploy/k8s/secret.yaml -f /tmp/kip-auth-k8s-build/deploy/k8s/deployment.yaml -f /tmp/kip-auth-k8s-build/deploy/k8s/service.yaml'
```

## Verify

```bash
ssh ub 'sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl -n kip-poc get pods,svc | grep kip-auth'
ssh ub 'sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl -n kip-poc rollout status deploy/kip-auth --timeout=180s'
ssh ub 'POD=$(sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl -n kip-poc get pod -l app=kip-auth -o jsonpath="{.items[0].metadata.name}") && \
  sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl -n kip-poc exec "$POD" -- curl -fsS http://127.0.0.1:5001/actuator/health && \
  sudo KUBECONFIG=/etc/kubernetes/admin.conf kubectl -n kip-poc exec "$POD" -- curl -fsS http://kip-auth:5001/actuator/health'

ACCESS_TOKEN=$(curl -fsS -X POST "http://10.42.0.125:8848/nacos/v1/auth/users/login" \
  -d "username=nacos&password=nacos8848" | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])')
curl -fsS -G "http://10.42.0.125:8848/nacos/v1/ns/instance/list" \
  --data-urlencode "accessToken=${ACCESS_TOKEN}" \
  --data-urlencode "serviceName=kip-auth" \
  --data-urlencode "groupName=K8S_POC" \
  --data-urlencode "namespaceId=74a3fe73-35c1-474e-ade8-9bc460b3f398"
```

预期：

- `kip-auth` Pod 为 `Running` 且 probes 通过
- `curl http://kip-auth:5001/actuator/health` 成功
- Nacos discovery 返回空实例列表

## Remaining required external dependencies

- MySQL
- Redis
- Nacos config
