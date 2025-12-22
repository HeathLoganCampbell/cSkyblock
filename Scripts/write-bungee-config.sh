#!/bin/bash
cat > runtime/proxy/config.yml << 'EOF'
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
    motd: Spawn Server
    address: localhost:25566
    restricted: false
  Islands-01:
    motd: Islands Server 1
    address: localhost:25567
    restricted: false
  Islands-02:
    motd: Islands Server 2
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
echo "✓ BungeeCord config written"

