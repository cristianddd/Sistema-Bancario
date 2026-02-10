# ------------------------------------------------------------------------------
# Dockerfile for auth-service
#
# This multi-stage build first compiles the application using the official
# Maven image and then produces a slim runtime image based on the JRE.  The
# resulting container exposes port 8080 and executes the Spring Boot fat jar.
# ------------------------------------------------------------------------------

## Build stage: use Maven to compile and package the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

## Runtime stage: copy the packaged jar into a minimal JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/auth-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]