# Usa OpenJDK 21 ufficiale
FROM openjdk:21-jdk-slim

# Directory di lavoro
WORKDIR /app

# Copia solo i file necessari per il build iniziale
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Rendi eseguibile il wrapper
RUN chmod +x mvnw

# Installa curl e unzip necessari per Maven/Nixpacks (opzionale)
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# Copia tutto il resto del progetto
COPY . .

# Build del progetto senza test
RUN ./mvnw clean package -DskipTests

# Esponi porta dell'app Spring Boot
EXPOSE 8080

# Comando di avvio
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
