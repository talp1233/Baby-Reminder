package com.example.babyreminder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val iconScale = remember { Animatable(0.3f) }
    val titleAlpha = remember { Animatable(0f) }
    val loadingAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Bouncy scale-up for the icon
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Fade in the title
        titleAlpha.animateTo(1f, tween(500))
        // Fade in the loading indicator
        loadingAlpha.animateTo(1f, tween(300))
        // Hold for a moment
        delay(1500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Custom bell-with-baby icon drawn on Canvas
            BellBabyIcon(
                modifier = Modifier
                    .size(200.dp)
                    .scale(iconScale.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D),
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .alpha(loadingAlpha.value),
                color = Color(0xFFFF9800),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun BellBabyIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawBellWithBaby()
    }
}

private fun DrawScope.drawBellWithBaby() {
    val w = size.width
    val h = size.height

    val bellOrange = Color(0xFFFF9800)
    val darkOutline = Color(0xFF3D3D3D)
    val white = Color.White
    val redBorder = Color(0xFFE53935)
    val hairOrange = Color(0xFFFF6D00)

    // Outer red rounded rectangle border (matching the app icon border)
    drawRoundRect(
        color = redBorder,
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.15f),
        style = Stroke(width = w * 0.03f)
    )

    // Bell top knob (small circle at the very top)
    drawCircle(
        color = bellOrange,
        radius = w * 0.035f,
        center = Offset(w * 0.5f, h * 0.14f)
    )
    drawCircle(
        color = darkOutline,
        radius = w * 0.035f,
        center = Offset(w * 0.5f, h * 0.14f),
        style = Stroke(width = w * 0.015f)
    )

    // Connectors from knob to bell top
    drawLine(
        color = darkOutline,
        start = Offset(w * 0.47f, h * 0.17f),
        end = Offset(w * 0.47f, h * 0.21f),
        strokeWidth = w * 0.015f
    )
    drawLine(
        color = darkOutline,
        start = Offset(w * 0.53f, h * 0.17f),
        end = Offset(w * 0.53f, h * 0.21f),
        strokeWidth = w * 0.015f
    )

    // Bell body (orange, bell-shaped curve)
    val bellPath = Path().apply {
        // Start at top-left of bell
        moveTo(w * 0.40f, h * 0.22f)
        // Top straight edge
        lineTo(w * 0.60f, h * 0.22f)
        // Right side curve (flares out)
        cubicTo(
            w * 0.75f, h * 0.28f,
            w * 0.80f, h * 0.48f,
            w * 0.80f, h * 0.62f
        )
        // Right bottom flare
        cubicTo(
            w * 0.80f, h * 0.70f,
            w * 0.78f, h * 0.75f,
            w * 0.72f, h * 0.77f
        )
        // Bottom edge
        lineTo(w * 0.28f, h * 0.77f)
        // Left bottom flare
        cubicTo(
            w * 0.22f, h * 0.75f,
            w * 0.20f, h * 0.70f,
            w * 0.20f, h * 0.62f
        )
        // Left side curve (flares out)
        cubicTo(
            w * 0.20f, h * 0.48f,
            w * 0.25f, h * 0.28f,
            w * 0.40f, h * 0.22f
        )
        close()
    }

    // Draw bell outline first, then fill
    drawPath(bellPath, darkOutline, style = Stroke(width = w * 0.025f))
    drawPath(bellPath, bellOrange)

    // Bell clapper (small circle at bottom)
    drawCircle(
        color = bellOrange,
        radius = w * 0.03f,
        center = Offset(w * 0.5f, h * 0.82f)
    )
    drawCircle(
        color = darkOutline,
        radius = w * 0.03f,
        center = Offset(w * 0.5f, h * 0.82f),
        style = Stroke(width = w * 0.012f)
    )

    // White inner area (where the baby sits)
    drawRoundRect(
        color = white,
        topLeft = Offset(w * 0.28f, h * 0.32f),
        size = Size(w * 0.44f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.04f)
    )

    // ---- Baby drawing ----

    // Baby head (circle outline)
    val headCx = w * 0.5f
    val headCy = h * 0.42f
    val headR = w * 0.06f
    drawCircle(
        color = darkOutline,
        radius = headR,
        center = Offset(headCx, headCy),
        style = Stroke(width = w * 0.014f)
    )

    // Hair tuft (small orange circle on top of head)
    drawCircle(
        color = hairOrange,
        radius = w * 0.018f,
        center = Offset(headCx, headCy - headR + w * 0.005f)
    )

    // Eyes (two small filled dots)
    drawCircle(
        color = darkOutline,
        radius = w * 0.008f,
        center = Offset(headCx - w * 0.025f, headCy)
    )
    drawCircle(
        color = darkOutline,
        radius = w * 0.008f,
        center = Offset(headCx + w * 0.025f, headCy)
    )

    // Smile (small arc below eyes)
    drawArc(
        color = darkOutline,
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(headCx - w * 0.025f, headCy + w * 0.01f),
        size = Size(w * 0.05f, w * 0.03f),
        style = Stroke(width = w * 0.008f)
    )

    // Body (vertical line from neck down)
    drawLine(
        color = darkOutline,
        start = Offset(headCx, headCy + headR),
        end = Offset(headCx, h * 0.57f),
        strokeWidth = w * 0.012f,
        cap = StrokeCap.Round
    )

    // Seatbelt harness straps (V shape going to buckle at center)
    val shoulderY = headCy + headR + w * 0.015f
    val buckleY = h * 0.56f
    // Left strap
    drawLine(
        color = darkOutline,
        start = Offset(headCx - w * 0.05f, shoulderY),
        end = Offset(headCx, buckleY),
        strokeWidth = w * 0.01f,
        cap = StrokeCap.Round
    )
    // Right strap
    drawLine(
        color = darkOutline,
        start = Offset(headCx + w * 0.05f, shoulderY),
        end = Offset(headCx, buckleY),
        strokeWidth = w * 0.01f,
        cap = StrokeCap.Round
    )

    // Buckle circle at center
    drawCircle(
        color = darkOutline,
        radius = w * 0.012f,
        center = Offset(headCx, buckleY)
    )

    // Arms (going outward from shoulders)
    drawLine(
        color = darkOutline,
        start = Offset(headCx - w * 0.04f, shoulderY + w * 0.01f),
        end = Offset(headCx - w * 0.09f, shoulderY + w * 0.04f),
        strokeWidth = w * 0.01f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = darkOutline,
        start = Offset(headCx + w * 0.04f, shoulderY + w * 0.01f),
        end = Offset(headCx + w * 0.09f, shoulderY + w * 0.04f),
        strokeWidth = w * 0.01f,
        cap = StrokeCap.Round
    )

    // Legs (V shape from body bottom, sitting position)
    val legStartY = h * 0.57f
    val legEndY = h * 0.63f
    drawLine(
        color = darkOutline,
        start = Offset(headCx, legStartY),
        end = Offset(headCx - w * 0.06f, legEndY),
        strokeWidth = w * 0.01f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = darkOutline,
        start = Offset(headCx, legStartY),
        end = Offset(headCx + w * 0.06f, legEndY),
        strokeWidth = w * 0.01f,
        cap = StrokeCap.Round
    )

    // Small feet (circles at end of legs)
    drawCircle(
        color = darkOutline,
        radius = w * 0.01f,
        center = Offset(headCx - w * 0.06f, legEndY)
    )
    drawCircle(
        color = darkOutline,
        radius = w * 0.01f,
        center = Offset(headCx + w * 0.06f, legEndY)
    )
}
