import os
import json
import time
import shutil
import requests
from kafka import KafkaConsumer, KafkaProducer

KAFKA_SERVER = os.getenv("KAFKA_SERVERS", "localhost:9092")
RESULT_ENDPOINT = os.getenv("RESULT_ENDPOINT", "http://localhost:8082/submit/result")
API_KEY = os.getenv("RESULT_API_KEY", "WORKER-KEY")
WORKDIR = "/app/workdir"
TESTCASE_DIR_BASE = "/app/testcases"

consumer = KafkaConsumer(
    "submission-C",
    bootstrap_servers=KAFKA_SERVER,
    group_id="c-judge-group",
    value_deserializer=lambda x: x.decode('utf-8'),
)
producer = KafkaProducer(
    bootstrap_servers=KAFKA_SERVER,
    value_serializer=lambda x: x.encode('utf-8'),
)

def post_result(payload):
    headers = {"Content-Type": "application/json", "X-API-KEY": API_KEY}
    try:
        res = requests.post(RESULT_ENDPOINT, headers=headers, data=json.dumps(payload))
        print(f"[RESULT POST] Status: {res.status_code}")
        if res.status_code >= 400:
            print("[SERVER ERROR RESPONSE]", res.text)
    except Exception as e:
        print("[ERROR] Failed to post result:", e)

def process_submission(data):
    try:
        obj = json.loads(data)
        submit_id = str(obj["submitId"])
        user_id = str(obj["userId"])
        problem_id = str(obj["problemId"])
        code = obj["code"]

        work_path = os.path.join(WORKDIR, submit_id)
        os.makedirs(work_path, exist_ok=True)
        code_path = os.path.join(work_path, "main.c")
        binary_path = os.path.join(work_path, "main.out")
        with open(code_path, "w") as f:
            f.write(code)

        # Compile C code
        compile = os.system(f"gcc {code_path} -o {binary_path}")
        if compile != 0:
            post_result({
                "submitId": submit_id,
                "score": 0,
                "executionTime": 0.0,
                "errorDetail": "Compilation failed"
            })
            return

        testcase_dir = os.path.join(TESTCASE_DIR_BASE, problem_id, "testcase")
        total_score = 0
        total_time = 0.0
        case_count = 0

        for i in range(1, 100):
            input_file = os.path.join(testcase_dir, f"input{i}.txt")
            output_file = os.path.join(testcase_dir, f"output{i}.txt")
            if not os.path.exists(input_file) or not os.path.exists(output_file):
                break

            start = time.time()
            result_file = os.path.join(work_path, f"actual{i}.txt")
            ret = os.system(f"timeout 3s {binary_path} < {input_file} > {result_file}")
            end = time.time()
            exec_time = end - start
            total_time += exec_time

            if ret != 0:
                continue

            with open(result_file) as actual, open(output_file) as expected:
                if actual.read().strip() == expected.read().strip():
                    total_score += 100
            case_count += 1

        score = total_score // case_count if case_count > 0 else 0
        avg_time = total_time / case_count if case_count > 0 else 0.0

        status = "CORRECT" if score == 100 else "WRONG"

        post_result({
            "submitId": submit_id,
            "score": score,
            "executionTime": round(avg_time, 3),
            "errorDetail": ""
        })

        done_msg = json.dumps({
            "submitId": submit_id,
            "problemId": problem_id,
            "userId": user_id,
            "language": "C"
        })
        producer.send("compile-done", done_msg)
    except Exception as e:
        print("[ERROR] Exception occurred while processing submission:", e)

print("[C-JUDGE] Listening for submissions...")
for msg in consumer:
    print("[RECEIVED]", msg.value)
    process_submission(msg.value)
