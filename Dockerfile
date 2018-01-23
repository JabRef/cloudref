FROM maven:3-jdk-8 as builder

RUN rm /dev/random && ln -s /dev/urandom /dev/random \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /tmp/cloudref
COPY . /tmp/cloudref
RUN ./gradlew war
RUN sed -ie "s/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/.\/urandom/g" /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security

# initialize CloudRef.sqlite
FROM ubuntu:latest as sqlite
RUN sudo apt -y update && apt-y upgrade && apt -y install sqlite3 libsqlite3-dev
RUN mkdir /db
COPY user-maintainer.sql /db
RUN echo ".read /db/user-maintainer.sql" | /usr/bin/sqlite3 /db/CloudRef.sqlite

FROM tomcat:8.5-jre8

RUN rm /dev/random && ln -s /dev/urandom /dev/random \
    && rm -rf ${CATALINA_HOME}/webapps/*

COPY --from=builder /tmp/cloudref/frontend-war/build/libs/cloudref.war ${CATALINA_HOME}/webapps/cloudref.war
COPY --from=builder /tmp/cloudref/backend/build/libs/backend.war ${CATALINA_HOME}/webapps/ROOT.war
RUN mkdir /root/CloudRef
COPY --from=sqlite /db/CloudRef.sqlite /root/CloudRef/

EXPOSE 8080

CMD ${CATALINA_HOME}/bin/catalina.sh run
