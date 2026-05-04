FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY server/pom.xml ./pom.xml

COPY server/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package dependency:copy-dependencies -DoutputDirectory=target/dependency -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends wget unzip gnupg2 ca-certificates curl jq xvfb && \
    wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends google-chrome-stable && \
    CHROME_VERSION=$(google-chrome --version | awk '{ print $3 }' | awk -F'.' '{ print $1 }') && \
    CHROMEDRIVER_VERSION=$(curl -s "https://googlechromelabs.github.io/chrome-for-testing/latest-versions-per-milestone.json" | jq -r ".milestones.\"${CHROME_VERSION}\".version") && \
    echo "Chrome version: ${CHROME_VERSION}, ChromeDriver version: ${CHROMEDRIVER_VERSION}" && \
    wget -q "https://storage.googleapis.com/chrome-for-testing-public/${CHROMEDRIVER_VERSION}/linux64/chromedriver-linux64.zip" -O /tmp/chromedriver.zip && \
    unzip /tmp/chromedriver.zip -d /tmp && \
    mv /tmp/chromedriver-linux64/chromedriver /usr/local/bin/ && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf /tmp/chromedriver* && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

RUN echo '#!/bin/bash\nexec google-chrome --no-sandbox --disable-dev-shm-usage --disable-gpu --disable-software-rasterizer "$@"' > /usr/local/bin/chrome-wrapper && \
    chmod +x /usr/local/bin/chrome-wrapper

COPY --from=builder /workspace/target/*-exec.jar /app/app.jar
COPY --from=builder /workspace/target/dependency /app/lib

RUN mkdir -p /app/lib && \
    if jar tf /app/app.jar | grep -q "BOOT-INF/lib/"; then \
        jar -xf /app/app.jar BOOT-INF/lib && \
        if [ -d BOOT-INF/lib ]; then mv BOOT-INF/lib/* /app/lib/; rm -rf BOOT-INF; fi; \
    fi

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

