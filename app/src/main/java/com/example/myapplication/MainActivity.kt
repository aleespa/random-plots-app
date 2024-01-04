package com.example.myapplication

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileOutputStream
import java.util.Base64


class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_CODE = 100
    private var imageUri: Uri? = null
    companion object {
        private var cachedBitmap: Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContentView(R.layout.form_layout)

        val screenWidth: Int
        val screenHeight: Int
        var darkModeBool = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API level 30) and above
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            // For older versions
            val size = Point()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getSize(size)
            screenWidth = size.x
            screenHeight = size.y
        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val generateButton = findViewById<Button>(R.id.generateButton)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        val wallpaperButton = findViewById<TextView>(R.id.wallpaperButton)
        val mainLayout = findViewById<RelativeLayout>(R.id.mainLayout)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        cachedBitmap?.let {
            imageView.setImageBitmap(it)
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Apply dark mode colors
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.darkModeBackground))
                textViewResult.setTextColor(ContextCompat.getColor(this, R.color.darkModeTextColor))
                switchDarkMode.setTextColor(ContextCompat.getColor(this, R.color.darkModeTextColor))
                generateButton.setTextColor(ContextCompat.getColor(this, R.color.darkModeButton1))
                wallpaperButton.setTextColor(ContextCompat.getColor(this, R.color.darkModeButton2))
                darkModeBool = true
                // ... Update other UI elements as needed
            } else {
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.lightModeBackground))
                textViewResult.setTextColor(ContextCompat.getColor(this, R.color.lightModeTextColor))
                switchDarkMode.setTextColor(ContextCompat.getColor(this, R.color.lightModeTextColor))
                generateButton.setTextColor(ContextCompat.getColor(this, R.color.lightModeButton1))
                wallpaperButton.setTextColor(ContextCompat.getColor(this, R.color.lightModeButton2))
                darkModeBool = false
            }
        }

        generateButton.setOnClickListener {
            val py = Python.getInstance()
            val mainModule = py.getModule("main")
            val result = mainModule.callAttr("generate", darkModeBool).asList()


            // Decode the base64 image to bytes
            val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())

            // Convert bytes to a Bitmap and display it in the ImageView
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            cachedBitmap = bitmap
            imageView.setImageBitmap(bitmap)
            textViewResult.text = result[1].toString()

            imageUri = saveImageToInternalStorage(bitmap, this)

        }
        wallpaperButton.setOnClickListener {
            val localImageUri = imageUri
            if (localImageUri == null) {
                // Show a message that no image has been generated yet
                Toast.makeText(this, "Please generate an image first", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with setting the wallpaper
                try {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    val source = ImageDecoder.createSource(this.contentResolver, localImageUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)

                    wallpaperManager.setBitmap(bitmap)

                    Toast.makeText(this, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    fun saveImageToInternalStorage(bitmap: Bitmap, context: Context): Uri {
        // Use the app's internal storage
        val directory = context.cacheDir
        val file = File(directory, "shared_image.png")  // Name of the image file

        // Write the bitmap to a file
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        return Uri.fromFile(file)
    }
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

}
