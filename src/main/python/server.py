import asyncio
import websockets
import json
import httpx
from bs4 import BeautifulSoup
import re
import logging
import os

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

key_file_path = os.path.join(os.path.dirname(__file__), "extra", "apollo_key.txt")

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
            name = name_element.text.strip() if name_element else "Nom Inconnu"

            company_element = soup.find('span', {'aria-hidden': 'true'}, text=re.compile(r'.*'))
            company = company_element.text.strip() if company_element else ""

            return {
                "name": name,
                "company": company
            }
    except Exception as e:
        logger.error(f"Erreur lors de l'extraction LinkedIn: {e}")
        return {"name": "Nom Inconnu", "company": ""}

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
        logger.error(f"Erreur Apollo: {e}")
        return "email@inconnu.fr"

async def process_prospect(websocket):
    try:
        async for message in websocket:
            data = json.loads(message)
            logger.info(f"Message reçu: {data}")

            linkedin_url = data.get("linkedinURL", "")

            if linkedin_url and "linkedin.com" in linkedin_url:
                linkedin_info = await extract_linkedin_info(linkedin_url)
                email = await get_email_from_apollo(linkedin_url, linkedin_info["company"])

                data.update({
                    "name": linkedin_info["name"],
                    "email": email,
                    "status": "completed"
                })
            else:
                data.update({
                    "name": "Nom Inconnu",
                    "email": "email@inconnu.fr",
                    "status": "error"
                })

            response = json.dumps(data)
            logger.info(f"Envoi réponse: {response}")
            await websocket.send(response)

    except websockets.exceptions.ConnectionClosed:
        logger.info("Connexion WebSocket fermée normalement")
    except Exception as e:
        logger.error(f"Erreur WebSocket: {e}")
        try:
            await websocket.send(json.dumps({
                "status": "error",
                "message": str(e)
            }))
        except:
            pass

async def main():
    host = "localhost"
    port = 9000

    logger.info(f"Démarrage du serveur WebSocket sur {host}:{port}")

    async with websockets.serve(process_prospect, host, port, ping_interval=None):
        logger.info("🚀 Serveur WebSocket démarré avec succès")
        await asyncio.Future()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Arrêt du serveur")
    except Exception as e:
        logger.error(f"Erreur fatale: {e}")