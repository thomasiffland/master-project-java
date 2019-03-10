FROM openjdk:8-jdk-slim
RUN apt-get update
RUN apt-get install imagemagick -y
RUN apt-get install software-properties-common -y
RUN add-apt-repository ppa:dhor/myway
RUN apt-get install dcraw -y
VOLUME /tmp
COPY ./build/libs/rawtojpg-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]