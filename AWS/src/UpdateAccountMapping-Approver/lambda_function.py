import json
import psycopg2
import psycopg2.extras
import os
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

    # Checks admin role
    role = event['requestContext']['authorizer']['jwt']['claims']['roleType']
    if role != 'Admin':
        return {
            "statusCode": 403,
            "body": json.dumps({"error": "Access denied. Requires admin only."})
        }
    
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

    expense_type = body_json.get("expense_type")
    account_code = body_json.get("account_code")


    update_query = """
    UPDATE ACCOUNT_MAPPING
    SET account_code = %s
    WHERE expense_type = %s
    """

    connection = None
    try:
        connection = psycopg2.connect(
            host=db_host,
            user=db_user,
            port = db_port,
            password=db_password,
            dbname=db_name
    )
        with connection.cursor() as cursor:
            cursor.execute(update_query, (account_code, expense_type))
            connection.commit()
            rows = cursor.rowcount

    except Exception as e:
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }
    finally:
        if connection:
            connection.close()

    return {
        "statusCode": 200,
        "body": json.dumps({"message": f"Updated {rows} row(s)"})
    }
