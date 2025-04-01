FROM maven:3.8.4-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Set JAVA_HOME explicitly
ENV JAVA_HOME=/usr/local/openjdk-17

# Copy only the files needed for dependency resolution
COPY pom.xml .
COPY src ./src

# Build the application with explicit Java version
RUN mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17

# Create the final image
FROM openjdk:17-slim

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/socket-server-0.0.1-SNAPSHOT.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD ["java", "-jar", "app.jar"]
