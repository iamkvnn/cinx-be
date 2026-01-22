FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./cart.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "cart.jar"]