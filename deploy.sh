#!/bin/bash

# Pull latest changes
git pull

# Docker 컨테이너 재시작
docker-compose down
docker-compose build
docker-compose up -d