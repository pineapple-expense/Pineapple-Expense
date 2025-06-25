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

    report_number = body_json.get('report_number')

    if report_number:
        update_query = """
        UPDATE report_data rd
        SET submitted = true,
            returned = false,
            current = false
        WHERE rd.report_number = %s 
            AND rd.user_id = %s 
            AND rd.approved = false;
        """
        query_params = (report_number, user_id)
    else:
        update_query = """
        UPDATE report_data rd
        SET submitted = true,
            returned = false,
            current = false
        WHERE rd.current = true 
            AND rd.user_id = %s;
        """
        query_params = (user_id,)

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
            cursor.execute(update_query, query_params)
            connection.commit()
            return {
                "statusCode": 200,
                "body": json.dumps({"message": "Update successful"})
            }
    except Exception as e:
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }
    finally:
        if connection:
            connection.close()