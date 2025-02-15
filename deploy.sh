#!/bin/bash

# Docker 컨테이너 재시작
docker compose down
docker compose build
docker compose up -d
