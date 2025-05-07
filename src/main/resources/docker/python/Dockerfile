FROM python:3.10-slim

RUN apt-get update && \
    apt-get install -y default-jdk gcc && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
COPY CompileWorker.py .

RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "CompileWorker.py"]
