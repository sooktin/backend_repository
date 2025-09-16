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

EXPOSE 8080

# cgroup 문제 해결하면서도 모든 메트릭 유지
ENTRYPOINT ["java", "-XX:-UseContainerSupport", "-jar", "app.jar"]
