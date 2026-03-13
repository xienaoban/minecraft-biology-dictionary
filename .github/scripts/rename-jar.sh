#!/bin/bash
set -e

# Validate required variables
if [ -z "$MOD_VERSION" ] || [ -z "$MC_VERSION" ]; then
  echo "Error: Required environment variables not set (MOD_VERSION, MC_VERSION)"
  echo "Please run check-tag-versions.sh first"
  exit 1
fi

echo "Renaming jars for version: $MOD_VERSION, MC: $MC_VERSION"

# Array of platforms
PLATFORMS=("fabric" "forge")

# Process each platform
for PLATFORM in "${PLATFORMS[@]}"; do
  # Source and target jar names
  SOURCE_JAR="${PLATFORM}/build/libs/biology-dictionary-${PLATFORM}-${MOD_VERSION}.jar"
  TARGET_JAR="${PLATFORM}/build/libs/biology-dictionary-${MOD_VERSION}-mc${MC_VERSION}-${PLATFORM}.jar"

  echo ""
  echo "Processing $PLATFORM:"
  echo "  Source: $SOURCE_JAR"
  echo "  Target: $TARGET_JAR"

  # Check if source jar exists
  if [ ! -f "$SOURCE_JAR" ]; then
    echo "  ❌Error: Source jar '$SOURCE_JAR' not found!"
    echo "  Available files in ${PLATFORM}/build/libs/:"
    ls -la "${PLATFORM}/build/libs/" || echo "  ${PLATFORM}/build/libs/ directory does not exist"
    exit 1
  fi

  # Rename the jar
  mv "$SOURCE_JAR" "$TARGET_JAR"
  echo "  ✓ Jar renamed successfully to: $TARGET_JAR"

  # Export to GITHUB_ENV if in GitHub Actions
  if [ -n "$GITHUB_ENV" ]; then
    echo "MOD_JAR_${PLATFORM^^}=$TARGET_JAR" >> "$GITHUB_ENV"
  fi
done

echo ""
echo "All jar renaming completed!"
