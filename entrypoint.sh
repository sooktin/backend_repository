#!/bin/bash

# Parameter Store에서 값을 가져와서 .env 파일 생성
aws ssm get-parameters-by-path \
    --path "/sooktin/prod" \
    --with-decryption \
    --recursive \
    --region ap-northeast-2 \
    --query "Parameters[*].[Name,Value]" \
    --output text | while read -r name value; do
    parameter_name=${name##*/}
    echo "$parameter_name=$value" >> .env
done

# Spring Boot 애플리케이션 실행
exec java -jar app.jar
