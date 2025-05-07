import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class CompileWorker {
    public static void main(String[] args) throws Exception {
        String kafkaServer = System.getenv().getOrDefault("KAFKA_SERVERS", "http://host.docker.internal:9092");
        String resultEndpoint = System.getenv().getOrDefault("RESULT_ENDPOINT", "http://host.docker.internal:8082/submit/result");

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
        String formattedCode = StringEscapeUtils.unescapeJava(code);
        Files.writeString(codeFile, formattedCode);

        Process compile = new ProcessBuilder("javac", codeFile.toString())
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
        compile.waitFor();

        if (compile.exitValue() != 0) {
            String error = new String(compile.getInputStream().readAllBytes());
            sendResultToServer(submissionId, userId, language, 0, Status.ERROR, error, 0.0, resultEndpoint);
            return;
        }

        String testcaseDir = "/app/testcases/" + problemId + "/testcase/";
        int totalScore = 0;
        int numCases = 0;
        double totalTime = 0.0;

        for (int i = 1; ; i++) {
            Path inputFile = Paths.get(testcaseDir, "input" + i + ".txt");
            Path outputFile = Paths.get(testcaseDir, "output" + i + ".txt");
            if (!Files.exists(inputFile) || !Files.exists(outputFile)) break;

            Path resultFile = workDir.resolve("actual" + i + ".txt");
            long start = System.nanoTime();
            Process run = new ProcessBuilder("timeout", "3s", "java", "Main")
                    .directory(workDir.toFile())
                    .redirectInput(inputFile.toFile())
                    .redirectOutput(resultFile.toFile())
                    .start();
            run.waitFor();
            long end = System.nanoTime();

            double executionTime = (end - start) / 1_000_000_000.0;
            totalTime += executionTime;

            String actual = Files.readString(resultFile).strip();
            String expected = Files.readString(outputFile).strip();

            System.out.println("[TESTCASE " + i + "] Actual: " + actual);
            System.out.println("[TESTCASE " + i + "] Expected: " + expected);

            if (actual.equals(expected)) {
                totalScore += 100;
            }
            numCases++;
        }

        int score = numCases == 0 ? 0 : totalScore / numCases;
        double avgExecutionTime = numCases == 0 ? 0.0 : totalTime / numCases;

        sendResultToServer(submissionId, userId, language, score, Status.PASS, "", avgExecutionTime, resultEndpoint);

        String doneJson = String.format("{\"submitId\":\"%s\",\"problemId\":\"%s\",\"userId\":\"%s\",\"language\":\"%s\"}",
                submissionId, problemId, userId, language);
        producer.send(new ProducerRecord<>("compile-done", doneJson));
        System.out.println("[COMPILE + TESTCASE SUCCESS] sent to topic compile-done");
    }

    private static void sendResultToServer(String submitId, String userId, String language, int score, Status status, String error, double executionTime, String endpoint) throws Exception {
        String apiKey = System.getenv().getOrDefault("RESULT_API_KEY", "WORKER-KEY");

        String body = String.format(
                "{" +
                        "\"submitId\":%s," +
                        "\"score\":%d," +
                        "\"status\":\"%s\"," +
                        "\"executionTime\":%.3f," +
                        "\"errorDetail\":\"%s\"" +
                        "}",
                submitId, score, status, executionTime, error.replace("\"", "'")
        );

        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("X-API-KEY", apiKey);
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        System.out.println("[RESULT POST] Response Code: " + code);
    }

    private static String extractJson(String json, String key) {
        String pattern = "\"" + key + "\":(?:\"(.*?)\"|(\\d+))";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return "";
    }
}

enum Status {
    CORRECT,
    WRONG,
    REJECTED,
    PASS,
    ERROR,
    NOT_SUBMITTED
}
