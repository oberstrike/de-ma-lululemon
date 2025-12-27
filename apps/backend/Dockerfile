FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Gradle wrapper and build configuration
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY apps/backend/build.gradle.kts ./apps/backend/
COPY apps/backend/checkstyle.xml apps/backend/checkstyle-suppressions.xml apps/backend/spotbugs-exclude.xml ./apps/backend/
COPY apps/backend/src ./apps/backend/src

# Build the application (disable toolchain auto-download since JDK is already installed)
RUN chmod +x gradlew && ./gradlew :apps:backend:bootJar --no-daemon -x test -x spotbugsMain -x spotbugsTest -Porg.gradle.java.installations.auto-download=false

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache megacmd curl
COPY --from=build /app/apps/backend/build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
