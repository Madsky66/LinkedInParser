import json
import time

def read_json(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return json.load(file)
    except Exception as e:
        print(f"❌ Erreur de lecture JSON : {e}")
        return {"status": "error"}


def write_json(data, file_path):
    with open(file_path, 'w', encoding='utf-8') as file:
        json.dump(data, file, indent=4)

def process_prospect(file_path):
    print("📥 Lecture des données depuis Kotlin...")
    data = read_json(file_path)

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
