#!/bin/bash
set -e

# Validate required environment variables
if [ -z "$MOD_JAR" ] || [ -z "$MC_VERSION" ] || [ -z "$LOADER_TYPE" ]; then
  echo "Error: Required environment variables not set (MOD_JAR, MC_VERSION, LOADER_TYPE)"
  exit 1
fi

SERVER_DIR="${SERVER_DIR:-${LOADER_TYPE}/build/vanillaServer}"

echo "Setting up Minecraft server integration test..."
echo "Server directory: $SERVER_DIR"
echo "Mod jar: $MOD_JAR"
echo "Minecraft version: $MC_VERSION"
echo "Loader type: $LOADER_TYPE"

# Create server directory
mkdir -p "$SERVER_DIR"
cd "$SERVER_DIR"
rm -rf *

# Function to get server jar URL from Mojang's version manifest
# Not used
get_server_url() {
  local ver=$1

  python3 << EOF
import json
import urllib.request

manifest_url = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
with urllib.request.urlopen(manifest_url) as response:
    manifest = json.loads(response.read())

version_url = None
for v in manifest.get("versions", []):
    if v.get("id") == "$ver":
        version_url = v.get("url")
        break

if not version_url:
    print(f"Error: Version $ver not found in manifest", file=__import__('sys').stderr)
    exit(1)

with urllib.request.urlopen(version_url) as response:
    version_data = json.loads(response.read())

server_url = version_data.get("downloads", {}).get("server", {}).get("url")
if server_url:
    print(server_url)
else:
    print("Error: Could not extract server jar URL", file=__import__('sys').stderr)
    exit(1)
EOF
}

# Download the server by mod loader

# Get server jar URL dynamically
# SERVER_URL=$(get_server_url "$MC_VERSION")
# echo "Server jar URL: $SERVER_URL"

# Download Minecraft server jar
# echo "Downloading Minecraft server $MC_VERSION..."
# wget -q "$SERVER_URL" -O server.jar

# Install loader based on type
if [ "$LOADER_TYPE" = "fabric" ]; then
  echo "Downloading Fabric installer..."
  wget -q "https://maven2.fabricmc.net/net/fabricmc/fabric-installer/1.1.1/fabric-installer-1.1.1.jar" -O fabric-installer.jar
  echo "Installing Fabric loader..."
  java -jar fabric-installer.jar server -mcversion "$MC_VERSION" -downloadMinecraft

  echo "Files after Fabric installation:"
  ls -la

  # Check for fabric-server-launch.jar
  if [ -f "fabric-server-launch.jar" ]; then
    echo "✓ Fabric loader installed successfully"
  else
    echo "✗ fabric-server-launch.jar not found"
    exit 1
  fi
elif [ "$LOADER_TYPE" = "neoforge" ]; then
  echo "Downloading NeoForge installer..."
  FORGE_VERSION=$(grep "^neoforge_version=" ../../../gradle.properties | cut -d'=' -f2)
  echo "FORGE_VERSION=${FORGE_VERSION}"
  FORGE_INSTALLER_URL="https://maven.neoforged.net/releases/net/neoforged/neoforge/${FORGE_VERSION}/neoforge-${FORGE_VERSION}-installer.jar"
  echo "FORGE_INSTALLER_URL=${FORGE_INSTALLER_URL}"
  wget -q "$FORGE_INSTALLER_URL" -O neoforge-installer.jar
  echo "Installing neoforge loader..."
  java -jar neoforge-installer.jar --installServer

  echo "Files after NeoForge installation:"
  ls -la

  # Check for run.sh
  if [ -f "run.sh" ]; then
    echo "✓ NeoForge loader installed successfully"
    chmod +x run.sh
  else
    echo "✗ run.sh not found"
    exit 1
  fi
else
  echo "Error: Unknown loader type: $LOADER_TYPE"
  exit 1
fi

# Accept EULA
echo "eula=true" > eula.txt
echo "✓ EULA accepted"

# Create mods directory and copy mod
mkdir -p mods
cp "../../../$MOD_JAR" "mods/"
echo "✓ Mod copied to mods/"

# Download and install dependencies
if [ "$LOADER_TYPE" = "fabric" ]; then
  echo "Downloading Fabric API..."
  # Get Fabric API version from gradle.properties
  FABRIC_API_VERSION=$(grep "^fabric_api_version=" ../../../gradle.properties | cut -d'=' -f2)

  # Fabric API URL pattern
  FABRIC_API_URL="https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/${FABRIC_API_VERSION}/fabric-api-${FABRIC_API_VERSION}.jar"

  echo "Downloading Fabric API from: $FABRIC_API_URL"
  if wget -q "$FABRIC_API_URL" -P mods/; then
    echo "✓ Fabric API downloaded successfully"
  else
    echo "✗ Failed to download Fabric API for version $FABRIC_API_VERSION"
    exit 1
  fi

  echo "Downloading Cloth Config..."
  # Get Cloth Config version from gradle.properties
  CLOTHCONFIG_VERSION=$(grep "^clothconfig_version=" ../../../gradle.properties | cut -d'=' -f2)

  # Cloth Config URL from Maven
  CLOTHCONFIG_URL="https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-fabric/${CLOTHCONFIG_VERSION}/cloth-config-fabric-${CLOTHCONFIG_VERSION}-fabric.jar"

  echo "Downloading Cloth Config from: $CLOTHCONFIG_URL"
  if wget -q "$CLOTHCONFIG_URL" -P mods/; then
    echo "✓ Cloth Config downloaded successfully"
  else
    echo "✗ Failed to download Cloth Config for version $CLOTHCONFIG_VERSION"
    exit 1
  fi
elif [ "$LOADER_TYPE" = "neoforge" ]; then
  echo "Downloading Architectury API..."
  # Get Architectury API version from gradle.properties
  ARCHITECTURY_API_VERSION=$(grep "^architectury_api_version=" ../../../gradle.properties | cut -d'=' -f2)

  # Architectury API URL
  ARCHITECTURY_API_URL="https://maven.architectury.dev/dev/architectury/architectury-neoforge/${ARCHITECTURY_API_VERSION}/architectury-neoforge-${ARCHITECTURY_API_VERSION}.jar"

  echo "Downloading Architectury API from: $ARCHITECTURY_API_URL"
  if wget -q "$ARCHITECTURY_API_URL" -P mods/; then
    echo "✓ Architectury API downloaded successfully"
  else
    echo "✗ Failed to download Architectury API for version $ARCHITECTURY_API_VERSION"
    exit 1
  fi

  echo "Downloading Cloth Config..."
  # Get Cloth Config version from gradle.properties
  CLOTHCONFIG_VERSION=$(grep "^clothconfig_version=" ../../../gradle.properties | cut -d'=' -f2)

  # Cloth Config NeoForge URL from Maven
  CLOTHCONFIG_URL="https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-neoforge/${CLOTHCONFIG_VERSION}/cloth-config-neoforge-${CLOTHCONFIG_VERSION}-neoforge.jar"

  echo "Downloading Cloth Config from: $CLOTHCONFIG_URL"
  if wget -q "$CLOTHCONFIG_URL" -P mods/; then
    echo "✓ Cloth Config downloaded successfully"
  else
    echo "✗ Failed to download Cloth Config for version $CLOTHCONFIG_VERSION"
    exit 1
  fi
fi

# Set server.properties for headless operation
cat > server.properties << EOF
online-mode=false
level-seed=testing
gamemode=survival
difficulty=hard
spawn-protection=0
enable-command-block=true
EOF

# Run server in background with timeout
echo "Starting Minecraft server..."
if [ "$LOADER_TYPE" = "fabric" ]; then
  timeout 120s java -Xmx1G -Xms1G -jar fabric-server-launch.jar nogui &
  SERVER_PID=$!
elif [ "$LOADER_TYPE" = "neoforge" ]; then
  # NeoForge uses run.sh script
  timeout 120s ./run.sh &
  SERVER_PID=$!
fi

# Wait for mod to load (look for "EntityManager initialized." in logs)
for i in {1..60}; do
  if grep -q "EntityManager initialized\." logs/latest.log 2>/dev/null; then
    echo "✓ Mod loaded successfully!"
    break
  fi
  if ! kill -0 $SERVER_PID 2>/dev/null; then
    echo "✗ Server process died unexpectedly!"
    cat logs/latest.log
    exit 1
  fi
  sleep 2
done

# Check if mod loaded successfully
if ! grep -q "EntityManager initialized\." logs/latest.log 2>/dev/null; then
  echo "✗ Mod failed to load within timeout"
  tail -n 50 logs/latest.log
  exit 1
fi

# Stop server gracefully
echo "Stopping server..."
kill $SERVER_PID
wait $SERVER_PID || true

echo "✓ Integration test passed!"
