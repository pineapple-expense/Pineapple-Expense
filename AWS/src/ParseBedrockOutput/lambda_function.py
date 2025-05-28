import json
import re

def lambda_handler(event, context):
    key = event['key']
    user_id = event['user_id']
    name = event['name']

    ## llame model
    generation = event['bedrock_response']['Body']['generation']

    predicted_category = parse_llama_response(generation)

    predicted_date = event['predicted_date']
    predicted_amount = event['predicted_amount']

    content = {
        "key": key,
        "user_id": user_id,
        "predicted_category" : predicted_category,
        "predicted_date" : predicted_date,
        "predicted_amount" : predicted_amount,
        "name": name
    }

    return content




def parse_llama_response(parsed_body):
    regex = r'(Meals|Supplies|Safety|Travel|Lodging|Other)'
    match = re.search(regex, parsed_body)
    print(match)
    if match is None:
        return "Meals"
    return match.group()