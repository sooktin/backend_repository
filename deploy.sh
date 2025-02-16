#!/bin/bash

# Docker 컨테이너와 볼륨 재시작
docker-compose down -v
docker-compose build
docker-compose up -d
