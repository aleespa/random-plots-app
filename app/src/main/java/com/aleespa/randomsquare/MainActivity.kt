package com.aleespa.randomsquare

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.DatabaseProvider
import com.aleespa.randomsquare.data.ImageRepository
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.data.VisualizeModelFactory
import com.aleespa.randomsquare.screens.MainScreen
import com.aleespa.randomsquare.ui.theme.MyApplicationTheme
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : ComponentActivity() {
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private var interstitialAd: InterstitialAd? = null
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val dao = DatabaseProvider.getDatabase(this).imageDao()
        val settingsRepository = AppSettingsRepository(dataStore)
        val imageRepository = ImageRepository(dao, applicationContext)
        val factory = VisualizeModelFactory(imageRepository, settingsRepository)
        val visualizeModel = ViewModelProvider(this, factory)[VisualizeModel::class.java]



        setContent {
            MyApplicationTheme (darkThemeSetting = visualizeModel.settingDarkMode){
                MainScreen(visualizeModel,
                    {showInterstitial()})
            }
        }

    }
    private fun showInterstitial() {
        Log.d("MainActivity","showInterstitial called")
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        interstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        interstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }
                }
            interstitialAd?.show(this)
        } else {
            loadAd()
        }
    }

    private fun loadAd() {
        // Request a new ad if one isn't already loaded.
        if (interstitialAd != null) {
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    interstitialAd = null
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Toast.makeText(
                        this@MainActivity,
                        "onAdFailedToLoad() with error $error",
                        Toast.LENGTH_SHORT,
                    )
                        .show()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    interstitialAd = ad
                    Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    companion object {
        // This is an ad unit ID for a test ad. Replace with your own interstitial ad unit ID.
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val GAME_LENGTH_MILLISECONDS = 3000L
        private const val TAG = "MainActivity"

        // Check your logcat output for the test device hashed ID e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device" or
        // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345") to set this as
        // a debug device".
        const val TEST_DEVICE_HASHED_ID = "ABCDEF012345"
    }
}
