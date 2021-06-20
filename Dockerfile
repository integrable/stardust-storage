FROM adoptopenjdk:15-hotspot
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
