FROM openjdk:22-ea-11-slim

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy the pom.xml file first
COPY pom.xml /app/

# Copy the entire project
COPY src /app/src

# Set the working directory to where the pom.xml is located
WORKDIR /app

# Compile the project and build the JAR
RUN mvn clean package

# Set the working directory for runtime
WORKDIR /app/src/main/java/com/group12/husksheets/ui

# Run the frontend application
CMD ["java", "-cp", "/app/target/Husksheets-1.0-SNAPSHOT.jar", "com.group12.husksheets.ui.Main"]
