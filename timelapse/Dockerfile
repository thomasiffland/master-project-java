FROM openjdk:8-jdk-slim
RUN apt-get update
RUN apt-get install unzip -y
RUN apt-get install ffmpeg -y
VOLUME /tmp
COPY ./build/libs/timelapse-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]