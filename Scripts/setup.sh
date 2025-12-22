#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RUNTIME_DIR="$PROJECT_ROOT/runtime"

# Server versions
PAPER_VERSION="1.20.2"
PAPER_BUILD="318"
BUNGEECORD_BUILD="1772"

log() { echo -e "${BLUE}[SETUP]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }

# ============================================
# Check dependencies
# ============================================
check_dependencies() {
    log "Checking dependencies..."
    
    command -v java >/dev/null 2>&1 || error "Java is required. Install Java 21+."
    command -v mvn >/dev/null 2>&1 || error "Maven is required. Install with: brew install maven"
    command -v docker >/dev/null 2>&1 || error "Docker is required. Install Docker Desktop."
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        error "Java 17+ is required. Found Java $JAVA_VERSION"
    fi
    
    success "All dependencies found (Java $JAVA_VERSION)"
}

# ============================================
# Create runtime directories
# ============================================
create_directories() {
    log "Creating runtime directories..."
    
    mkdir -p "$RUNTIME_DIR"/{proxy,spawn,islands-1,islands-2}
    mkdir -p "$RUNTIME_DIR"/proxy/plugins
    mkdir -p "$RUNTIME_DIR"/spawn/plugins
    mkdir -p "$RUNTIME_DIR"/islands-1/plugins
    mkdir -p "$RUNTIME_DIR"/islands-2/plugins
    
    success "Directories created at $RUNTIME_DIR"
}

# ============================================
# Download server JARs
# ============================================
download_servers() {
    log "Downloading server JARs..."
    
    # Download Paper (Spigot fork, better performance)
    if [ ! -f "$RUNTIME_DIR/paper.jar" ]; then
        log "Downloading Paper $PAPER_VERSION..."
        curl -sL -o "$RUNTIME_DIR/paper.jar" \
            "https://api.papermc.io/v2/projects/paper/versions/$PAPER_VERSION/builds/$PAPER_BUILD/downloads/paper-$PAPER_VERSION-$PAPER_BUILD.jar"
        success "Downloaded Paper"
    else
        success "Paper already downloaded"
    fi
    
    # Download BungeeCord
    if [ ! -f "$RUNTIME_DIR/bungeecord.jar" ]; then
        log "Downloading BungeeCord..."
        curl -sL -o "$RUNTIME_DIR/bungeecord.jar" \
            "https://ci.md-5.net/job/BungeeCord/$BUNGEECORD_BUILD/artifact/bootstrap/target/BungeeCord.jar"
        success "Downloaded BungeeCord"
    else
        success "BungeeCord already downloaded"
    fi
    
    # Copy to server directories
    cp "$RUNTIME_DIR/paper.jar" "$RUNTIME_DIR/spawn/server.jar"
    cp "$RUNTIME_DIR/paper.jar" "$RUNTIME_DIR/islands-1/server.jar"
    cp "$RUNTIME_DIR/paper.jar" "$RUNTIME_DIR/islands-2/server.jar"
    cp "$RUNTIME_DIR/bungeecord.jar" "$RUNTIME_DIR/proxy/bungeecord.jar"
}

# ============================================
# Accept EULA for Minecraft servers
# ============================================
accept_eula() {
    log "Accepting Minecraft EULA..."
    
    for dir in spawn islands-1 islands-2; do
        echo "eula=true" > "$RUNTIME_DIR/$dir/eula.txt"
    done
    
    success "EULA accepted for all servers"
}

# ============================================
# Create server configs
# ============================================
create_configs() {
    log "Creating server configurations..."
    
    # Spawn server.properties
    cat > "$RUNTIME_DIR/spawn/server.properties" << 'EOF'
server-port=25566
online-mode=false
spawn-protection=0
level-name=world
view-distance=10
max-players=100
motd=Skyblock Spawn Server
EOF

    # Islands-1 server.properties
    cat > "$RUNTIME_DIR/islands-1/server.properties" << 'EOF'
server-port=25567
online-mode=false
spawn-protection=0
level-name=world
view-distance=8
max-players=50
motd=Skyblock Islands Server 1
EOF

    # Islands-2 server.properties
    cat > "$RUNTIME_DIR/islands-2/server.properties" << 'EOF'
server-port=25568
online-mode=false
spawn-protection=0
level-name=world
view-distance=8
max-players=50
motd=Skyblock Islands Server 2
EOF

    # BungeeCord config
    cat > "$RUNTIME_DIR/proxy/config.yml" << 'EOF'
server_connect_timeout: 5000
remote_ping_cache: -1
forge_support: false
player_limit: -1
permissions:
  default:
  - bungeecord.command.server
  - bungeecord.command.list
  admin:
  - bungeecord.command.alert
  - bungeecord.command.end
  - bungeecord.command.ip
  - bungeecord.command.reload
timeout: 30000
log_commands: false
network_compression_threshold: 256
online_mode: false
disabled_commands:
- disabledcommandhere
servers:
  Spawn-01:
    motd: 'Spawn Server'
    address: localhost:25566
    restricted: false
  Islands-01:
    motd: 'Islands Server 1'
    address: localhost:25567
    restricted: false
  Islands-02:
    motd: 'Islands Server 2'
    address: localhost:25568
    restricted: false
listeners:
- query_port: 25577
  motd: '&bSkyblock Network'
  tab_list: GLOBAL_PING
  query_enabled: false
  proxy_protocol: false
  forced_hosts: {}
  ping_passthrough: false
  priorities:
  - Spawn-01
  bind_local_address: true
  host: 0.0.0.0:25565
  max_players: 200
  tab_size: 60
  force_default_server: true
ip_forward: true
remote_ping_timeout: 5000
prevent_proxy_connections: false
groups: {}
connection_throttle: 4000
stats: false
connection_throttle_limit: 3
log_pings: false
EOF

    # Paper global config for IP forwarding
    for dir in spawn islands-1 islands-2; do
        mkdir -p "$RUNTIME_DIR/$dir/config"
        cat > "$RUNTIME_DIR/$dir/config/paper-global.yml" << 'EOF'
proxies:
  velocity:
    enabled: false
  bungee-cord:
    online-mode: false
EOF
        # Also create spigot.yml with bungeecord enabled
        cat > "$RUNTIME_DIR/$dir/spigot.yml" << 'EOF'
settings:
  bungeecord: true
EOF
    done

    success "Configurations created"
}

# ============================================
# Build the project
# ============================================
build_project() {
    log "Building project with Maven..."
    
    cd "$PROJECT_ROOT"
    mvn clean package -DskipTests -q
    
    success "Project built successfully"
}

# ============================================
# Copy plugins to servers
# ============================================
copy_plugins() {
    log "Copying plugins to servers..."
    
    # Copy to proxy
    cp "$PROJECT_ROOT/out/Proxy-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/proxy/plugins/Skyblock-Proxy.jar"
    
    # Copy to spawn
    cp "$PROJECT_ROOT/out/Spawn-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/spawn/plugins/Skyblock-Spawn.jar"
    
    # Copy to island servers
    cp "$PROJECT_ROOT/out/Islands-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/islands-1/plugins/Skyblock-Islands.jar"
    cp "$PROJECT_ROOT/out/Islands-1.0-SNAPSHOT.jar" "$RUNTIME_DIR/islands-2/plugins/Skyblock-Islands.jar"
    
    success "Plugins copied"
}

# ============================================
# Start infrastructure (Redis + PostgreSQL)
# ============================================
start_infrastructure() {
    log "Starting infrastructure (Redis + PostgreSQL)..."
    
    cd "$PROJECT_ROOT"
    docker compose up -d
    
    # Wait for services to be healthy
    log "Waiting for services to be ready..."
    sleep 3
    
    # Check Redis
    if docker exec skyopoly-redis redis-cli ping | grep -q "PONG"; then
        success "Redis is ready"
    else
        error "Redis failed to start"
    fi
    
    # Check PostgreSQL
    if docker exec skyopoly-postgres pg_isready -U skyblock | grep -q "accepting"; then
        success "PostgreSQL is ready"
    else
        error "PostgreSQL failed to start"
    fi
}

# ============================================
# Main setup
# ============================================
main() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       Skyopoly Development Setup       ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    
    check_dependencies
    create_directories
    download_servers
    accept_eula
    create_configs
    build_project
    copy_plugins
    start_infrastructure
    
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║            Setup Complete!             ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "Run ${YELLOW}./Scripts/dev.sh${NC} to start all servers"
    echo -e "Run ${YELLOW}./Scripts/dev.sh --rebuild${NC} to rebuild and restart"
    echo ""
    echo -e "Servers:"
    echo -e "  • Proxy:     ${BLUE}localhost:25565${NC}"
    echo -e "  • Spawn:     ${BLUE}localhost:25566${NC}"
    echo -e "  • Islands-1: ${BLUE}localhost:25567${NC}"
    echo -e "  • Islands-2: ${BLUE}localhost:25568${NC}"
    echo ""
    echo -e "Infrastructure:"
    echo -e "  • Redis:     ${BLUE}localhost:6379${NC}"
    echo -e "  • PostgreSQL:${BLUE}localhost:5432${NC} (skyblock/skyblock)"
    echo ""
}

main "$@"

