package com.monliev.brainwave.audio.playback

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMobManager provides unified cached loading and displaying of App Open, Interstitial,
 * and Rewarded Ads on standard (free) tiers safely with automatic cache replenishment.
 */
object AdMobManager {


    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdLoading = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialAdLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedAdLoading = false

    // Timestamp to track when a full screen ad was last closed, to prevent App Open from showing immediately on MainActivity resume
    var lastFullScreenAdDismissedTime: Long = 0L

    // Flag to track when user navigates externally (e.g. sharing, rating, email support, native ad clicks) to prevent app open ad on return
    @Volatile var isNavigatingExternally: Boolean = false

    // Frequency capping for Interstitial Ads to prevent excessive showing (3-minute cooldown)
    private var lastInterstitialShowTime: Long = 0L
    private const val INTERSTITIAL_COOLDOWN_MS = 3 * 60 * 1000

    // --- App Open Ad ---

    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isAppOpenAdLoading) return
        isAppOpenAdLoading = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            context.getString(com.monliev.brainwave.R.string.admob_app_open_ad_id),
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenAdLoading = false
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdClosed: () -> Unit) {
        val ad = appOpenAd
        if (ad == null) {
            onAdClosed()
            loadAppOpenAd(activity.applicationContext)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                lastFullScreenAdDismissedTime = System.currentTimeMillis()
                appOpenAd = null
                onAdClosed()
                loadAppOpenAd(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                lastFullScreenAdDismissedTime = System.currentTimeMillis()
                appOpenAd = null
                onAdClosed()
                loadAppOpenAd(activity.applicationContext)
            }
        }
        ad.show(activity)
    }

    // --- Interstitial Ad ---

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialAdLoading) return
        isInterstitialAdLoading = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            context.getString(com.monliev.brainwave.R.string.admob_interstitial_ad_id),
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialAdLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShowTime < INTERSTITIAL_COOLDOWN_MS) {
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            onAdClosed()
            loadInterstitialAd(activity.applicationContext)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                lastInterstitialShowTime = System.currentTimeMillis()
                lastFullScreenAdDismissedTime = System.currentTimeMillis()
                interstitialAd = null
                onAdClosed()
                loadInterstitialAd(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                lastFullScreenAdDismissedTime = System.currentTimeMillis()
                interstitialAd = null
                onAdClosed()
                loadInterstitialAd(activity.applicationContext)
            }
        }
        ad.show(activity)
    }

    // --- Rewarded Ad ---

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedAdLoading) return
        isRewardedAdLoading = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            context.getString(com.monliev.brainwave.R.string.admob_rewarded_ad_id),
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedAdLoading = false
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: () -> Unit, onAdClosed: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onAdClosed()
            loadRewardedAd(activity.applicationContext)
            return
        }

        var rewardEarned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                lastFullScreenAdDismissedTime = System.currentTimeMillis()
                rewardedAd = null
                if (rewardEarned) {
                    onUserEarnedReward()
                } else {
                    onAdClosed()
                }
                loadRewardedAd(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                lastFullScreenAdDismissedTime = System.currentTimeMillis()
                rewardedAd = null
                onAdClosed()
                loadRewardedAd(activity.applicationContext)
            }
        }
        ad.show(activity) {
            rewardEarned = true
        }
    }
}
