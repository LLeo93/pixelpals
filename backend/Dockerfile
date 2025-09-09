# Base image con Java 17
FROM eclipse-temurin:17-jdk-alpine

# Imposta la working directory
WORKDIR /app

# Copia Maven wrapper e POM
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Rendi eseguibile mvnw
RUN chmod +x mvnw

# Copia il codice sorgente
COPY

# Installa strumenti utili (opzionale)
RUN apk add --no-cache bash curl unzip

# Build del progetto
RUN ./mvnw clean package -DskipTests

# Espone la porta
EXPOSE 8080

# Comando di avvio
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
