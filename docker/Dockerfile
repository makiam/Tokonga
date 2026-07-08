# Tokonga development and testing environment
# Usage: docker compose build && docker compose run --rm test

FROM eclipse-temurin:17-jdk-jammy

# Mesa (software OpenGL) + Xvfb + X11 libs for JOGL/Jemmy GUI tests
# openbox provides a real window manager (required by Jemmy's JFrameOperator)
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    libgl1-mesa-glx \
    libgl1-mesa-dri \
    libglu1-mesa \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxext6 \
    libxrandr2 \
    libfreetype6 \
    libfontconfig1 \
    mesa-utils \
    openbox \
    git \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Force software OpenGL (no GPU in container)
ENV DISPLAY=:99 \
    LIBGL_ALWAYS_SOFTWARE=1 \
    GALLIUM_DRIVER=llvmpipe

WORKDIR /workspace

# -- Layer 1: Gradle wrapper + build config (cached unless changed) --
COPY gradlew gradlew.bat settings.gradle build.gradle gradle.properties ./
COPY gradle/wrapper gradle/wrapper/
COPY buildSrc buildSrc/
RUN chmod +x gradlew

# -- Layer 2: All source (for dependency resolution) --
COPY . .
RUN ./gradlew --no-daemon --quiet dependencies || true

# Xvfb startup wrapper with proper readiness loop
RUN printf '#!/bin/bash\n\
Xvfb :99 -screen 0 1280x1024x24 -ac +extension GLX &\n\
XVFB_PID=$!\n\
for i in $(seq 1 20); do\n\
  xdpyinfo -display :99 >/dev/null 2>&1 && break\n\
  sleep 0.5\n\
done\n\
openbox &>/dev/null &\n\
sleep 1\n\
"$@"\n\
EXIT_CODE=$?\n\
kill $XVFB_PID 2>/dev/null\n\
exit $EXIT_CODE\n' > /usr/local/bin/xvfb-run-wrapper && \
    chmod +x /usr/local/bin/xvfb-run-wrapper

ENTRYPOINT ["/usr/local/bin/xvfb-run-wrapper"]
CMD ["./gradlew", "--no-daemon", "test", "--continue"]
