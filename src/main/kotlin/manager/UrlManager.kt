package manager

import config.GlobalConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.composable.processInput
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
                    if (!Desktop.isDesktopSupported()) {gC.consoleMessage.value = ConsoleMessage("❌ Votre système ne supporte pas Desktop browsing.", ConsoleMessageType.ERROR); return@launch}
                    val uri = URI("${gC.pastedUrl.value}/overlay/contact-info/")
                    gC.consoleMessage.value = ConsoleMessage("⏳ Ouverture de la page LinkedIn en cours...", ConsoleMessageType.INFO)
                    Desktop.getDesktop().browse(uri)

                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val robot = Robot()
                    delay(5000) // <--- Rendre cette valeur dynamique

                    gC.consoleMessage.value = ConsoleMessage("⏳ Détection de la page de profil en cours...", ConsoleMessageType.INFO)

                    copyUrlContent(robot)
                    var clipboardContent = getClipboardContent(clipboard)

                    val isModalOpen = clipboardContent.lines().take(5).any {it.contains("dialogue")}
                    if (!isModalOpen && !modalDetectionStep({clipboardContent.lines().take(5).any {it.contains("dialogue")}}, robot, clipboard, "Détection de la page de profil impossible", {clipboardContent = it}, {gC.consoleMessage.value = it})) return@launch
                    if (isModalOpen && clipboardContent.length <= 5000 && !modalDetectionStep({clipboardContent.length > 5000}, robot, clipboard, "Quantité de données insuffisante", {clipboardContent = it}, {gC.consoleMessage.value = it})) return@launch

                    gC.consoleMessage.value = ConsoleMessage("⏳ Analyse des données en cours...", ConsoleMessageType.INFO)
                    gC.pastedInput.value = clipboardContent
                    processInput(applicationScope, gC, clipboardContent)
                }
                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", ConsoleMessageType.ERROR)}
            }
        }
    }

    suspend fun modalDetectionStep(condition: () -> Boolean, robot: Robot, clipboard: Clipboard, errorActionType: String, onNewClipBoardContent: (String) -> Unit, onNewMessage: (ConsoleMessage) -> Unit): Boolean {
        val maxAttempts = 100
        var attempts = 0
        while (!condition() && attempts < maxAttempts) {
            delay(250)
            copyUrlContent(robot)
            val newClipboardContent = getClipboardContent(clipboard)
            onNewClipBoardContent(newClipboardContent)
            attempts++
            onNewMessage(ConsoleMessage("⏳ Détection de la page en cours... [Tentative $attempts/$maxAttempts]", ConsoleMessageType.INFO))
        }
        if (attempts >= maxAttempts) {onNewMessage(ConsoleMessage("❌ $errorActionType après $maxAttempts tentatives", ConsoleMessageType.ERROR)); return false}
        return true
    }

    fun getClipboardContent(clipboard: Clipboard): String {
        return try {clipboard.getData(DataFlavor.stringFlavor) as? String ?: ""}
        catch (e: Exception) {""}
    }

    suspend fun copyUrlContent(robot: Robot) {
        robot.ctrlAnd(KeyEvent.VK_A)
        delay(250)
        robot.ctrlAnd(KeyEvent.VK_C)
        delay(250)
    }

    fun Robot.ctrlAnd(key: Int) {
        keyPress(KeyEvent.VK_CONTROL)
        keyPress(key)
        keyRelease(key)
        keyRelease(KeyEvent.VK_CONTROL)
    }
}