package com.aleespa.randomsquare.quick

import android.app.Activity
import android.os.Bundle

/** Target of the "New wallpaper" app shortcut: starts the work and closes without any UI. */
class WallpaperShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRandomWallpaper(applicationContext)
        finish()
    }
}
