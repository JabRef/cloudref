# build wars
FROM ubuntu:17.10 as builder

RUN apt-get -y update && apt-get -y upgrade && apt-get -y install build-essential git openjdk-8-jdk openjdk-8-jre-headless sqlite3 libsqlite3-dev python2.7 python-pip
WORKDIR /tmp/cloudref
COPY . /tmp/cloudref
RUN ./gradlew war

# initialize CloudRef.sqlite
RUN mkdir /db
COPY user-maintainer.sql /db
RUN echo ".read /db/user-maintainer.sql" | /usr/bin/sqlite3 /db/CloudRef.sqlite


# runtime configuration
FROM tomcat:8.5-jre8

RUN rm /dev/random && ln -s /dev/urandom /dev/random \
    && rm -rf ${CATALINA_HOME}/webapps/*

COPY --from=builder /tmp/cloudref/frontend-war/build/libs/cloudref.war ${CATALINA_HOME}/webapps/cloudref.war
COPY --from=builder /tmp/cloudref/backend/build/libs/backend.war ${CATALINA_HOME}/webapps/ROOT.war
RUN mkdir /root/CloudRef
COPY --from=builder /db/CloudRef.sqlite /root/CloudRef/

EXPOSE 8080

CMD ${CATALINA_HOME}/bin/catalina.sh run
