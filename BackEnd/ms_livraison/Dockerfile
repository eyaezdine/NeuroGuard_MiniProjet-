FROM openjdk:17-slim

WORKDIR /app

COPY target/ms_livraison-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
