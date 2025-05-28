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

    # Extract user_id from JWT claims
    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']

    # Parse request body
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
    if not report_number:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Missing report_number in request"})
        }

    # SQL Queries
    detach_query = """
    UPDATE receipt_data rd
    SET report_number = NULL
    FROM report_data r
    WHERE rd.report_number = r.report_number
    AND rd.user_id = r.user_id
    AND r.report_number = %s
    AND r.user_id = %s;
    """

    delete_query = """
    DELETE FROM report_data
    WHERE report_number = %s
    AND user_id = %s;
    """

    connection = None
    try:
        # Connect to PostgreSQL
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            dbname=db_name
        )
        cursor = connection.cursor()

        # Step 1: Detach receipts
        cursor.execute(detach_query, (report_number, user_id))
        receipts_detached = cursor.rowcount  # Number of receipts detached

        # Step 2: Delete report
        cursor.execute(delete_query, (report_number, user_id))
        reports_deleted = cursor.rowcount  # Should be 1 if report was deleted

        connection.commit()

        if reports_deleted == 0:
            message = "No matching report found to delete."
        else:
            message = f"Successfully deleted report {report_number} and detached {receipts_detached} receipt(s)."

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
