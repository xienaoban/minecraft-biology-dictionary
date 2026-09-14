#!/bin/bash
set -e

PLATFORM="$1"
if [ -z "$PLATFORM" ]; then
  echo "Error: Platform argument is required (e.g. fabric, neoforge)"
  exit 1
fi

if [ -z "$MOD_VERSION" ] || [ -z "$MC_VERSION" ]; then
  echo "Error: Required environment variables not set (MOD_VERSION, MC_VERSION)"
  exit 1
fi

MOD_ID=$(grep "^mod_id=" gradle.properties | cut -d'=' -f2)
if [ -z "$MOD_ID" ]; then
  echo "Error: mod_id not found in gradle.properties"
  exit 1
fi

SOURCE_JAR="${PLATFORM}/build/libs/${MOD_ID}-${PLATFORM}-${MOD_VERSION}.jar"
TARGET_JAR="${PLATFORM}/build/libs/biology-dictionary-${MOD_VERSION}-mc${MC_VERSION}-${PLATFORM}.jar"

if [ ! -f "$SOURCE_JAR" ]; then
  echo "❌Error: Source jar '$SOURCE_JAR' not found!"
  ls -la "${PLATFORM}/build/libs/" 2>/dev/null || echo "${PLATFORM}/build/libs/ directory does not exist"
  exit 1
fi

mv "$SOURCE_JAR" "$TARGET_JAR"
echo "✓ Jar renamed successfully to: $TARGET_JAR"

if [ -n "$GITHUB_ENV" ]; then
  echo "MOD_JAR_${PLATFORM^^}=$TARGET_JAR" >> "$GITHUB_ENV"
fi
