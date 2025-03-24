import os
import json
import logging
import asyncio
import websockets
import undetected_chromedriver as uc
import time
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException

# Configuration du logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class LinkedInScraper:
    def __init__(self):
        self.driver = None
        self.wait_time = 10

    def initialize_driver(self):
        try:
            chrome_path = os.environ.get("CHROME_PATH")
            if not chrome_path:
                raise ValueError("CHROME_PATH n'est pas défini dans les variables d'environnement")

            options = uc.ChromeOptions()
            options.add_argument(f"--user-data-dir={chrome_path}/temp")
            options.add_argument("--start-maximized")
            options.add_argument("--disable-gpu")
            options.add_argument("--no-sandbox")
            options.add_argument("--disable-dev-shm-usage")
            options.add_argument("--disable-blink-features=AutomationControlled")
            options.add_argument("--disable-features=BlinkGenPropertyTrees")

            self.driver = uc.Chrome(options=options)
            return True
        except Exception as e:
            logger.error(f"Erreur d'initialisation du driver: {e}")
            return False

    def wait_for_element(self, selector, by=By.CSS_SELECTOR, timeout=None):
        try:
            wait_time = timeout if timeout else self.wait_time
            return WebDriverWait(self.driver, wait_time).until(
                EC.presence_of_element_located((by, selector))
            )
        except TimeoutException:
            logger.warning(f"Timeout en attendant l'élément: {selector}")
            return None

    def parse_profile_info(self, url):
        """Extrait les informations du profil LinkedIn"""
        try:
            if not self.initialize_driver():
                return {"status": "error", "error": "Échec de l'initialisation du driver"}

            self.driver.get(url)
            time.sleep(5)  # Attente pour le chargement initial

            # Vérifier si on est sur la page de login
            if "login" in self.driver.current_url:
                return {
                    "status": "error",
                    "error": "Session expirée, reconnexion nécessaire"
                }

            # Attendre et extraire le nom complet
            full_name_element = self.wait_for_element("h1.text-heading-xlarge")
            if not full_name_element:
                return {"status": "error", "error": "Impossible de trouver le nom"}

            full_name = full_name_element.text.strip()
            names = full_name.split(' ', 1)
            first_name = names[0]
            last_name = names[1] if len(names) > 1 else ""

            # Position actuelle
            position = ""
            position_element = self.wait_for_element("div.text-body-medium.break-words")
            if position_element:
                position = position_element.text.strip()

            # Entreprise
            company = ""
            company_element = self.wait_for_element("span.text-body-small.inline")
            if company_element:
                company = company_element.text.strip()

            # Email
            email = ""
            try:
                contact_info_button = self.wait_for_element("a[href*='overlay/contact-info']")
                if contact_info_button:
                    contact_info_button.click()
                    time.sleep(2)
                    email_element = self.wait_for_element("a[href^='mailto:']", timeout=5)
                    if email_element:
                        email = email_element.text.strip()
            except Exception as e:
                logger.warning(f"Impossible de récupérer l'email: {e}")

            return {
                "fullName": full_name,
                "firstName": first_name,
                "lastName": last_name,
                "company": company,
                "position": position,
                "email": email,
                "linkedinURL": url,
                "status": "completed"
            }

        except Exception as e:
            logger.error(f"Erreur lors du parsing: {e}")
            return {"status": "error", "error": str(e)}
        finally:
            if self.driver:
                try:
                    self.driver.quit()
                except Exception as e:
                    logger.error(f"Erreur lors de la fermeture du driver: {e}")
                self.driver = None

async def websocket_handler(websocket, path):
    """Gère les connexions WebSocket"""
    logger.info("🔌 Nouvelle connexion WebSocket")
    scraper = LinkedInScraper()

    try:
        async for message in websocket:
            try:
                data = json.loads(message)
                if "linkedinURL" in data and data["status"] == "request":
                    logger.info(f"📥 Traitement de l'URL: {data['linkedinURL']}")
                    result = scraper.parse_profile_info(data["linkedinURL"])
                    await websocket.send(json.dumps(result))
                    logger.info("✅ Résultat envoyé au client")
            except json.JSONDecodeError as e:
                logger.error(f"Erreur de décodage JSON: {e}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": "Format de message invalide"
                }))
            except Exception as e:
                logger.error(f"Erreur de traitement: {e}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": str(e)
                }))
    except websockets.exceptions.ConnectionClosed:
        logger.info("🔌 Connexion WebSocket fermée")
    finally:
        if hasattr(scraper, 'driver') and scraper.driver:
            scraper.driver.quit()

async def start_server():
    """Démarre le serveur WebSocket"""
    port = 9000
    max_attempts = 10

    for attempt in range(max_attempts):
        try:
            server = await websockets.serve(
                websocket_handler,  # Retiré le paramètre path
                "127.0.0.1",
                port,
                ping_interval=None
            )
            logger.info(f"🚀 Serveur WebSocket démarré sur ws://127.0.0.1:{port}")

            with open("websocket_port.txt", "w") as f:
                f.write(str(port))

            return server
        except OSError:
            logger.warning(f"Port {port} déjà utilisé, tentative avec le port {port + 1}")
            port += 1

    raise RuntimeError(f"Impossible de démarrer le serveur après {max_attempts} tentatives")

async def main():
    try:
        server = await start_server()
        await server.wait_closed()
    except Exception as e:
        logger.error(f"Erreur fatale du serveur: {e}")
        raise

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Arrêt du serveur par l'utilisateur")
    except Exception as e:
        logger.error(f"Erreur fatale: {e}")
        raise