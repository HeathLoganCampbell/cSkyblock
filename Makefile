# Skyopoly Development Makefile

.PHONY: infra build copy config rebuild start stop restart status logs setup clean reset help

RUNTIME = runtime

help:
	@echo ""
	@echo "Skyopoly Development"
	@echo "━━━━━━━━━━━━━━━━━━━━"
	@echo ""
	@echo "  make setup   - First time setup"
	@echo "  make start   - Start all servers"
	@echo "  make stop    - Stop all servers"
	@echo "  make restart - Rebuild + restart"
	@echo "  make status  - Show server status"
	@echo "  make logs    - Tail all logs"
	@echo "  make rebuild - Build plugins only"
	@echo "  make clean   - Remove runtime"
	@echo ""
	@echo "Plugin Folders:"
	@echo "  runtime/plugins-shared/  → Copied to ALL backend servers"
	@echo "  runtime/plugins-proxy/   → Copied to proxy only"
	@echo ""

infra:
	@echo "Starting Docker..."
	@docker compose up -d
	@sleep 2
	@echo "✓ Redis + PostgreSQL ready"

build:
	@echo "Building..."
	@mvn clean package -DskipTests -q
	@echo "✓ Build complete"

copy:
	@mkdir -p $(RUNTIME)/spawn/plugins $(RUNTIME)/islands-1/plugins $(RUNTIME)/islands-2/plugins $(RUNTIME)/proxy/plugins
	@mkdir -p $(RUNTIME)/plugins-shared $(RUNTIME)/plugins-proxy
	@# Copy project plugins
	@cp out/Spawn-1.0-SNAPSHOT.jar $(RUNTIME)/spawn/plugins/Skyblock-Spawn.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-1/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-2/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@cp out/Proxy-1.0-SNAPSHOT.jar $(RUNTIME)/proxy/plugins/Skyblock-Proxy.jar 2>/dev/null || true
	@# Copy shared plugins to all backend servers
	@cp $(RUNTIME)/plugins-shared/*.jar $(RUNTIME)/spawn/plugins/ 2>/dev/null || true
	@cp $(RUNTIME)/plugins-shared/*.jar $(RUNTIME)/islands-1/plugins/ 2>/dev/null || true
	@cp $(RUNTIME)/plugins-shared/*.jar $(RUNTIME)/islands-2/plugins/ 2>/dev/null || true
	@# Copy proxy-specific plugins
	@cp $(RUNTIME)/plugins-proxy/*.jar $(RUNTIME)/proxy/plugins/ 2>/dev/null || true
	@echo "✓ Plugins copied"

config:
	@mkdir -p $(RUNTIME)/proxy
	@bash Scripts/write-bungee-config.sh

rebuild: build copy
	@echo "✓ Rebuild complete"

start:
	@bash Scripts/server.sh start

stop:
	@bash Scripts/server.sh stop

restart: stop rebuild start

status:
	@bash Scripts/server.sh status

logs:
	@bash Scripts/server.sh logs

setup: infra build copy config
	@echo ""
	@echo "✓ Setup complete!"
	@echo "  Run 'make start' to start servers"
	@echo ""

clean:
	@-bash Scripts/server.sh stop 2>/dev/null || true
	@# Preserve shared plugin folders, remove server dirs
	@rm -rf $(RUNTIME)/spawn $(RUNTIME)/islands-1 $(RUNTIME)/islands-2 $(RUNTIME)/proxy $(RUNTIME)/paper.jar
	@echo "✓ Cleaned (plugins-shared/ and plugins-proxy/ preserved)"

reset: clean setup
