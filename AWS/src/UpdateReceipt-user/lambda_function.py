import json
import psycopg2
import psycopg2.extras
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


    #user_id = event['user_id']
    user_id = event['requestContext']['authorizer']['jwt']['claims']['sub']

    body = event.get("body")
    if not body:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Missing request body"})
        }

    # Parse JSON safely
    try:
        body_json = json.loads(body)
    except json.JSONDecodeError:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Invalid JSON format"})
        }

    # Extract contents
    receipt_id = body_json.get("receipt_id")
    act_date_str = body_json.get("date")
    act_amount = body_json.get('amount')
    act_category = body_json.get('category')
    title = body_json.get('title')
    comment = body_json.get('comment')

    report_number = body_json.get('report_number')

    act_date = datetime.datetime.strptime(act_date_str, "%m/%d/%Y").date() if act_date_str else None

    print(receipt_id)
    update_query = """
    UPDATE receipt_data AS rd
        SET act_amount = %s,
        act_date = %s,
        act_category = %s,
        title = %s,
        comment = %s
    FROM report_data AS r
    WHERE rd.receipt_id = %s
    AND rd.user_id = %s
    AND (
        (rd.report_number = r.report_number 
            AND rd.user_id = r.user_id
            AND r.approved = false
            AND r.submitted = false)
        OR
        NOT EXISTS (
            SELECT 1 FROM report_data r2 
            WHERE r2.report_number = rd.report_number 
            AND r2.user_id = rd.user_id
        )
    );  
    """
    
    connection = None
    try:
        print("here")
        connection = psycopg2.connect(
            host=db_host,
            user=db_user,
            port = db_port,
            password=db_password,
            dbname=db_name
    )
        print("here2")
        with connection.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
            print("Running update with values:", act_amount, act_date, act_category, title, comment, receipt_id, user_id)
            
            cursor.execute(update_query, (act_amount, act_date, act_category, title, comment, receipt_id, user_id))

            connection.commit()
            # Fetch and print the updated row
            cursor.execute(
                """
                SELECT * FROM receipt_data
                WHERE receipt_id = %s AND user_id = %s
                """,
                (receipt_id, user_id)
            )
            updated_row = cursor.fetchone()
            print("Updated row:", updated_row)


            
    except Exception as e:

        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }
    
    return {
    "statusCode": 200,
    "body": json.dumps({"message": "Receipt updated successfully."})
    }