FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./payment.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "payment.jar"]