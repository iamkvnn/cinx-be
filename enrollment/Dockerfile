FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./media.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "media.jar"]