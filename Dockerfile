FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Cache das dependências separado do código fonte (rebuild mais rápido)
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/financeiro-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Exec form: signal handling correto (sem sh wrapper)
# Spring Boot lê SPRING_PROFILES_ACTIVE automaticamente do ambiente
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
