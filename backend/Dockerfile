FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Maven 镜像配置（使用阿里云）
RUN mkdir -p /usr/share/maven/ref/ && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /usr/share/maven/ref/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">' >> /usr/share/maven/ref/settings.xml && \
    echo '  <mirrors>' >> /usr/share/maven/ref/settings.xml && \
    echo '    <mirror>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <name>阿里云公共仓库</name>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> /usr/share/maven/ref/settings.xml && \
    echo '    </mirror>' >> /usr/share/maven/ref/settings.xml && \
    echo '  </mirrors>' >> /usr/share/maven/ref/settings.xml && \
    echo '</settings>' >> /usr/share/maven/ref/settings.xml

COPY pom.xml .
RUN mvn dependency:go-offline -B -s /usr/share/maven/ref/settings.xml

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -m -s /usr/sbin/nologin appuser

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

USER appuser

ENV SERVER_PORT=18080
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 18080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -fsS http://127.0.0.1:${SERVER_PORT}/areas/online >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
