#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: smoke-provider.sh

Performs provider-specific smoke checks against a port-forwarded service.
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

K8S_NAMESPACE="${K8S_NAMESPACE:-kip-poc}"
K8S_SERVICE="${K8S_SERVICE:-kip-provider}"
K8S_SERVICE_PORT="${K8S_SERVICE_PORT:-6001}"
SMOKE_PORT="${SMOKE_PORT:-16001}"

port_forward_log="$(mktemp)"
pf_pid=""
cleanup() {
  if [[ -n "${pf_pid}" ]]; then
    kill "${pf_pid}" >/dev/null 2>&1 || true
    wait "${pf_pid}" >/dev/null 2>&1 || true
  fi
  rm -f "${port_forward_log}"
}
trap cleanup EXIT

kubectl -n "${K8S_NAMESPACE}" port-forward "svc/${K8S_SERVICE}" "${SMOKE_PORT}:${K8S_SERVICE_PORT}" \
  >"${port_forward_log}" 2>&1 &
pf_pid="$!"

for _ in $(seq 1 30); do
  if curl -fsS "http://127.0.0.1:${SMOKE_PORT}/actuator/health" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

endpoints=(
  /actuator/health
  /actuator/health/readiness
  /actuator/health/liveness
)

for endpoint in "${endpoints[@]}"; do
  response="$(curl -fsS "http://127.0.0.1:${SMOKE_PORT}${endpoint}")"
  if ! grep -q '"status"[[:space:]]*:[[:space:]]*"UP"' <<<"${response}"; then
    echo "Smoke check failed for ${endpoint}" >&2
    echo "${response}" >&2
    exit 1
  fi
  echo "Smoke check passed for ${endpoint}"
done
