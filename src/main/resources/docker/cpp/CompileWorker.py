import os
import json
import time
import subprocess
from kafka import KafkaConsumer, KafkaProducer

KAFKA_SERVERS = os.getenv("KAFKA_SERVERS", "localhost:9092")
RESULT_ENDPOINT = os.getenv("RESULT_ENDPOINT", "http://localhost:8082/submit/result")
API_KEY = os.getenv("RESULT_API_KEY", "WORKER-KEY")

consumer = KafkaConsumer(
    "submission-CPP",
    bootstrap_servers=KAFKA_SERVERS,
    value_deserializer=lambda m: json.loads(m.decode("utf-8")),
    auto_offset_reset="earliest",
    group_id="cpp-judge-group"
)

producer = KafkaProducer(
    bootstrap_servers=KAFKA_SERVERS,
    value_serializer=lambda m: json.dumps(m).encode("utf-8")
)

print("[CPP-JUDGE] Listening for submissions...")

def send_result(data):
    import requests
    headers = {
        "Content-Type": "application/json",
        "X-API-KEY": API_KEY
    }
    try:
        res = requests.post(RESULT_ENDPOINT, headers=headers, json=data)
        print(f"[RESULT POST] Status: {res.status_code}")
        if res.status_code >= 400:
            print(f"[SERVER ERROR] {res.text}")
    except Exception as e:
        print(f"[ERROR] Failed to send result: {e}")

for msg in consumer:
    data = msg.value
    print("[RECEIVED]", data)

    submit_id = data["submitId"]
    user_id = data["userId"]
    language = data["language"]
    problem_id = data["problemId"]
    code = data["code"]

    work_dir = f"/app/workdir/{submit_id}"
    os.makedirs(work_dir, exist_ok=True)

    code_path = os.path.join(work_dir, "main.cpp")
    with open(code_path, "w") as f:
        f.write(code)

    executable = os.path.join(work_dir, "a.out")
    compile_cmd = ["g++", code_path, "-o", executable]

    try:
        result = subprocess.run(compile_cmd, capture_output=True, text=True, timeout=10)
        if result.returncode != 0:
            send_result({
                "submitId": submit_id,
                "score": 0,
                "status": "ERROR",
                "executionTime": 0.0,
                "errorDetail": result.stderr
            })
            continue
    except subprocess.TimeoutExpired:
        send_result({
            "submitId": submit_id,
            "score": 0,
            "status": "ERROR",
            "executionTime": 0.0,
            "errorDetail": "Compilation timeout"
        })
        continue

    testcase_dir = f"/app/testcases/{problem_id}/testcase"
    total_score = 0
    total_time = 0.0
    num_cases = 0

    while True:
        i = num_cases + 1
        input_path = os.path.join(testcase_dir, f"input{i}.txt")
        output_path = os.path.join(testcase_dir, f"output{i}.txt")
        if not os.path.exists(input_path) or not os.path.exists(output_path):
            break

        with open(input_path, "r") as fin, open(os.path.join(work_dir, f"actual{i}.txt"), "w") as fout:
            start = time.time()
            try:
                run = subprocess.run([executable], stdin=fin, stdout=fout, timeout=3)
            except subprocess.TimeoutExpired:
                total_time += 3.0
                num_cases += 1
                continue
            end = time.time()
            total_time += (end - start)

        with open(os.path.join(work_dir, f"actual{i}.txt")) as f1, open(output_path) as f2:
            actual = f1.read().strip()
            expected = f2.read().strip()
            if actual == expected:
                total_score += 100
        num_cases += 1

    avg_time = total_time / num_cases if num_cases else 0
    final_score = total_score // num_cases if num_cases else 0

    status = "CORRECT" if score == 100 else "WRONG"

    send_result({
        "submitId": submit_id,
        "score": final_score,
        "status": status,
        "executionTime": round(avg_time, 3),
        "errorDetail": ""
    })

    producer.send("compile-done", {
        "submitId": submit_id,
        "userId": user_id,
        "problemId": problem_id,
        "language": language
    })
    print("[SUCCESS] Result sent and compile-done published.")
