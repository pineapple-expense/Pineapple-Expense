import requests
import pytest

RETRIEVE_TOTAL_URL = "https://t6oydoeb76.execute-api.us-east-1.amazonaws.com/dev/user/RetreiveCurrentReport"

@pytest.mark.dependency(name="check_report", depends=["update_receipt"], scope="session")
def test_report_total_after_update(auth_token):
    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }

    response = requests.get(RETRIEVE_TOTAL_URL, headers=headers)
    print("Retrieve Report Total Status:", response.status_code)
    print("Retrieve Report Total Body:", response.text)

    assert response.status_code == 200

    reports = response.json()
    matching = next((r for r in reports if r["report_number"] == "TestReport123"), None)
    assert matching is not None, "Report TestReport123 not found"
    assert float(matching["total"]) == 21.49
