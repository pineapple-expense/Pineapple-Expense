import requests
import uuid

API_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/user/CreateReport"

def test_create_report(auth_token):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "report_number": "TestReport123",
        "name": "GithubTestUser"
    }

    response = requests.put(API_URL, headers=headers, json=payload)
    print("Create Report Status:", response.status_code)
    print("Create Report Body:", response.text)

    assert response.status_code == 200
