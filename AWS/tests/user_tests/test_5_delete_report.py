import requests

DELETE_REPORT_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/user/DeleteReport"

def test_delete_report(auth_token):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "report_number": "TestReport123"
    }

    response = requests.delete(DELETE_REPORT_URL, headers=headers, json=payload)
    print("Delete Report Status:", response.status_code)
    print("Delete Report Body:", response.text)

    assert response.status_code == 200
