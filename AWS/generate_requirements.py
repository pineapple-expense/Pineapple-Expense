import os

def create_requirements_for_all_functions(base_path="src"):
    for root, dirs, files in os.walk(base_path):
        if "lambda_function.py" in files:
            req_path = os.path.join(root, "requirements.txt")
            if not os.path.exists(req_path):
                with open(req_path, "w") as f:
                    f.write("# Add required packages here\n")
                print(f"✅ Created: {req_path}")
            else:
                print(f"ℹ️ Already exists: {req_path}")

if __name__ == "__main__":
    create_requirements_for_all_functions()
