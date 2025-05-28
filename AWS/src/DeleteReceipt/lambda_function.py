import json
import psycopg2
import os
import time
import boto3

stepfunction_client = boto3.client('stepfunctions')

def lambda_handler(event, context):

    step_function_arn = os.environ.get("STEP_FUNCTION_ARN")
    bucket = os.environ.get('BUCKET')

    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']

    # Ensure event["body"] exists
    body = event.get("body")
    if not body:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Missing request body"})
        }

    # Parse JSON safely
    try:
        body_json = json.loads(body)
    except json.JSONDecodeError:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Invalid JSON format"})
        }

    # Extract `fileName` and `contentType`
    receipt_id = body_json.get("receipt_id")

    try:
        print("in try loop")
        step_function_input = {
            "bucket": bucket,
            "key": receipt_id,
            "user_id": user_id
        }

        # Trigger the Step Function
        response = stepfunction_client.start_execution(
            stateMachineArn=step_function_arn,
            input=json.dumps(step_function_input)
        )
        print("returned from step function")
        print(response)

        # Get the executionArn
        execution_arn = response['executionArn']
        print(f"Execution ARN: {execution_arn}")

        # Poll for the Step Function execution result
        output = get_execution_output(execution_arn)
        print(output)
        return output
        
    except Exception as e:
        print(e)
        print(f"Error getting object {receipt_id} from bucket {bucket}. Make sure they exist and your bucket is in the same region as this function.")
        raise e

def get_execution_output(execution_arn):
    while True:
        # Describe the execution
        execution_response = stepfunction_client.describe_execution(executionArn=execution_arn)
        
        status = execution_response['status']
        if status == 'SUCCEEDED':
            # Return the output if execution succeeded
            return json.loads(execution_response['output'])
        elif status in ['FAILED', 'TIMED_OUT', 'ABORTED']:
            raise Exception(f"Step Function execution failed with status: {status}")

        # Wait before polling again
        time.sleep(2)