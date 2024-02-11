package com.alejandro.randomplots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.alejandro.randomplots.screens.MainScreen
import com.alejandro.randomplots.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}
//        checkAndRequestPermissions()
//        setContentView(R.layout.form_layout)
//
//        var darkModeBool = false
//
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(this))
//        }
//        val generateButton = findViewById<MaterialButton>(R.id.generateButton)
//        val wallpaperButton = findViewById<MaterialButton>(R.id.wallpaperButton)
//        val infoButton = findViewById<MaterialButton>(R.id.infoButton)
//        val imageView = findViewById<ImageView>(R.id.imageView)
//        val mainLayout = findViewById<RelativeLayout>(R.id.mainLayout)
//        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
//        val instagramLink: ImageView = findViewById(R.id.instagramLink)
//        val latexView = findViewById<JLatexMathView>(R.id.j_latex_math_view)
//        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//        cachedBitmap?.let {
//            imageView.setImageBitmap(it)
//        }
//
//        generateButton.setOnClickListener {
//            generateRandomPlot()
//        }
//
//        infoButton.setOnClickListener{
//            if (imageView.visibility == View.GONE) {
//                imageView.visibility = View.VISIBLE
//                latexView.visibility = View.GONE
//                infoButton.text = "+ Info"
//            } else {
//                imageView.visibility = View.GONE
//                latexView.visibility = View.VISIBLE
//                infoButton.text = "View"
//            }
//        }
//        wallpaperButton.setOnClickListener {
//            val localImageUri = imageUri
//            if (localImageUri == null) {
//                // Show a message that no image has been generated yet
//                Toast.makeText(this, "Please generate an image first", Toast.LENGTH_SHORT).show()
//            } else {
//                // Proceed with setting the wallpaper
//                try {
//                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
//                    val source = ImageDecoder.createSource(this.contentResolver, localImageUri)
//                    val bitmap = ImageDecoder.decodeBitmap(source)
//
//                    wallpaperManager.setBitmap(bitmap)
//
//                    Toast.makeText(this, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
//                } catch (e: Exception) {
//                    Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        instagramLink.setOnClickListener {
//            openInstagramProfile()
//        }
//
//    }
//
//
//    @SuppressLint("QueryPermissionsNeeded")
//    private fun openInstagramProfile() {
//        val instagramUri = Uri.parse("http://instagram.com/random_plot")
//        val intent = Intent(Intent.ACTION_VIEW, instagramUri)
//
//        if (intent.resolveActivity(packageManager) != null) {
//            intent.setPackage("com.instagram.android")
//            startActivity(intent)
//        } else {
//            intent.data = Uri.parse("http://instagram.com/random_plot")
//            startActivity(intent)
//        }
//    }
//    private fun saveImageToInternalStorage(bitmap: Bitmap, context: Context): Uri {
//        // Use the app's internal storage
//        val directory = context.cacheDir
//        val file = File(directory, "shared_image.png")  // Name of the image file
//
//        // Write the bitmap to a file
//        val stream = FileOutputStream(file)
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        stream.flush()
//        stream.close()
//
//        return Uri.fromFile(file)
//    }
//    private fun checkAndRequestPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
//        }
//    }

//}
