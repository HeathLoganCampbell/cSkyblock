# cSkyblock

A distributed Minecraft Skyblock server network built with BungeeCord, Paper, and a centralized Controller service.

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Player    │────▶│   Proxy     │────▶│   Spawn     │
│  (1.21.8)   │     │ (BungeeCord)│     │  (Paper)    │
└─────────────┘     └──────┬──────┘     └─────────────┘
                          │
                          ├────────────▶ Islands-1 (Paper)
                          └────────────▶ Islands-2 (Paper)

                    ┌─────────────┐
                    │ Controller  │ ◀── All servers register via HTTP
                    │  (Javalin)  │     Island creation & routing
                    └─────────────┘
```

## Modules

| Module | Description |
|--------|-------------|
| **Controller** | Standalone HTTP service - island management, server routing |
| **Proxy** | BungeeCord plugin - handles player routing |
| **Spawn** | Paper plugin - spawn server with lobby features |
| **Islands** | Paper plugin - island servers with properties/farms |
| **Library** | Shared code - modules, database, commands |
| **Exchange** | Redis pub/sub (used for spawn teleport, inventory sync) |
| **Saga** | Simplified - only spawn teleport flow remains |

## Requirements

- Java 21
- Maven
- Docker (for Redis + PostgreSQL)
- Minecraft 1.21.8 client

## Quick Start

```bash
# 1. Start infrastructure (Redis + PostgreSQL)
make infra

# 2. Build and setup everything
make setup

# 3. Start all servers (includes Controller)
make start

# 4. Connect with Minecraft 1.21.8
#    Server: localhost:25565
```

## Development Commands

```bash
make help      # Show all commands
make start     # Start all servers + Controller
make stop      # Stop all servers
make restart   # Rebuild + restart
make rebuild   # Build plugins only
make status    # Show server status
make logs      # Tail all logs
make clean     # Remove runtime (keeps shared plugins)
make reset     # Full clean + setup
```

## Adding Plugins

Drop plugin JARs into these folders:

| Folder | Copied To |
|--------|-----------|
| `runtime/plugins-shared/` | All backend servers (spawn, islands) |
| `runtime/plugins-proxy/` | Proxy only |

Then run `make rebuild` to copy them.

## Project Structure

```
├── Controller/        # HTTP API for island management
├── Exchange/          # Redis messaging library
├── Saga/              # Spawn teleport flow (simplified)
├── Library/           # Shared Bukkit plugin code
├── Spawn/             # Spawn server plugin
├── Islands/           # Islands server plugin
├── Proxy/             # BungeeCord proxy plugin
├── Scripts/           # Setup and management scripts
├── runtime/           # Server files (gitignored)
│   ├── proxy/
│   ├── spawn/
│   ├── islands-1/
│   ├── islands-2/
│   ├── plugins-shared/
│   └── plugins-proxy/
├── docker-compose.yml # Redis + PostgreSQL
└── Makefile          # Development commands
```

## Controller API

The Controller runs on port 8080 and handles:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/servers` | POST | Register a server |
| `/api/servers` | GET | List all servers |
| `/api/servers/{id}/heartbeat` | POST | Server heartbeat |
| `/api/islands/{playerId}` | POST | Create/get island for player |
| `/api/islands/{id}/ready` | POST | Mark island as ready |
| `/api/islands/{id}/unload` | POST | Mark island as unloaded |
| `/api/route/{playerId}` | GET | Get server to route player to |

## Configuration

### Database
Edit `Library/.../Database.java` for PostgreSQL credentials.
Default: `skyblock` / `skyblock` / `skyblock`

### Servers
- Proxy: `runtime/proxy/config.yml`
- Backend: `runtime/*/server.properties`
- Controller: Runs on port 8080

## License

MIT
