package ui.composable

import GoogleSheetsManager
import MainContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowState
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun App(windowState: WindowState, applicationScope: CoroutineScope) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val googleSheetsManager = remember {GoogleSheetsManager()}
    val prospectList = remember {mutableStateListOf<ProspectData>()}

    ModalDrawer({DrawerMenu()}, Modifier.fillMaxSize().background(MaterialTheme.colors.background), drawerState) {
        Box(Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        if (drawerState.isOpen) drawerState.close()
                        else drawerState.open()
                    }
                },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
            MainContent(windowState, applicationScope, googleSheetsManager, prospectList)
        }
    }
}