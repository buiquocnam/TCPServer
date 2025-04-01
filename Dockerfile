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

# Set environment variables
ENV PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose the port
EXPOSE 8080

# Run the application with proper signal handling
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
