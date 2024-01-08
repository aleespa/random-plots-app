package com.example.randomplots

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.button.MaterialButton
import ru.noties.jlatexmath.JLatexMathView
import java.io.File
import java.io.FileOutputStream
import java.util.Base64


class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_CODE = 100
    private var imageUri: Uri? = null
    companion object {
        private var cachedBitmap: Bitmap? = null
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContentView(R.layout.form_layout)

        var darkModeBool = false

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val generateButton = findViewById<MaterialButton>(R.id.generateButton)
        val wallpaperButton = findViewById<MaterialButton>(R.id.wallpaperButton)
        val infoButton = findViewById<MaterialButton>(R.id.infoButton)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val mainLayout = findViewById<RelativeLayout>(R.id.mainLayout)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        val instagramLink: ImageView = findViewById(R.id.instagramLink)
        val latexView = findViewById<JLatexMathView>(R.id.j_latex_math_view)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        cachedBitmap?.let {
            imageView.setImageBitmap(it)
        }
        fun darkModeSet(){
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.darkModeBackground))
            switchDarkMode.setTextColor(ContextCompat.getColor(this, R.color.darkModeTextColor))
            latexView.setBackgroundColor(ContextCompat.getColor(this, R.color.darkModeBackground))
            latexView.textColor(ContextCompat.getColor(this, R.color.darkModeTextColor))
            generateButton.setTextColor(ContextCompat.getColor(this, R.color.darkModeTextButton1))
            generateButton.setBackgroundColor(ContextCompat.getColor(this, R.color.darkModeButton1))
            wallpaperButton.setTextColor(ContextCompat.getColor(this, R.color.darkModeTextButton2))
            wallpaperButton.setBackgroundColor(ContextCompat.getColor(this, R.color.darkModeButton2))
            infoButton.setTextColor(ContextCompat.getColor(this, R.color.darkModeTextButton3))
            infoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.darkModeButton3))
            instagramLink.setColorFilter(ContextCompat.getColor(this, R.color.darkModeTextButton2))
            darkModeBool = true
        }
        fun lightModeSet(){
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.lightModeBackground))
            switchDarkMode.setTextColor(ContextCompat.getColor(this, R.color.lightModeTextColor))
            latexView.setBackgroundColor(ContextCompat.getColor(this, R.color.lightModeBackground))
            latexView.textColor(ContextCompat.getColor(this, R.color.lightModeTextColor))
            generateButton.setTextColor(ContextCompat.getColor(this, R.color.lightModeTextButton1))
            generateButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightModeButton1))
            wallpaperButton.setTextColor(ContextCompat.getColor(this, R.color.lightModeTextButton2))
            wallpaperButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightModeButton2))
            infoButton.setTextColor(ContextCompat.getColor(this, R.color.lightModeTextButton3))
            infoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.lightModeButton3))
            instagramLink.setColorFilter(ContextCompat.getColor(this, R.color.lightModeTextButton2))
            darkModeBool = false
        }
        fun generateRandomPlot(){
            val py = Python.getInstance()
            val mainModule = py.getModule("main")
            val result = mainModule.callAttr("generate", darkModeBool).asList()

            val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            cachedBitmap = bitmap
            imageView.setImageBitmap(bitmap)
            latexView.setLatex(result[1].toString())
            imageUri = saveImageToInternalStorage(bitmap, this)
        }

        switchDarkMode.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                darkModeSet()
            } else {
                lightModeSet()
            }
        }
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            darkModeSet()
        } else {
            lightModeSet()
        }
        generateButton.setOnClickListener {
            generateRandomPlot()
        }

        infoButton.setOnClickListener{
            if (imageView.visibility == View.GONE) {
                imageView.visibility = View.VISIBLE
                latexView.visibility = View.GONE
                infoButton.text = "+ Info"
            } else {
                imageView.visibility = View.GONE
                latexView.visibility = View.VISIBLE
                infoButton.text = "View"
            }
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
        instagramLink.setOnClickListener {
            openInstagramProfile()
        }

    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun openInstagramProfile() {
        val instagramUri = Uri.parse("http://instagram.com/random_plot")
        val intent = Intent(Intent.ACTION_VIEW, instagramUri)

        if (intent.resolveActivity(packageManager) != null) {
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        } else {
            intent.data = Uri.parse("http://instagram.com/random_plot")
            startActivity(intent)
        }
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap, context: Context): Uri {
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
