FROM openjdk:17-alpine
EXPOSE 5000
WORKDIR /opt/micronaut

COPY gradle.properties gradle.properties
COPY build/libs/*-all.jar service.jar

ENTRYPOINT exec java $JAVA_OPTS -Dapp.version=$(grep version gradle.properties | cut -d '=' -f2 | sed -e 's/^[[:space:]]*//') -jar service.jar