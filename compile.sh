#!/bin/bash
# ============================================================
# compile.sh – Compiles the UrusSewa Car Rental System
# Usage: ./compile.sh
# ============================================================

# Clean previous build output
rm -rf out
mkdir -p out

echo "Compiling UrusSewa Car Rental System..."

# Find and compile all .java files under src/
find src -name "*.java" | xargs javac -d out

if [ $? -eq 0 ]; then
    echo ""
    echo "✔  Compilation successful!  Run with:  ./run.sh"
else
    echo ""
    echo "✘  Compilation failed. Fix the errors above and retry."
    exit 1
fi
