import os
import json
import logging
import asyncio
import websockets
import undetected_chromedriver as uc

# Configuration du logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class LinkedInScraper:
    def __init__(self):
        self.driver = None

    def initialize_driver(self):
        chrome_path = os.environ.get("CHROME_PATH")
        options = uc.ChromeOptions()
        options.add_argument(f"--user-data-dir={chrome_path}")
        options.add_argument("--start-maximized")
        options.headless = False
        self.driver = uc.Chrome(options=options)

    def parse_profile_info(self, url):
        """Extrait les informations du profil LinkedIn"""
        try:
            if not self.driver:
                self.initialize_driver()

            self.driver.get(url)

            # Attendre que le nom soit chargé
            name_element = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((
                    By.CSS_SELECTOR,
                    "h1.inline.t-24"
                ))
            )
            full_name = name_element.text.strip()

            # Extraire prénom/nom
            names = full_name.split(' ', 1)
            first_name = names[0]
            last_name = names[1] if len(names) > 1 else ""

            # Extraire l'entreprise actuelle
            try:
                company = self.driver.find_element(
                    By.CSS_SELECTOR,
                    "[aria-label*='Current company']"
                ).text.strip()
            except:
                company = ""

            # Extraire le poste actuel
            try:
                position = self.driver.find_element(
                    By.CSS_SELECTOR,
                    ".pv-text-details__right-panel .text-body-medium"
                ).text.strip()
            except:
                position = ""

            return {
                "fullName": full_name,
                "firstName": first_name,
                "lastName": last_name,
                "company": company,
                "position": position,
                "status": "completed"
            }

        except Exception as e:
            logger.error(f"❌ Erreur lors du parsing: {e}")
            return {
                "status": "error",
                "error": str(e)
            }
        finally:
            if self.driver:
                self.driver.quit()
                self.driver = None

async def websocket_handler(websocket, path):
    """Gère les connexions WebSocket"""
    logger.info("🔌 Nouvelle connexion WebSocket")
    scraper = LinkedInScraper()

    try:
        async for message in websocket:
            try:
                data = json.loads(message)
                if "linkedinURL" in data:
                    # Initialiser le driver Chrome
                    chrome_path = os.environ.get("CHROME_PATH")
                    options = uc.ChromeOptions()
                    options.add_argument(f"--user-data-dir={chrome_path}")
                    scraper.driver = uc.Chrome(options=options)

                    # Naviguer vers l'URL
                    scraper.driver.get(data["linkedinURL"])

                    # Parser le profil
                    result = scraper.parse_profile_info()
                    result["linkedinURL"] = data["linkedinURL"]

                    # Envoyer le résultat
                    await websocket.send(json.dumps(result))

                    # Fermer le navigateur
                    scraper.driver.quit()
            except Exception as e:
                logger.error(f"❌ Erreur de traitement: {e}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": str(e)
                }))
    except websockets.exceptions.ConnectionClosed:
        logger.info("🔌 Connexion WebSocket fermée")
    finally:
        if hasattr(scraper, 'driver'):
            scraper.driver.quit()

async def start_server():
    """Démarre le serveur WebSocket sur un port disponible"""
    port = 9000
    max_attempts = 10
    for attempt in range(max_attempts):
        try:
            server = await websockets.serve(websocket_handler, "127.0.0.1", port)
            logger.info(f"🚀 Serveur WebSocket démarré sur ws://127.0.0.1:{port}")

            # Écrire le port utilisé dans un fichier pour que l'application Kotlin puisse le lire
            with open("websocket_port.txt", "w") as f:
                f.write(str(port))

            return server
        except OSError as e:
            logger.warning(f"Port {port} déjà utilisé, tentative avec le port {port + 1}")
            port += 1
    raise RuntimeError(f"Impossible de démarrer le serveur après {max_attempts} tentatives")

async def main():
    server = await start_server()
    await server.wait_closed()

if __name__ == "__main__":
    asyncio.run(main())