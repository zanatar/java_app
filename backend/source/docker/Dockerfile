FROM openjdk
LABEL version=1.2
COPY tchallenge-service.jar /backend/tchallenge-service.jar
COPY jmx_prometheus_javaagent-0.14.0.jar /backend/jmx_prometheus_javaagent-0.14.0.jar
COPY config.yaml /backend/config/config.yaml
WORKDIR /backend
CMD java -javaagent:./jmx_prometheus_javaagent-0.14.0.jar=8080:config/config.yaml -jar tchallenge-service.jar > logs/java.log 2> logs/java_err.log
