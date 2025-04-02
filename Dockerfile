# 1️⃣ Usar una imagen base con Maven y JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# 2️⃣ Definir el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3️⃣ Copiar los archivos de Maven (para optimizar el uso de caché)
COPY pom.xml .
COPY src ./src

# 4️⃣ Construir la aplicación con Maven
RUN mvn clean package -DskipTests=true


# 5️⃣ Usar una imagen más ligera para ejecutar el JAR compilado
FROM eclipse-temurin:17-jdk

# 6️⃣ Definir el directorio de trabajo en la imagen final
WORKDIR /app

# 7️⃣ Copiar el JAR desde la imagen de compilación
COPY --from=builder /app/target/*.jar app.jar

# 8️⃣ Definir el puerto en el que corre la aplicación (si aplica)
EXPOSE 8080

# 9️⃣ Ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
