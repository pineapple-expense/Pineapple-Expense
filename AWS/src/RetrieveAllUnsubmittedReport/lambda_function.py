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

    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']

    query = """
    SELECT rd.report_number, rd.user_id, rct.receipt_id
    FROM report_data AS rd
    JOIN receipt_data AS rct ON rct.report_number = rd.report_number
    WHERE rd.user_id = %s
    AND rd.submitted = false
    AND rd.approved = false;
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
            cursor.execute(query, (user_id))
            report_numbers = cursor.fetchall()

        return {
            "statusCode": 200,
            "body": json.dumps({"report_numbers": report_numbers})
        }

    except Exception as e:
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }
    finally:
        if connection:
            connection.close()
