# Docker Development & Testing Environment

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) 20.10+
- Docker Compose v2 (included in Docker Desktop)

## Quick Start

```bash
# Build the image (first time only, ~2 minutes)
make -C docker build

# Run all tests (~90 seconds)
make -C docker test

# Interactive shell
make -C docker dev
```

## Available Commands

| Command | Description |
|---|---|
| `make -C docker build` | Build Docker image |
| `make -C docker test` | Run all tests (unit + GUI) |
| `make -C docker test-unit` | Run non-GUI tests only (fast, stable) |
| `make -C docker dev` | Interactive shell in container |
| `make -C docker assemble` | Build distribution (no tests) |
| `make -C docker clean` | Clean build artifacts |

You can also run from the `docker/` directory directly:

```bash
cd docker
make build
make test
```

## Module-Specific Tests

Inside the interactive dev shell:

```bash
make -C docker dev

# Inside the container:
./gradlew :ArtOfIllusion:test --no-daemon
./gradlew :StandardModules:test --no-daemon
./gradlew :Filters:test --no-daemon
```

## Architecture

The Docker environment consists of:

```
Eclipse Temurin 17 (JDK)
  → Mesa (software OpenGL, GALLIUM_DRIVER=llvmpipe)
    → Xvfb :99 (virtual framebuffer, 1280x1024x24)
      → openbox (window manager for Jemmy GUI tests)
        → Gradle (JUnit 5 + Mockito + Jemmy)
```

### Why Each Component

| Component | Purpose |
|---|---|
| **Eclipse Temurin 17** | Project Java toolchain version |
| **Xvfb** | Virtual display for GUI tests (no physical screen needed) |
| **Mesa + llvmpipe** | Software OpenGL renderer (JOGL viewports) |
| **openbox** | Minimal window manager — required by Jemmy's JFrameOperator |

## Services

`docker-compose.yml` defines three services:

| Service | Command | Use Case |
|---|---|---|
| `test` | `./gradlew --no-daemon test --continue` | CI / full test run |
| `build` | `./gradlew --no-daemon assemble` | Build distribution only |
| `dev` | `/bin/bash` | Interactive debugging |

## Resource Limits

| Resource | Value | Reason |
|---|---|---|
| Memory | 8 GB | Gradle -Xmx6g + JVM overhead |
| Shared Memory | 2 GB | Xvfb framebuffer |
| Gradle Cache | Named volume `gradle-cache` | Persists dependencies between runs |

The `gradle-cache` volume survives `docker compose down`. To clear it:

```bash
docker volume rm tokonga_gradle-cache
```

## Test Baseline

Verified in Docker on 2026-07-07:

| Metric | Value |
|---|---|
| Total test cases | 1,090 |
| Passed | 1,072 (98.3%) |
| Failed | 18 (pre-existing bugs) |

### Pre-existing Failures

| Category | Count | Root Cause |
|---|---|---|
| NPE in SplineMesh/PolyMesh | 11 | `mesh.texParam is null`, `ViewerCanvas.lineColor is null` |
| DataMap parsing bugs | 4 | Long overflow, locale-dependent decimal parsing |
| GUI/Jemmy flakiness | 3 | Xvfb timing, `startApplication` lifecycle |

These are bugs in the Tokonga codebase, not in the Docker infrastructure.

## Troubleshooting

### Build fails with "Cannot find '.git' directory"

The `.dockerignore` must NOT exclude `.git/` — the `com.palantir.git-version` Gradle plugin requires it. Verify:

```bash
grep .git .dockerignore
# Should return nothing
```

### GUI tests fail with "Cannot connect to X server"

Xvfb needs time to start. The wrapper script waits via `xdpyinfo` polling. If tests still fail:

```bash
make -C docker dev
# Inside container:
xdpyinfo -display :99  # verify X is running
```

### Out of memory

Increase limits in `docker/docker-compose.yml`:

```yaml
mem_limit: 12g
environment:
  - GRADLE_OPTS=-Xmx8g
```

### Slow first build

The first `make -C docker build` downloads the JDK image and all Gradle dependencies. Subsequent builds reuse the `gradle-cache` volume.

## Files

| File | Description |
|---|---|
| `docker/Dockerfile` | Image definition (Temurin 17 + Xvfb + Mesa + openbox) |
| `docker/docker-compose.yml` | Service definitions (test, build, dev) |
| `docker/Makefile` | Convenience commands |
| `.dockerignore` | Files excluded from build context |
