#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: render-manifests.sh <source-dir> <output-dir> <image-tag>

Renders provider K8s manifests into a deployable directory.
If MYSQL_URL, MYSQL_USERNAME, and MYSQL_PASSWORD are present, a runtime Secret
manifest is generated as well.
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

SOURCE_DIR="${1:?source-dir is required}"
OUTPUT_DIR="${2:?output-dir is required}"
IMAGE_TAG="${3:-${IMAGE_TAG:-}}"

if [[ -z "${IMAGE_TAG}" ]]; then
  echo "IMAGE_TAG is required" >&2
  usage >&2
  exit 1
fi

mkdir -p "${OUTPUT_DIR}"

cp "${SOURCE_DIR}/configmap.yaml" "${OUTPUT_DIR}/configmap.yaml"
cp "${SOURCE_DIR}/service.yaml" "${OUTPUT_DIR}/service.yaml"

sed "s/__IMAGE_TAG__/${IMAGE_TAG}/g" "${SOURCE_DIR}/deployment.yaml" > "${OUTPUT_DIR}/deployment.yaml"

if [[ -n "${MYSQL_URL:-}" && -n "${MYSQL_USERNAME:-}" && -n "${MYSQL_PASSWORD:-}" ]]; then
  cat > "${OUTPUT_DIR}/secret.yaml" <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: kip-provider-secret
  labels:
    app: kip-provider
type: Opaque
stringData:
  MYSQL_URL: "${MYSQL_URL}"
  MYSQL_USERNAME: "${MYSQL_USERNAME}"
  MYSQL_PASSWORD: "${MYSQL_PASSWORD}"
EOF
fi

echo "Rendered manifests:"
find "${OUTPUT_DIR}" -maxdepth 1 -type f | sort
