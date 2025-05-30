import requests
import pytest

API_BASE_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/predictions"
#TEST
@pytest.mark.dependency(name="predict_image_1",scope="session")
def test_prediction_on_image_1(auth_token, receipt_image_1):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }
    payload = {
        "receipt_id": receipt_image_1,
        "name": "GithubTestUser"
    }

    response = requests.post(API_BASE_URL, headers=headers, json=payload)
    print("Image 1 Prediction Status:", response.status_code)
    print("Image 1 Prediction Body:", response.text)

    assert response.status_code == 200
    body = response.json()
    assert "predicted_category" in body
    assert "predicted_date" in body
    assert "predicted_amount" in body

    assert body["predicted_category"] == "Meals"
    assert body["predicted_date"]['full_date'] == "10/30/2024"
    assert body["predicted_amount"] == '11.49'

@pytest.mark.dependency(name="predict_image_2",scope="session")
def test_prediction_on_image_2(auth_token, receipt_image_2):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }
    payload = {
        "receipt_id": receipt_image_2,
        "name": "GithubTestUser"
    }

    response = requests.post(API_BASE_URL, headers=headers, json=payload)
    print("Image 2 Prediction Status:", response.status_code)
    print("Image 2 Prediction Body:", response.text)

    assert response.status_code == 200

    body = response.json()
    
    assert "predicted_category" in body
    assert "predicted_date" in body
    assert "predicted_amount" in body

    assert body["predicted_category"] == "Meals"
    assert body["predicted_date"]['full_date'] == "01/01/1899"
    assert body["predicted_amount"] == '0.00'