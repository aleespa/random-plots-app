package com.aleespa.randomsquare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aleespa.randomsquare.data.DatabaseProvider
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.aleespa.randomsquare.screens.MainScreen
import com.aleespa.randomsquare.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val dao = DatabaseProvider.getDatabase(this).imageDao()

        setContent {
            MyApplicationTheme {
                MainScreen(dao)
            }
        }
    }
}
