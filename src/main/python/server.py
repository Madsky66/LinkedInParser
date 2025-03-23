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
        self.scraper = cloudscraper.create_scraper(
            browser={'browser': 'chrome', 'platform': 'windows', 'mobile': False}
        )
        self.setup_selenium()

    def setup_selenium(self):
        """Configure Selenium avec undetected-chromedriver"""
        options = uc.ChromeOptions()
        options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        self.driver = uc.Chrome(options=options)

    async def extract_linkedin_info(self, url: str) -> Dict[str, str]:
        """Extraction avec plusieurs méthodes"""
        methods = [
            self.extract_with_cloudscraper,
            self.extract_with_selenium,
            self.extract_with_httpx
        ]

        for method in methods:
            try:
                result = await method(url)
                if result["fullName"] != "Nom Inconnu":
                    return result
                await asyncio.sleep(random.uniform(2, 4))
            except Exception as e:
                logger.error(f"Méthode {method.__name__} échouée: {e}")
                continue

        return {"fullName": "Nom Inconnu", "company": ""}

    async def extract_with_cloudscraper(self, url: str) -> Dict[str, str]:
        """Extraction avec cloudscraper"""
        response = self.scraper.get(
            url,
            headers=self.get_random_headers(),
            proxies=self.get_random_proxy()
        )
        return self.parse_linkedin_content(response.text)

    async def extract_with_selenium(self, url: str) -> Dict[str, str]:
        """Extraction avec Selenium"""
        try:
            self.driver.get(url)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.TAG_NAME, "body"))
            )

            name_element = WebDriverWait(self.driver, 5).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, 'h1.text-heading-xlarge'))
            )
            company_element = self.driver.find_element(By.CSS_SELECTOR, '.experience-section .pv-entity__secondary-title')

            return {
                "fullName": name_element.text.strip(),
                "company": company_element.text.strip() if company_element else ""
            }
        except Exception as e:
            logger.error(f"Erreur Selenium: {e}")
            raise

    async def extract_with_httpx(self, url: str) -> Dict[str, str]:
        """Extraction avec HTTPX"""
        headers = self.get_random_headers()
        proxy = self.get_random_proxy()

        async with httpx.AsyncClient(
                headers=headers,
                proxies=proxy,
                follow_redirects=True,
                timeout=30.0
        ) as client:
            response = await client.get(url)
            return self.parse_linkedin_content(response.text)

    def get_random_headers(self) -> Dict[str, str]:
        """Génère des headers aléatoires"""
        ua = UserAgent()
        return {
            'User-Agent': ua.random,
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': f"{random.choice(['en-US', 'fr-FR', 'en-GB'])},en;q=0.5",
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

    def get_random_proxy(self) -> Dict[str, str]:
        """Retourne un proxy aléatoire"""
        proxies = [

        ]
        proxy = random.choice(proxies)
        return {"http://": proxy, "https://": proxy}

    def parse_linkedin_content(self, content: str) -> Dict[str, str]:
        """Parse le contenu LinkedIn"""
        soup = BeautifulSoup(content, 'html.parser')

        name_selectors = [
            'h1.text-heading-xlarge',
            'h1.top-card-layout__title',
            'h1[class*="name"]',
            '.profile-topcard-person-entity__name'
        ]

        company_selectors = [
            '.experience-section .pv-entity__secondary-title',
            '.experience-group .pv-entity__company-summary-info',
            '.pv-top-card-section__company',
            '.profile-topcard-person-entity__current-role'
        ]

        name = None
        for selector in name_selectors:
            element = soup.select_one(selector)
            if element:
                name = element.get_text().strip()
                break

        company = None
        for selector in company_selectors:
            element = soup.select_one(selector)
            if element:
                company = element.get_text().strip()
                break

        return {
            "fullName": name or "Nom Inconnu",
            "company": company or ""
        }

    def __del__(self):
        """Nettoyage des ressources"""
        if hasattr(self, 'driver'):
            self.driver.quit()

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