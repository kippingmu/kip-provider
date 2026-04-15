# kip-provider Kubernetes Cutover Plan

This repo now treats K8s as the target runtime path for `main`.
The provider remains internal-only and stays behind a `ClusterIP` Service.

## Source Of Truth

- Runtime manifests live in `deploy/k8s/`
- Reusable execution helpers live in `scripts/k8s/`
- Nacos config source lives in `deploy/k8s/nacos/kip-provider-k8s.yml`
- Shared execution helpers are mirrored on `ub` at `/home/xiaoshichuan/kip-shared/kip/scripts/shared/`

## Cutover Shape

1. Build and push the provider image with the Git SHA tag.
2. Publish or verify the provider Nacos config.
3. Render K8s manifests with the SHA tag.
4. Apply `ConfigMap`, `Secret` if present, `Service`, and `Deployment`.
5. Wait for rollout completion.
6. Smoke test `/actuator/health`, readiness, and liveness.
7. Roll back to the previous image if rollout or smoke fails.

## Important Constraints

- Do not reintroduce `k8s-poc-local`.
- Do not use `imagePullPolicy: Never`.
- Keep the provider internal-only; no Ingress is needed for this service.
- If CI does not render a runtime Secret, the cluster must already contain `kip-provider-secret`.

## Validation Focus

- Image is pulled from `registry.cn-hangzhou.aliyuncs.com/kip-app/kip-provider:<git-sha>`
- Deployment reaches `Available`
- Provider health endpoints return `UP` through the port-forward smoke check
- Nacos config on the cluster matches the repo copy or is synced from it
