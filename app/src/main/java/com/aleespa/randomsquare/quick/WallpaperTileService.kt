package com.aleespa.randomsquare.quick

import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.tools.QuickArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Quick Settings tile: sets a new random figure as the home screen wallpaper. */
class WallpaperTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        showIdle()
    }

    override fun onClick() {
        super.onClick()
        if (working) return
        working = true
        qsTile?.let { tile ->
            tile.state = Tile.STATE_UNAVAILABLE
            tile.subtitle = getString(R.string.wallpaper_working)
            tile.updateTile()
        }
        setRandomWallpaper(applicationContext) {
            working = false
            showIdle()
        }
    }

    private companion object {
        // The tile's own state is not a reliable flag: it is only applied while the panel is open.
        var working = false
    }

    private fun showIdle() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.subtitle = null
        tile.updateTile()
    }
}

/** Shared by the tile and the shortcut: sets the wallpaper in the background and reports it. */
internal fun setRandomWallpaper(context: Context, onDone: () -> Unit = {}) {
    QuickArt.scope.launch {
        val ok = QuickArt.setRandomWallpaper(context)
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context, if (ok) R.string.wallpaper_set else R.string.wallpaper_fail, Toast.LENGTH_SHORT
            ).show()
            onDone()
        }
    }
}
