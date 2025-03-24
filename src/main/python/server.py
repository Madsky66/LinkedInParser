import os
import json
import logging
import asyncio
import websockets
import undetected_chromedriver as uc
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class LinkedInScraper:
    def __init__(self):
        self.driver = None
        self.setup_selenium()

    def setup_selenium(self):
        """Configure Selenium avec undetected-chromedriver et démarre un navigateur"""
        try:
            chrome_path = os.environ.get('CHROME_PATH', os.path.join(
                os.getcwd(),
                "src",
                "main",
                "resources",
                "extra",
                "chrome"
            ))

            # Création du dossier temporaire pour Chrome
            temp_dir = os.path.join(chrome_path, "temp")
            os.makedirs(temp_dir, exist_ok=True)

            options = uc.ChromeOptions()
            options.binary_location = os.path.join(chrome_path, "chrome.exe")
            options.add_argument(f'--user-data-dir={temp_dir}')
            options.add_argument('--no-sandbox')
            options.add_argument('--disable-dev-shm-usage')
            options.add_argument('--disable-blink-features=AutomationControlled')
            options.add_argument('--start-maximized')
            options.add_argument('--disable-gpu')
            options.add_argument('--no-first-run')
            options.add_argument('--no-default-browser-check')
            options.add_argument('--disable-extensions')
            options.add_argument('--disable-popup-blocking')
            options.add_argument('--disable-notifications')

            # Désactiver les images pour accélérer le chargement
            prefs = {
                "profile.managed_default_content_settings.images": 2,
                "profile.default_content_setting_values.notifications": 2
            }
            options.add_experimental_option("prefs", prefs)

            self.driver = uc.Chrome(options=options)
            logger.info("✅ Navigateur Selenium démarré avec succès")

            # Accélérer le chargement de la page de connexion
            self.driver.set_page_load_timeout(30)
            self.driver.get("https://www.linkedin.com/login")
            logger.info("🔗 Accès à la page de connexion LinkedIn")

            # Attendre que la page soit chargée avec un timeout plus court
            try:
                WebDriverWait(self.driver, 30).until(
                    EC.presence_of_element_located((By.ID, "username"))
                )
                logger.info("✅ Page de connexion LinkedIn chargée")
            except TimeoutException:
                logger.warning("⚠️ Timeout lors du chargement de la page de connexion")

        except Exception as e:
            logger.error(f"❌ Erreur lors du démarrage de Selenium : {e}")
            if self.driver:
                self.driver.quit()
                self.driver = None
            raise

    def parse_profile_info(self, url):
        """Extrait les informations du profil LinkedIn"""
        try:
            if not self.driver:
                self.setup_selenium()

            logger.info(f"🔍 Analyse du profil: {url}")
            self.driver.get(url)

            # Attendre que le profil soit chargé (max 15 secondes)
            try:
                name_element = WebDriverWait(self.driver, 15).until(
                    EC.presence_of_element_located((
                        By.CSS_SELECTOR,
                        "h1.inline.t-24"
                    ))
                )
                full_name = name_element.text.strip()
            except TimeoutException:
                logger.warning("⚠️ Timeout lors du chargement du profil")
                return {
                    "status": "error",
                    "error": "Timeout lors du chargement du profil"
                }

            # Extraire prénom/nom
            names = full_name.split(' ', 1)
            first_name = names[0]
            last_name = names[1] if len(names) > 1 else ""

            # Extraire l'entreprise actuelle
            company = ""
            try:
                company_element = self.driver.find_element(
                    By.CSS_SELECTOR,
                    "[aria-label*='Current company']"
                )
                company = company_element.text.strip()
            except NoSuchElementException:
                try:
                    # Alternative selector
                    company_elements = self.driver.find_elements(
                        By.CSS_SELECTOR,
                        ".pv-text-details__right-panel .inline-show-more-text"
                    )
                    if company_elements:
                        company = company_elements[0].text.strip()
                except:
                    logger.warning("⚠️ Entreprise non trouvée")

            # Extraire le poste actuel
            position = ""
            try:
                position_element = self.driver.find_element(
                    By.CSS_SELECTOR,
                    ".pv-text-details__right-panel .text-body-medium"
                )
                position = position_element.text.strip()
            except NoSuchElementException:
                try:
                    # Alternative selector
                    position_elements = self.driver.find_elements(
                        By.CSS_SELECTOR,
                        ".pv-text-details__left-panel .text-body-medium"
                    )
                    if position_elements:
                        position = position_elements[0].text.strip()
                except:
                    logger.warning("⚠️ Poste non trouvé")

            logger.info(f"✅ Profil analysé: {full_name}")
            return {
                "linkedinURL": url,
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
                "linkedinURL": url,
                "status": "error",
                "error": str(e)
            }

async def websocket_handler(websocket, path):
    """Gère les connexions WebSocket"""
    logger.info("🔌 Nouvelle connexion WebSocket")
    scraper = LinkedInScraper()

    try:
        async for message in websocket:
            try:
                data = json.loads(message)
                logger.info(f"📩 Message reçu: {data}")

                if "linkedinURL" in data and data.get("status") == "request":
                    url = data["linkedinURL"]
                    logger.info(f"🔍 Requête d'analyse pour: {url}")

                    # Parser le profil
                    result = scraper.parse_profile_info(url)

                    # Envoyer le résultat
                    await websocket.send(json.dumps(result))
                    logger.info(f"📤 Résultat envoyé pour: {url}")

            except json.JSONDecodeError:
                logger.error(f"❌ Format JSON invalide: {message}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": "Format de message invalide"
                }))
            except Exception as e:
                logger.error(f"❌ Erreur de traitement: {e}")
                await websocket.send(json.dumps({
                    "status": "error",
                    "error": str(e)
                }))
    except websockets.exceptions.ConnectionClosed as e:
        logger.info(f"🔌 Connexion WebSocket fermée: {e}")
    finally:
        if hasattr(scraper, 'driver') and scraper.driver:
            logger.info("🧹 Nettoyage du driver Selenium")
            scraper.driver.quit()

async def start_server():
    """Démarre le serveur WebSocket sur un port disponible"""
    port = 9000
    max_attempts = 10
    for attempt in range(max_attempts):
        try:
            server = await websockets.serve(
                websocket_handler,
                "127.0.0.1",
                port,
                ping_interval=30,  # Ping toutes les 30 secondes pour maintenir la connexion
                ping_timeout=10    # Timeout de 10 secondes pour les pings
            )
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
    try:
        server = await start_server()
        await server.wait_closed()
    except Exception as e:
        logger.error(f"❌ Erreur critique du serveur: {e}")
        raise

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("🛑 Arrêt du serveur par l'utilisateur")
    except Exception as e:
        logger.error(f"❌ Erreur fatale: {e}")