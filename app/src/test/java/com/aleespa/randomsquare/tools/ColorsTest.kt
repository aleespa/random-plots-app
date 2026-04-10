package com.aleespa.randomsquare.tools

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ColorsTest {

    @Test
    fun testContrasted() {
        assertEquals(Color.White, Color.Black.contrasted())
        assertEquals(Color.Black, Color.White.contrasted())
        
        // Dark gray should result in White
        assertEquals(Color.White, Color(0xFF333333.toInt()).contrasted())
        
        // Light gray should result in Black
        assertEquals(Color.Black, Color(0xFFCCCCCC.toInt()).contrasted())
    }

    @Test
    fun testColorToHexWithoutAlpha() {
        assertEquals("#000000", colorToHexWithoutAlpha(Color.Black))
        assertEquals("#FFFFFF", colorToHexWithoutAlpha(Color.White))
        assertEquals("#FF0000", colorToHexWithoutAlpha(Color.Red))
        assertEquals("#00FF00", colorToHexWithoutAlpha(Color.Green))
        assertEquals("#0000FF", colorToHexWithoutAlpha(Color.Blue))
    }

    @Test
    fun testIsColorDark() {
        assertTrue(isColorDark(Color.Black.toArgb()))
        assertFalse(isColorDark(Color.White.toArgb()))
        
        // Testing some specific colors
        assertTrue(isColorDark(0xFF000080.toInt())) // Navy is dark
        assertFalse(isColorDark(0xFFFFFFE0.toInt())) // Light yellow is light
    }

    @Test
    fun testIntColorToHexWithoutAlpha() {
        assertEquals("#000000", intColorToHexWithoutAlpha(0xFF000000.toInt()))
        assertEquals("#FFFFFF", intColorToHexWithoutAlpha(0xFFFFFFFF.toInt()))
        assertEquals("#FF0000", intColorToHexWithoutAlpha(0xFFFF0000.toInt()))
    }

    @Test
    fun testHslConversion() {
        val red = Color.Red
        val hsl = red.toHsl()
        // Red is H=0, S=1, L=0.5
        assertEquals(0f, hsl[0], 0.01f)
        assertEquals(1f, hsl[1], 0.01f)
        assertEquals(0.5f, hsl[2], 0.01f)

        val recoveredColor = hslToColor(hsl[0], hsl[1], hsl[2])
        assertEquals(red.red, recoveredColor.red, 0.01f)
        assertEquals(red.green, recoveredColor.green, 0.01f)
        assertEquals(red.blue, recoveredColor.blue, 0.01f)
    }
}
