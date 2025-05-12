import os
import json
import time
import shutil
import subprocess
import requests
from kafka import KafkaConsumer, KafkaProducer

KAFKA_SERVERS = os.getenv("KAFKA_SERVERS", "host.docker.internal:9092")
RESULT_ENDPOINT = os.getenv("RESULT_ENDPOINT", "http://host.docker.internal:8082/submit/result")
RESULT_API_KEY = os.getenv("RESULT_API_KEY", "WORKER-KEY")

consumer = KafkaConsumer(
    'submission-PYTHON',
    bootstrap_servers=KAFKA_SERVERS,
    group_id='python-judge-group',
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    auto_offset_reset='earliest'
)

producer = KafkaProducer(
    bootstrap_servers=KAFKA_SERVERS,
    value_serializer=lambda m: json.dumps(m).encode('utf-8')
)

def send_result(submit_id, user_id, language, score, error_detail, execution_time):
    body = {
        "submitId": submit_id,
        "score": score,
        "executionTime": round(execution_time, 3),
        "errorDetail": error_detail
    }
    try:
        res = requests.post(
            RESULT_ENDPOINT,
            json=body,
            headers={"Content-Type": "application/json", "X-API-KEY": RESULT_API_KEY},
            timeout=5
        )
        print(f"[RESULT POST] Response Code: {res.status_code}")
        if res.status_code >= 400:
            print("[SERVER ERROR RESPONSE]", res.text)
    except Exception as e:
        print("[ERROR] Failed to send result:", e)

def handle_submission(data):
    code = data['code']
    submit_id = str(data['submitId'])
    user_id = str(data['userId'])
    language = data['language']
    problem_id = str(data['problemId'])

    work_dir = f"/app/workdir/{submit_id}"
    os.makedirs(work_dir, exist_ok=True)
    code_path = os.path.join(work_dir, "main.py")
    with open(code_path, 'w') as f:
        f.write(code)

    # Syntax check first
    syntax = subprocess.run(["python3", "-m", "py_compile", code_path], capture_output=True, text=True)
    if syntax.returncode != 0:
        send_result(submit_id, user_id, language, 0, syntax.stderr, 0.0)
        return

    testcase_dir = f"/app/testcases/{problem_id}/testcase"
    total_score = 0
    total_time = 0.0
    num_cases = 0

    for i in range(1, 100):
        input_file = os.path.join(testcase_dir, f"input{i}.txt")
        output_file = os.path.join(testcase_dir, f"output{i}.txt")
        if not os.path.exists(input_file) or not os.path.exists(output_file):
            break

        with open(input_file, 'r') as inp:
            start = time.time()
            result = subprocess.run(["python3", code_path], input=inp.read(), text=True,
                                    capture_output=True, timeout=3)
            end = time.time()

        execution_time = end - start
        total_time += execution_time

        with open(output_file, 'r') as exp:
            expected = exp.read().strip()
            actual = result.stdout.strip()

        print(f"[TESTCASE {i}] Actual: {actual}")
        print(f"[TESTCASE {i}] Expected: {expected}")

        if actual == expected:
            total_score += 100
        num_cases += 1

    avg_time = total_time / num_cases if num_cases > 0 else 0.0
    score = total_score // num_cases if num_cases > 0 else 0

    send_result(submit_id, user_id, language, score, "", avg_time)

    done_msg = {
        "submitId": submit_id,
        "problemId": problem_id,
        "userId": user_id,
        "language": language
    }
    producer.send("compile-done", done_msg)
    print("[COMPILE + TESTCASE SUCCESS] sent to topic compile-done")

print("[PYTHON-JUDGE] Listening for submissions...")
for msg in consumer:
    try:
        handle_submission(msg.value)
    except Exception as e:
        print("[UNCAUGHT ERROR]", e)
