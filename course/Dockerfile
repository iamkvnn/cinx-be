FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/*.jar ./course.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "course.jar"]