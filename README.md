# cSkyblock

A distributed Minecraft Skyblock server network built with BungeeCord, Paper, and Redis.

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Player    │────▶│   Proxy     │────▶│   Spawn     │
│  (1.21.8)   │     │ (BungeeCord)│     │  (Paper)    │
└─────────────┘     └──────┬──────┘     └─────────────┘
                          │
                          ├────────────▶ Islands-1 (Paper)
                          └────────────▶ Islands-2 (Paper)

All servers communicate via Redis pub/sub
```

## Modules

| Module | Description |
|--------|-------------|
| **Proxy** | BungeeCord plugin - handles player routing, island management |
| **Spawn** | Paper plugin - spawn server with lobby features |
| **Islands** | Paper plugin - island servers with properties/farms |
| **Library** | Shared code - modules, database, commands |
| **Exchange** | Redis pub/sub messaging layer |
| **Saga** | Distributed transaction flows (create island, teleport, etc.) |

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

# 3. Start all servers
make start

# 4. Connect with Minecraft 1.21.8
#    Server: localhost:25565
```

## Development Commands

```bash
make help      # Show all commands
make start     # Start all servers
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
├── Exchange/          # Redis messaging library
├── Saga/              # Distributed flow definitions
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

## Configuration

### Database
Edit `Library/.../Database.java` for PostgreSQL credentials.
Default: `skyblock` / `skyblock` / `skyblock`

### Servers
- Proxy: `runtime/proxy/config.yml`
- Backend: `runtime/*/server.properties`

## Flows

The project uses a saga pattern for distributed operations:

- **CreateIslandFlow** - Creates new island for player
- **TeleportToIslandFlow** - Teleports player to their island
- **TeleportToSpawnFlow** - Teleports player back to spawn
- **LoadIslandFlow** - Loads island world on server

## License

MIT

