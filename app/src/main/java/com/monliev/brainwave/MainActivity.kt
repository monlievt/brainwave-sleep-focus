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

    val sharedPrefs = getSharedPreferences("brainwave_prefs", MODE_PRIVATE)
    val isPremium = sharedPrefs.getBoolean("is_premium_unlocked", false)

    // Initialize Google Mobile Ads SDK and start loading test ads
    try {
      com.google.android.gms.ads.MobileAds.initialize(this) {
        if (!isPremium) {
          com.monliev.brainwave.audio.playback.AdMobManager.loadAppOpenAd(this@MainActivity)
          com.monliev.brainwave.audio.playback.AdMobManager.loadInterstitialAd(this@MainActivity)
          com.monliev.brainwave.audio.playback.AdMobManager.loadRewardedAd(this@MainActivity)
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
        if (com.monliev.brainwave.audio.playback.AdMobManager.isNavigatingExternally) {
          com.monliev.brainwave.audio.playback.AdMobManager.isNavigatingExternally = false
          return
        }
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
