package com.monliev.brainwave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monliev.brainwave.theme.BrainwaveSleepFocusTheme
import com.monliev.brainwave.ui.main.MainScreenViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Google Mobile Ads SDK and start loading test ads
    try {
      com.google.android.gms.ads.MobileAds.initialize(this) {
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[MainScreenViewModel::class.java]
        if (!viewModel.isPremium.value) {
          com.monliev.brainwave.audio.playback.AdMobManager.loadAppOpenAd(this)
          com.monliev.brainwave.audio.playback.AdMobManager.loadInterstitialAd(this)
          com.monliev.brainwave.audio.playback.AdMobManager.loadRewardedAd(this)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    enableEdgeToEdge()
    setContent {
      val viewModel: MainScreenViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsState()

      // Handle initial launch deep link intent data
      androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.handleDeepLinkUri(intent?.dataString)
      }

      BrainwaveSleepFocusTheme(
        darkTheme = isDarkMode,
        dynamicColor = false
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainNavigation()
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    try {
      val viewModel = androidx.lifecycle.ViewModelProvider(this)[MainScreenViewModel::class.java]
      if (!viewModel.isPremium.value) {
        val now = System.currentTimeMillis()
        val elapsed = now - com.monliev.brainwave.audio.playback.AdMobManager.lastFullScreenAdDismissedTime
        if (elapsed > 1000) {
          com.monliev.brainwave.audio.playback.AdMobManager.showAppOpenAd(this) {}
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    try {
      val viewModel = androidx.lifecycle.ViewModelProvider(this)[MainScreenViewModel::class.java]
      viewModel.handleDeepLinkUri(intent.dataString)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
