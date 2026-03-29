# kip-provider Kubernetes 部署计划

## 目标

- `kip-provider` 只提供内部 Feign 调用，不经过 gateway
- Kubernetes 内部仅保留 `ClusterIP` Service
- 当前阶段先按单副本运行
- 本文档只给部署方案，不在当前阶段真正上线 K8s

## 现状

- 已有容器镜像：`registry.cn-hangzhou.aliyuncs.com/kip-app/kip-provider`
- 已有 Actuator 探针能力：`/actuator/health`、`/actuator/health/liveness`、`/actuator/health/readiness`
- 已有优雅关闭能力，可配合 `terminationGracePeriodSeconds` 和 `preStop`
- 仍依赖外部 Nacos、Sentinel、MySQL、Seata

## 清单说明

- [`deployment.yaml`](/Users/xiaoshichuan/ide/idea/kip-provider/deploy/k8s/deployment.yaml)
  - 单副本 `Deployment`
  - 一个容器，无 sidecar
  - 探针和优雅关闭已接入
- [`service.yaml`](/Users/xiaoshichuan/ide/idea/kip-provider/deploy/k8s/service.yaml)
  - `ClusterIP`
  - 仅供集群内访问
- [`configmap.yaml`](/Users/xiaoshichuan/ide/idea/kip-provider/deploy/k8s/configmap.yaml)
  - 管理非敏感启动配置
- [`secret.example.yaml`](/Users/xiaoshichuan/ide/idea/kip-provider/deploy/k8s/secret.example.yaml)
  - 给出 MySQL 示例，正式环境应由 Secret 平台下发

## 计划部署流程

1. 先确认集群节点到外部依赖可连通：
   - `10.42.0.125:8848` Nacos
   - `10.42.0.125:8090` Sentinel
   - `10.42.0.124:3306` MySQL
   - `10.42.0.125:8091` Seata Server
2. 在 CI/CD 中继续构建并推送 `kip-provider` 镜像，镜像 tag 使用 Git SHA。
3. 在 K8s 中创建业务命名空间，比如 `kip-system` 或 `kip-dev`。
4. 按环境生成真实 Secret，不直接使用示例文件里的明文。
5. 依次应用：
   - `configmap.yaml`
   - `secret.yaml`
   - `service.yaml`
   - `deployment.yaml`
6. 等待 Pod 就绪：
   - `kubectl get pod -l app=kip-provider -w`
   - `kubectl describe pod <pod-name>`
7. 验证健康检查和日志：
   - `kubectl logs -f deploy/kip-provider`
   - `kubectl port-forward deploy/kip-provider 6001:6001`
   - `curl http://127.0.0.1:6001/actuator/health`
8. 在 `kip-app` 中引入 `provider-facade`，开启 `@EnableFeignClients`，通过 Nacos 服务名 `kip-provider` 调用。
9. 验证 `kip-app -> OpenFeign -> kip-provider` 链路，再考虑切正式环境。

## 为什么暂时不加 Ingress

- `kip-provider` 不提供前端访问入口
- 它只对内部微服务开放
- `kip-app` 通过 OpenFeign + Nacos 服务发现调用即可

## 后续建议

- 等内部接口稳定后，再考虑扩成多副本
- 多副本前先确认：
  - 业务是否无状态
  - Sentinel 端口和日志是否适合多 Pod
  - Nacos 注册与摘流在 K8s 中是否稳定
  - 是否需要 HPA、PDB、NetworkPolicy
