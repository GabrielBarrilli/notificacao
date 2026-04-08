FROM gradle:8.7-jdk17 as build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon --stacktrace

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/notificacao.jar

EXPOSE 8082

CMD ["java", "-jar", "/app/notificacao.jar"]
