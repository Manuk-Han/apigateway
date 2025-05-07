FROM openjdk:17-slim

WORKDIR /app

RUN apt-get update && apt-get install -y curl unzip && \
    curl -L -o kafka-clients.jar https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.6.0/kafka-clients-3.6.0.jar && \
    curl -L -o slf4j-api.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar && \
    curl -L -o slf4j-simple.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar && \
    curl -L -o commons-text.jar https://repo1.maven.org/maven2/org/apache/commons/commons-text/1.11.0/commons-text-1.11.0.jar && \
    curl -L -o commons-lang3.jar https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar && \
    mkdir -p libs && \
    mv kafka-clients.jar slf4j-api.jar slf4j-simple.jar commons-text.jar commons-lang3.jar libs/ && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY ./CompileWorker.java /app/CompileWorker.java

RUN javac -cp ".:/app/libs/*" CompileWorker.java

CMD ["java", "-cp", ".:/app/libs/*", "CompileWorker"]
