import asyncio
import websockets
import httpx
from bs4 import BeautifulSoup
import re
import os
import json
import logging
from typing import Dict, Optional

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

# Configuration des chemins
base_dir = os.path.dirname(os.path.abspath(__file__))
key_file_path = os.path.join(base_dir, "..", "resources", "extra", "apollo_key.txt")

# Configuration des constantes
DEFAULT_EMAIL = "email@inconnu.fr"
DEFAULT_NAME = "Nom Inconnu"
WEBSOCKET_HOST = "localhost"
WEBSOCKET_PORT = 9000

def load_api_key() -> str:
    """Charge la clé API depuis le fichier."""
    try:
        if not os.path.exists(key_file_path):
            raise FileNotFoundError(f"Key file not found at {key_file_path}")

        with open(key_file_path, "r") as key_file:
            api_key = key_file.read().strip()
            if not api_key:
                raise ValueError("API key file is empty")
            logger.info("API key loaded successfully")
            return api_key
    except Exception as e:
        logger.error(f"Error loading API key: {e}")
        raise

async def extract_linkedin_info(url: str) -> Dict[str, str]:
    """Extrait les informations depuis LinkedIn."""
    try:
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}

        async with httpx.AsyncClient(headers=headers, follow_redirects=True, timeout=30.0) as client:
            response = await client.get(url)
            response.raise_for_status()

            soup = BeautifulSoup(response.text, 'html.parser')

            name_element = soup.find('h1', class_=re.compile('text-heading-xlarge'))
            full_name = name_element.text.strip() if name_element else DEFAULT_NAME

            company_element = soup.find('span', {'aria-hidden': 'true'}, text=re.compile(r'.*'))
            company = company_element.text.strip() if company_element else ""

            return {
                "fullName": full_name,
                "company": company
            }
    except Exception as e:
        logger.error(f"LinkedIn extraction error: {e}")
        return {"fullName": DEFAULT_NAME, "company": ""}

async def get_email_from_apollo(linkedin_url: str, company: str = "") -> str:
    """Récupère l'email depuis Apollo."""
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

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(api_url, json=params, headers=headers)
            response.raise_for_status()
            data = response.json()

            email = data.get("email")
            if not email and "person" in data:
                email = data["person"].get("email")
            if not email:
                email = next((e for e in data.get("emails", []) if e), None)

            return email if email else DEFAULT_EMAIL

    except Exception as e:
        logger.error(f"Apollo API error: {e}")
        return DEFAULT_EMAIL

def generate_email(full_name: str, company: str) -> str:
    """Génère un email basé sur le nom et l'entreprise."""
    if not full_name or not company:
        return ""

    # Nettoyage et normalisation
    name_parts = ''.join(c.lower() for c in full_name if c.isalnum() or c.isspace()).split()
    company_clean = ''.join(c.lower() for c in company if c.isalnum())

    if not name_parts or not company_clean:
        return ""

    first_name = name_parts[0]
    last_name = name_parts[-1] if len(name_parts) > 1 else ""

    email_formats = [
        f"{first_name}.{last_name}@{company_clean}.com",
        f"{first_name[0]}{last_name}@{company_clean}.com",
        f"{first_name}@{company_clean}.com",
        f"{first_name}.{last_name}@{company_clean}.fr",
        f"{first_name[0]}{last_name}@{company_clean}.fr"
    ]

    return email_formats[0]

async def process_prospect(websocket):
    """Traite les messages WebSocket entrants."""
    try:
        async for message in websocket:
            try:
                data = json.loads(message)
                logger.info(f"Received message: {data}")

                linkedin_url = data.get("linkedinURL", "")

                if linkedin_url and "linkedin.com" in linkedin_url:
                    linkedin_info = await extract_linkedin_info(linkedin_url)
                    email = await get_email_from_apollo(linkedin_url, linkedin_info["company"])

                    generated_email = generate_email(linkedin_info["fullName"], linkedin_info["company"]) if email == DEFAULT_EMAIL else ""

                    data.update({
                        "fullName": linkedin_info["fullName"],
                        "email": email,
                        "generatedEmail": generated_email,
                        "company": linkedin_info["company"],
                        "status": "completed"
                    })
                else:
                    data.update({
                        "fullName": DEFAULT_NAME,
                        "email": DEFAULT_EMAIL,
                        "status": "error",
                        "error": "Invalid LinkedIn URL"
                    })

                response = json.dumps(data)
                logger.info(f"Sending response: {response}")
                await websocket.send(response)

            except json.JSONDecodeError as e:
                logger.error(f"JSON decode error: {e}")
                await websocket.send(json.dumps({"status": "error", "error": "Invalid JSON format"}))
            except Exception as e:
                logger.error(f"Processing error: {e}")
                await websocket.send(json.dumps({"status": "error", "error": str(e)}))

    except websockets.exceptions.ConnectionClosed:
        logger.info("WebSocket connection closed normally")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")

async def main():
    """Point d'entrée principal du serveur."""
    logger.info(f"Starting WebSocket server on {WEBSOCKET_HOST}:{WEBSOCKET_PORT}")

    async with websockets.serve(process_prospect, WEBSOCKET_HOST, WEBSOCKET_PORT, ping_interval=None):
        logger.info("🚀 WebSocket server started successfully")
        await asyncio.Future()

if __name__ == "__main__":
    try:
        # Chargement de la clé API au démarrage
        API_APOLLO_KEY = load_api_key()
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Server shutdown requested")
    except Exception as e:
        logger.error(f"Fatal error: {e}")