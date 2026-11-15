FROM gradle:9.4.1-jdk17 AS build

WORKDIR /app

COPY . .

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/build/libs/usuario-0.0.1-SNAPSHOT.jar /app/usuario.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/usuario.jar"]