import json

def lambda_handler(event, context):
    print("received event: ", event)

    condensed_extract = {}
    receipt_id = event['key']
    user_id = event['user_id']
    name = event['name']
    textract = event['textract_response']
 
    for i in range(len(textract['ExpenseDocuments'][0]['SummaryFields'])):
        key = textract['ExpenseDocuments'][0]['SummaryFields'][i]['Type']['Text']
        value = textract['ExpenseDocuments'][0]['SummaryFields'][i]['ValueDetection']['Text']
        
        if key not in condensed_extract.keys():
                condensed_extract[key] = value

        else:
            temp = " " + value
            condensed_extract[key] += temp
        
        if len(textract['ExpenseDocuments'][0]['LineItemGroups'][0]['LineItems'])> 0:
            condensed_extract['items'] = {}
            for j in range(len(textract['ExpenseDocuments'][0]['LineItemGroups'][0]['LineItems'][0]['LineItemExpenseFields'])):
                value = textract['ExpenseDocuments'][0]['LineItemGroups'][0]['LineItems'][0]['LineItemExpenseFields'][j]['ValueDetection']['Text']
                condensed_extract['items']['item'+str(j)] = value
    
    if len(condensed_extract) == 0:
        return {
            'key': receipt_id,
            'user_id':user_id,
            "name": name,
            'empty':'empty'
        }

    output = {
        'key':receipt_id,
        'user_id':user_id,
        'name': name,
        'condensed_extract': condensed_extract
    }
    return output