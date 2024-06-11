FROM openjdk:22-ea-11-slim

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy the pom.xml file first
COPY pom.xml /app/

# Copy the entire project
COPY src /app/src

# Copy the keystore file
# FIXME: cannot find keystore becuase in HusksheetsServer.java, the keystore path is hardcoded and relative to the project root
COPY src/main/resources/keystore.jks /app/keystore.jks

# Set the working directory to where the pom.xml is located
WORKDIR /app

# Compile the project and build the JAR
RUN mvn clean package

# Set the working directory for runtime
WORKDIR /app

# Run the backend server
CMD ["java", "-jar", "target/Husksheets-1.0-SNAPSHOT.jar"]