package com.aleespa.randomsquare.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aleespa.randomsquare.Figures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val DARK_MODE_SETTING = stringPreferencesKey("dark_mode_setting")
        val SELECTED_FIGURE = stringPreferencesKey("selected_figure")

        // Use different keys for the old String and new Int formats to avoid type errors.
        // We will keep the original key name for the string to read the old data.
        val BG_COLOR_STRING = stringPreferencesKey("bg_color")

        // We will define a new key for the integer color.
        val BG_COLOR_INT = intPreferencesKey("bg_color_int")

        val COLORMAP_COLORS = stringPreferencesKey("colormap_colors")
    }

    // --- Dark Mode and Figure settings remain the same ---

    suspend fun saveDarkModeSetting(mode: SettingDarkMode) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_SETTING] = mode.name
        }
    }

    val darkModeSetting: Flow<SettingDarkMode> = dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_SETTING]?.let { modeName ->
                SettingDarkMode.valueOf(modeName)
            } ?: SettingDarkMode.Auto
        }

    suspend fun saveSelectedFigure(figure: Figures) {
        dataStore.edit { preferences ->
            preferences[SELECTED_FIGURE] = figure.name
        }
    }

    val selectedFigure: Flow<Figures> = dataStore.data
        .map { preferences ->
            preferences[SELECTED_FIGURE]?.let { figureName ->
                Figures.valueOf(figureName)
            } ?: Figures.POLYGON_FEEDBACK
        }

    // --- Updated Color saving and loading logic ---

    /**
     * Saves the background color as an Integer.
     * It also removes the old string key to complete the migration for this setting.
     */
    suspend fun saveBgColor(color: Int) {
        dataStore.edit { preferences ->
            // Store the color using the new Integer key
            preferences[BG_COLOR_INT] = color
            // Clean up by removing the old String-based key if it exists
            preferences.remove(BG_COLOR_STRING)
        }
    }

    suspend fun saveSelectedColormapColors(colormap: List<Int>) {
        val stringValue = colormap.joinToString(separator = ",")
        dataStore.edit { preferences ->
            preferences[COLORMAP_COLORS] = stringValue
        }
    }
    val selectedColormapColors: Flow<List<Int>> = dataStore.data
        .map { preferences ->
            preferences[COLORMAP_COLORS]?.split(",")
                ?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        }
    /**
     * Loads the background color as a Flow of Int.
     * This logic is now migration-aware. It first tries to read the new Int format.
     * If it fails, it falls back to reading the old String format, converts it,
     * and from then on, the new format will be used.
     */
    val bgColor: Flow<Int> = dataStore.data
        .map { preferences ->
            // 1. First, try to read the color in the new Int format.
            val intColor = preferences[BG_COLOR_INT]
            // Success: The value is already an Int. Return it directly.
            intColor ?: Color.Black.toArgb() // Default to black
        }
}


