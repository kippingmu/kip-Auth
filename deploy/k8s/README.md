# kip-auth K8s POC

## Files

- `namespace.yaml`: 创建 `kip-poc` namespace
- `configmap.yaml`: 非敏感环境变量
- `secret.example.yaml`: 敏感变量样例，复制后改成真实 `secret.yaml`
- `deployment.yaml`: `kip-auth` Deployment 模板，镜像 tag 由脚本渲染为 SHA
- `service.yaml`: `kip-auth` ClusterIP Service
- `nacos/kip-auth-k8s.yml`: 发布到 Nacos `K8S_POC` group 的配置
- `scripts/ops/k8s/nacos-sync.sh`: Nacos 发布/验证
- `scripts/ops/k8s/deploy.sh`: 渲染并应用 K8s 资源
- `scripts/ops/k8s/smoke.sh`: auth 健康检查和 Nacos 验证
- `scripts/ops/k8s/rollback.sh`: K8s 回滚

## Workflow shape

- build and push `registry.cn-hangzhou.aliyuncs.com/kip-app/kip-auth:${IMAGE_TAG}`
- publish the live Nacos config and verify it matches `deploy/k8s/nacos/kip-auth-k8s.yml`
- verify-only mode is still available for manual drift checks
- render `deploy/k8s/deployment.yaml` with the SHA tag
- apply namespace, configmap, secret, deployment, and service
- wait for `kubectl rollout status`
- run auth smoke verification from inside the pod
- verify Nacos discovery stays empty for `kip-auth`
- rollback with `kubectl rollout undo` if anything fails

If `/etc/kubernetes/admin.conf` is root-owned on the runner or `ub`, run the K8s scripts with `sudo -E`.

## Required CI secrets

The GitHub Actions workflow expects these secrets to exist:

- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `AUTH_JWT_SECRET`
- `ALIYUN_REGISTRY_USERNAME`
- `ALIYUN_REGISTRY_PASSWORD`
- `NACOS_ADDR` if the default address is not correct
- `NACOS_USER` and `NACOS_PASS` if the default Nacos login is not correct

## Manual run

```bash
cd /Users/xiaoshichuan/ide/idea/kip-auth
export IMAGE_TAG="$(git rev-parse --short=12 HEAD)"
export MYSQL_URL='jdbc:mysql://...'
export MYSQL_USERNAME='...'
export MYSQL_PASSWORD='...'
export REDIS_PASSWORD='...'
export AUTH_JWT_SECRET='...'

scripts/ops/k8s/nacos-sync.sh publish
scripts/ops/k8s/deploy.sh
scripts/ops/k8s/smoke.sh
```

If the rollout fails or smoke checks regress, run:

```bash
scripts/ops/k8s/rollback.sh
```

## Remaining required external dependencies

- MySQL
- Redis
- Nacos config
