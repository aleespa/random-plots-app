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
        )
    ),
    MAGMA(
        "Magma",
        listOf(
            0xFF000004.toInt(),
            0xFF1B0C41.toInt(),
            0xFF4B0C6B.toInt(),
            0xFF781C6D.toInt(),
            0xFFA52C60.toInt(),
            0xFFCF4446.toInt(),
            0xFFED6925.toInt(),
            0xFFFB9B06.toInt(),
            0xFFF7D03C.toInt(),
            0xFFFCFFA4.toInt()
        )
    ),
    INFERNO(
        "Inferno",
        listOf(
            0xFF000004.toInt(),
            0xFF1B0C41.toInt(),
            0xFF4B0C6B.toInt(),
            0xFF781C6D.toInt(),
            0xFFA52C60.toInt(),
            0xFFCF4446.toInt(),
            0xFFED6925.toInt(),
            0xFFFB9B06.toInt(),
            0xFFF7D03C.toInt(),
            0xFFFCFFA4.toInt()
        )
    ),
    CIVIDIS(
        "Cividis",
        listOf(
            0xFF00224E.toInt(),
            0xFF123570.toInt(),
            0xFF3B4D71.toInt(),
            0xFF5A6572.toInt(),
            0xFF787E73.toInt(),
            0xFF969873.toInt(),
            0xFFB5B374.toInt(),
            0xFFD6CE76.toInt(),
            0xFFF9E978.toInt()
        )
    ),
    TURBO(
        "Turbo",
        listOf(
            0xFF30123B.toInt(),
            0xFF4145AD.toInt(),
            0xFF4675ED.toInt(),
            0xFF39A2FF.toInt(),
            0xFF19D4D6.toInt(),
            0xFF25ED94.toInt(),
            0xFF6AFA4A.toInt(),
            0xFFB1EC35.toInt(),
            0xFFE7C632.toInt(),
            0xFFFE942C.toInt(),
            0xFFF35914.toInt(),
            0xFFD23105.toInt(),
            0xFF911803.toInt(),
            0xFF7A0403.toInt()
        )
    ),
    TWILIGHT(
        "Twilight",
        listOf(
            0xFFE2D9E2.toInt(),
            0xFF9EB3D2.toInt(),
            0xFF585F91.toInt(),
            0xFF3F2D4A.toInt(),
            0xFF5C363C.toInt(),
            0xFF9B6E64.toInt(),
            0xFFD6B9B1.toInt(),
            0xFFE2D9E2.toInt()
        )
    ),
    SPECTRAL(
        "Spectral",
        listOf(
            0xFFD53E4F.toInt(),
            0xFFF46D43.toInt(),
            0xFFFDAE61.toInt(),
            0xFFFEE08B.toInt(),
            0xFFFFFFBF.toInt(),
            0xFFE6F598.toInt(),
            0xFFABDDA4.toInt(),
            0xFF66C2A5.toInt(),
            0xFF3288BD.toInt()
        )
    ),
    COOLWARM(
        "Coolwarm",
        listOf(
            0xFF3B4CC0.toInt(),
            0xFF688AEE.toInt(),
            0xFFB4C7F0.toInt(),
            0xFFF2F2F2.toInt(),
            0xFFF2BEA3.toInt(),
            0xFFD57B67.toInt(),
            0xFFB40426.toInt()
        )
    ),
    COPPER(
        "Copper",
        listOf(
            0xFF000000.toInt(),
            0xFF332014.toInt(),
            0xFF664028.toInt(),
            0xFF99603C.toInt(),
            0xFFCC8050.toInt(),
            0xFFFF9F64.toInt()
        )
    ),
    BONE(
        "Bone",
        listOf(
            0xFF000000.toInt(),
            0xFF1B1B27.toInt(),
            0xFF36364E.toInt(),
            0xFF515175.toInt(),
            0xFF6C6C9C.toInt(),
            0xFF8787C3.toInt(),
            0xFFA2A2DA.toInt(),
            0xFFBDBDF1.toInt(),
            0xFFD8D8F1.toInt(),
            0xFFFFFFFF.toInt()
        )
    ),
    SPRING(
        "Spring",
        listOf(
            0xFFFF00FF.toInt(),
            0xFFFFFF00.toInt()
        )
    ),
    SUMMER(
        "Summer",
        listOf(
            0xFF008066.toInt(),
            0xFFFFFF66.toInt()
        )
    ),
    AUTUMN(
        "Autumn",
        listOf(
            0xFFFF0000.toInt(),
            0xFFFF7F00.toInt(),
            0xFFFFFF00.toInt()
        )
    ),
    WINTER(
        "Winter",
        listOf(
            0xFF0000FF.toInt(),
            0xFF007FFF.toInt(),
            0xFF00FF7F.toInt()
        )
    ),
    HOT(
        "Hot",
        listOf(
            0xFF000000.toInt(),
            0xFF800000.toInt(),
            0xFFFF0000.toInt(),
            0xFFFFA500.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFFFFFF.toInt()
        )
    ),
    AFM_HOT(
        "AFM Hot",
        listOf(
            0xFF000000.toInt(),
            0xFF550000.toInt(),
            0xFFAA0000.toInt(),
            0xFFFF0000.toInt(),
            0xFFFF5500.toInt(),
            0xFFFFAA00.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFFFFAA.toInt(),
            0xFFFFFFFF.toInt()
        )
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
    COPPER_FRACTAL(
        "Copper",
        listOf(
            0xFF000000.toInt(),
            0xFF332014.toInt(),
            0xFF664028.toInt(),
            0xFF99603C.toInt(),
            0xFFCC8050.toInt(),
            0xFFFF9F64.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    BONE_FRACTAL(
        "Bone",
        listOf(
            0xFF000000.toInt(),
            0xFF36364E.toInt(),
            0xFF6C6C9C.toInt(),
            0xFFA2A2DA.toInt(),
            0xFFD8D8F1.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    HOT_FRACTAL(
        "Hot",
        listOf(
            0xFF000000.toInt(),
            0xFF800000.toInt(),
            0xFFFF0000.toInt(),
            0xFFFFA500.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    SPECTRAL_FRACTAL(
        "Spectral",
        listOf(
            0xFF000000.toInt(),
            0xFFD53E4F.toInt(),
            0xFFF46D43.toInt(),
            0xFFFDAE61.toInt(),
            0xFFFEE08B.toInt(),
            0xFFE6F598.toInt(),
            0xFFABDDA4.toInt(),
            0xFF66C2A5.toInt(),
            0xFF3288BD.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    COOLWARM_FRACTAL(
        "Coolwarm",
        listOf(
            0xFF000000.toInt(),
            0xFF3B4CC0.toInt(),
            0xFFB4C7F0.toInt(),
            0xFFF2F2F2.toInt(),
            0xFFF2BEA3.toInt(),
            0xFFB40426.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    MAGMA_FRACTAL_V2(
        "Magma Dark",
        listOf(
            0xFF000004.toInt(),
            0xFF1B0C41.toInt(),
            0xFF4B0C6B.toInt(),
            0xFF781C6D.toInt(),
            0xFFA52C60.toInt(),
            0xFFCF4446.toInt(),
            0xFFED6925.toInt(),
            0xFFFB9B06.toInt(),
            0xFFF7D03C.toInt(),
            0xFFFCFFA4.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    VIRIDIS_FRACTAL(
        "Viridis",
        listOf(
            0xFF000000.toInt(),
            0xFF440154.toInt(),
            0xFF3E4989.toInt(),
            0xFF25828E.toInt(),
            0xFF35B778.toInt(),
            0xFFB5DD2B.toInt(),
            0xFFFDE724.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    INFERNO_FRACTAL(
        "Inferno",
        listOf(
            0xFF000000.toInt(),
            0xFF1B0C41.toInt(),
            0xFF781C6D.toInt(),
            0xFFED6925.toInt(),
            0xFFF7D03C.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    CIVIDIS_FRACTAL(
        "Cividis",
        listOf(
            0xFF000000.toInt(),
            0xFF00224E.toInt(),
            0xFF5A6572.toInt(),
            0xFF969873.toInt(),
            0xFFF9E978.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    TURBO_FRACTAL(
        "Turbo",
        listOf(
            0xFF000000.toInt(),
            0xFF30123B.toInt(),
            0xFF39A2FF.toInt(),
            0xFF6AFA4A.toInt(),
            0xFFFE942C.toInt(),
            0xFF7A0403.toInt(),
            0xFF000000.toInt()
        ),
        isFractalSpecific = true
    ),
    ;
}
