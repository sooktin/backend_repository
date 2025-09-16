FROM openjdk:17-jdk-slim

# AWS CLI 설치
RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws

WORKDIR /app
COPY build/libs/*.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# JVM 옵션 환경변수 설정
ENV JAVA_OPTS="-XX:-UseContainerSupport -Dfile.encoding=UTF-8"

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
