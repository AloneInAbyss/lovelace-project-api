#!/bin/bash

cleanup_function() {
  echo "Ctrl+C detected. Performing cleanup..."

  sudo systemctl stop mongod
  sudo systemctl stop redis-server

  echo "Cleanup complete. Exiting."
  exit 1
}

sudo systemctl start mongod
sudo systemctl start redis-server

trap cleanup_function SIGINT

(set -a; source .env.prod; set +a; ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod)
