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
import uuid

# Configuration du logging
logging.basicConfig(
    level=logging.DEBUG,
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
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
    "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1"
]
PROXY_LIST = [
    "https://51.91.237.124:8080",
    "https://51.91.109.83:80",
]

async def extract_linkedin_info(url: str) -> Dict[str, str]:
    """Extrait les informations depuis LinkedIn."""
    try:
        # Rotation des User-Agents
        user_agent = random.choice(USER_AGENTS)

        # Délai aléatoire pour éviter la détection
        await asyncio.sleep(random.uniform(2, 5))

        headers = {
            'User-Agent': user_agent,
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
            'Referer': 'https://www.google.com/',
            'DNT': '1',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Cache-Control': 'max-age=0'
        }

        # Cookies pour simuler une session
        cookies = {
            'li_at': '',  # Idéalement, un cookie de session valide
            'JSESSIONID': f'"ajax:{random.randint(100000, 999999)}"',
            'bcookie': f'"v=2&{uuid.uuid4()}"'
        }

        async with httpx.AsyncClient(
                headers=headers,
                cookies=cookies,
                follow_redirects=True,
                timeout=30.0,
                verify=False  # Désactiver la vérification SSL peut aider
        ) as client:
            response = await client.get(url)

            if response.status_code == 999:
                logger.error("LinkedIn is blocking our request (Error 999)")

            response.raise_for_status()

    except httpx.TimeoutException:
        logger.error("Request timed out while accessing LinkedIn")
        return {"fullName": DEFAULT_NAME, "company": ""}
    except httpx.HTTPStatusError as e:
        logger.error(f"HTTP error occurred: {e.response.status_code} - {e.response.text}")
        return {"fullName": DEFAULT_NAME, "company": ""}
    except Exception as e:
        logger.error(f"LinkedIn extraction error: {str(e)}", exc_info=True)
        return {"fullName": DEFAULT_NAME, "company": ""}

def load_api_key() -> str:
    """Charge la clé API depuis le fichier."""
    try:
        if not os.path.exists(key_file_path):
            raise FileNotFoundError(f"Key file not found at {key_file_path}")

        with open(key_file_path, "r") as key_file:
            api_key = key_file.read().strip()

            if not api_key or len(api_key) < 20:
                raise ValueError("Invalid API key format")

            logger.info("API key loaded successfully")
            return api_key
    except Exception as e:
        logger.error(f"Error loading API key: {e}")
        raise

async def validate_apollo_api():
    """Valide la clé API Apollo au démarrage."""
    try:
        api_url = "https://api.apollo.io/v1/auth/health"
        headers = {"Authorization": f"Bearer {API_APOLLO_KEY}"}

        async with httpx.AsyncClient() as client:
            response = await client.get(api_url, headers=headers)

            if response.status_code == 200:
                logger.info("Apollo API key validated successfully")
                return True
            else:
                logger.error(f"Apollo API key validation failed: {response.status_code}")
                return False

    except Exception as e:
        logger.error(f"Apollo API validation error: {e}")
        return False

async def get_email_from_apollo(linkedin_url: str, company: str = "") -> str:
    """Récupère l'email depuis Apollo."""
    try:
        api_url = "https://api.apollo.io/api/v1/people/match"

        # Vérification de la clé API
        if not API_APOLLO_KEY or len(API_APOLLO_KEY) < 20:
            logger.error("Invalid or missing Apollo API key")
            return DEFAULT_EMAIL

        headers = {
            "Authorization": f"Bearer {API_APOLLO_KEY}",
            "Content-Type": "application/json",
            "Cache-Control": "no-cache"
        }

        params = {
            "api_key": API_APOLLO_KEY,
            "linkedin_url": linkedin_url,
            "organization_name": company,
            "reveal_personal_emails": True
        }

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(api_url, json=params, headers=headers)

            if response.status_code != 200:
                logger.error(f"Apollo API error: {response.status_code} - {response.text}")
                return DEFAULT_EMAIL

            data = response.json()

            # Log plus détaillé de la réponse
            logger.debug(f"Apollo API success response: {data}")

            if "person" in data and data["person"]:
                return data["person"].get("email", DEFAULT_EMAIL)

            return DEFAULT_EMAIL

    except Exception as e:
        logger.error(f"Apollo API error: {str(e)}", exc_info=True)
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
    try:
        # Validation de l'API Apollo
        if not await validate_apollo_api():
            logger.error("Failed to validate Apollo API key. Exiting...")
            return

        logger.info(f"Starting WebSocket server on {WEBSOCKET_HOST}:{WEBSOCKET_PORT}")
        async with websockets.serve(process_prospect, WEBSOCKET_HOST, WEBSOCKET_PORT, ping_interval=None):
            logger.info("🚀 WebSocket server started successfully")
            await asyncio.Future()

    except Exception as e:
        logger.error(f"Server startup error: {e}")
        raise

if __name__ == "__main__":
    try:
        API_APOLLO_KEY = load_api_key()
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Server shutdown requested")
    except Exception as e:
        logger.error(f"Fatal error: {e}")