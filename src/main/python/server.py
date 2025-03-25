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
from selenium.common.exceptions import TimeoutException, NoSuchElementException, WebDriverException
from urllib.parse import urlparse

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class LinkedInScraper:
    def __init__(self):
        self.driver = None
        self.wait_time = 10
        self.max_retries = 3
        self.chrome_path = self._get_chrome_path()

    def _get_chrome_path(self):
        chrome_path = os.environ.get("CHROME_PATH")
        if not chrome_path:
            raise ValueError("CHROME_PATH n'est pas défini dans les variables d'environnement")
        return chrome_path

    def initialize_driver(self):
        try:
            options = uc.ChromeOptions()
            options.add_argument(f"--user-data-dir={self.chrome_path}/temp")
            options.add_argument("--start-maximized")
            options.add_argument("--disable-gpu")
            options.add_argument("--no-sandbox")
            options.add_argument("--disable-dev-shm-usage")
            options.add_argument("--disable-blink-features=AutomationControlled")
            options.add_argument("--disable-features=BlinkGenPropertyTrees")
            options.add_argument("--lang=fr-FR")

            self.driver = uc.Chrome(options=options)
            logger.info("Driver Chrome initialisé avec succès")
            return True
        except Exception as e:
            logger.error(f"Erreur d'initialisation du driver: {e}")
            return False

    def wait_for_element(self, selector, by=By.CSS_SELECTOR, timeout=None):
        try:
            wait_time = timeout if timeout else self.wait_time
            element = WebDriverWait(self.driver, wait_time).until(
                EC.presence_of_element_located((by, selector))
            )
            logger.info(f"Élément trouvé: {selector}")
            return element
        except TimeoutException:
            logger.warning(f"Timeout en attendant l'élément: {selector}")
            return None

    def is_valid_url(self, url):
        """Vérifie si l'URL est valide."""
        try:
            result = urlparse(url)
            return all([result.scheme, result.netloc])
        except:
            return False

    def parse_profile_info(self, url):
        """Extrait les informations du profil LinkedIn"""
        logger.info(f"Début de l'analyse du profil: {url}")
        if not self.is_valid_url(url):
            logger.warning("URL invalide détectée")
            return {"status": "error", "error": "URL invalide"}

        for attempt in range(self.max_retries):
            try:
                logger.info(f"Tentative de scraping (attempt {attempt + 1}/{self.max_retries})")
                if not self.initialize_driver():
                    logger.error("Échec de l'initialisation du driver, arrêt du scraping")
                    return {"status": "error", "error": "Échec de l'initialisation du driver"}

                self.driver.get(url)
                logger.info(f"Page chargée: {url}")
                time.sleep(5)

                if "login" in self.driver.current_url:
                    logger.warning("Session expirée détectée, redirection vers la page de login")
                    return {
                        "status": "error",
                        "error": "Session expirée, reconnexion nécessaire"
                    }

                full_name_element = self.wait_for_element("h1.text-heading-xlarge")
                if not full_name_element:
                    logger.error("Impossible de trouver l'élément nom (h1.text-heading-xlarge)")
                    return {"status": "error", "error": "Impossible de trouver le nom"}

                full_name = full_name_element.text.strip()
                logger.info(f"Nom complet trouvé: {full_name}")
                names = full_name.split(' ', 1)
                first_name = names[0]
                last_name = names[1] if len(names) > 1 else ""

                position = ""
                position_element = self.wait_for_element("div.text-body-medium.break-words")
                if position_element:
                    position = position_element.text.strip()
                    logger.info(f"Position trouvée: {position}")
                else:
                    logger.warning("Impossible de trouver l'élément position (div.text-body-medium.break-words), position vide")

                company = ""
                company_element = self.wait_for_element("span.text-body-small.inline")
                if company_element:
                    company = company_element.text.strip()
                    logger.info(f"Entreprise trouvée: {company}")
                else:
                    logger.warning("Impossible de trouver l'élément entreprise (span.text-body-small.inline), entreprise vide")


                email = ""
                try:
                    contact_info_button = self.wait_for_element("a[href*='overlay/contact-info']")
                    if contact_info_button:
                        contact_info_button.click()
                        logger.info("Bouton 'Contact Info' cliqué")
                        time.sleep(2)
                        email_element = self.wait_for_element("a[href^='mailto:']", timeout=5)
                        if email_element:
                            email = email_element.text.strip()
                            logger.info(f"Email trouvé: {email}")
                        else:
                            logger.warning("Impossible de trouver l'élément email (a[href^='mailto:']) après clic sur 'Contact Info', email vide")
                    else:
                        logger.warning("Impossible de trouver le bouton 'Contact Info' (a[href*='overlay/contact-info']), email non récupéré") # LOG AJOUTÉ
                except Exception as e:
                    logger.warning(f"Erreur lors de la récupération de l'email: {e}")

                logger.info("Scraping réussi")
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

            except WebDriverException as e:
                logger.error(f"Erreur WebDriver lors du parsing (attempt {attempt + 1}/{self.max_retries}): {e}")
                if attempt < self.max_retries - 1:
                    logger.info(f"Nouvelle tentative de scraping dans {self.wait_time} secondes...")
                    time.sleep(self.wait_time)
                    continue
                else:
                    logger.error("Nombre maximal de tentatives atteint, arrêt du scraping après erreurs WebDriver répétées")
                    return {"status": "error", "error": f"Erreur WebDriver après plusieurs tentatives: {e}"}
            except Exception as e:
                logger.error(f"Erreur générale lors du parsing: {e}")
                return {"status": "error", "error": str(e)}
            finally:
                if self.driver:
                    try:
                        self.driver.quit()
                        logger.info("Driver Chrome fermé")
                    except Exception as e:
                        logger.error(f"Erreur lors de la fermeture du driver: {e}")
                    self.driver = None

async def websocket_handler(websocket, path):
    """Gère les connexions WebSocket"""
    logger.info(f"🔌 Nouvelle connexion WebSocket sur le chemin: {path}")
    scraper = LinkedInScraper()

    try:
        if path == "/" or path == "":
            async for message in websocket:
                logger.info(f"Message WebSocket reçu: {message}")
                try:
                    data = json.loads(message)
                    if "linkedinURL" in data and data["status"] == "request":
                        logger.info(f"📥 Traitement de l'URL: {data['linkedinURL']}")
                        result = scraper.parse_profile_info(data["linkedinURL"])
                        await websocket.send(json.dumps(result))
                        logger.info("✅ Résultat envoyé au client WebSocket")
                    else:
                        logger.warning(f"Message WebSocket non reconnu ou incomplet: {message}")
                        await websocket.send(json.dumps({
                            "status": "error",
                            "error": "Message non reconnu"
                        }))
                except json.JSONDecodeError as e:
                    logger.error(f"Erreur de décodage JSON: {e}")
                    await websocket.send(json.dumps({
                        "status": "error",
                        "error": "Format de message JSON invalide"
                    }))
                except Exception as e:
                    logger.error(f"Erreur de traitement dans websocket_handler: {e}")
                    await websocket.send(json.dumps({
                        "status": "error",
                        "error": str(e)
                    }))
        elif path == "/status":
            await websocket.send(json.dumps({
                "status": "ok",
                "message": "Serveur opérationnel"
            }))
        else:
            await websocket.send(json.dumps({
                "status": "error",
                "error": f"Chemin non reconnu: {path}"
            }))
    except websockets.exceptions.ConnectionClosed:
        logger.info("🔌 Connexion WebSocket fermée par le client")
    finally:
        if hasattr(scraper, 'driver') and scraper.driver:
            scraper.driver.quit()
            logger.info("Driver Chrome fermé après fermeture WebSocket")

async def start_server():
    """Démarre le serveur WebSocket"""
    port = 9000
    max_attempts = 10

    for attempt in range(max_attempts):
        try:
            server = await websockets.serve(
                websocket_handler,
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