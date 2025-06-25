import json
import psycopg2
import os
import datetime
import boto3

CACHED_CREDENTIALS = None

def get_credentials(parameter_store = "/pineapple_expense/db/credentials"):
    global CACHED_CREDENTIALS
    if CACHED_CREDENTIALS:
        return CACHED_CREDENTIALS
    
    ssm = boto3.client("ssm")
    response = ssm.get_parameters(
        Names=[parameter_store],
        WithDecryption=True
    )

    CACHED_CREDENTIALS = json.loads(response["Parameters"][0]["Value"])
    return CACHED_CREDENTIALS

def lambda_handler(event, context):
    credentials = get_credentials()

    db_host = credentials["DB_HOST"]
    db_port = credentials["DB_PORT"]
    db_user = credentials["DB_USER"]
    db_password = credentials["DB_PASSWORD"]   
    db_name = credentials["DB_NAME"]    

    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']

    body = event.get("body")
    if not body:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Missing request body"})
        }

    try:
        body_json = json.loads(body)
    except json.JSONDecodeError:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Invalid JSON format"})
        }

    receipt_id = body_json.get("receipt_id")
    act_amount = body_json.get("amount")
    act_date_str =  body_json.get("date")
    act_category = body_json.get('category')
    comment = body_json.get('comment')
    name = body_json.get('name')
    
    act_date = datetime.datetime.strptime(act_date_str, "%m/%d/%Y").date() if act_date_str else None


    insert_query = """
    INSERT INTO receipt_data (receipt_id, user_id, act_amount, act_date, act_category, comment, name)
    VALUES (%s, %s, %s, %s, %s, %s, %s)
    """
    
    connection = None
    try:
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database = db_name
        )
        
        cursor = connection.cursor()

        cursor.execute(insert_query, (receipt_id, user_id, act_amount, act_date, act_category, comment, name))

        connection.commit()

        print(f"Successfully inserted receipt {receipt_id} in receipt_data")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        if connection:
            connection.close()

    return event
