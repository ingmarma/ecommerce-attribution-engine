# ETAPA 1: Construcción (Generamos el ejecutable .jar)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compilamos saltando los tests para ganar tiempo
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Contenedor final ultra liviano)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Traemos el .jar de la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto estándar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]