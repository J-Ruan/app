#!/usr/bin/env bash
# Source this file from the existing checkout; no extra worktree is needed.
export ANDROID_HOME="${ANDROID_HOME:-/workspace/toolchains/android-sdk}"
export ANDROID_USER_HOME="${ANDROID_USER_HOME:-/workspace/android-user}"
export ANDROID_EMULATOR_HOME="$ANDROID_USER_HOME"
export ANDROID_AVD_HOME="$ANDROID_USER_HOME/avd"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/workspace/gradle-home}"
if [[ -x /workspace/toolchains/jdk-21.0.5+11/bin/javac ]]; then
  export JAVA_HOME=/workspace/toolchains/jdk-21.0.5+11
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
