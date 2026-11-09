# Сборка из корня мультипроекта: docker build -f gateway-service/Dockerfile .
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl gateway-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 bank
COPY --from=build /workspace/gateway-service/target/gateway-service.jar app.jar
USER bank
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
