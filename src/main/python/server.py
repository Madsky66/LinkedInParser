import asyncio
import websockets
import httpx
from bs4 import BeautifulSoup
import re
import os
import json
import logging
import random
import time
from typing import Dict, Optional
from fake_useragent import UserAgent

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
USER_AGENTS = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59'
]
PROXY_LIST = [
    "51.91.237.124:8080",
    "51.91.109.83:80",
]

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
        headers = {
            'User-Agent': random.choice(USER_AGENTS),
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
            'Accept-Encoding': 'gzip, deflate, br',
            'DNT': '1',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Sec-Fetch-Dest': 'document',
            'Sec-Fetch-Mode': 'navigate',
            'Sec-Fetch-Site': 'none',
            'Sec-Fetch-User': '?1',
            'Cache-Control': 'max-age=0'
        }

        # Ajoutez un délai aléatoire
        await asyncio.sleep(random.uniform(1, 3))

        proxy = random.choice(PROXY_LIST) if PROXY_LIST else None

        async with httpx.AsyncClient(
                headers=headers,
                follow_redirects=True,
                timeout=30.0,
                proxies=proxy
        ) as client:
            response = await client.get(url)
            response.raise_for_status()

            soup = BeautifulSoup(response.text, 'html.parser')

            # Sélecteurs pour le nom
            name_selectors = [
                ('h1', {'class_': re.compile('text-heading-xlarge')}),
                ('h1', {'class_': 'top-card-layout__title'}),
                ('h1', {'class_': 'text-heading-xlarge inline t-24 v-align-middle break-words'})
            ]

            full_name = DEFAULT_NAME
            for selector in name_selectors:
                name_element = soup.find(selector[0], selector[1])
                if name_element:
                    full_name = name_element.text.strip()
                    break

            # Sélecteurs pour l'entreprise
            company_selectors = [
                ('span', {'aria-hidden': 'true'}),
                ('div', {'class_': 'experience-item__subtitle'}),
                ('span', {'class_': 'top-card-layout__company'})
            ]

            company = ""
            for selector in company_selectors:
                company_element = soup.find(selector[0], selector[1])
                if company_element:
                    company = company_element.text.strip()
                    break

            return {
                "fullName": full_name,
                "company": company
            }
    except Exception as e:
        logger.error(f"LinkedIn extraction error: {e}")
        return {"fullName": DEFAULT_NAME, "company": ""}

async def get_email_from_apollo(linkedin_url: str, company: str = "") -> str:
    """Récupère l'email depuis Apollo avec gestion du rate limiting."""
    try:
        api_url = "https://api.apollo.io/api/v1/people/match"
        headers = {
            "Authorization": f"Bearer {API_APOLLO_KEY}",
            "Content-Type": "application/json",
            "Cache-Control": "no-cache"
        }
        params = {
            "linkedin_url": linkedin_url,
            "organization_name": company,
            "reveal_personal_emails": True
        }

        # Ajoutez un délai entre les requêtes
        await asyncio.sleep(random.uniform(0.5, 1.5))

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(api_url, json=params, headers=headers)

            # Gestion du rate limiting
            if response.status_code == 429:
                retry_after = int(response.headers.get('Retry-After', 60))
                logger.warning(f"Rate limited by Apollo. Waiting {retry_after} seconds...")
                await asyncio.sleep(retry_after)
                response = await client.post(api_url, json=params, headers=headers)

            response.raise_for_status()
            data = response.json()

            email = data.get("email")
            if not email and "person" in data:
                email = data["person"].get("email")
            if not email:
                email = next((e for e in data.get("emails", []) if e), None)

            return email if email else DEFAULT_EMAIL

    except httpx.HTTPStatusError as e:
        if e.response.status_code == 401:
            logger.error("Invalid Apollo API key. Please check your credentials.")
        else:
            logger.error(f"Apollo API error: {e}")
        return DEFAULT_EMAIL
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

                if not linkedin_url or "linkedin.com" not in linkedin_url:
                    raise ValueError("Invalid LinkedIn URL")

                if linkedin_url and "linkedin.com" in linkedin_url:
                    linkedin_info = await extract_linkedin_info(linkedin_url)
                    if linkedin_info["fullName"] == DEFAULT_NAME:
                        logger.warning("Could not extract LinkedIn info")

                    email = await get_email_from_apollo(linkedin_url, linkedin_info["company"])
                    if email == DEFAULT_EMAIL:
                        logger.warning("Could not get email from Apollo")

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