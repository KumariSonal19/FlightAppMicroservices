FROM maven:3.8.5-openjdk-17
WORKDIR /config-server
COPY target/config-server-0.0.1-SNAPSHOT.jar app.jar
CMD ["java","-jar","app.jar"]