#!/bin/bash
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RUNTIME_DIR="$PROJECT_ROOT/runtime"

# PID files
PIDS_DIR="$RUNTIME_DIR/.pids"
mkdir -p "$PIDS_DIR"

log() { echo -e "${BLUE}[DEV]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }

# ============================================
# Rebuild project
# ============================================
rebuild() {
    log "Rebuilding project..."
    cd "$PROJECT_ROOT"
    mvn package -DskipTests -q
    
    # Copy fresh plugins
    cp "$PROJECT_ROOT/out/Proxy-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/proxy/plugins/Skyblock-Proxy.jar"
    cp "$PROJECT_ROOT/out/Spawn-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/spawn/plugins/Skyblock-Spawn.jar"
    cp "$PROJECT_ROOT/out/Islands-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/islands-1/plugins/Skyblock-Islands.jar"
    cp "$PROJECT_ROOT/out/Islands-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/islands-2/plugins/Skyblock-Islands.jar"
    
    success "Rebuilt and copied plugins"
}

# ============================================
# Start a single server
# ============================================
start_server() {
    local name=$1
    local dir=$2
    local jar=$3
    local color=$4
    
    if [ -f "$PIDS_DIR/$name.pid" ]; then
        local pid=$(cat "$PIDS_DIR/$name.pid")
        if ps -p $pid > /dev/null 2>&1; then
            warn "$name already running (PID: $pid)"
            return
        fi
    fi
    
    log "Starting $name..."
    cd "$RUNTIME_DIR/$dir"
    
    # Start server in background, redirect output to log file
    java -Xms512M -Xmx1G -jar "$jar" nogui > "$RUNTIME_DIR/$dir/server.log" 2>&1 &
    local pid=$!
    echo $pid > "$PIDS_DIR/$name.pid"
    
    success "$name started (PID: $pid)"
}

# ============================================
# Stop a single server
# ============================================
stop_server() {
    local name=$1
    
    if [ -f "$PIDS_DIR/$name.pid" ]; then
        local pid=$(cat "$PIDS_DIR/$name.pid")
        if ps -p $pid > /dev/null 2>&1; then
            log "Stopping $name (PID: $pid)..."
            kill $pid 2>/dev/null || true
            
            # Wait for graceful shutdown
            for i in {1..10}; do
                if ! ps -p $pid > /dev/null 2>&1; then
                    break
                fi
                sleep 1
            done
            
            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                kill -9 $pid 2>/dev/null || true
            fi
            
            success "$name stopped"
        fi
        rm -f "$PIDS_DIR/$name.pid"
    fi
}

# ============================================
# Start all servers
# ============================================
start_all() {
    log "Starting all servers..."
    
    # Check infrastructure
    if ! docker ps | grep -q skyopoly-redis; then
        log "Starting infrastructure..."
        cd "$PROJECT_ROOT"
        docker compose up -d
        sleep 2
    fi
    
    # Start Minecraft servers first (they take time to boot)
    start_server "spawn" "spawn" "server.jar"
    start_server "islands-1" "islands-1" "server.jar"
    start_server "islands-2" "islands-2" "server.jar"
    
    # Wait for servers to start accepting connections
    log "Waiting for servers to start..."
    sleep 10
    
    # Start proxy last
    start_server "proxy" "proxy" "bungeecord.jar"
    
    echo ""
    success "All servers started!"
    echo ""
    echo -e "Connect to: ${CYAN}localhost:25565${NC}"
    echo ""
    echo -e "View logs:"
    echo -e "  ${YELLOW}tail -f $RUNTIME_DIR/proxy/server.log${NC}"
    echo -e "  ${YELLOW}tail -f $RUNTIME_DIR/spawn/server.log${NC}"
    echo -e "  ${YELLOW}tail -f $RUNTIME_DIR/islands-1/server.log${NC}"
    echo ""
}

# ============================================
# Stop all servers
# ============================================
stop_all() {
    log "Stopping all servers..."
    
    stop_server "proxy"
    stop_server "spawn"
    stop_server "islands-1"
    stop_server "islands-2"
    
    success "All servers stopped"
}

# ============================================
# Show status
# ============================================
status() {
    echo ""
    echo -e "${BLUE}Server Status${NC}"
    echo "─────────────────────────────────────"
    
    for name in proxy spawn islands-1 islands-2; do
        if [ -f "$PIDS_DIR/$name.pid" ]; then
            local pid=$(cat "$PIDS_DIR/$name.pid")
            if ps -p $pid > /dev/null 2>&1; then
                echo -e "  ${GREEN}●${NC} $name (PID: $pid)"
            else
                echo -e "  ${RED}●${NC} $name (dead)"
            fi
        else
            echo -e "  ${YELLOW}○${NC} $name (not started)"
        fi
    done
    
    echo ""
    echo -e "${BLUE}Infrastructure${NC}"
    echo "─────────────────────────────────────"
    
    if docker ps | grep -q skyopoly-redis; then
        echo -e "  ${GREEN}●${NC} Redis"
    else
        echo -e "  ${RED}●${NC} Redis"
    fi
    
    if docker ps | grep -q skyopoly-postgres; then
        echo -e "  ${GREEN}●${NC} PostgreSQL"
    else
        echo -e "  ${RED}●${NC} PostgreSQL"
    fi
    
    echo ""
}

# ============================================
# Tail all logs
# ============================================
logs() {
    local server=${1:-all}
    
    if [ "$server" = "all" ]; then
        tail -f "$RUNTIME_DIR"/*/server.log
    else
        tail -f "$RUNTIME_DIR/$server/server.log"
    fi
}

# ============================================
# Send command to server console
# ============================================
console() {
    local server=$1
    local cmd=$2
    
    if [ -z "$server" ] || [ -z "$cmd" ]; then
        echo "Usage: ./dev.sh console <server> <command>"
        echo "Example: ./dev.sh console spawn say Hello!"
        exit 1
    fi
    
    # For now, just show how to do it
    echo "Send command to $server: $cmd"
    echo "Note: Use screen or tmux for interactive consoles"
}

# ============================================
# Quick restart with rebuild
# ============================================
restart() {
    stop_all
    rebuild
    start_all
}

# ============================================
# Usage
# ============================================
usage() {
    echo ""
    echo -e "${BLUE}Skyopoly Development Server${NC}"
    echo ""
    echo "Usage: ./dev.sh <command>"
    echo ""
    echo "Commands:"
    echo "  start       Start all servers"
    echo "  stop        Stop all servers"
    echo "  restart     Stop, rebuild, and start all servers"
    echo "  rebuild     Rebuild plugins only (no restart)"
    echo "  status      Show server status"
    echo "  logs [srv]  Tail logs (all or specific server)"
    echo "  infra       Start only Redis + PostgreSQL"
    echo ""
    echo "Examples:"
    echo "  ./dev.sh start"
    echo "  ./dev.sh logs spawn"
    echo "  ./dev.sh restart"
    echo ""
}

# ============================================
# Main
# ============================================
case "${1:-start}" in
    start)
        start_all
        ;;
    stop)
        stop_all
        ;;
    restart|--rebuild)
        restart
        ;;
    rebuild)
        rebuild
        ;;
    status)
        status
        ;;
    logs)
        logs "$2"
        ;;
    infra)
        cd "$PROJECT_ROOT"
        docker compose up -d
        success "Infrastructure started"
        ;;
    help|--help|-h)
        usage
        ;;
    *)
        usage
        exit 1
        ;;
esac

