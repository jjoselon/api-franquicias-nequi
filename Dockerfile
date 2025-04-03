FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Construir la aplicación con Maven
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
