# -------------------------
# Stage 1: build con Maven + JDK21
# -------------------------
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copia wrapper e pom per sfruttare cache
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Rendi eseguibile il wrapper (se usi mvnw)
RUN chmod +x mvnw

# Copia il source
COPY src ./src

# Build (senza test per velocità)
RUN ./mvnw -B -DskipTests package

# -------------------------
# Stage 2: runtime con JRE21 snello
# -------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia jar dal build stage
COPY --from=build /app/target/*.jar app.jar

# porta gestita via $PORT
ENV PORT=8080
EXPOSE 8080
# Comando di avvio
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]
