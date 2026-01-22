FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./discovery.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "discovery.jar"]