package com.aleespa.randomsquare

import com.aleespa.randomsquare.tools.LastRenders
import android.content.Intent
import android.os.Bundle
import android.service.quicksettings.TileService
import com.aleespa.randomsquare.data.AppLaunch
import com.aleespa.randomsquare.tools.imageEntityFromJson
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialise AdMob SDK and preload the first interstitial ad early.
        AdManager.init(applicationContext)

        val dao = DatabaseProvider.getDatabase(this).imageDao()
        val settingsRepository = AppSettingsRepository(dataStore)
        val imageRepository = ImageRepository(dao, applicationContext)
        val factory = VisualizeModelFactory(imageRepository, settingsRepository)
        val visualizeModel = ViewModelProvider(this, factory)[VisualizeModel::class.java]

        scheduleImagePruning()
        // Which figures have a last render, so Browse's first frame already shows them.
        LastRenders.ensureLoaded(applicationContext)

        handleLaunchIntent(intent)

        setContent {
            MyApplicationTheme(
                darkThemeSetting = visualizeModel.settingDarkMode,
                themeSource = visualizeModel.themeSource,
                imageBitmap = visualizeModel.imageBitmapState
            ) {
                MainScreen(visualizeModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLaunchIntent(intent)
    }

    /** App shortcuts and the art widget start the activity with one of the actions below. */
    private fun handleLaunchIntent(intent: Intent?) {
        val visualizeModel = ViewModelProvider(this)[VisualizeModel::class.java]
        visualizeModel.pendingLaunch = when (intent?.action) {
            ACTION_RANDOM_FIGURE -> AppLaunch.RandomFigure
            ACTION_OPEN_GALLERY -> AppLaunch.Gallery
            TileService.ACTION_QS_TILE_PREFERENCES -> AppLaunch.Settings
            ACTION_OPEN_RECIPE -> intent.getStringExtra(EXTRA_RECIPE)
                ?.let { runCatching { imageEntityFromJson(JSONObject(it)) }.getOrNull() }
                ?.let { AppLaunch.Recipe(it) }
            else -> return
        }
        // Handled once; a configuration change must not replay it.
        intent.action = Intent.ACTION_MAIN
    }

    private fun scheduleImagePruning() {
        val pruneRequest =
            PeriodicWorkRequestBuilder<com.aleespa.randomsquare.workers.PruneImagesWorker>(
                1, TimeUnit.DAYS
            ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PruneImagesWork",
            ExistingPeriodicWorkPolicy.KEEP,
            pruneRequest
        )
    }

    companion object {
        const val ACTION_RANDOM_FIGURE = "com.aleespa.randomsquare.action.RANDOM_FIGURE"
        const val ACTION_OPEN_GALLERY = "com.aleespa.randomsquare.action.OPEN_GALLERY"
        const val ACTION_OPEN_RECIPE = "com.aleespa.randomsquare.action.OPEN_RECIPE"
        const val EXTRA_RECIPE = "recipe"
    }
}
