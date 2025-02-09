package com.aleespa.randomplots

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aleespa.randomplots.data.DatabaseProvider
import com.aleespa.randomplots.data.VisualizeModel
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.aleespa.randomplots.screens.MainScreen
import com.aleespa.randomplots.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class MainActivity : ComponentActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val dao = DatabaseProvider.getDatabase(this).imageDao()
        val visualizeModel = VisualizeModel(dao)
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-1817119126251176/3331426294",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.toString().toString())
                    mInterstitialAd = null
                }
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })

        setContent {
            MyApplicationTheme (darkTheme = visualizeModel.settingDarkMode){
                MainScreen(visualizeModel, mInterstitialAd)
            }
        }


    }
}
