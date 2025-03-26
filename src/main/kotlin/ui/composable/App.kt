package ui.composable

import MainContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun App(windowState: WindowState, applicationScope: CoroutineScope) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalDrawer({DrawerMenu()}, Modifier.fillMaxSize().background(Color.DarkGray), drawerState) {
        IconButton(onClick = {
            coroutineScope.launch {
                if (drawerState.isOpen) {drawerState.close()}
                else {drawerState.open()}
            }
        }) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu")
        }
        Column(Modifier.fillMaxSize()) {
            MainContent(windowState, applicationScope)
        }
    }
}