import json
import os
import boto3

S3_BUCKET_NAME = os.getenv("BUCKET")

s3_client = boto3.client("s3")

def lambda_handler(event, context):
    role = event['requestContext']['authorizer']['jwt']['claims']['roleType']
    if role != 'Admin':
        return {
            "statusCode": 403,
            "body": json.dumps({"error": "Access denied. Requires admin only."})
        }
    
    try:
        
        print("Received event:", json.dumps(event, indent=2))

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


        file_name = body_json.get("fileName")
        content_type = body_json.get("contentType", "text/csv")  

        print("Extracted fileName:", file_name)  

        if not file_name or not isinstance(file_name, str):
            return {
                "statusCode": 400,
                "body": json.dumps({"error": "Invalid or missing 'fileName'"})
            }


        presigned_url = s3_client.generate_presigned_url(
            "put_object",
            Params={
                "Bucket": S3_BUCKET_NAME,
                "Key": file_name,
                "ContentType": content_type
            },
            ExpiresIn=3600
        )

        return {
            "statusCode": 200,
            "body": json.dumps({"presignedUrl": presigned_url})
        }

    except Exception as e:
        print(f"Error generating presigned URL: {str(e)}")
        return {
            "statusCode": 500,
            "body": json.dumps({"error": "Failed to generate presigned URL", "details": str(e)})
        }
