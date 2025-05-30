import requests
import pytest

UPDATE_RECEIPT_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/user/UpdateReceipt"

@pytest.mark.dependency(name="update_receipt", depends=["add_items"])
def test_update_receipt_fields(auth_token):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "receipt_id": "TestImage2.jpg",
        "amount": 10.00,
        "date": "01/01/1899",
        "category": "Meals",
        "title": "TEST",
        "comment": "TEST",
        "report_number": "TestReport123"
    }

    response = requests.patch(UPDATE_RECEIPT_URL, headers=headers, json=payload)
    print("Update Receipt Status:", response.status_code)
    print("Update Receipt Body:", response.text)

    assert response.status_code == 200
