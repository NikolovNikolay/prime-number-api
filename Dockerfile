FROM openjdk:11

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} primeapi.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar","/primeapi.jar"]
