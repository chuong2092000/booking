FROM amazoncorretto:21-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

ARG SERVICE_NAME

COPY ${SERVICE_NAME}/target/${SERVICE_NAME}.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]