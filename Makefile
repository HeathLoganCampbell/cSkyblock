# Skyopoly Development Makefile

.PHONY: all

RUNTIME = runtime
MVN = mvn -DskipTests -q -T 1C -o
MVN_ONLINE = mvn -DskipTests -q -T 1C

# ═══════════════════════════════════════════════════════════════════════════════
# QUICK REFERENCE
# ═══════════════════════════════════════════════════════════════════════════════

help:
	@echo ""
	@echo "⚡ Fast Commands"
	@echo "  make r       - Rebuild ALL + restart (~8s)"
	@echo "  make f       - Rebuild ALL, no restart (~3s)"
	@echo "  make lib     - Rebuild Library only (~1.5s)"
	@echo "  make proxy   - Rebuild Proxy only (~1s)"
	@echo "  make ctrl    - Rebuild Controller only (~1s)"
	@echo ""
	@echo "🖥️  Server"
	@echo "  make start   - Start all servers"
	@echo "  make stop    - Stop all servers"  
	@echo "  make status  - Show status"
	@echo "  make logs    - Tail logs"
	@echo ""
	@echo "🔧 Setup"
	@echo "  make setup   - First time setup"
	@echo "  make clean   - Remove runtime"
	@echo ""

# ═══════════════════════════════════════════════════════════════════════════════
# FAST BUILDS (Module-specific)
# ═══════════════════════════════════════════════════════════════════════════════

# Library only (most common - shared code)
lib:
	@printf "Building Library..."
	@$(MVN) package -pl Library,Saga,Exchange -am 2>/dev/null || $(MVN_ONLINE) package -pl Library,Saga,Exchange -am
	@cp out/Spawn-1.0-SNAPSHOT.jar $(RUNTIME)/spawn/plugins/Skyblock-Spawn.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-1/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-2/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@echo " ✓"

# Proxy only
proxy:
	@printf "Building Proxy..."
	@$(MVN) package -pl Proxy -am 2>/dev/null || $(MVN_ONLINE) package -pl Proxy -am
	@cp out/Proxy-1.0-SNAPSHOT.jar $(RUNTIME)/proxy/plugins/Skyblock-Proxy.jar 2>/dev/null || true
	@echo " ✓"

# Controller only
ctrl:
	@printf "Building Controller..."
	@$(MVN) package -pl Controller 2>/dev/null || $(MVN_ONLINE) package -pl Controller
	@cp Controller/target/Controller-1.0-SNAPSHOT.jar $(RUNTIME)/controller/controller.jar 2>/dev/null || true
	@echo " ✓"

# Spawn plugin only
spawn:
	@printf "Building Spawn..."
	@$(MVN) package -pl Spawn -am 2>/dev/null || $(MVN_ONLINE) package -pl Spawn -am
	@cp out/Spawn-1.0-SNAPSHOT.jar $(RUNTIME)/spawn/plugins/Skyblock-Spawn.jar 2>/dev/null || true
	@echo " ✓"

# Islands plugin only
islands:
	@printf "Building Islands..."
	@$(MVN) package -pl Islands -am 2>/dev/null || $(MVN_ONLINE) package -pl Islands -am
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-1/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-2/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@echo " ✓"

# ═══════════════════════════════════════════════════════════════════════════════
# FULL BUILDS
# ═══════════════════════════════════════════════════════════════════════════════

# Full incremental build (no clean)
fast:
	@printf "Building all (incremental)..."
	@$(MVN) package 2>/dev/null || $(MVN_ONLINE) package
	@echo " ✓"
	@$(MAKE) -s copy

# Full clean build (slower)
build:
	@echo "Building (clean)..."
	@$(MVN_ONLINE) clean package
	@echo "✓ Build complete"

copy:
	@mkdir -p $(RUNTIME)/spawn/plugins $(RUNTIME)/islands-1/plugins $(RUNTIME)/islands-2/plugins $(RUNTIME)/proxy/plugins $(RUNTIME)/controller
	@cp out/Spawn-1.0-SNAPSHOT.jar $(RUNTIME)/spawn/plugins/Skyblock-Spawn.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-1/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@cp out/Islands-1.0-SNAPSHOT.jar $(RUNTIME)/islands-2/plugins/Skyblock-Islands.jar 2>/dev/null || true
	@cp out/Proxy-1.0-SNAPSHOT.jar $(RUNTIME)/proxy/plugins/Skyblock-Proxy.jar 2>/dev/null || true
	@cp Controller/target/Controller-1.0-SNAPSHOT.jar $(RUNTIME)/controller/controller.jar 2>/dev/null || true
	@mkdir -p $(RUNTIME)/plugins-shared $(RUNTIME)/plugins-proxy
	@cp $(RUNTIME)/plugins-shared/*.jar $(RUNTIME)/spawn/plugins/ 2>/dev/null || true
	@cp $(RUNTIME)/plugins-shared/*.jar $(RUNTIME)/islands-1/plugins/ 2>/dev/null || true
	@cp $(RUNTIME)/plugins-shared/*.jar $(RUNTIME)/islands-2/plugins/ 2>/dev/null || true
	@cp $(RUNTIME)/plugins-proxy/*.jar $(RUNTIME)/proxy/plugins/ 2>/dev/null || true

rebuild: build copy

# ═══════════════════════════════════════════════════════════════════════════════
# SERVER CONTROL
# ═══════════════════════════════════════════════════════════════════════════════

start:
	@bash Scripts/server.sh start

stop:
	@bash Scripts/server.sh stop

# Fast restart (incremental build)
r: stop fast start

# Build only, no restart
f: fast

status:
	@bash Scripts/server.sh status

logs:
	@bash Scripts/server.sh logs

controller:
	@cd $(RUNTIME)/controller && java -jar controller.jar 8080

# ═══════════════════════════════════════════════════════════════════════════════
# SETUP & CONFIG
# ═══════════════════════════════════════════════════════════════════════════════

infra:
	@echo "Starting Docker..."
	@docker compose up -d
	@sleep 2
	@echo "✓ Redis + PostgreSQL ready"

config:
	@mkdir -p $(RUNTIME)/proxy $(RUNTIME)/spawn/plugins/SkyblockSpawn $(RUNTIME)/islands-1/plugins/SkyblockIslands $(RUNTIME)/islands-2/plugins/SkyblockIslands
	@bash Scripts/write-bungee-config.sh
	@echo "server:\n  id: Spawn-01" > $(RUNTIME)/spawn/plugins/SkyblockSpawn/config.yml
	@echo "server:\n  id: Islands-01" > $(RUNTIME)/islands-1/plugins/SkyblockIslands/config.yml
	@echo "server:\n  id: Islands-02" > $(RUNTIME)/islands-2/plugins/SkyblockIslands/config.yml
	@echo "✓ Configs created"

setup: infra build copy config
	@echo ""
	@echo "✓ Setup complete! Run 'make start'"
	@echo ""

clean:
	@-bash Scripts/server.sh stop 2>/dev/null || true
	@rm -rf $(RUNTIME)/spawn $(RUNTIME)/islands-1 $(RUNTIME)/islands-2 $(RUNTIME)/proxy $(RUNTIME)/paper.jar $(RUNTIME)/controller
	@echo "✓ Cleaned"

reset: clean setup
