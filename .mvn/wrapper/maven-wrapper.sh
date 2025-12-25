#!/bin/sh
set -e

WRAPPER_DIR="$(cd "$(dirname "$0")"; pwd -P)"
BASE_DIR="$(cd "$WRAPPER_DIR/../.."; pwd -P)"

PROPS_FILE="$WRAPPER_DIR/maven-wrapper.properties"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"

distributionUrl="$(grep -E '^distributionUrl=' "$PROPS_FILE" | cut -d'=' -f2-)"
wrapperUrl="$(grep -E '^wrapperUrl=' "$PROPS_FILE" | cut -d'=' -f2-)"

if [ ! -f "$WRAPPER_JAR" ]; then
  if [ -z "$wrapperUrl" ]; then
    echo "wrapperUrl is not set in $PROPS_FILE" >&2
    exit 1
  fi
  echo "Downloading Maven Wrapper from $wrapperUrl"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$wrapperUrl" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$wrapperUrl" -O "$WRAPPER_JAR"
  else
    echo "Neither curl nor wget is available to download Maven Wrapper." >&2
    exit 1
  fi
fi

exec java -Dmaven.multiModuleProjectDirectory="$BASE_DIR" -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
