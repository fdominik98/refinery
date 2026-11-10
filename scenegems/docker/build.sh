#!/usr/bin/env bash

# SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
#
# SPDX-License-Identifier: EPL-2.0

# Builds the SceneGEMS model generation image from this Refinery checkout.
#
#   scenegems/docker/build.sh [--test] [--tag IMAGE] [docker build options...]
#
# --test  runs the tests of the interface in the build container first
# --tag   the image name (default: $SCENEGEMS_MODEL_GENERATION_IMAGE or scenegems-model-generation-subsystem:latest)

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repository_root="$(cd "${script_dir}/../.." && pwd)"
image="${SCENEGEMS_MODEL_GENERATION_IMAGE:-scenegems-model-generation-subsystem:latest}"
run_tests=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --test)
            run_tests=true
            shift
            ;;
        --tag)
            image="$2"
            shift 2
            ;;
        -h | --help)
            sed -n '7,12p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *)
            break
            ;;
    esac
done

revision="$(git -C "${repository_root}" rev-parse HEAD 2>/dev/null || echo unknown)"
if [[ -n "$(git -C "${repository_root}" status --porcelain 2>/dev/null || true)" ]]; then
    revision="${revision}-dirty"
fi

if [[ "${run_tests}" == true ]]; then
    docker build -f "${script_dir}/Dockerfile" --target test "$@" "${repository_root}"
fi

docker build -f "${script_dir}/Dockerfile" --target runtime \
    --label "org.opencontainers.image.revision=${revision}" \
    --label "org.opencontainers.image.title=SceneGEMS model generation" \
    -t "${image}" "$@" "${repository_root}"

echo "Built ${image} (Refinery ${revision})"
