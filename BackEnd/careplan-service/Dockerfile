FROM eclipse-temurin:17

WORKDIR /app

COPY target/careplan-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
