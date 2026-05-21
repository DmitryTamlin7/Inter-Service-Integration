
FROM gradle:8.5-jdk21-jammy AS build
WORKDIR /app
COPY settings.gradle* build.gradle* ./
COPY storing-service ./storing-service
COPY analysis-service ./analysis-service
COPY gateway-service ./gateway-service
RUN for svc in storing-service analysis-service; do \
      if [ ! -f "$$svc/src/main/resources/application.yml" ] && [ -f "$$svc/src/main/resources/application.yml.example" ]; then \
        cp "$$svc/src/main/resources/application.yml.example" "$$svc/src/main/resources/application.yml"; \
      fi; \
    done
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy AS storing-service
WORKDIR /app
COPY --from=build /app/storing-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-jammy AS analysis-service
WORKDIR /app
COPY --from=build /app/analysis-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-jammy AS gateway-service
WORKDIR /app
COPY --from=build /app/gateway-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]