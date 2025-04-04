package manager

import config.GlobalConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.ConsoleMessage
import utils.ConsoleMessageType
import java.awt.Desktop
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent
import java.net.URI

class UrlManager {
    fun openPastedUrl(applicationScope: CoroutineScope, gC: GlobalConfig) {
        applicationScope.launch {
            if (gC.pastedUrl.value.isNotBlank()) {
                try {
                    // Ouverture de l'URL dans le navigateur
                    if (!Desktop.isDesktopSupported()) {gC.consoleMessage.value = ConsoleMessage("❌ Votre système ne supporte pas Desktop browsing.", ConsoleMessageType.ERROR); return@launch}
                    val uri = URI("${gC.pastedUrl.value}/overlay/contact-info/")
                    gC.consoleMessage.value = ConsoleMessage("⏳ Ouverture de la page LinkedIn en cours...", ConsoleMessageType.INFO)
                    Desktop.getDesktop().browse(uri)

                    // Initialisation de la détection
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val robot = Robot()
                    delay(1000) // <--- Rendre cette valeur dynamique

                    // Copie du contenu de la page
                    val clipboardContent = copyUrlContent(robot, clipboard)

                    // Boucles de détection de la page de profil
                    if (!detect(gC, robot, clipboard)) {return@launch} else {gC.consoleMessage.value = ConsoleMessage("✅ Page de profil détectée et correctement chargée", ConsoleMessageType.SUCCESS)}

                    // Démarrage de l'analyse du texte
                    gC.consoleMessage.value = ConsoleMessage("⏳ Analyse des données en cours...", ConsoleMessageType.INFO)
                    gC.pastedInput.value = clipboardContent
                    gC.linkedinManager.processInput(applicationScope, gC, clipboardContent)
                }
                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", ConsoleMessageType.ERROR)}
            }
        }
    }

    suspend fun detect(gC: GlobalConfig, robot: Robot, clipboard: Clipboard): Boolean {
        val maxAttempts = 6
        var attempts = 1

        var clipboardContent = copyUrlContent(robot, clipboard)
        var isCoordModalOpen = clipboardContent.lines().take(5).any {it.contains("dialogue")}
        var isTextLengthValid = clipboardContent.length > 5000

        println("-------------------------------------------------------------")
        println("Modale détectée : " + (if (isCoordModalOpen) {"✅"} else {"❌"}))
        println("Longueur du texte : " + (if (isTextLengthValid) {"✅"} else {"❌"}))
        println("-------------------------------------------------------------")
        println("-------------------------------------------------------------")

        while (!isCoordModalOpen || !isTextLengthValid || attempts < maxAttempts) {
            attempts++
            delay(250)

            clipboardContent = copyUrlContent(robot, clipboard)
            isCoordModalOpen = clipboardContent.lines().take(5).any {it.contains("dialogue")}
            isTextLengthValid = clipboardContent.length > 5000

            println("Modale détectée : " + (if (isCoordModalOpen) {"✅"} else {"❌"}))
            println("Longueur du texte : " + (if (isTextLengthValid) {"✅"} else {"❌"}))
            println("-------------------------------------------------------------")

            if (!isCoordModalOpen) {gC.consoleMessage.value = ConsoleMessage("⏳  Détection de la page en cours... [Tentative $attempts/$maxAttempts]", ConsoleMessageType.INFO)}
            else if (!isTextLengthValid) {gC.consoleMessage.value = ConsoleMessage("⏳ Vérification du chargement de la page en cours... [Tentative $attempts/$maxAttempts]", ConsoleMessageType.INFO)}
        }

        println("-------------------------------------------------------------")

        if (attempts == maxAttempts) {
            gC.consoleMessage.value =
                if (!isCoordModalOpen) {ConsoleMessage("❌ La page de profil n'a pas été détectée après $maxAttempts tentatives", ConsoleMessageType.ERROR)}
                else {ConsoleMessage("❌ Quantité de données insuffisante pour l'analyse après $maxAttempts tentatives", ConsoleMessageType.ERROR)}
            returnToApp(robot)
            return false
        }

        gC.consoleMessage.value = ConsoleMessage("✅ Page de profil détectée", ConsoleMessageType.SUCCESS)
        returnToApp(robot)
        return true
    }

    fun Robot.ctrlAnd(key: Int) {
        keyPress(KeyEvent.VK_CONTROL)    // <--------------------------------------------- Touche "Ctrl" pressée
        delay(10)
        keyPress(key)    // <------------------------------------------------------------- Touche secondaire pressée
        delay(10)
        keyRelease(key)    // <----------------------------------------------------------- Touche secondaire relâchée
        delay(10)
        keyRelease(KeyEvent.VK_CONTROL)    // <------------------------------------------- Touche "Ctrl" relâchée
    }

    suspend fun copyUrlContent(robot: Robot, clipboard: Clipboard): String {
        robot.ctrlAnd(KeyEvent.VK_A)    // <---------------------------------------------- "Ctrl" + "A"
        delay(50)
        robot.ctrlAnd(KeyEvent.VK_C)    // <---------------------------------------------- "Ctrl" + "C"
        delay(50)
        val clipboardContent = getClipboardContent(clipboard)
        return clipboardContent
    }

    fun getClipboardContent(clipboard: Clipboard): String {
        return try {clipboard.getData(DataFlavor.stringFlavor) as? String ?: ""}
        catch (e: Exception) {""}
    }

    fun Robot.altAnd(key: Int) {
        keyPress(KeyEvent.VK_ALT)    // <------------------------------------------------- Touche "Alt" pressée
        delay(10)
        keyPress(key)    // <------------------------------------------------------------- Touche secondaire pressée
        delay(10)
        keyRelease(key)    // <----------------------------------------------------------- Touche secondaire relâchée
        delay(10)
        keyRelease(KeyEvent.VK_ALT)    // <----------------------------------------------- Touche "Alt" relâchée
    }

    suspend fun returnToApp(robot: Robot) {
        robot.ctrlAnd(KeyEvent.VK_W)    // <---------------------------------------------- "Ctrl" + "W"
        delay(50)
        robot.altAnd(KeyEvent.VK_TAB)    // <--------------------------------------------- "Alt" + "Tab"
        delay(50)
    }
}