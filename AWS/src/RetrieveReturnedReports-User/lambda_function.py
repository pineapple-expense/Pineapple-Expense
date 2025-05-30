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


    #user_id = event['user_id']
    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']


    select_query = """
    SELECT rd.report_number, rd.comment,
           SUM(rct.act_amount) AS total
    FROM report_data rd
    JOIN receipt_data rct
      ON rd.report_number = rct.report_number
      AND rd.user_id = rct.user_id
    WHERE rd.user_id = %s
      AND rd.returned = true
    GROUP BY rd.report_number, rd.comment;
    """
    results = []
    connection = None
    try:
        connection = psycopg2.connect(
            host=db_host,
            user=db_user,
            port = db_port,
            password=db_password,
            dbname=db_name
    )
        with connection.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
            cursor.execute(select_query, (user_id,))
            results = cursor.fetchall()
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
        "body": json.dumps(results)
    }

