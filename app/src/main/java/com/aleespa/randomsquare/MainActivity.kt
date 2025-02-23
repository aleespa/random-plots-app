package com.aleespa.randomsquare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aleespa.randomsquare.data.DatabaseProvider
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.screens.MainScreen
import com.aleespa.randomsquare.ui.theme.MyApplicationTheme
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val dao = DatabaseProvider.getDatabase(this).imageDao()
        val visualizeModel = VisualizeModel(dao)


        setContent {
            MyApplicationTheme (darkThemeSetting = visualizeModel.settingDarkMode){
                MainScreen(visualizeModel)
            }
        }


    }
}
