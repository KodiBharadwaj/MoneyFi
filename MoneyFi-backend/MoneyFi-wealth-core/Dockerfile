# Step 1: Build the application
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Accept GitHub credentials
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

ENV GITHUB_USERNAME=$GITHUB_USERNAME
ENV GITHUB_TOKEN=$GITHUB_TOKEN

# Create Maven settings.xml for GitHub Packages authentication
RUN mkdir -p /root/.m2 && \
    echo "<settings>" > /root/.m2/settings.xml && \
    echo "  <servers>" >> /root/.m2/settings.xml && \
    echo "    <server>" >> /root/.m2/settings.xml && \
    echo "      <id>github</id>" >> /root/.m2/settings.xml && \
    echo "      <username>${GITHUB_USERNAME}</username>" >> /root/.m2/settings.xml && \
    echo "      <password>${GITHUB_TOKEN}</password>" >> /root/.m2/settings.xml && \
    echo "    </server>" >> /root/.m2/settings.xml && \
    echo "  </servers>" >> /root/.m2/settings.xml && \
    echo "</settings>" >> /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]