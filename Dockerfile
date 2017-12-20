FROM maven:3-jdk-8 as builder

RUN rm /dev/random && ln -s /dev/urandom /dev/random \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /tmp/cloudref
COPY . /tmp/cloudref
RUN ./gradlew war
RUN sed -ie "s/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/.\/urandom/g" /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security

FROM tomcat:8.5-jre8

RUN rm /dev/random && ln -s /dev/urandom /dev/random \
    && rm -rf ${CATALINA_HOME}/webapps/*

COPY --from=builder /tmp/cloudref/frontend-war/build/libs/frontend-war.war ${CATALINA_HOME}/webapps/ROOT.war
COPY --from=builder /tmp/cloudref/backend/build/libs/backend.war ${CATALINA_HOME}/webapps/ROOT.war

EXPOSE 8080

CMD ${CATALINA_HOME}/bin/catalina.sh run
