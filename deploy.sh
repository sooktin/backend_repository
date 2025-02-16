#!/bin/bash

# 파일 상태 확인
echo "현재 경로: $(pwd)"
echo "확인 중... nginx-default.conf:"
ls -l nginx-default.conf
echo "nginx-default.conf 내용:"
cat nginx-default.conf

# 기존 컨테이너와 볼륨 정리
docker compose down -v
docker volume prune -f

# 다시 시작
docker compose build --no-cache
docker compose up -d
