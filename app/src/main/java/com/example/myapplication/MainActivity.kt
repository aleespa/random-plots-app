package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.random.Random
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.util.Base64


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_layout)

        // Initialize Python (You should call this only once in your app)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val generateButton = findViewById<Button>(R.id.generateButton)
        val imageView = findViewById<ImageView>(R.id.imageView)

        generateButton.setOnClickListener {
            // Call the generate_image function to generate a new plot each time
            val py = Python.getInstance()
            val mainModule = py.getModule("main")
            val imageBase64 = mainModule.callAttr("generate").toJava(String::class.java)

            // Decode the base64 image to bytes
            val imageBytes = Base64.getDecoder().decode(imageBase64.toByteArray())

            // Convert bytes to a Bitmap and display it in the ImageView
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        }
    }
}

@Composable
fun GreetingWithButton(name: String, modifier: Modifier = Modifier) {
    var textColor by remember { mutableStateOf(Color.Black) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hello $name!",
            color = textColor,
            fontWeight = FontWeight(600)
        )
        Button(onClick = {
            textColor = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
        },
            shape = RectangleShape) {
            Text("Change Color")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingWithButtonPreview() {

    MyApplicationTheme {
        GreetingWithButton("Android")
    }
}
