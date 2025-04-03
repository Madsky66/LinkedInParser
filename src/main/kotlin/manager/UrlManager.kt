package manager

import config.GlobalInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import ui.composable.processInput
import utils.ConsoleMessage
import utils.ConsoleMessageType
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent

class UrlManager {
    val globalConfig = GlobalInstance.config
    suspend fun openPastedUrl(applicationScope: CoroutineScope) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val robot = Robot()
        delay(5000) // <--- Rendre cette valeur dynamique

        globalConfig.consoleMessage.value = ConsoleMessage("⏳ Détection de la page de profil en cours...", ConsoleMessageType.INFO)

        copyUrlContent(robot)
        var clipboardContent = getClipboardContent(clipboard)

        val isModalOpen = clipboardContent.lines().take(5).any {it.contains("dialogue")}
        if (!isModalOpen && !modalDetectionStep({clipboardContent.lines().take(5).any {it.contains("dialogue")}}, robot, clipboard, "Détection de la page de profil impossible", {clipboardContent = it}, {globalConfig.consoleMessage.value = it})) return
        if (isModalOpen && clipboardContent.length <= 5000 && !modalDetectionStep({clipboardContent.length > 5000}, robot, clipboard, "Quantité de données insuffisante", {clipboardContent = it}, {globalConfig.consoleMessage.value = it})) return

        globalConfig.consoleMessage.value = ConsoleMessage("⏳ Analyse des données en cours...", ConsoleMessageType.INFO)
        globalConfig.pastedInput.value = clipboardContent
        processInput(applicationScope, clipboardContent, setStatus = {globalConfig.consoleMessage.value = it}, setProfile = {globalConfig.currentProfile.value = it}, setLoading = {globalConfig.isExtractionLoading.value = it})
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