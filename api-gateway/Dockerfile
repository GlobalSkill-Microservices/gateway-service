# ===== STAGE 1: BUILD (Using Maven and JDK 21) =====
# Use Maven image running on JDK 21 (Temurin) for compilation.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# Copy all source code into the container
COPY . .
# Compile the application, create the JAR file, and skip tests
RUN mvn clean package -DskipTests

# ===== STAGE 2: RUN (Using minimal JRE 21) =====
# Use minimal JRE 21 to run the application (reduces final image size)
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the compiled JAR file from the 'build' stage and name it app.jar
COPY --from=build /src/target/*.jar app.jar

# Expose port 8080 as defined in application.yml
EXPOSE 8080

# Application startup command
# Note: The AIVEN_PASSWORD environment variable must be provided
# when deploying to Render to connect to the Aiven database.
ENTRYPOINT echo "🚀 Starting gateway-service..." && java -jar app.jar