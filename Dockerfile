# -------------------------------
# STAGE 1: BUILD THE APPLICATION
# -------------------------------

# Use Maven with JDK 17 to build the Spring Boot project
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set working directory inside container
WORKDIR /app

# Copy Maven configuration file (pom.xml)
COPY app/pom.xml .

# Copy source code
COPY app/src ./src

# Build the application and skip tests for faster build
RUN mvn clean package -DskipTests


# -------------------------------
# STAGE 2: RUN THE APPLICATION
# -------------------------------

# Use lightweight JRE 17 image for running the app
FROM eclipse-temurin:17.0.15_6-jre

# Set working directory
WORKDIR /app

# Copy the built JAR file from builder stage
COPY --from=builder /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
