import json

def lambda_handler(event, context):
    receipt_extract = event["condensed_extract"]

    prompt = '''<s>[INST] <<SYS>>
    You are an expert in receipt categorization. Categorize the following receipt into one of these categories: Meals, Supplies, Safety, Travel, Lodging, or Other. 

    These are the definitions of each category with examples:
    Meals: Expenses for food and drinks (e.g., restaurant bills, coffee shop receipts).
    Supplies: Purchases for office or work-related materials (e.g., stationery, printer ink, electronics).
    Safety: Expenses related to safety equipment or services (e.g., gloves, helmets, fire extinguishers).
    Travel: Expenses for transportation (e.g., airfare, train tickets, taxi fares, gas, car rentals).
    Lodging: Accommodation expenses (e.g., hotel bills, Airbnb receipts).
    Other: Any expense that does not fit the above categories.
    
    Do not include explanations, steps, or any additional text.
    If you do not know, pick a category at random.
    Respond strictly in the format: Category:<category>

    <</SYS>>

    Receipt:

    '''
    for key in receipt_extract.keys():
        if key == 'items':
            prompt+=key +":\n"
            for k in receipt_extract['items'].keys():
                prompt+= k + ":" + receipt_extract['items'][k].replace('\n',' ') +'\n'
        else:
            prompt += key +":"+receipt_extract[key]+"\n"

    prompt+= '''[/INST] What category does this receipt belong to? </s>'''
    
  
    event['prompt'] = prompt

    return event