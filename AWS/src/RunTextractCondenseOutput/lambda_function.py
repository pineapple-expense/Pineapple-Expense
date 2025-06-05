import boto3
import json
import os

textract = boto3.client("textract")

def lambda_handler(event, context):
    print("Received event: ", event)

    bucket = event["bucket"]
    key = event["key"]
    user_id = event["user_id"]
    name = event["name"]
    print(bucket)
    print(key)

    # Call Textract AnalyzeExpense
    response = textract.analyze_expense(
        Document={
            "S3Object": {
                "Bucket": bucket,
                "Name": key
            }
        }
    )

    condensed_extract = {}
    receipt_id = key  # You use `key` as the ID elsewhere

    try:
        summary_fields = response['ExpenseDocuments'][0]['SummaryFields']
        for field in summary_fields:
            k = field['Type']['Text']
            v = field.get('ValueDetection', {}).get('Text', '')

            if k not in condensed_extract:
                condensed_extract[k] = v
            else:
                condensed_extract[k] += " " + v

        line_items = response['ExpenseDocuments'][0].get('LineItemGroups', [{}])[0].get('LineItems', [])
        if line_items:
            condensed_extract['items'] = {}
            for j, item in enumerate(line_items[0].get('LineItemExpenseFields', [])):
                val = item.get('ValueDetection', {}).get('Text', '')
                condensed_extract['items'][f'item{j}'] = val

    except Exception as e:
        print("Error while condensing Textract output:", e)

    if not condensed_extract:
        return {
            'key': receipt_id,
            'user_id': user_id,
            'name': name,
            'empty': 'empty'
        }

    return {
        'key': receipt_id,
        'user_id': user_id,
        'name': name,
        'condensed_extract': condensed_extract
    }
