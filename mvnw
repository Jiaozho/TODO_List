#!/bin/sh
set -e

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")"; pwd -P)"

exec "$MAVEN_PROJECTBASEDIR"/.mvn/wrapper/maven-wrapper.sh "$@"

