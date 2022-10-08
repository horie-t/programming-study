#!/usr/bin/env python3

import boto3
import sys

runtime = boto3.client("sagemaker-runtime")

endpoint_name = "CTranslate2"
content_type = "text/plain"
payload = sys.argv[1]

response = runtime.invoke_endpoint(
    EndpointName=endpoint_name,
    ContentType=content_type,
    Body=payload
)

print(response['Body'].read().decode('utf-8'))
