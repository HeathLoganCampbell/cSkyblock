#!/bin/bash

# Auto-rebuild on file changes
# Requires: fswatch (brew install fswatch)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

if ! command -v fswatch &> /dev/null; then
    echo -e "${RED}fswatch not found. Install with: brew install fswatch${NC}"
    exit 1
fi

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     Watching for changes...            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""
echo -e "Watching: ${YELLOW}*/src/main/java/**/*.java${NC}"
echo -e "Press ${RED}Ctrl+C${NC} to stop"
echo ""

last_build=0
cooldown=3  # seconds between builds

fswatch -r \
    --include '\.java$' \
    --exclude '.*' \
    "$PROJECT_ROOT/Exchange/src" \
    "$PROJECT_ROOT/Library/src" \
    "$PROJECT_ROOT/Saga/src" \
    "$PROJECT_ROOT/Islands/src" \
    "$PROJECT_ROOT/Spawn/src" \
    "$PROJECT_ROOT/Proxy/src" \
    | while read file; do
        current_time=$(date +%s)
        
        # Cooldown to batch multiple file changes
        if [ $((current_time - last_build)) -lt $cooldown ]; then
            continue
        fi
        last_build=$current_time
        
        echo ""
        echo -e "${YELLOW}[$(date +%H:%M:%S)]${NC} Change detected: $(basename "$file")"
        echo -e "${BLUE}Rebuilding...${NC}"
        
        cd "$PROJECT_ROOT"
        if mvn package -DskipTests -q; then
            # Copy plugins
            cp "$PROJECT_ROOT/out/Proxy-1.0-SNAPSHOT.jar" "$PROJECT_ROOT/runtime/proxy/plugins/Skyblock-Proxy.jar" 2>/dev/null || true
            cp "$PROJECT_ROOT/out/Spawn-1.0-SNAPSHOT.jar" "$PROJECT_ROOT/runtime/spawn/plugins/Skyblock-Spawn.jar" 2>/dev/null || true
            cp "$PROJECT_ROOT/out/Islands-1.0-SNAPSHOT.jar" "$PROJECT_ROOT/runtime/islands-1/plugins/Skyblock-Islands.jar" 2>/dev/null || true
            cp "$PROJECT_ROOT/out/Islands-1.0-SNAPSHOT.jar" "$PROJECT_ROOT/runtime/islands-2/plugins/Skyblock-Islands.jar" 2>/dev/null || true
            
            echo -e "${GREEN}[✓] Build successful!${NC}"
            echo -e "${YELLOW}Note: Restart servers or use /reload to apply changes${NC}"
        else
            echo -e "${RED}[✗] Build failed${NC}"
        fi
    done

