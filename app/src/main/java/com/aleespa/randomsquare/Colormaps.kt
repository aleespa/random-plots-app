package com.aleespa.randomsquare

enum class Colormaps(
    val key: String,
    val colorlist: List<Int>,
    val isFractalSpecific: Boolean = false
) {
    SOLID_WHITE(
        "Solid White",
        listOf(
            0xFFFFFFFF.toInt(),
            0xFFFFFFFF.toInt()
        )
    ),
    SOLID_BLACK(
        "Solid Black",
        listOf(
            0xFF000000.toInt(),
            0xFF000000.toInt()
        )
    ),
    RAINBOW(
        "Rainbow",
        listOf(
            0xFFFF0000.toInt(), // Red
            0xFFFFFF00.toInt(), // Yellow
            0xFF00FF00.toInt(), // Green
            0xFF00FFFF.toInt(), // Cyan
            0xFF0000FF.toInt(), // Blue
            0xFFFF00FF.toInt()  // Magenta
        ),
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
    ),
    VIRIDIS(
        "Viridis",
        listOf(
            0xFF440154.toInt(), // RGBA(68, 1, 84, 255)
            0xFF472777.toInt(), // RGBA(71, 39, 119, 255)
            0xFF3E4989.toInt(), // RGBA(62, 73, 137, 255)
            0xFF30678D.toInt(), // RGBA(48, 103, 141, 255)
            0xFF25828E.toInt(), // RGBA(37, 130, 142, 255)
            0xFF1E9D88.toInt(), // RGBA(30, 157, 136, 255)
            0xFF35B778.toInt(), // RGBA(53, 183, 120, 255)
            0xFF6DCE58.toInt(), // RGBA(109, 206, 88, 255)
            0xFFB5DD2B.toInt(), // RGBA(181, 221, 43, 255)
            0xFFFDE724.toInt(), // RGBA(253, 231, 36, 255)
        ),
        isFractalSpecific = true
    ),
    PLASMA(
        "Plasma",
        listOf(
            0xFF0C0786.toInt(), // RGBA(12, 7, 134, 255)
            0xFF45039E.toInt(), // RGBA(69, 3, 158, 255)
            0xFF7200A8.toInt(), // RGBA(114, 0, 168, 255)
            0xFF9B179E.toInt(), // RGBA(155, 23, 158, 255)
            0xFFBC3685.toInt(), // RGBA(188, 54, 133, 255)
            0xFFD7576B.toInt(), // RGBA(215, 87, 107, 255)
            0xFFEC7853.toInt(), // RGBA(236, 120, 83, 255)
            0xFFFA9F3A.toInt(), // RGBA(250, 159, 58, 255)
            0xFFFCC926.toInt(), // RGBA(252, 201, 38, 255)
            0xFFEFF821.toInt(), // RGBA(239, 248, 33, 255)
        )
    ),
    WHITE_WHITE(
        "White",
        listOf(
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt(),
            0xFFFFFFFF.toInt()
        ),
        isFractalSpecific = true
    ),
    BLACK_BLACK(
        "Black",
        listOf(
            0xFF000000.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    RED_BLACK(
        "Red",
        listOf(
            0xFF000000.toInt(),
            0xFFFF0000.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    PLASMA_FRACTAL(
        "Plasma",
        listOf(
            0xFF000000.toInt(), // Start with black
            0xFF45039E.toInt(),
            0xFF7200A8.toInt(),
            0xFF9B179E.toInt(),
            0xFFBC3685.toInt(),
            0xFFD7576B.toInt(),
            0xFFEC7853.toInt(),
            0xFFFA9F3A.toInt(),
            0xFFFCC926.toInt(),
            0xFF000000.toInt(), // End with black
        ),
        isFractalSpecific = true
    ),
    MAGMA_FRACTAL(
        "Magma",
        listOf(
            0xFF000000.toInt(),
            0xFF3B0F70.toInt(),
            0xFF8C2981.toInt(),
            0xFFDE4968.toInt(),
            0xFFFE9F6D.toInt(),
            0xFFFCFDBF.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    ICE_FRACTAL(
        "Ice",
        listOf(
            0xFFFFFFFF.toInt(),
            0xFF00FFFF.toInt(),
            0xFF0080FF.toInt(),
            0xFF000080.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),

    BLUE_FRACTAL(
        "Blue",
        listOf(
            0xFF000000.toInt(),
            0xFF000080.toInt(),
            0xFF0080FF.toInt(),
            0xFF00FFFF.toInt(),
            0xFFFFFFFF.toInt(),
        ),
        isFractalSpecific = true
    ),
    EMERALD_FRACTAL(
        "Emerald",
        listOf(
            0xFF000000.toInt(),
            0xFF004D00.toInt(),
            0xFF00FF00.toInt(),
            0xFF99FF99.toInt(),
            0xFFFFFFFF.toInt()
        ),
        isFractalSpecific = true
    ),
    FIRE_FRACTAL(
        "Fire",
        listOf(
            0xFF000000.toInt(),
            0xFF800000.toInt(),
            0xFFFF0000.toInt(),
            0xFFFFA500.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFFFFFF.toInt()
        ),
        isFractalSpecific = true
    ),
    ELECTRIC_FRACTAL(
        "Electric",
        listOf(
            0xFF000000.toInt(),
            0xFF4B0082.toInt(),
            0xFF8A2BE2.toInt(),
            0xFFDDA0DD.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    ;
}
