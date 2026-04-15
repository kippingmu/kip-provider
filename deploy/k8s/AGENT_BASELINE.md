# kip-provider K8s Agent Baseline

Use this file as the shared baseline for parallel agents working on the provider cutover.

## Stable Facts

- Service: `kip-provider`
- Namespace: `kip-poc`
- App port: `6001`
- Runtime profile: `k8s`
- Image repo: `registry.cn-hangzhou.aliyuncs.com/kip-app/kip-provider`
- Image tag convention: Git SHA tag rendered at deploy time
- K8s service type: `ClusterIP`
- Nacos config dataId: `kip-provider-k8s.yml`
- Nacos group: `K8S_POC`
- Nacos namespace: `74a3fe73-35c1-474e-ade8-9bc460b3f398`
- Default Nacos server: `10.42.0.125:8848`

## External Dependencies

- MySQL: `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`
- Kafka: default `10.42.0.125:9092`
- Sentinel dashboard: default `10.42.0.125:8090`
- Seata server: default `10.42.0.125:8091`

## Shared Execution Scripts

- Shared server copy: `/home/xiaoshichuan/kip-shared/kip/scripts/shared/`
- `scripts/k8s/render-manifests.sh`
- `scripts/k8s/publish-or-verify-nacos.sh`
- `scripts/k8s/smoke-provider.sh`
- Prefer the shared `check-kip-baseline.sh`, `publish-nacos-config.sh`, `verify-service-health.sh`, and `rollout-and-verify.sh` helpers when running on `ub`.

## Smoke Criteria

- Deployment rolls out successfully in `kip-poc`
- Service responds on `/actuator/health`
- Readiness and liveness endpoints return `UP`
- Nacos config matches the repo copy or is published from it
- Provider smoke uses a port-forward from the runner because the runtime image does not bundle `curl`

## Notes For Agents

- Treat `deploy/k8s` as the source of truth for runtime manifests.
- Do not reintroduce `k8s-poc-local` or `imagePullPolicy: Never`.
- Keep rollback K8s-native and image-tag based.
- Treat Nacos drift as real cutover work; verify first, then publish from the repo copy if the live config differs.
- If secret rendering is not available in CI, expect a pre-existing `kip-provider-secret` in the cluster.
