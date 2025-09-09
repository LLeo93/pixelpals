# Usa OpenJDK 21 ufficiale
FROM openjdk:21-jdk-slim

WORKDIR /app

RUN chmod +x mvnw
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# Copia Maven wrapper e configurazioni
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn .mvn

RUN chmod +x mvnw

# Copia tutto il resto del backend
COPY backend/src ./src
COPY backend/target ./target

# Build senza usare BuildKit
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
