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

    receipt_id = event['key']
    user_id = event['user_id']
    pred_amount = event["predicted_amount"]
    pred_date_str = event["predicted_date"]['full_date']
    pred_category = event['predicted_category']
    
    pred_date = datetime.datetime.strptime(pred_date_str, "%m/%d/%Y").date() if pred_date_str else None


    insert_query = """
    INSERT INTO receipt_pred (receipt_id, user_id, pred_amount, pred_date, pred_category)
    VALUES (%s, %s, %s, %s, %s)
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

        cursor.execute(insert_query, (receipt_id, user_id, pred_amount, pred_date, pred_category))

        connection.commit()

        print(f"Successfully inserted receipt {receipt_id} for user {user_id} into receipt_pred")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        if connection:
            connection.close()

    return event
