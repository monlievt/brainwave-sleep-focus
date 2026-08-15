package com.monliev.brainwave

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.monliev.brainwave.ui.main.CategoryDetailScreen
import com.monliev.brainwave.ui.main.MainTabsScreen
import com.monliev.brainwave.ui.main.OnboardingScreen
import com.monliev.brainwave.ui.main.PlayerScreen
import com.monliev.brainwave.ui.main.StatisticsScreen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monliev.brainwave.ui.main.MainScreenViewModel

@Composable
fun MainNavigation(viewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
    val accepted = sharedPrefs.getBoolean("disclaimer_accepted", false)
    
    // Choose start screen based on disclaimer acceptance
    val startKey = if (accepted) Main else Onboarding
    val backStack = rememberNavBackStack(startKey)

    val isPremiumUnlocked by viewModel.isPremium.collectAsState()

    val navigateWithAd: (NavKey) -> Unit = { navKey ->
        if (navKey is Player) {
            if (isPremiumUnlocked) {
                backStack.add(navKey)
            } else {
                val activity = context as? android.app.Activity
                if (activity != null) {
                    com.monliev.brainwave.audio.playback.AdMobManager.showInterstitialAd(activity) {
                        backStack.add(navKey)
                    }
                } else {
                    backStack.add(navKey)
                }
            }
        } else {
            backStack.add(navKey)
        }
    }

    // Handle deep linking navigation events
    val pendingPresetId by viewModel.pendingDeepLinkPreset.collectAsState()
    LaunchedEffect(pendingPresetId) {
        pendingPresetId?.let { id ->
            if (viewModel.allPresets.any { it.preset_id == id }) {
                // If user hasn't accepted onboarding disclaimer, we should keep them there,
                // but once they accept, they will go directly. In our case, if accepted we jump to player:
                if (accepted) {
                    backStack.add(Player(id))
                }
            }
            viewModel.clearPendingDeepLink()
        }
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Onboarding> {
                OnboardingScreen(
                    onAccept = {
                        sharedPrefs.edit().putBoolean("disclaimer_accepted", true).apply()
                        // Replace Onboarding with Main (clear backstack)
                        backStack.add(Main)
                        backStack.remove(Onboarding)
                    }
                )
            }
            
            entry<Main> {
                MainTabsScreen(
                    onNavigate = navigateWithAd
                )
            }

            entry<CategoryDetail> { key ->
                CategoryDetailScreen(
                    categoryName = key.categoryName,
                    onNavigate = navigateWithAd,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Player> { key ->
                PlayerScreen(
                    presetId = key.presetId,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Statistics> {
                StatisticsScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = viewModel
                )
            }
        }
    )
}
