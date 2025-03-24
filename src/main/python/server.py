import os
import logging
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

            self.driver = uc.Chrome(options=options)
            logger.info("✅ Navigateur Selenium démarré avec succès")

            self.driver.get("https://www.linkedin.com/login")
            logger.info("🔗 Accès à la page de connexion LinkedIn")

            WebDriverWait(self.driver, 120).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "input[role='combobox']"))
            )
            logger.info("✅ Connexion LinkedIn réussie")
        except Exception as e:
            logger.error(f"❌ Erreur lors du démarrage de Selenium : {e}")
            raise