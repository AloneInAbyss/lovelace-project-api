#!/bin/bash

cleanup_function() {
  echo "Ctrl+C detected. Performing cleanup..."

  docker compose -f docker-compose.dev.yml down -v

  echo "Cleanup complete. Exiting."
  exit 1
}

trap cleanup_function SIGINT

docker compose --env-file .env.local -f docker-compose.dev.yml up -d
