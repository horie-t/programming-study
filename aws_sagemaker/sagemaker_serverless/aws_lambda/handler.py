import json
import boto3

def translate(event, context):
    runtime = boto3.client("sagemaker-runtime")

    endpoint_name = "CTranslate2"
    content_type = "text/plain"
    original_text = event['body']

    translate_response = runtime.invoke_endpoint(
        EndpointName=endpoint_name,
        ContentType=content_type,
        Body=original_text
    )
    translated_text = translate_response['Body'].read().decode('utf-8')

    response = {
        "statusCode": 200,
        "body": json.dumps(translated_text)
    }
    return response
