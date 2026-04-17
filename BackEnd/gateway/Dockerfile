FROM eclipse-temurin:17

WORKDIR /app

COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
