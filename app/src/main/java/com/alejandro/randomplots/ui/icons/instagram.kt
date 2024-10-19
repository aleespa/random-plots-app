package com.alejandro.randomplots.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.Suppress

val InstagramLogo: ImageVector
    get() {
        if (_InstagramLogo != null) {
            return _InstagramLogo!!
        }
        _InstagramLogo = ImageVector.Builder(
            name = "InstagramLogo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(8f, 3f)
                curveTo(5.243f, 3f, 3f, 5.243f, 3f, 8f)
                lineTo(3f, 16f)
                curveTo(3f, 18.757f, 5.243f, 21f, 8f, 21f)
                lineTo(16f, 21f)
                curveTo(18.757f, 21f, 21f, 18.757f, 21f, 16f)
                lineTo(21f, 8f)
                curveTo(21f, 5.243f, 18.757f, 3f, 16f, 3f)
                lineTo(8f, 3f)
                close()
                moveTo(8f, 5f)
                lineTo(16f, 5f)
                curveTo(17.654f, 5f, 19f, 6.346f, 19f, 8f)
                lineTo(19f, 16f)
                curveTo(19f, 17.654f, 17.654f, 19f, 16f, 19f)
                lineTo(8f, 19f)
                curveTo(6.346f, 19f, 5f, 17.654f, 5f, 16f)
                lineTo(5f, 8f)
                curveTo(5f, 6.346f, 6.346f, 5f, 8f, 5f)
                close()
                moveTo(17f, 6f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 7f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17f, 8f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 7f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17f, 6f)
                close()
                moveTo(12f, 7f)
                curveTo(9.243f, 7f, 7f, 9.243f, 7f, 12f)
                curveTo(7f, 14.757f, 9.243f, 17f, 12f, 17f)
                curveTo(14.757f, 17f, 17f, 14.757f, 17f, 12f)
                curveTo(17f, 9.243f, 14.757f, 7f, 12f, 7f)
                close()
                moveTo(12f, 9f)
                curveTo(13.654f, 9f, 15f, 10.346f, 15f, 12f)
                curveTo(15f, 13.654f, 13.654f, 15f, 12f, 15f)
                curveTo(10.346f, 15f, 9f, 13.654f, 9f, 12f)
                curveTo(9f, 10.346f, 10.346f, 9f, 12f, 9f)
                close()
            }
        }.build()

        return _InstagramLogo!!
    }

@Suppress("ObjectPropertyName")
private var _InstagramLogo: ImageVector? = null
