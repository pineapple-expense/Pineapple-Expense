# Pineapple Expense
#### A sweeter way to manage your business expenses

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Design Details](#design-details)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

This project is intended to produce a system which can scan, store, and classify receipts in order to produce expense reports for accounting systems.

## Features

- Automatically read and categorize your receipts
- Stores all your receipts in the cloud for record-keeping and compliance
- Exports .csv files to integrate with your existing accounting system

## Design Details

This project has two main components: The user-facing android frontend, and the backend server running on AWS.

The android app is built in kotlin using jetpack compose.

![Architecture diagram](https://github.com/ximixu/Pineapple-Expense/blob/main/SDD.jpeg)

### Example Code:
For our app we're using [Jetpack Compose](https://developer.android.com/compose), Google's native android UI framework:
```kotlin
@Composable
fun commentBox(initialText: String? = null): String {
    var comment by remember { mutableStateOf(initialText ?: "") }
    TextField(
        value = comment,
        onValueChange = {comment = it},
        label = {Text("Comment")},
        trailingIcon = {
            if (comment.isNotEmpty()) {
                IconButton(onClick = { comment = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                }
            }
        },
        modifier = Modifier.width(400.dp).height(120.dp)
    )
    return comment
}
```

Our backend is mostly compromised of lambda functions on AWS;
```python
S3_BUCKET_NAME = os.getenv("BUCKET")

s3_client = boto3.client("s3")

def lambda_handler(event, context):
    role = event['requestContext']['authorizer']['jwt']['claims']['roleType']
    if role != 'Admin':
        return {
            "statusCode": 403,
            "body": json.dumps({"error": "Access denied. Requires admin only."})
        }
    
    try:
        
        print("Received event:", json.dumps(event, indent=2))

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
```

# Installation steps
To install the app, the .apk file can be downloaded under [releases](https://github.com/pineapple-expense/Pineapple-Expense/releases)

To set up a development environment for the app, clone this repository using git
```bash
git clone git@github.com:pineapple-expense/Pineapple-Expense.git
```
Then install [Android Studio](https://developer.android.com/studio) and open the "Android" folder in the repository as a project.

An AWS CloudFormation template along with our step functions and lambdas are in the "AWS" folder, but setup is beyond the scope of this document.

## Contributing
Due to backend costs there's a decent chance that the app won't actually be up by the time you're reading this, but feel free to fork this project :)

## License
 Pineapple Expense  © 2024 by Pineapple Expense is licensed under CC BY-NC-SA 4.0. To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/
