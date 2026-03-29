FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./enrollment.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "enrollment.jar"]