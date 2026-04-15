#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: publish-or-verify-nacos.sh <config-file>

Ensures the Nacos config matches the repository copy.
If the remote value differs or is missing, the local file is published first
and then verified.
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

CONFIG_FILE="${1:?config-file is required}"
test -f "${CONFIG_FILE}"

NACOS_SERVER_ADDR="${NACOS_SERVER_ADDR:?NACOS_SERVER_ADDR is required}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:?NACOS_NAMESPACE is required}"
NACOS_GROUP="${NACOS_GROUP:?NACOS_GROUP is required}"
NACOS_DATA_ID="${NACOS_DATA_ID:-$(basename "${CONFIG_FILE}")}"
NACOS_USERNAME="${NACOS_USERNAME:-}"
NACOS_PASSWORD="${NACOS_PASSWORD:-}"

curl_args=()
if [[ -n "${NACOS_USERNAME}" || -n "${NACOS_PASSWORD}" ]]; then
  curl_args+=(-u "${NACOS_USERNAME}:${NACOS_PASSWORD}")
fi

base_url="http://${NACOS_SERVER_ADDR}/nacos/v1/cs/configs"

remote_file="$(mktemp)"
trap 'rm -f "${remote_file}"' EXIT

fetch_remote() {
  curl -sS "${curl_args[@]}" --get "${base_url}" \
    --data-urlencode "dataId=${NACOS_DATA_ID}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    -o "${remote_file}"
}

publish_local() {
  curl -sS "${curl_args[@]}" -X POST "${base_url}" \
    --data-urlencode "dataId=${NACOS_DATA_ID}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "content@${CONFIG_FILE}" \
    --data-urlencode "type=yaml" \
    >/dev/null
}

if fetch_remote; then
  if cmp -s "${CONFIG_FILE}" "${remote_file}"; then
    echo "Nacos config already matches ${NACOS_DATA_ID}."
    exit 0
  fi
  echo "Nacos config differs from ${NACOS_DATA_ID}; publishing source of truth."
else
  echo "Nacos config missing or unreadable for ${NACOS_DATA_ID}; publishing source of truth."
fi

publish_local
fetch_remote

if cmp -s "${CONFIG_FILE}" "${remote_file}"; then
  echo "Nacos config published and verified for ${NACOS_DATA_ID}."
else
  echo "Published Nacos config still differs from local source for ${NACOS_DATA_ID}." >&2
  diff -u "${CONFIG_FILE}" "${remote_file}" || true
  exit 1
fi
