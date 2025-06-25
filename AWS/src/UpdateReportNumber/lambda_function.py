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

    receipt_id = body_json.get("receipt_id")

    if not receipt_id:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Missing receipt_id in request"})
        }

    # SQL Query to update receipt_data with report_number from report_data where current = true
    update_query = """
    UPDATE receipt_data rd
    SET report_number = r.report_number
    FROM report_data r
    WHERE rd.receipt_id = %s
    AND rd.user_id = r.user_id
    AND r.current = true;
    """

    connection = None
    try:
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            dbname=db_name
        )
        
        with connection.cursor() as cursor:
            cursor.execute(update_query, (receipt_id,))
            connection.commit()

            if cursor.rowcount == 0:
                message = "No matching report found or receipt not updated."
            else:
                message = f"Successfully updated receipt {receipt_id} with current report number."

        return {
            "statusCode": 200,
            "body": json.dumps({"message": message})
        }

    except Exception as e:
        if connection:
            connection.rollback()
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }

    finally:
        if connection:
            connection.close()