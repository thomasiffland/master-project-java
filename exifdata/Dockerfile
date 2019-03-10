FROM openjdk:8-jdk-slim
RUN apt-get update
RUN apt-get install libimage-exiftool-perl -y
VOLUME /tmp
COPY ./build/libs/exifdata-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]