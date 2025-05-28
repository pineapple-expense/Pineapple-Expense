import json
import urllib.parse
import boto3
import os

print('Loading function')

s3 = boto3.client('s3')


def lambda_handler(event, context):


    bucket = os.environ.get("BUCKET")
    key = event['key']
    user_id = event['user_id']
    name = event['name']
    
    content = { 
                'bucket': bucket,
                'key': key,
                'user_id' : user_id,
                'name': name
    }
    
    return content