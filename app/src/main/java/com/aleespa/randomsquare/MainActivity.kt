package com.aleespa.randomsquare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.DatabaseProvider
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.data.VisualizeModelFactory
import com.aleespa.randomsquare.screens.MainScreen
import com.aleespa.randomsquare.ui.theme.MyApplicationTheme
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


class MainActivity : ComponentActivity() {
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val dao = DatabaseProvider.getDatabase(this).imageDao()
        val settingsRepository = AppSettingsRepository(dataStore)
        val factory = VisualizeModelFactory(dao, settingsRepository)

        // Get ViewModel through factory
        val visualizeModel = ViewModelProvider(this, factory)[VisualizeModel::class.java]


        setContent {
            MyApplicationTheme (darkThemeSetting = visualizeModel.settingDarkMode){
                MainScreen(visualizeModel)
            }
        }


    }
}
