#!/bin/bash
set -e

# Validate required environment variables
REQUIRED_VARS=("MOD_VERSION" "MC_VERSION" "RELEASE_TYPE")
MISSING=()

for VAR in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!VAR}" ]; then
    MISSING+=("$VAR")
  fi
done

if [ ${#MISSING[@]} -gt 0 ]; then
  echo "Error: Missing required environment variables: ${MISSING[*]}"
  exit 1
fi

echo "Validated: MOD_VERSION=$MOD_VERSION, MC_VERSION=$MC_VERSION, RELEASE_TYPE=$RELEASE_TYPE"

# Export to GITHUB_OUTPUT (for downstream jobs)
echo "mod_version=$MOD_VERSION" >> "$GITHUB_OUTPUT"
echo "mc_version=$MC_VERSION" >> "$GITHUB_OUTPUT"
echo "release_type=$RELEASE_TYPE" >> "$GITHUB_OUTPUT"

# Export to GITHUB_ENV (for subsequent steps in this job)
echo "MOD_VERSION=$MOD_VERSION" >> "$GITHUB_ENV"
echo "MC_VERSION=$MC_VERSION" >> "$GITHUB_ENV"
echo "RELEASE_TYPE=$RELEASE_TYPE" >> "$GITHUB_ENV"

# JAR validation: controlled by the first argument
#   (no argument)  - validate MOD_JAR
#   no-jar-name    - skip jar validation
#   both-jars      - validate MOD_JAR_FABRIC and MOD_JAR_FORGE separately
case "$1" in
  both-jars)
    MISSING=()
    if [ -z "$MOD_JAR_FABRIC" ]; then
      MISSING+=("MOD_JAR_FABRIC")
    fi
    if [ -z "$MOD_JAR_FORGE" ]; then
      MISSING+=("MOD_JAR_FORGE")
    fi
    if [ ${#MISSING[@]} -gt 0 ]; then
      echo "Error: Missing required environment variables: ${MISSING[*]}"
      exit 1
    fi
    echo "mod_jar_fabric=$MOD_JAR_FABRIC" >> "$GITHUB_OUTPUT"
    echo "mod_jar_forge=$MOD_JAR_FORGE" >> "$GITHUB_OUTPUT"
    echo "MOD_JAR_FABRIC=$MOD_JAR_FABRIC" >> "$GITHUB_ENV"
    echo "MOD_JAR_FORGE=$MOD_JAR_FORGE" >> "$GITHUB_ENV"
    echo "Validated: MOD_JAR_FABRIC=$MOD_JAR_FABRIC, MOD_JAR_FORGE=$MOD_JAR_FORGE"
    ;;
  no-jar-name)
    # No jar validation needed
    ;;
  *)
    if [ -z "$MOD_JAR" ]; then
      echo "Error: MOD_JAR is required but not set"
      exit 1
    fi
    echo "mod_jar=$MOD_JAR" >> "$GITHUB_OUTPUT"
    echo "MOD_JAR=$MOD_JAR" >> "$GITHUB_ENV"
    echo "Validated: MOD_JAR=$MOD_JAR"
    ;;
esac
