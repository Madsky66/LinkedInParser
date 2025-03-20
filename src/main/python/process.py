import json
import time
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')
DEBUG_MODE = True

def log(message):
    if DEBUG_MODE:
        print(f"🔹 {message}")

def read_json(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
            return json.loads(content)
    except Exception as e:
        print(f"❌ Erreur de lecture JSON : {e}")
        return {"status": "error"}

def write_json(data, file_path):
    try:
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(data, file, indent=4)
    except Exception as e:
        print(f"❌ Erreur d'écriture JSON : {e}")

def process_prospect(file_path):
    if not os.path.exists(file_path):
        print(f"❌ Fichier JSON introuvable : {file_path}")
        return

    data = read_json(file_path)

    if "linkedinURL" not in data or not data["linkedinURL"]:
        print("❌ URL LinkedIn manquante ou invalide")
        return

    time.sleep(2)
    data["name"] = "John Doe"
    data["email"] = "johndoe@example.com"
    data["status"] = "completed"

    print("📤 Enregistrement des résultats...")
    write_json(data, file_path)
    print("✅ Traitement terminé !")

if __name__ == "__main__":
    json_file = "src/main/data/data.json"
    process_prospect(json_file)
