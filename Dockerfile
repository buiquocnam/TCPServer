FROM maven:3.8.4-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy only the files needed for dependency resolution
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create the final image
FROM openjdk:17-slim

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/socket-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar

# Create and copy public directory
RUN mkdir -p /app/public
COPY --from=build /app/src/main/public /app/public

# Set environment variables
ENV PORT=8080
ENV PUBLIC_PATH=/app/public

# Expose the port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
