FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./learning.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "learning.jar"]