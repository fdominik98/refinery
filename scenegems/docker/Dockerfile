# syntax=docker/dockerfile:1
# SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
#
# SPDX-License-Identifier: EPL-2.0

# The model generation image of SceneGEMS: Refinery with the `generate-models` script.
#
# Build it from the root of the Refinery repository (scenegems/docker/build.sh does this):
#   docker build -f scenegems/docker/Dockerfile -t scenegems-model-generation-subsystem:latest .
# The `test` target runs the tests of the interface instead:
#   docker build -f scenegems/docker/Dockerfile --target test .

FROM docker.io/eclipse-temurin:25-jdk AS build
ARG TARGETARCH
WORKDIR /refinery
COPY . .
# A Windows checkout may have CRLF line endings in the wrapper script.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --console=plain :refinery-scenegems:distTar
# Keep only the native solver jars of the target platform; the start script skips the missing ones.
RUN set -eux; \
    mkdir /dist; \
    tar -xf scenegems/build/distributions/refinery-scenegems-*.tar -C /dist --strip-components=1; \
    rm -f /dist/lib/*-darwin-*.jar /dist/lib/*-win32-*.jar; \
    case "${TARGETARCH:-amd64}" in \
      amd64) rm -f /dist/lib/*-linux-aarch64-*.jar ;; \
      arm64) rm -f /dist/lib/*-linux-x86-64-*.jar ;; \
    esac

FROM build AS test
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --console=plain :refinery-scenegems:test

FROM docker.io/eclipse-temurin:25-jre AS runtime
COPY --link --from=build /dist /app
RUN ln -s /app/bin/refinery-scenegems /usr/local/bin/generate-models \
    && mkdir /job && chown 1000:1000 /job
# Enumerating every model keeps every explored state in memory, so let the heap grow past the JVM's default quarter
# of the container's memory.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
USER 1000
WORKDIR /job
CMD ["generate-models", "job"]
