package manager

import javafx.application.Platform
import org.slf4j.LoggerFactory
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import io.github.bonigarcia.wdm.WebDriverManager

object JavaFxManager {
    private var initialized = false
    private val logger = LoggerFactory.getLogger(JavaFxManager::class.java)

    @Synchronized
    fun initialize() {
        if (!initialized) {
            try {
                Platform.startup {}
                initialized = true
                logger.info("JavaFX initialized successfully")
            }
            catch (e: IllegalStateException) {
                if (e.message?.contains("Toolkit already initialized") == true) {
                    initialized = true
                    logger.info("JavaFX was already initialized")
                }
                else {
                    logger.error("Error during JavaFX initialization", e)
                    throw e
                }
            }
        }
    }

    @Synchronized
    fun shutdown(): Boolean {
        return if (initialized) {
            try {
                Platform.exit()
                initialized = false
                logger.info("JavaFX shut down successfully")
                true
            }
            catch (e: Exception) {
                logger.error("Error during JavaFX shutdown: ${e.message}", e)
                false
            }
        }
        else {
            logger.warn("JavaFX is not initialized, shutdown skipped")
            false
        }
    }

    fun isInitialized(): Boolean = initialized

    fun initDriver(): ChromeDriver {
        WebDriverManager.chromedriver().setup()
        val options = ChromeOptions()
        val chromeExecutablePath = "resources/extra/chrome/chrome.exe"
        options.setBinary(File(chromeExecutablePath))
        options.addArguments("--headless=new")
        return ChromeDriver(options)
    }
}