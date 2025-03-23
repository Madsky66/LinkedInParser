import asyncio
import websockets
import httpx
from bs4 import BeautifulSoup
import re
import os

base_dir = os.path.dirname(os.path.abspath(__file__))
key_file_path = os.path.join(base_dir, "..", "resources", "extra", "apollo_key.txt")

if not os.path.exists(key_file_path):
    raise FileNotFoundError(f"Key file not found at {key_file_path}")

try:
    with open(key_file_path, "r") as key_file:
        print("File content:", key_file.read())
except FileNotFoundError:
    print(f"File not found: {key_file_path}")
except Exception as e:
    print(f"An error occurred: {e}")

with open(key_file_path, "r") as key_file:
    API_APOLLO_KEY = key_file.read().strip()


async def extract_linkedin_info(url):
    try:
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}

        async with httpx.AsyncClient(headers=headers, follow_redirects=True) as client:
            response = await client.get(url)
            response.raise_for_status()

            soup = BeautifulSoup(response.text, 'html.parser')

            name_element = soup.find('h1', class_=re.compile('text-heading-xlarge'))
            full_name = name_element.text.strip() if name_element else "Nom Inconnu"

            company_element = soup.find('span', {'aria-hidden': 'true'}, text=re.compile(r'.*'))
            company = company_element.text.strip() if company_element else ""

            return {
                "fullName": full_name,
                "company": company
            }
    except Exception as e:
        print(f"Erreur lors de l'extraction LinkedIn: {e}")
        return {"fullName": "Nom Inconnu", "company": ""}

async def get_email_from_apollo(linkedin_url, company=""):
    try:
        api_url = "https://api.apollo.io/api/v1/people/match"
        headers = {
            "Authorization": f"Bearer {API_APOLLO_KEY}",
            "Content-Type": "application/json"
        }
        params = {
            "linkedin_url": linkedin_url,
            "organization_name": company,
            "reveal_personal_emails": True
        }

        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.post(api_url, json=params, headers=headers)
            response.raise_for_status()
            data = response.json()

            email = data.get("email")
            if not email and "person" in data:
                email = data["person"].get("email")
            if not email:
                email = next((e for e in data.get("emails", []) if e), None)

            return email if email else "email@inconnu.fr"

    except Exception as e:
        print(f"Erreur Apollo: {e}")
        return "email@inconnu.fr"

def generate_email(fullName, company):
    if not fullName or not company:
        return ""

    # Nettoyage du nom
    name = fullName.lower()
    name = ''.join(e for e in name if e.isalnum() or e.isspace())
    first_name, *last_name = name.split()
    last_name = last_name[0] if last_name else ""

    # Nettoyage du nom de l'entreprise
    company = company.lower()
    company = ''.join(e for e in company if e.isalnum())

    # Formats d'email courants
    email_formats = [
        f"{first_name}.{last_name}@{company}.com",
        f"{first_name[0]}{last_name}@{company}.com",
        f"{first_name}@{company}.com",
        f"{first_name}.{last_name}@{company}.fr",
        f"{first_name[0]}{last_name}@{company}.fr"
    ]

    return email_formats[0]

async def process_prospect(websocket):
    try:
        async for message in websocket:
            data = json.loads(message)
            print(f"Message reçu: {data}")

            linkedin_url = data.get("linkedinURL", "")

            if linkedin_url and "linkedin.com" in linkedin_url:
                linkedin_info = await extract_linkedin_info(linkedin_url)
                email = await get_email_from_apollo(linkedin_url, linkedin_info["company"])

                # Génération d'email si Apollo n'en trouve pas
                if email == "email@inconnu.fr":
                    generated_email = generate_email(linkedin_info["fullName"], linkedin_info["company"])
                else:
                    generated_email = ""

                print(f"Envoi réponse: {json.dumps(data)}")
                data.update({
                    "fullName": linkedin_info["fullName"],
                    "email": email,
                    "generatedEmail": generated_email,
                    "company": linkedin_info["company"],
                    "status": "completed"
                })
            else:
                data.update({
                    "fullName": "Nom Inconnu",
                    "email": "email@inconnu.fr",
                    "status": "error"
                })

            response = json.dumps(data)
            print(f"Envoi réponse: {response}")
            print("")
            await websocket.send(response)

    except websockets.exceptions.ConnectionClosed:
        print("Connexion WebSocket fermée normalement")
    except Exception as e:
        print(f"Erreur WebSocket: {e}")

async def main():
    host = "localhost"
    port = 9000

    print(f"Démarrage du serveur WebSocket sur {host}:{port}")

    async with websockets.serve(process_prospect, host, port, ping_interval=None):
        print("🚀 Serveur WebSocket démarré avec succès")
        await asyncio.Future()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("Arrêt du serveur")
    except Exception as e:
        print(f"Erreur fatale: {e}")