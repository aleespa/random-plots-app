package com.aleespa.randomsquare.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aleespa.randomsquare.Figures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val DARK_MODE_SETTING = stringPreferencesKey("dark_mode_setting")
        val SELECTED_FIGURE = stringPreferencesKey("selected_figure")
        val BG_COLOR = longPreferencesKey("bg_color")
    }

    // Save settingDarkMode
    suspend fun saveDarkModeSetting(mode: SettingDarkMode) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_SETTING] = mode.name
        }
    }

    // Load settingDarkMode as Flow
    val darkModeSetting: Flow<SettingDarkMode> = dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_SETTING]?.let { modeName ->
                SettingDarkMode.valueOf(modeName)
            } ?: SettingDarkMode.Auto // Default value
        }

    suspend fun saveSelectedFigure(figure: Figures) {
        dataStore.edit { preferences ->
            preferences[SELECTED_FIGURE] = figure.name
        }
    }

    // Load selectedFigure as Flow
    val selectedFigure: Flow<Figures> = dataStore.data
        .map { preferences ->
            preferences[SELECTED_FIGURE]?.let { figureName ->
                Figures.valueOf(figureName)
            } ?: Figures.POLYGON_FEEDBACK // Default value
        }

    suspend fun saveBgColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[BG_COLOR] = color.toArgb().toLong()
        }
    }

    // Load bgColor as Flow
    val bgColor: Flow<Color> = dataStore.data
        .map { preferences ->
            val colorLong = preferences[BG_COLOR] ?: Color(0, 0, 0, 0).toArgb().toLong()
            Color(colorLong.toInt())
        }
}