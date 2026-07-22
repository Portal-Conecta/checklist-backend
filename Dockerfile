FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
ARG MAVEN_USERNAME
RUN --mount=type=secret,id=maven_password \
    if [ -z "$MAVEN_USERNAME" ]; then echo "MAVEN_USERNAME is required to download portal-logging from GitHub Packages." >&2; exit 1; fi && \
    if [ ! -s /run/secrets/maven_password ]; then echo "MAVEN_PASSWORD is required to download portal-logging from GitHub Packages." >&2; exit 1; fi && \
    chmod +x mvnw && MAVEN_PASSWORD="$(cat /run/secrets/maven_password)" ./mvnw -B dependency:go-offline
COPY src/ src/
RUN --mount=type=secret,id=maven_password \
    if [ -z "$MAVEN_USERNAME" ]; then echo "MAVEN_USERNAME is required to download portal-logging from GitHub Packages." >&2; exit 1; fi && \
    if [ ! -s /run/secrets/maven_password ]; then echo "MAVEN_PASSWORD is required to download portal-logging from GitHub Packages." >&2; exit 1; fi && \
    MAVEN_PASSWORD="$(cat /run/secrets/maven_password)" ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER app
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
