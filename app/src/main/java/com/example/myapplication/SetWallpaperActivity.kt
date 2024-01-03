package com.example.myapplication
import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.myapplication.R

class SetWallpaperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_wallpaper)

        val wallpaperManager = WallpaperManager.getInstance(applicationContext)

        // Get the image URI passed from MainActivity
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)

        // Load the bitmap from the URI
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        // Set the bitmap as wallpaper
        wallpaperManager.setBitmap(bitmap)
    }

}
