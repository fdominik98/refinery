<!--
  SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>

  SPDX-License-Identifier: EPL-2.0
-->

# SceneGEMS model generation interface

This folder holds everything SceneGEMS needs to run Refinery as step 0 of a campaign: a container that takes a metamodel with a partial model, generates its models up to a configurable cap (or all of them), reports its progress continuously and hands back the models as `.problem` files.

The container speaks the SceneGEMS per-job protocol (`backend/src/scenegems_tool/cluster/node/launchers/per_job/job_container_io.py` in SceneGEMS), so a SceneGEMS subsystem can run it like its other per-job runners.

| Path | Content |
|------|---------|
| `src/main/java/tools/refinery/scenegems/` | The `generate-models` program (Gradle project `:refinery-scenegems`) |
| `src/test/java/tools/refinery/scenegems/` | Its tests, including the model counts of the SceneGEMS COLREGS catalog |
| `docker/Dockerfile` | The image: a JDK 25 build stage, a `test` stage and the JRE runtime |
| `docker/build.sh` | Builds the image from this checkout |

## Building

From the root of the Refinery repository:

```bash
scenegems/docker/build.sh --test
```

This runs the tests and builds `scenegems-model-generation-subsystem:latest` (change it with `--tag` or `SCENEGEMS_MODEL_GENERATION_IMAGE`). The build runs Gradle inside the image, so the host needs only Docker. The image records the Refinery revision in the label `org.opencontainers.image.revision`.

## The job protocol

The image's default command is `generate-models job`, which works in the job folder `/job`:

| When | File | Content |
|------|------|---------|
| before the start | `/job/job.json` | `{"job": JobSpec}`; the generation request is the job's `payload` |
| while running | standard output | one status line at a time (see below); logs go to the standard error |
| while running, optional | `/job/stop` | its presence stops the generation within a quarter second; the models found so far are kept |
| after the stop | `/job/result.json` | `{"result": {...}}` or `{"error": "...", "retryable": false}` |
| after the stop | `/job/models/model_NNNNN.problem` | one file per model, numbered in the order they were found |

The container needs no network (`network_mode: none` works) and runs as user 1000. The exit code is 0 with a result and 1 with an error.

### Payload

| Field | Type | Default | Meaning |
|-------|------|---------|---------|
| `problem` | string | required | Refinery source of the metamodel and the partial model |
| `max_models` | integer at least 1, or `null` | `null` | model cap; `null` generates every model |
| `scopes` | list of strings | `[]` | extra scope constraints, for example `["TS=2..3"]` |
| `scope_overrides` | list of strings | `[]` | scope constraints that replace the problem's scope of the same type |
| `random_seed` | integer | `1` | seed of the exploration order |
| `timeout_s` | positive number, or `null` | `null` | time limit of the generation; on timeout the models found so far are returned |
| `load_timeout_s` | positive number | `120` | time limit of loading the problem (see below) |
| `state_coder_depth` | integer at least 1 | Refinery's default (7) | neighborhood depth for telling models apart |
| `status_interval_s` | positive number | `1` | seconds between status lines while generating |
| `include_sources` | boolean | `true` | whether `result.json` holds each model's source next to its file name |

Unknown fields are an error, so a misspelled limit is never ignored. Set `timeout_s` below the job's own timeout: the launcher kills the container when the job times out, and a killed container has no `result.json`.

Without `include_sources` the models stay in `/job/models/`, which the current SceneGEMS launcher does not copy out (it copies only `result.json`).

### Status lines

```text
scenegems-status {"message":"generating models: 17 of at most 100 found after 4.2 s","progress":0.17,"phase":"generating","models_found":17,"max_models":100,"elapsed_s":4.213}
```

SceneGEMS reads `message` and `progress` and treats every line without the prefix as a log line. `progress` is the share of the cap that is filled, `null` while generating without a cap (the number of models is not known in advance), and `1.0` at the end. `phase` is one of `loading`, `initializing`, `generating`, `finished` and `failed`. While generating, a line is written every `status_interval_s` even when no model was found.

### Result

```json
{"result": {
  "stop_reason": "exhausted",
  "complete": true,
  "models_found": 28,
  "max_models": null,
  "random_seed": 1,
  "elapsed_s": 3.512,
  "message": null,
  "models": [{"index": 0, "file": "models/model_00000.problem", "source": "..."}]
}}
```

| `stop_reason` | `complete` | Meaning |
|---------------|------------|---------|
| `exhausted` | `true` | no unexplored state was left: these are all the models |
| `unsatisfiable` | `true` | the problem has no model; `message` says why when the partial model itself is inconsistent |
| `cap_reached` | `false` | `max_models` models were found; there may be more, even if exactly `max_models` exist |
| `timeout` | `false` | `timeout_s` passed |
| `stopped` | `false` | `/job/stop` appeared |

An invalid payload, a problem with syntax or validation errors and an invalid scope give `{"error": ..., "retryable": false}`, since another attempt fails the same way.

Refinery's parser loops forever on some syntax errors, for example a class body without its closing brace at the end of the file. Loading normally takes a few seconds, so when it takes longer than `load_timeout_s`, the container writes such an error result and exits with code 1. Without this the job would only end at the launcher's timeout, which SceneGEMS retries.

## Running without SceneGEMS

`generate-models generate` runs the same generation on a file and writes `models/` and `result.json` into the output folder:

```bash
docker run --rm -v "$PWD:/work" scenegems-model-generation-subsystem:latest \
    generate-models generate /work/colregs.problem -o /work/out -s TS=2 -n 1000
```

Its options mirror the payload: `-max-models`/`-n`, `-scope`/`-s`, `-scope-override`/`-S`, `-random-seed`/`-r`, `-timeout`/`-t`, `-load-timeout`, `-state-coder-depth`, `-status-interval` and `-no-sources`. A file `out/stop` stops it. From a checkout with JDK 25, `./gradlew :refinery-scenegems:generateModels --args="generate ..."` runs it without Docker.

## What "all models" means

The generator explores the refinements of the partial model: every decision adds a fact (a type, a link, an object from a scope), and an accepted state is completed by turning its remaining unknown facts to false. Without a cap the exploration ends when no state is left, so the result is every model of the problem, up to two limits:

- **Models are distinct up to isomorphism, by a hash.** Two models that differ only in the names of their objects count once (the two target ships of a COLREGS class are interchangeable). Refinery decides this with a 32-bit hash of the neighborhoods of the objects, not with a full isomorphism check, so in rare cases two different models get the same hash and one of them is missing. A larger `state_coder_depth` makes this rarer.
- **Every upper scope bound must be finite**, for example `scope node = 0..12` or one bound per class. With `*` there are infinitely many models and the generation only ends at the cap or the timeout.

Attribute values (integers, reals) are not enumerated: each model gets one value that satisfies the constraints. Relations marked `@decide(false)` are never set to true by the generator.

The explored states stay in memory, and their number grows quickly with the scope. The image lets the Java heap use 75 % of the container's memory (`JAVA_OPTS`); give the container a memory limit that fits the largest scope you run.

## Wiring it into SceneGEMS

What the new SceneGEMS subsystem needs, beyond this image:

- a job type for model generation, whose payload is the table above;
- a runner service with the command `["generate-models", "job"]`: `runner_service()` in `job_stack.py` always sets the Python job main as the command today;
- reading `result["models"]` from the job result, and optionally copying `/job/stop` into the container to stop a run early.
