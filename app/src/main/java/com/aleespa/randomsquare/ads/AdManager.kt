package com.aleespa.randomsquare.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.aleespa.randomsquare.AD_UNIT_ID
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Application-scoped singleton that owns all interstitial ad state.
 *
 * Usage:
 *   1. Call [init] once in MainActivity.onCreate (before setContent).
 *   2. Call [showIfReady] with the current Activity and a completion lambda.
 *      - If an ad is loaded it is shown; [onComplete] is called after dismiss.
 *      - If no ad is ready [onComplete] is called immediately and a background
 *        preload is triggered so the next opportunity is covered.
 */
object AdManager {

    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isInitialised = false

    private val mainHandler = Handler(Looper.getMainLooper())

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Initialise the AdMob SDK and load the first ad. Call once from Application or MainActivity. */
    fun init(context: Context) {
        if (isInitialised) return
        isInitialised = true
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob SDK initialised")
            preload(context)
        }
    }

    /**
     * Show the loaded interstitial ad if one is available, then invoke [onComplete]
     * after the ad is dismissed. If no ad is ready, [onComplete] is called immediately
     * and a new ad is preloaded in the background.
     */
    fun showIfReady(activity: Activity, onComplete: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "No ad ready — proceeding immediately and preloading next.")
            onComplete()
            preload(activity.applicationContext)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed.")
                interstitialAd = null
                onComplete()
                preload(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show: ${adError.message}")
                interstitialAd = null
                onComplete()
                preload(activity.applicationContext)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed full-screen content.")
            }
        }

        ad.show(activity)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Request a new interstitial ad. Does nothing if one is already loaded or loading. */
    private fun preload(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true
        Log.d(TAG, "Ad preload requested.")

        val appContext = context.applicationContext
        mainHandler.post {
            InterstitialAd.load(
                appContext,
                AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        interstitialAd = ad
                        isLoading = false
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(
                            TAG,
                            "Ad failed to load — domain: ${adError.domain}, " +
                                "code: ${adError.code}, message: ${adError.message}"
                        )
                        interstitialAd = null
                        isLoading = false
                    }
                }
            )
        }
    }
}
