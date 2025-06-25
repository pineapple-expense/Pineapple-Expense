import json
import urllib.parse
import boto3
import time
import os

print('Loading function')

stepfunction_client = boto3.client('stepfunctions')



def lambda_handler(event, context):

    step_function_arn = os.environ.get("STEP_FUNCTION_ARN")
    bucket = os.environ.get('BUCKET')

    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']


    body = event.get("body")
    if not body:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Missing request body"})
        }

    try:
        body_json = json.loads(body)
    except json.JSONDecodeError:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Invalid JSON format"})
        }

    receipt_id = body_json.get("receipt_id")
    name = body_json.get("name")

    try:
        print("in try loop")
        step_function_input = {
            "bucket": bucket,
            "key": receipt_id,
            "user_id": user_id,
            "name": name
        }

        response = stepfunction_client.start_execution(
            stateMachineArn=step_function_arn,
            input=json.dumps(step_function_input)
        )
        print("returned from step function")
        print(response)

        execution_arn = response['executionArn']
        print(f"Execution ARN: {execution_arn}")

        output = get_execution_output(execution_arn)
        print(output)
        return output
        
    except Exception as e:
        print(e)
        print(f"Error getting object {receipt_id} from bucket {bucket}. Make sure they exist and your bucket is in the same region as this function.")
        raise e

def get_execution_output(execution_arn):
    while True:
        execution_response = stepfunction_client.describe_execution(executionArn=execution_arn)
        
        status = execution_response['status']
        if status == 'SUCCEEDED':
            return json.loads(execution_response['output'])
        elif status in ['FAILED', 'TIMED_OUT', 'ABORTED']:
            raise Exception(f"Step Function execution failed with status: {status}")
        
        time.sleep(2)