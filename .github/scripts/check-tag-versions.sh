#!/bin/bash
set -e

# Check if gradle.properties exists
if [ ! -f "gradle.properties" ]; then
  echo "Error: gradle.properties not found"
  exit 1
fi

# Read properties from gradle.properties
MOD_VERSION=$(grep "^mod_version=" gradle.properties | cut -d'=' -f2)
MC_VERSION=$(grep "^minecraft_version=" gradle.properties | cut -d'=' -f2)

# Get tag name from environment or argument
TAG_NAME="${GITHUB_REF_NAME:-$1}"

if [ -z "$TAG_NAME" ]; then
  echo "Error: Tag name not provided (set GITHUB_REF_NAME or pass as argument)"
  exit 1
fi

echo "Checking tag: $TAG_NAME"
echo "mod_version: $MOD_VERSION"
echo "minecraft_version: $MC_VERSION"

# Check 1: mod_version must be in tag
if ! echo "$TAG_NAME" | grep -q "$MOD_VERSION"; then
  echo "Error: Tag '$TAG_NAME' does not contain mod_version '$MOD_VERSION'"
  exit 1
fi
echo "✓ mod_version check passed"

# Check 2: minecraft_version must be in tag
if ! echo "$TAG_NAME" | grep -q "$MC_VERSION"; then
  echo "Error: Tag '$TAG_NAME' does not contain minecraft_version '$MC_VERSION'"
  exit 1
fi
echo "✓ minecraft_version check passed"

# Note: No longer checking for loader type in tag name
# We publish both fabric and neoforge from the same tag

# Determine release type
# Check for beta first (since 'beta' contains 'alpha' as substring if we're not careful)
# Convert to lowercase for case-insensitive matching
TAG_LOWER=$(echo "$TAG_NAME" | tr '[:upper:]' '[:lower:]')
if echo "$TAG_LOWER" | grep -q "beta"; then
  RELEASE_TYPE="beta"
elif echo "$TAG_LOWER" | grep -q "alpha"; then
  RELEASE_TYPE="alpha"
else
  RELEASE_TYPE="release"
fi
echo "Release type: $RELEASE_TYPE"

# Export to GITHUB_ENV if in GitHub Actions
if [ -n "$GITHUB_ENV" ]; then
  echo "MOD_VERSION=$MOD_VERSION" >> "$GITHUB_ENV"
  echo "MC_VERSION=$MC_VERSION" >> "$GITHUB_ENV"
  echo "RELEASE_TYPE=$RELEASE_TYPE" >> "$GITHUB_ENV"
fi

echo "All tag version checks passed!"
