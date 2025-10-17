#!/bin/bash

docker compose --env-file .env.local -f docker-compose.dev.yml up -d --build
