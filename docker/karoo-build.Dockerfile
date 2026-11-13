# Android build environment for the Karoo extension. Used by docker-compose.build.yml / `just build-docker`.
# The repository is mounted at /workspace at run time; nothing from the source tree is baked into the image.
FROM docker.io/library/eclipse-temurin:21-jdk-jammy

ARG ANDROID_COMMAND_LINE_TOOLS=15859902
ARG ANDROID_COMMAND_LINE_TOOLS_SHA256=4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583

ENV ANDROID_HOME=/opt/android-sdk \
    HOME=/home/builder \
    GRADLE_USER_HOME=/home/builder/.gradle \
    ANDROID_USER_HOME=/home/builder/.android
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN apt-get update \
    && apt-get install --yes --no-install-recommends ca-certificates curl unzip \
    && rm -rf /var/lib/apt/lists/*

# Extra CA certificates (e.g. a TLS-inspecting corporate proxy). Every *.crt in docker/certs/ is trusted
# by the OS (curl, sdkmanager) and by the JDK (Gradle). The directory may be empty.
COPY docker/certs/ /usr/local/share/ca-certificates/
RUN update-ca-certificates \
    && for f in /usr/local/share/ca-certificates/*.crt; do \
        [ -e "$f" ] || continue; \
        keytool -importcert -noprompt -trustcacerts -cacerts -storepass changeit \
            -alias "extra-$(basename "$f" .crt)" -file "$f"; \
    done

RUN curl --fail --location --retry 3 \
        "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_COMMAND_LINE_TOOLS}_latest.zip" \
        --output /tmp/android-tools.zip \
    && echo "${ANDROID_COMMAND_LINE_TOOLS_SHA256}  /tmp/android-tools.zip" | sha256sum --check - \
    && mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && unzip -q /tmp/android-tools.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/android-tools.zip
RUN yes | sdkmanager --licenses >/dev/null
# compileSdk: app/ = 35, lib/ = 34. build-tools 35.0.0 is AGP's default.
RUN sdkmanager "platforms;android-35" "platforms;android-34" "build-tools;35.0.0" "platform-tools"

ARG KAROO_BUILD_UID=1000
ARG KAROO_BUILD_GID=1000
RUN groupadd --non-unique --gid "${KAROO_BUILD_GID}" builder \
    && useradd --create-home --non-unique --uid "${KAROO_BUILD_UID}" --gid builder builder \
    && mkdir -p "${GRADLE_USER_HOME}" "${ANDROID_USER_HOME}" /workspace \
    && chown -R builder:builder /home/builder /workspace

USER builder
WORKDIR /workspace
# gradle.properties pins org.gradle.java.home to a host (Windows) JDK; a -D on the command line wins.
# The Kotlin compile daemon fails to start under amd64 emulation, so compile in-process.
ENTRYPOINT ["bash", "gradlew", "--no-daemon", "--no-watch-fs", \
    "-Dorg.gradle.java.home=/opt/java/openjdk", "-Pkotlin.compiler.execution.strategy=in-process"]
CMD [":app:assembleDebug"]
