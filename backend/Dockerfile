# Usa OpenJDK 21 ufficiale
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copia Maven wrapper e file essenziali
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# Copia il resto del progetto
COPY . .

# Build senza usare BuildKit
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
