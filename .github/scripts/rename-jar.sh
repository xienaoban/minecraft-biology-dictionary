#!/bin/bash
set -e

# Validate required variables
if [ -z "$MOD_VERSION" ] || [ -z "$MC_VERSION" ] || [ -z "$LOADER_TYPE" ]; then
  echo "Error: Required environment variables not set (MOD_VERSION, MC_VERSION, LOADER_TYPE)"
  echo "Please run check-tag-versions.sh first"
  exit 1
fi

# Source and target jar names
SOURCE_JAR="build/libs/biologydictionary-${MOD_VERSION}.jar"
TARGET_JAR="build/libs/biologydictionary-${MOD_VERSION}-mc${MC_VERSION}-${LOADER_TYPE}.jar"

echo "Renaming jar:"
echo "  Source: $SOURCE_JAR"
echo "  Target: $TARGET_JAR"

# Check if source jar exists
if [ ! -f "$SOURCE_JAR" ]; then
  echo "Error: Source jar '$SOURCE_JAR' not found!"
  echo "Available files in build/libs/:"
  ls -la build/libs/ || echo "build/libs/ directory does not exist"
  exit 1
fi

# Rename the jar
mv "$SOURCE_JAR" "$TARGET_JAR"

echo "✓ Jar renamed successfully to: $TARGET_JAR"

# Export to GITHUB_ENV if in GitHub Actions
if [ -n "$GITHUB_ENV" ]; then
  echo "MOD_JAR=$TARGET_JAR" >> "$GITHUB_ENV"
fi
