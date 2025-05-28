import json
from datetime import datetime
import re

def lambda_handler(event, context):
    json_file = event['condensed_extract']
    key = event['key']
    user_id = event['user_id']
    name = event['name']

    if len(json_file) == 0:

        return {
            "empty" : "empty"
        }   


    if 'INVOICE_RECEIPT_DATE' in json_file.keys():
        date = extract_date_from_invoice_date_string(json_file['INVOICE_RECEIPT_DATE'].replace(',',' ').replace('.', ' ').strip())
        reformatted_date = reformat_date(date)
            
    else: 
        # If field not found, attempt to extract from full text
        date = extract_date_from_full_string(json.dumps(json_file))
 
        if date != "":
            reformatted_date = reformat_date(date)
        else:
            temp_date = datetime(1899, 1, 1)
            
            reformatted_date= { 
                "full_date" : temp_date.strftime('%m/%d/%Y'),
                "month" : temp_date.strftime("%m"),
                "year" : temp_date.strftime("%Y"),
                "day" : temp_date.strftime("%d")
            }
        
    if 'TOTAL' in json_file.keys() and extract_amt_from_string(json_file['TOTAL']) != 0.00:
        extracted_total = extract_amt_from_string(json_file['TOTAL'])
            
    elif 'AMOUNT_PAID' in json_file.keys():
        extracted_total = extract_amt_from_string(json_file['AMOUNT_PAID'])
        
    elif "SUBTOTAL" in json_file.keys():
        subtotal = extract_amt_from_string(json_file['SUBTOTAL'])
            
        try:
            tax = extract_amt_from_string(json_file['TAX'])
        except KeyError:
            tax = 0
        extracted_total = subtotal + tax
            
    else:
        extracted_total = 0
    
    

    content = {
        "key": key,
        "user_id": user_id,
        "predicted_date" : reformatted_date,
        "predicted_amount" : extracted_total,
        "condensed_extract" : json_file,
        "name": name
    }

    return content


    
            


# Function to extract an amount from a string input from Textract

def extract_amt_from_string(s):
    regex = r'\d+\.\d{2}?'
    amounts = re.findall(regex, s)
    if len(amounts) >0:
        amounts = [float(j) for j in amounts]
        amount = max(amounts)
    else:
        amount = 0
    formatted_amount = f"{amount:.2f}"

    return formatted_amount


def extract_date_from_invoice_date_string(s):
    # List of prioritized regex patterns
    regex_patterns = [
        r'\b\d{1,2}[A-Za-z]{3}\d{2}\b',             # Specific format: 22Sep24
        r'\b\d{1,2}[- ][A-Za-z]{3}[- ]\d{4}\b',     # dd-MMM-yyyy, e.g., 14-Dec-2024
        r'\b[A-Za-z]+\s+\d{1,2}\s+\d{4}\b',         # Full month name with day and year, e.g., September 4  2024
        r'\b[A-Za-z]{3}\s+\d{1,2},?\s+\d{4}\b',     # Abbreviated month name with day and year, e.g., Sep 4, 2024
        r'\b[A-Za-z]{3}\s+\d{1,2}\b',               # Abbreviated month name with day, e.g., Sep 4
        r'\b\d{4}-\d{1,2}-\d{1,2}\b',               # yyyy-mm-dd
        r'\b\d{1,2}[-/]\d{1,2}[-/]\d{2,4}\b',       # mm/dd/yy, mm/dd/yyyy, mm-dd-yy, mm-dd-yyyy
        r'\b\d{1,2}-\d{1,2}\b',                     # mm-dd
        r'\b\d{1,2}/\d{1,2}\b',                     # mm/dd
    ]
    
    # Try each regex pattern in order
    for pattern in regex_patterns:
        matches = re.findall(pattern, s)
        if matches:
            return matches[-1].strip()
    
    # Return empty string if no matches are found
    return ""

def extract_date_from_full_string(s):

    # mm/dd/yy, mm/dd/yyyy, mm-dd-yy, mm-dd-yyyy
    regex = r'\b\d{1,2}[-/]\d{1,2}[-/]\d{2}\d{2}?\b'

    matches = re.findall(regex, s)

    if matches:
  
        # Return the last match found for the current pattern
        return matches[-1].strip()
    
    # Return Other if no matches are found
    return ""

# We'll use this to convert whatever date Textract retrieved into a datetime object format m/d/yyyy.

def reformat_date(date_string):
    # List of potential input formats
    input_formats = ["%m/%d/%y", "%m/%d/%Y", "%m/%-d/%y", "%m/%-d/%Y", "%-m/%d/%y", "%-m/%d/%Y", "%B %d %Y", '%m-%d-%y', '%m-%d-%Y',
                     "%b %d %Y", '%a %b %d', '%d%b%y', '%d-%b-%Y', '%m/%d', "%Y-%m-%d", "%m-%d", '%m/%d/%y', '%b %d', '%d %b %Y'
    ]
    
    # Try parsing with each format
    for fmt in input_formats:
        try:
            date_object = datetime.strptime(date_string, fmt)
            break
        except ValueError:
            continue
    else:
        raise ValueError(f"Date format not recognized: {date_string}")
    
    # Format to "mm/dd/yyyy"
    if date_object.year == 1900:
        date_object = date_object.replace(year = 2024)

    day = date_object.strftime("%d")
    year = date_object.strftime("%Y")
    month = date_object.strftime("%m")
    date_object = date_object.strftime("%m/%d/%Y")

    json_date = {
        "full_date" : date_object,
        "month" : month,
        "year" : year,
        "day" : day
    }

    return json_date