# Use the official OpenJDK image as the base image
FROM openjdk:21-jdk

WORKDIR /app
# Copy the JAR file into the image
COPY ./target/RekazProject-1.0-SNAPSHOT-jar-with-dependencies.jar /app/RekazProject-1.0-SNAPSHOT-jar-with-dependencies.jar

# Set the startup command to execute the Java application
CMD ["java", "-jar", "/app/RekazProject-1.0-SNAPSHOT-jar-with-dependencies.jar"]
