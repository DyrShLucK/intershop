FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/payService-0.0.1-SNAPSHOT.jar payService-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "payService-0.0.1-SNAPSHOT.jar"]