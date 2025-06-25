import json
import psycopg2
import psycopg2.extras
import os
import boto3

CACHED_CREDENTIALS = None

def get_credentials(parameter_store="/pineapple_expense/db/credentials"):
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

    report_number = body_json.get("report_number")



    query = """
    SELECT * 
    FROM receipt_data
    WHERE report_number = %s;
    """

    connection = None
    try:
        connection = psycopg2.connect(
            host=db_host,
            user=db_user,
            port=db_port,
            password=db_password,
            dbname=db_name
        )
        with connection.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
            cursor.execute(query,(report_number,))
            receipts = cursor.fetchall()

        return {
            "statusCode": 200,
            "body": json.dumps({"receipts": receipts}, default=str)
        }

    except Exception as e:
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }
    finally:
        if connection:
            connection.close()
