#!/bin/bash
# ============================================================
# run.sh – Runs the UrusSewa Car Rental System
# Usage: ./run.sh
# ============================================================

if [ ! -d "out" ]; then
    echo "No compiled output found. Running compile.sh first..."
    ./compile.sh
fi

echo "Starting UrusSewa Car Rental System..."
java -cp out carrental.CarRentalApp
