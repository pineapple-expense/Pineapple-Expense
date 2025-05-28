import json

def lambda_handler(event, context):
    
    return {
  "predicted_category": "Meals",
  "predicted_date": {
    "full_date": "01/01/1899",
    "month": "01",
    "year": "1899",
    "day": "01"
  },
  "predicted_amount": "0.00"
}