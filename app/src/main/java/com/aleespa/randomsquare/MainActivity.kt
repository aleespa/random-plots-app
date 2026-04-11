package com.aleespa.randomsquare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aleespa.randomsquare.ads.AdManager
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.DatabaseProvider
import com.aleespa.randomsquare.data.ImageRepository
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.data.VisualizeModelFactory
import com.aleespa.randomsquare.data.dataStore
import com.aleespa.randomsquare.pages.MainScreen
import com.aleespa.randomsquare.ui.theme.MyApplicationTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise AdMob SDK and preload the first interstitial ad early.
        AdManager.init(applicationContext)

        val dao = DatabaseProvider.getDatabase(this).imageDao()
        val settingsRepository = AppSettingsRepository(dataStore)
        val imageRepository = ImageRepository(dao, applicationContext)
        val factory = VisualizeModelFactory(imageRepository, settingsRepository)
        val visualizeModel = ViewModelProvider(this, factory)[VisualizeModel::class.java]

        scheduleImagePruning()

        setContent {
            MyApplicationTheme(darkThemeSetting = visualizeModel.settingDarkMode) {
                MainScreen(visualizeModel)
            }
        }
    }

    private fun scheduleImagePruning() {
        val pruneRequest = PeriodicWorkRequestBuilder<com.aleespa.randomsquare.workers.PruneImagesWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PruneImagesWork",
            ExistingPeriodicWorkPolicy.KEEP,
            pruneRequest
        )
    }
}