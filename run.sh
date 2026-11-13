#!/usr/bin/env bash
# SPDX-License-Identifier: MIT
# Co-creator: momofile
# Cross-platform entry point for macOS / Linux (and Git Bash on Windows).
#   ./run.sh build    -> compile into ./out
#   ./run.sh start    -> start the RMI service (foreground)
#   ./run.sh verify   -> compile if needed, start, probe, stop
set -e
cd "$(dirname "$0")"

JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/javac}"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/java}"
[ -x "$JAVAC" ] || JAVAC="$(command -v javac || true)"
[ -x "$JAVA" ]  || JAVA="$(command -v java || true)"
if [ -z "$JAVAC" ] || [ -z "$JAVA" ]; then
  echo "[ERROR] JDK not found. Install JDK 8+ or set JAVA_HOME." >&2
  exit 1
fi

build() {
  mkdir -p out
  "$JAVAC" -encoding UTF-8 -d out \
    src/reflect/nya/al/protocol/LoggerProtocol.java \
    src/StubDll.java src/LoggerServer.java src/LoggerProbe.java
  echo "BUILD OK - classes in ./out"
}

start_server() {
  echo "Starting RMI diagnostic service on 127.0.0.1:1099 (Ctrl+C to stop)"
  exec "$JAVA" -Djava.rmi.server.hostname=127.0.0.1 \
    -Djava.rmi.server.useCodebaseOnly=true -cp "$PWD/out" LoggerServer
}

case "${1:-verify}" in
  build)  build ;;
  start)  [ -f out/LoggerServer.class ] || build; start_server ;;
  verify)
    build
    "$JAVA" -Djava.rmi.server.hostname=127.0.0.1 \
      -Djava.rmi.server.useCodebaseOnly=true -cp "$PWD/out" LoggerServer &
    PID=$!
    trap 'kill "$PID" 2>/dev/null || true' EXIT
    for _ in 1 2 3 4 5 6 7 8 9 10; do
      "$JAVA" -cp "$PWD/out" LoggerProbe >/dev/null 2>&1 && break
      sleep 0.4
    done
    "$JAVA" -cp "$PWD/out" LoggerProbe
    ;;
  *) echo "usage: ./run.sh build|start|verify" >&2; exit 2 ;;
esac
