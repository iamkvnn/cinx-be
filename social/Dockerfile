FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./social.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "social.jar"]