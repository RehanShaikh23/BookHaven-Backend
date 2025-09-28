# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app
COPY BookHaven/pom.xml .
COPY BookHaven/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:22-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
