package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PreferencesRepository
import com.example.data.remote.MultiplayerManager
import com.example.ui.game.GameScreen
import com.example.ui.game.GameViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.lobby.LobbyScreen
import com.example.ui.rules.HowToPlayScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.FeltDark
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    GAME,
    LOBBY,
    HOW_TO_PLAY,
    SETTINGS,
    STATS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FeltDark
                ) {
                    Taash29App()
                }
            }
        }
    }
}

@Composable
fun Taash29App() {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val gameViewModel: GameViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsRepo = remember { PreferencesRepository(context) }
    val multiplayerManager = remember { MultiplayerManager() }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                onPlayOffline = { practiceMode ->
                    gameViewModel.startNewMatch(practiceMode = practiceMode)
                    currentScreen = AppScreen.GAME
                },
                onOpenMultiplayer = {
                    currentScreen = AppScreen.LOBBY
                },
                onOpenHowToPlay = {
                    currentScreen = AppScreen.HOW_TO_PLAY
                },
                onOpenSettings = {
                    currentScreen = AppScreen.SETTINGS
                },
                onOpenStats = {
                    currentScreen = AppScreen.STATS
                }
            )
        }

        AppScreen.GAME -> {
            GameScreen(
                viewModel = gameViewModel,
                onNavigateBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.LOBBY -> {
            LobbyScreen(
                multiplayerManager = multiplayerManager,
                onStartGame = {
                    gameViewModel.startNewMatch(practiceMode = false)
                    currentScreen = AppScreen.GAME
                },
                onNavigateBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.HOW_TO_PLAY -> {
            HowToPlayScreen(
                onNavigateBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.SETTINGS -> {
            SettingsScreen(
                prefsRepo = prefsRepo,
                onNavigateBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.STATS -> {
            StatsScreen(
                prefsRepo = prefsRepo,
                onNavigateBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }
    }
}
