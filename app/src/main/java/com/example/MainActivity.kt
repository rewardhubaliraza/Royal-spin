package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.DragonTigerScreen
import com.example.ui.screens.GameRouletteScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.LuckyMinesScreen
import com.example.ui.screens.SevenUpDownScreen
import com.example.ui.screens.WithdrawalScreen
import com.example.ui.theme.CasinoPurpleDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val currentScreen by viewModel.currentScreen.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.uiMessage.collect { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CasinoPurpleDark,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    AppNavigation(
                        viewModel = viewModel,
                        currentScreen = currentScreen,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: GameViewModel,
    currentScreen: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            "AUTH" -> AuthScreen(viewModel = viewModel)
            "LOBBY" -> LobbyScreen(viewModel = viewModel)
            "LOADING" -> LoadingScreen(viewModel = viewModel)
            "GAME" -> GameRouletteScreen(viewModel = viewModel)
            "DRAGON_TIGER" -> DragonTigerScreen(viewModel = viewModel)
            "SEVEN_UP_DOWN" -> SevenUpDownScreen(viewModel = viewModel)
            "LUCKY_MINES" -> LuckyMinesScreen(viewModel = viewModel)
            "DEPOSIT" -> DepositScreen(viewModel = viewModel)
            "WITHDRAWAL" -> WithdrawalScreen(viewModel = viewModel)
            "ADMIN" -> AdminPanelScreen(viewModel = viewModel)
            "HISTORY" -> HistoryScreen(viewModel = viewModel)
            else -> LobbyScreen(viewModel = viewModel)
        }
    }
}
