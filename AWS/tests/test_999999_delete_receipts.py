import pytest
import requests
import boto3
import os

DELETE_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/user/DeleteReceipt"


# Used to confirm the file was deleted from S3
s3_client = boto3.client("s3", region_name=os.environ["AWS_REGION"])
bucket = os.environ["BUCKET"]

# Helper to check if a file still exists in S3
def s3_object_exists(key):
    try:
        s3_client.head_object(Bucket=bucket, Key=key)
        return True
    except s3_client.exceptions.ClientError as e:
        if e.response["ResponseMetadata"]["HTTPStatusCode"] == 404:
            return False
        raise e

@pytest.mark.dependency(name="delete_image_1", depends=["delete_report"])
def test_delete_receipt_image_1(auth_token, receipt_image_1):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }
    payload = {
        "receipt_id": receipt_image_1
    }

    response = requests.post(DELETE_URL, headers=headers, json=payload)
    print("Delete Image 1 Status:", response.status_code)
    print("Delete Image 1 Response:", response.text)

    assert response.status_code == 200
    assert not s3_object_exists(receipt_image_1)

@pytest.mark.dependency(name="delete_image_2", depends=["delete_report"])
def test_delete_receipt_image_2(auth_token, receipt_image_2):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }
    payload = {
        "receipt_id": receipt_image_2
    }

    response = requests.post(DELETE_URL, headers=headers, json=payload)
    print("Delete Image 2 Status:", response.status_code)
    print("Delete Image 2 Response:", response.text)

    assert response.status_code == 200
    assert not s3_object_exists(receipt_image_2)
