#!/bin/bash
# Runs the compiled application. Builds first if build/ doesn't exist yet.
set -e
cd "$(dirname "$0")"
if [ ! -d "build" ]; then
    ./build.sh
fi
java -cp build com.bank.Main
