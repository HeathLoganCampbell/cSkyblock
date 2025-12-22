#!/bin/bash

# Simple startup script - runs servers in separate terminal tabs/windows
# This is more reliable than background processes

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RUNTIME_DIR="$PROJECT_ROOT/runtime"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Starting Skyopoly Servers${NC}"
echo ""

# Check if runtime exists
if [ ! -d "$RUNTIME_DIR" ]; then
    echo -e "${RED}Runtime directory not found. Run 'make setup' first.${NC}"
    exit 1
fi

# Check for Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java not found. Please install Java 17+${NC}"
    exit 1
fi

# Function to start a server in a new Terminal window (macOS)
start_in_terminal() {
    local name=$1
    local dir=$2
    local jar=$3
    local memory=$4
    
    osascript <<EOF
tell application "Terminal"
    do script "cd '$RUNTIME_DIR/$dir' && echo 'Starting $name...' && java -Xms${memory} -Xmx${memory} -jar $jar nogui"
    set custom title of front window to "$name"
end tell
EOF
}

# Alternative: Start in background with log files
start_background() {
    local name=$1
    local dir=$2
    local jar=$3
    local memory=$4
    
    echo -e "Starting ${YELLOW}$name${NC}..."
    cd "$RUNTIME_DIR/$dir"
    java -Xms${memory} -Xmx${memory} -jar $jar nogui > server.log 2>&1 &
    echo $! > "$RUNTIME_DIR/.pids/$name.pid"
    echo -e "  PID: $(cat "$RUNTIME_DIR/.pids/$name.pid")"
}

mkdir -p "$RUNTIME_DIR/.pids"

# Check if running on macOS with Terminal.app available
if [[ "$OSTYPE" == "darwin"* ]] && [ -z "$NO_TERMINAL" ]; then
    echo -e "${BLUE}Opening servers in separate Terminal windows...${NC}"
    echo ""
    
    # Start backend servers first
    start_in_terminal "Spawn" "spawn" "server.jar" "1G"
    sleep 2
    
    start_in_terminal "Islands-1" "islands-1" "server.jar" "1G"
    sleep 2
    
    start_in_terminal "Islands-2" "islands-2" "server.jar" "1G"
    sleep 2
    
    echo -e "${YELLOW}Waiting 15 seconds for servers to start...${NC}"
    sleep 15
    
    # Start proxy last
    start_in_terminal "Proxy" "proxy" "bungeecord.jar" "512M"
    
    echo ""
    echo -e "${GREEN}All servers starting in separate Terminal windows!${NC}"
    echo ""
    echo -e "Wait for all servers to show 'Done' then connect to:"
    echo -e "  ${BLUE}localhost:25565${NC}"
    echo ""
else
    # Fallback: run in background
    echo -e "${BLUE}Starting servers in background...${NC}"
    echo ""
    
    start_background "spawn" "spawn" "server.jar" "1G"
    start_background "islands-1" "islands-1" "server.jar" "1G" 
    start_background "islands-2" "islands-2" "server.jar" "1G"
    
    echo ""
    echo -e "${YELLOW}Waiting 20 seconds for servers to initialize...${NC}"
    sleep 20
    
    start_background "proxy" "proxy" "bungeecord.jar" "512M"
    
    echo ""
    echo -e "${GREEN}Servers started!${NC}"
    echo ""
    echo -e "View logs with:"
    echo -e "  ${YELLOW}tail -f $RUNTIME_DIR/spawn/server.log${NC}"
    echo -e "  ${YELLOW}tail -f $RUNTIME_DIR/proxy/server.log${NC}"
    echo ""
    echo -e "Connect to: ${BLUE}localhost:25565${NC}"
fi

