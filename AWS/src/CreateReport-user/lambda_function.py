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



    name = body_json.get("name")
    report_number = body_json.get('report_number')


    insert_query = """
    INSERT INTO report_data (report_number, user_id, name)
    VALUES (%s, %s, %s)
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


        cursor.execute(insert_query, (report_number, user_id, name))

        connection.commit()

        print(f"Successfully inserted report {report_number} for user {user_id} into report_data")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        if connection:
            connection.close()

    return event
