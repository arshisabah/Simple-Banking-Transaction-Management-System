#!/bin/bash
# Compiles all Java sources into the build/ directory.
set -e
cd "$(dirname "$0")"
mkdir -p build
find src -name "*.java" > sources.txt
javac -d build --release 17 @sources.txt
rm sources.txt
echo "Build successful. Class files are in build/"
