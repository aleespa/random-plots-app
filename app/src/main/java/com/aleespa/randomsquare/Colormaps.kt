package com.aleespa.randomsquare

enum class Colormaps(
    val key: String,
    val colorlist: List<Int>,
) {
    RAINBOW(
        "Rainbow",
        listOf(
            0xFFFF0000.toInt(), // Red
            0xFFFFFF00.toInt(), // Yellow
            0xFF00FF00.toInt(), // Green
            0xFF00FFFF.toInt(), // Cyan
            0xFF0000FF.toInt(), // Blue
            0xFFFF00FF.toInt()  // Magenta
        )
    ),

    HEAT(
        "Heat",
        listOf(
            0xFF000000.toInt(), // Black
            0xFFFF0000.toInt(), // Red
            0xFFFFFF00.toInt(), // Yellow
            0xFFFFFFFF.toInt()  // White
        )
    ),

    COOL(
        "Cool",
        listOf(
            0xFF00FFFF.toInt(), // Cyan
            0xFFFF00FF.toInt()  // Magenta
        )
    ),

    GRAYSCALE(
        "Grayscale",
        listOf(
            0xFF000000.toInt(), // Black
            0xFFFFFFFF.toInt()  // White
        )
    ),

    OCEAN(
        "Ocean",
        listOf(
            0xFF000080.toInt(), // Navy
            0xFF0000FF.toInt(), // Blue
            0xFF00FFFF.toInt(), // Cyan
            0xFFFFFFFF.toInt()  // White
        )
    ),

    FOREST(
        "Forest",
        listOf(
            0xFF003300.toInt(), // Dark Green
            0xFF228B22.toInt(), // Forest Green
            0xFFADFF2F.toInt()  // Green Yellow
        )
    ),

    SUNSET(
        "Sunset",
        listOf(
            0xFFFF4500.toInt(), // Orange Red
            0xFFFFA500.toInt(), // Orange
            0xFFFFFF00.toInt(), // Yellow
            0xFF000000.toInt()  // Black (night)
        )
    ),

    PASTEL(
        "Pastel",
        listOf(
            0xFFFFC0CB.toInt(), // Pink
            0xFFADD8E6.toInt(), // Light Blue
            0xFF90EE90.toInt(), // Light Green
            0xFFFFFFE0.toInt()  // Light Yellow
        )
    );
}
