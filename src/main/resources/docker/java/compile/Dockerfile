### java/compile/Dockerfile
FROM openjdk:17-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl -L -o kafka-clients.jar https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.6.0/kafka-clients-3.6.0.jar && \
    curl -L -o okhttp.jar https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.10.0/okhttp-4.10.0.jar && \
    curl -L -o slf4j-api.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar && \
    curl -L -o slf4j-simple.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar && \
    mkdir -p libs && \
    mv kafka-clients.jar libs/ && \
    mv okhttp.jar libs/ && \
    mv slf4j-api.jar libs/ && \
    mv slf4j-simple.jar libs/ && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY ./CompileWorker.java /app/CompileWorker.java

RUN javac -cp ".:/app/libs/*" CompileWorker.java

CMD ["java", "-cp", ".:/app/libs/*", "CompileWorker"]