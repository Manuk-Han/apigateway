FROM python:3.10-slim

RUN apt-get update && \
    apt-get install -y gcc && \
    apt-get clean

WORKDIR /app

COPY requirements.txt .
COPY CompileWorker.py .

RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "CompileWorker.py"]
