import json
import boto3
import os

s3_client = boto3.client('s3')

BUCKET_NAME = os.environ.get('BUCKET')

def lambda_handler(event, context):
    
    role = event['requestContext']['authorizer']['jwt']['claims']['roleType']
    if role != 'Admin':
        return {
            "statusCode": 403,
            "body": json.dumps({"error": "Access denied. Requires admin only."})
        }

    try:
        body = event.get('body')
        if body:
            try:
                data = json.loads(body)
            except json.JSONDecodeError:
                return {
                    'statusCode': 400,
                    'body': json.dumps({'error': 'Invalid JSON in request body'})
                }
        else:
            data = {}

        csv_file = data.get('fileName')

        if not csv_file:
            return {
                'statusCode': 400,
                'body': json.dumps({'error': 'Missing receipt_id in request body'})
            }

        presigned_url = s3_client.generate_presigned_url(
            'get_object',
            Params={'Bucket': BUCKET_NAME, 'Key': csv_file},
            ExpiresIn=3600
        )

        return {
            'statusCode': 200,
            'body': json.dumps({'url': presigned_url}),
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            }
        }

    except Exception as e:
        print(f"Error generating pre-signed URL: {e}")
        return {
            'statusCode': 500,
            'body': json.dumps({'error': 'Internal server error'})
        }
