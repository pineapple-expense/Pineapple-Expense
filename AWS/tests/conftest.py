import pytest
import requests
import os
import json

@pytest.fixture(scope="session")
def auth_token():
    """Retrieve a fresh Auth0 token for the test session."""
    resp = requests.post(f"https://{os.environ['AUTH0_DOMAIN']}/oauth/token", json={
        "grant_type": "password",
        "username": os.environ["AUTH0_TEST_USERNAME"],
        "password": os.environ["AUTH0_TEST_PASSWORD"],
        "audience": os.environ["AUTH0_AUDIENCE"],
        "client_id": os.environ["AUTH0_CLIENT_ID"],
        "client_secret": os.environ["AUTH0_CLIENT_SECRET"],
        "scope": "openid"
    })
    resp.raise_for_status()
    return resp.json()["access_token"]

def upload_image_to_s3(auth_token, file_name):
    """Call the app to get a presigned URL and upload an image to S3."""
    content_type = "image/jpeg"
    presign_url = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/s3-presigned-url"

    # Step 1: Get the presigned URL
    response = requests.post(
        presign_url,
        headers={
            "Authorization": f"Bearer {auth_token}",
            "Content-Type": "application/json"
        },
        json={"fileName": file_name, "contentType": content_type}
    )
    assert response.status_code == 200
    presigned_url = response.json()["presignedUrl"]

    # Step 2: Upload the file
    with open(f"AWS/tests/{file_name}", "rb") as f:
        upload_response = requests.put(
            presigned_url,
            data=f,
            headers={"Content-Type": content_type}
        )
    assert upload_response.status_code == 200

@pytest.fixture(scope="session")
def receipt_image_1(auth_token):
    """Upload TestImage1.jpg to S3 and return the file name."""
    file_name = "TestImage1.jpg"
    upload_image_to_s3(auth_token, file_name)
    return file_name

@pytest.fixture(scope="session")
def receipt_image_2(auth_token):
    """Upload TestImage2.jpg to S3 and return the file name."""
    file_name = "TestImage2.jpg"
    upload_image_to_s3(auth_token, file_name)
    return file_name
