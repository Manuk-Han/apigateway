import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class CompileWorker {
    public static void main(String[] args) throws Exception {
        String kafkaServer = System.getenv().getOrDefault("KAFKA_SERVERS", "kafka:9092");
        String resultEndpoint = System.getenv().getOrDefault("RESULT_ENDPOINT", "http://submit:8082/result");

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", kafkaServer);
        consumerProps.put("group.id", "java-judge-group");
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", kafkaServer);
        producerProps.put("key.serializer", StringSerializer.class.getName());
        producerProps.put("value.serializer", StringSerializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        consumer.subscribe(Collections.singletonList("submission-JAVA"));
        System.out.println("[JAVA-JUDGE] Listening for submissions...");

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String json = record.value();
                System.out.println("[RECEIVED] " + json);
                handleSubmission(json, producer, resultEndpoint);
            }
        }
    }

    private static void handleSubmission(String json, KafkaProducer<String, String> producer, String resultEndpoint) throws Exception {
        String code = extractJson(json, "code");
        String submissionId = extractJson(json, "submitId");
        String userId = extractJson(json, "userId");
        String language = extractJson(json, "language");
        String problemId = extractJson(json, "problemId");

        Path workDir = Paths.get("/app/workdir/" + submissionId);
        Files.createDirectories(workDir);
        Path codeFile = workDir.resolve("Main.java");
        Files.writeString(codeFile, code);

        // 컴파일 단계
        Process compile = new ProcessBuilder("javac", codeFile.toString())
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
        compile.waitFor();

        if (compile.exitValue() != 0) {
            String error = new String(compile.getInputStream().readAllBytes());
            sendResultToServer(submissionId, userId, language, 0, "COMPILE_ERROR", error, resultEndpoint);
            return;
        }

        // 성공 시 컴파일 완료 토픽 전송
        String doneJson = String.format("{\"submitId\":\"%s\",\"problemId\":\"%s\",\"userId\":\"%s\",\"language\":\"%s\"}",
                submissionId, problemId, userId, language);
        producer.send(new ProducerRecord<>("compile-done", doneJson));
        System.out.println("[COMPILE SUCCESS] sent to topic compile-done");
    }

    private static void sendResultToServer(String submitId, String userId, String language, int score, String status, String error, String endpoint) throws Exception {
        String body = String.format("{\"submitId\":\"%s\",\"accountId\":\"%s\",\"language\":\"%s\",\"score\":%d,\"status\":\"%s\",\"output\":\"\",\"error\":\"%s\"}",
                submitId, userId, language, score, status, error.replace("\"", "'"));

        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        System.out.println("[RESULT POST] Response Code: " + code);
    }

    private static String extractJson(String json, String key) {
        String pattern = "\"" + key + "\":\"(.*?)\"";
        return json.replaceAll(".*" + pattern + ".*", "$1");
    }
}