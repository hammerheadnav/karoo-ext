set dotenv-load := true
#set dotenv-required := true
set windows-shell := ["powershell.exe", "/c"]
set shell := ["bash", "-c"]

JAVA_HOME := "C:\\Program Files\\Java\\latest\\jdk-21"
container := env("CONTAINER_ENGINE", if os_family() == "windows" { "podman" } else { `command -v docker >/dev/null && echo docker || echo podman` })

devs:
    adb devices

install:
    adb -s 00446GA250970042 install -r app/build/outputs/apk/debug/app-debug.apk

make-spotless:
    ./gradlew :spotlessApply


build: make-spotless
    ./gradlew build

# Build the debug APK in a container (no local JDK/Android SDK needed) -> app/build/outputs/apk/debug/app-debug.apk
build-docker:
    {{ container }} compose -f docker-compose.build.yml run --rm --build karoo :app:assembleDebug

clean:
    ./gradlew clean

list-tasks:
    ./gradlew tasks

kill-java:
    taskkill /F /FI "IMAGENAME eq java.exe"

[env("ANTHROPIC_BASE_URL", "https://api.deepseek.com/anthropic")]
[env("ANTHROPIC_DEFAULT_HAIKU_MODEL", "deepseek-v4-flash")]
[env("ANTHROPIC_DEFAULT_OPUS_MODEL", "deepseek-v4-pro[1m]")]
[env("ANTHROPIC_DEFAULT_SONNET_MODEL", "deepseek-v4-pro[1m]")]
[env("ANTHROPIC_MODEL", "deepseek-v4-pro[1m]")]
[env("CLAUDE_CODE_EFFORT_LEVEL", "max")]
[env("CLAUDE_CODE_SUBAGENT_MODEL", "deepseek-v4-flash")]
[env("CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC", "1")]
claude-deepseek:
    claude --model opus --effort max

alias claude := claude-deepseek
