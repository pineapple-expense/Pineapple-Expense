import boto3
import os
import json

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
        paginator = s3_client.get_paginator('list_objects_v2')
        page_iterator = paginator.paginate(Bucket=BUCKET_NAME)

        csv_files = []

        for page in page_iterator:
            for obj in page.get('Contents', []):
                key = obj['Key']
                if key.lower().endswith('.csv'):
                    csv_files.append({
                        'key': key,
                        'last_modified': obj['LastModified'].isoformat()
                    })

        csv_files.sort(key=lambda x: x['last_modified'], reverse=True)

        return {
            'statusCode': 200,
            'body': json.dumps({'files': csv_files}),
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            }
        }

    except Exception as e:
        print(f"Error listing CSV files: {e}")
        return {
            'statusCode': 500,
            'body': json.dumps({'error': 'Internal server error'})
        }
