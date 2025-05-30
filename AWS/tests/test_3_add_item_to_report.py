import requests
import pytest

API_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/user/AttachReceiptToCurrentReport"

@pytest.mark.dependency(name="add_items", depends=["create_report"],scope="session")
def test_add_receipts_to_current_report(auth_token):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }

    for receipt_id in ["TestImage1.jpg", "TestImage2.jpg"]:
        payload = {
            "receipt_id": receipt_id
        }

        response = requests.patch(API_URL, headers=headers, json=payload)
        print(f"Add Receipt {receipt_id} Status:", response.status_code)
        print(f"Add Receipt {receipt_id} Body:", response.text)

        assert response.status_code == 200
