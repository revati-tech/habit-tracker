#!/bin/bash

# Simple script to run the app locally with .env file

# Load .env file
if [ -f .env ]; then
    set -a
    source .env
    set +a
    echo "✅ Loaded .env file"
else
    echo "❌ .env file not found! Create it from env.example"
    exit 1
fi

# Verify profile is set
if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
    echo "⚠️  SPRING_PROFILES_ACTIVE not set, defaulting to 'dev'"
    export SPRING_PROFILES_ACTIVE=dev
fi

echo "🚀 Starting application with profile: $SPRING_PROFILES_ACTIVE"
echo ""

# Run with explicit environment variables
./mvnw spring-boot:run

