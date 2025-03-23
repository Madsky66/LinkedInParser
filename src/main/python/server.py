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
import cloudscraper
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# Configuration du logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class LinkedInScraper:
    def __init__(self):
        self.setup_selenium()

def setup_selenium(self):
    """Configure Selenium avec undetected-chromedriver et démarre un navigateur"""
    try:
        options = uc.ChromeOptions()
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--disable-blink-features=AutomationControlled')
        options.add_argument('--start-maximized')

        # Démarre le navigateur
        self.driver = uc.Chrome(options=options)
        logger.info("✅ Navigateur Selenium démarré avec succès")

        # Accède à LinkedIn
        self.driver.get("https://www.linkedin.com/login")
        logger.info("🔗 Accès à la page de connexion LinkedIn")

        # Attendez que l'utilisateur se connecte manuellement
        WebDriverWait(self.driver, 120).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "input[role='combobox']"))
        )
        logger.info("✅ Connexion LinkedIn réussie")
    except Exception as e:
        logger.error(f"❌ Erreur lors du démarrage de Selenium : {e}")
        raise

    async def extract_linkedin_info(self, url: str = None) -> Dict[str, str]:
        """Extraction depuis la page active du navigateur"""
        try:
            # Si aucune URL n'est fournie, on utilise la page active
            if not url:
                url = self.driver.current_url

            if not url or "linkedin.com" not in url:
                return {"fullName": "Nom Inconnu", "company": "", "error": "Page LinkedIn non détectée"}

            # Attente des éléments
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.TAG_NAME, "body"))
            )

            # Extraction des données
            try:
                name_element = WebDriverWait(self.driver, 5).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, 'h1.text-heading-xlarge'))
                )
                name = name_element.text.strip()
            except:
                name = "Nom Inconnu"

            try:
                company_element = self.driver.find_element(By.CSS_SELECTOR, '.experience-section .pv-entity__secondary-title')
                company = company_element.text.strip()
            except:
                company = ""

            # Extraction de l'email si visible
            try:
                email_element = self.driver.find_element(By.CSS_SELECTOR, '.pv-contact-info__contact-type.ci-email .pv-contact-info__contact-link')
                email = email_element.text.strip()
            except:
                email = ""

            return {
                "fullName": name,
                "company": company,
                "email": email,
                "status": "completed"
            }

        except Exception as e:
            logger.error(f"Erreur lors de l'extraction: {e}")
            return {"fullName": "Nom Inconnu", "company": "", "error": str(e)}

    async def get_current_profile(self) -> Dict[str, str]:
        """Récupère les informations du profil actuellement ouvert"""
        return await self.extract_linkedin_info()

# Initialisation du scraper
linkedin_scraper = LinkedInScraper()

async def process_prospect(websocket):
    """Traitement des messages WebSocket"""
    try:
        async for message in websocket:
            try:
                data = json.loads(message)
                logger.info(f"Message reçu: {data}")

                linkedin_url = data.get("linkedinURL", "")
                if not linkedin_url or "linkedin.com" not in linkedin_url:
                    raise ValueError("URL LinkedIn invalide")

                # Extraction des informations
                linkedin_info = await linkedin_scraper.extract_linkedin_info(linkedin_url)

                # Mise à jour des données
                data.update({
                    "fullName": linkedin_info["fullName"],
                    "company": linkedin_info["company"],
                    "status": "completed"
                })

                # Envoi de la réponse
                response = json.dumps(data)
                logger.info(f"Envoi réponse: {response}")
                await websocket.send(response)

            except json.JSONDecodeError as e:
                logger.error(f"Erreur JSON: {e}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": "Format JSON invalide"
                }))
            except Exception as e:
                logger.error(f"Erreur de traitement: {e}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": str(e)
                }))

    except websockets.exceptions.ConnectionClosed:
        logger.info("Connexion WebSocket fermée normalement")
    except Exception as e:
        logger.error(f"Erreur WebSocket: {e}")

async def main():
    """Point d'entrée principal"""
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