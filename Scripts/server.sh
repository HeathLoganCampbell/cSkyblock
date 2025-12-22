#!/bin/bash
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME="$ROOT/runtime"
PIDS="$RUNTIME/.pids"

mkdir -p "$PIDS"

JAVA_HOME="/Users/sprockpls/Library/Java/JavaVirtualMachines/corretto-21.0.4/Contents/Home"
JAVA="$JAVA_HOME/bin/java"

start_server() {
    local name=$1
    local dir=$2
    local jar=$3
    local mem=$4
    
    if [ -f "$PIDS/$name.pid" ]; then
        pid=$(cat "$PIDS/$name.pid")
        if ps -p $pid > /dev/null 2>&1; then
            echo "  $name already running (PID: $pid)"
            return
        fi
    fi
    
    cd "$RUNTIME/$dir"
    $JAVA -Xms${mem} -Xmx${mem} -jar $jar nogui > server.log 2>&1 &
    echo $! > "$PIDS/$name.pid"
    echo "  ✓ $name started (PID: $!)"
}

stop_server() {
    local name=$1
    if [ -f "$PIDS/$name.pid" ]; then
        pid=$(cat "$PIDS/$name.pid")
        if ps -p $pid > /dev/null 2>&1; then
            kill $pid 2>/dev/null || true
            echo "  ✓ $name stopped"
        fi
        rm -f "$PIDS/$name.pid"
    fi
}

case "$1" in
    start)
        echo "Starting servers..."
        start_server "spawn" "spawn" "server.jar" "1G"
        start_server "islands" "islands-1" "server.jar" "1G"
        echo "  ⏳ Waiting 20s for Minecraft servers..."
        sleep 20
        start_server "proxy" "proxy" "bungeecord.jar" "512M"
        echo ""
        echo "✓ All servers running!"
        echo "  Connect: localhost:25565"
        echo ""
        ;;
    stop)
        echo "Stopping servers..."
        stop_server "proxy"
        stop_server "spawn"
        stop_server "islands"
        echo "✓ Stopped"
        ;;
    restart)
        $0 stop
        sleep 2
        $0 start
        ;;
    status)
        echo ""
        echo "Server Status"
        echo "─────────────"
        for name in spawn islands proxy; do
            if [ -f "$PIDS/$name.pid" ]; then
                pid=$(cat "$PIDS/$name.pid")
                if ps -p $pid > /dev/null 2>&1; then
                    echo "  ● $name (PID: $pid)"
                else
                    echo "  ○ $name (dead)"
                fi
            else
                echo "  ○ $name (stopped)"
            fi
        done
        echo ""
        ;;
    logs)
        tail -f "$RUNTIME"/*/server.log
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs}"
        exit 1
        ;;
esac

