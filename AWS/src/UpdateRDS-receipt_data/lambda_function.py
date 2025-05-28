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

    receipt_id = event['receipt_id']
    act_amount = event["amount"]
    act_date_str = event["date"]
    act_category = event['category']
    comment = event['comment']
    act_date = datetime.datetime.strptime(act_date_str, "%m/%d/%Y").date() if act_date_str else None


    update_query = """
    UPDATE receipt_data
    SET act_amount = %s,
    act_date = %s,
    act_category = %s,
    comment = %s
    WHERE receipt_id = %s;
    """
    
    connection = None
    try:
        # Connect to PostgreSQL database
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database = db_name
        )
        
        cursor = connection.cursor()


        cursor.execute(update_query, (act_amount, act_date, act_category, comment, receipt_id))

        connection.commit()

        print(f"Successfully updated receipt {receipt_id} in receipt_data")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        if connection:
            connection.close()

    return event
