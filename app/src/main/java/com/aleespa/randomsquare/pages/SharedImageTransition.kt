package com.aleespa.randomsquare.pages

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.aleespa.randomsquare.Figures

/**
 * Shared-element transition between a figure thumbnail (a Browse card or a Gallery image) and the
 * Visualize image: the thumbnail grows into the image when opened, and predictive back plays it in
 * reverse under the finger. [MainScreen] provides both scopes; outside it the modifier is a no-op.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Duration of the shared image motion; the screens' own fades in [BottomNavGraph] follow it. */
const val SHARED_IMAGE_MS = 450

object SharedImageKeys {
    fun figure(figure: Figures) = "figure-${figure.key}"
    fun gallery(id: Int) = "gallery-$id"
}

/** Marks this element as one end of the shared image transition identified by [key]. */
@Composable
fun Modifier.sharedImage(key: String, shape: Shape): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedScope.current ?: return this
    val layerShift = remember { LayerShift() }
    return with(sharedScope) {
        this@sharedImage
            .compensateLayerTranslation(sharedScope, layerShift)
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedScope,
                // The outgoing picture stays opaque until the incoming one has faded in, so the
                // moving image is never see-through.
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(100, delayMillis = 200)),
                boundsTransform = { _, _ -> tween(SHARED_IMAGE_MS, easing = FastOutSlowInEasing) },
                clipInOverlayDuringTransition = OverlayClip(shape),
            )
    }
}

private class LayerShift {
    var x = 0
    var y = 0
}

/**
 * Shared elements take their bounds from layout positions, which leave out graphics-layer
 * translations. The Browse carousel shifts each card with one (to pack the masked items), so the
 * image used to land a few pixels left of the card and then jump. This measures that shift,
 * places the content by it (so the layout position matches what is on screen) and draws it back
 * by the same amount (so nothing moves visibly).
 */
private fun Modifier.compensateLayerTranslation(scope: SharedTransitionScope, shift: LayerShift) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val lookingAhead = isLookingAhead
        layout(placeable.width, placeable.height) {
            val coords = coordinates
            if (!lookingAhead && coords != null) {
                with(scope) {
                    val root = lookaheadScopeCoordinates
                    val onScreen = root.localPositionOf(coords, Offset.Zero)
                    val laidOut = root.localLookaheadPositionOf(coords)
                    shift.x = (onScreen.x - laidOut.x).roundToInt()
                    shift.y = (onScreen.y - laidOut.y).roundToInt()
                }
            }
            val dx = shift.x
            val dy = shift.y
            placeable.placeWithLayer(dx, dy) {
                translationX = -dx.toFloat()
                translationY = -dy.toFloat()
            }
        }
    }
