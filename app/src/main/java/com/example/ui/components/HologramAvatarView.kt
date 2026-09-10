package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AvatarState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 100% Native Jetpack Compose 3D Hologram Avatar.
 *
 * Renders a fully hardware-accelerated 3D faceted cyber-core on Android Compose Canvas
 * with real-time perspective projection, dual gyroscopic orbital rings, energy pulse core,
 * ambient particle field, and interactive drag-to-rotate gestures.
 *
 * This completely replaces legacy WebViews and eliminates all Chromium/MESA DRM rendernode errors.
 */
@Composable
fun HologramAvatarView(
    avatarState: AvatarState,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    // User touch interaction angles
    var userYaw by remember { mutableFloatStateOf(0f) }
    var userPitch by remember { mutableFloatStateOf(0f) }

    // State-dependent color schemes and animation speeds
    val accentColor = when (avatarState) {
        AvatarState.IDLE -> NeonCyan
        AvatarState.LISTENING -> CyberPink
        AvatarState.THINKING -> CyberAmber
        AvatarState.SPEAKING -> CyberEmerald
    }

    val stateText = when (avatarState) {
        AvatarState.IDLE -> "CAMPUS AI READY"
        AvatarState.LISTENING -> "LISTENING TO VOICE..."
        AvatarState.THINKING -> "RAG ENGINE THINKING..."
        AvatarState.SPEAKING -> "SYNTHESIZING AUDIO..."
    }

    // Infinite transitions for continuous fluid rotation and pulse
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_anim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (avatarState) {
                    AvatarState.THINKING -> 3500
                    AvatarState.SPEAKING -> 6000
                    AvatarState.LISTENING -> 5000
                    AvatarState.IDLE -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val gyroAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (avatarState) {
                    AvatarState.THINKING -> 4000
                    else -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gyro"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = when (avatarState) {
            AvatarState.SPEAKING -> 1.35f
            AvatarState.LISTENING -> 1.25f
            AvatarState.THINKING -> 1.2f
            AvatarState.IDLE -> 1.05f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (avatarState) {
                    AvatarState.SPEAKING -> 450
                    AvatarState.LISTENING -> 650
                    AvatarState.THINKING -> 500
                    AvatarState.IDLE -> 1200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(CyberDarkBg)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    userYaw += dragAmount.x * 0.01f
                    userPitch -= dragAmount.y * 0.01f
                }
            }
            .testTag("native_hologram_avatar_view")
    ) {
        // Native 3D Compose Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val canvasHeight = size.height
            val centerX = width / 2f
            val centerY = canvasHeight / 2f
            val baseRadius = minOf(width, canvasHeight) * 0.28f

            // 1. Holographic Scanning Grid & Ambient Radial Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.22f * pulseScale),
                        accentColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.8f
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.8f
            )

            // Horizontal scanning line
            val currentScanY = canvasHeight * scanlineY
            drawLine(
                color = accentColor.copy(alpha = 0.35f),
                start = Offset(0f, currentScanY),
                end = Offset(width, currentScanY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )

            // 2. 3D Mathematical Icosahedron Geometry Projection
            val phi = (1.0 + sqrt(5.0)) / 2.0
            val norm = sqrt(1.0 + phi * phi)
            val v1 = (1.0 / norm).toFloat()
            val vPhi = (phi / norm).toFloat()

            // 12 Normalized 3D Vertices
            val rawVertices = arrayOf(
                floatArrayOf(-v1, vPhi, 0f),
                floatArrayOf(v1, vPhi, 0f),
                floatArrayOf(-v1, -vPhi, 0f),
                floatArrayOf(v1, -vPhi, 0f),
                floatArrayOf(0f, -v1, vPhi),
                floatArrayOf(0f, v1, vPhi),
                floatArrayOf(0f, -v1, -vPhi),
                floatArrayOf(0f, v1, -vPhi),
                floatArrayOf(vPhi, 0f, -v1),
                floatArrayOf(vPhi, 0f, v1),
                floatArrayOf(-vPhi, 0f, -v1),
                floatArrayOf(-vPhi, 0f, v1)
            )

            // 30 Standard Icosahedron Edges
            val edges = arrayOf(
                intArrayOf(0, 11), intArrayOf(0, 5), intArrayOf(0, 1), intArrayOf(0, 7), intArrayOf(0, 10),
                intArrayOf(1, 5), intArrayOf(1, 9), intArrayOf(1, 8), intArrayOf(1, 7),
                intArrayOf(2, 11), intArrayOf(2, 4), intArrayOf(2, 3), intArrayOf(2, 6), intArrayOf(2, 10),
                intArrayOf(3, 9), intArrayOf(3, 4), intArrayOf(3, 8), intArrayOf(3, 6),
                intArrayOf(4, 5), intArrayOf(4, 9), intArrayOf(4, 11),
                intArrayOf(5, 9), intArrayOf(5, 11),
                intArrayOf(6, 7), intArrayOf(6, 8), intArrayOf(6, 10),
                intArrayOf(7, 8), intArrayOf(7, 10),
                intArrayOf(8, 9), intArrayOf(10, 11)
            )

            // Rotation angles (radians)
            val radY = ((rotationAngle * (PI / 180.0)).toFloat()) + userYaw
            val radX = (sin(rotationAngle * 0.015f) * 0.35f) + userPitch
            val cosY = cos(radY)
            val sinY = sin(radY)
            val cosX = cos(radX)
            val sinX = sin(radX)

            // Camera distance for perspective projection
            val cameraDist = 3.5f
            val projectedPoints = Array(rawVertices.size) { Offset.Zero }
            val vertexDepths = FloatArray(rawVertices.size)

            for (i in rawVertices.indices) {
                val x0 = rawVertices[i][0]
                val y0 = rawVertices[i][1]
                val z0 = rawVertices[i][2]

                // Rotate around Y-axis
                val x1 = (x0 * cosY + z0 * sinY)
                val z1 = (-x0 * sinY + z0 * cosY)

                // Rotate around X-axis
                val y2 = (y0 * cosX - z1 * sinX)
                val z2 = (y0 * sinX + z1 * cosX)

                // Perspective projection
                val fov = cameraDist / (cameraDist + z2)
                val projX = centerX + x1 * baseRadius * pulseScale * fov
                val projY = centerY + y2 * baseRadius * pulseScale * fov

                projectedPoints[i] = Offset(projX, projY)
                vertexDepths[i] = z2
            }

            // Draw Core Pulse Glow
            drawCircle(
                color = accentColor.copy(alpha = 0.35f * pulseScale),
                radius = baseRadius * 0.45f * pulseScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = baseRadius * 0.12f * pulseScale,
                center = Offset(centerX, centerY)
            )

            // Draw 3D Edges
            for (edge in edges) {
                val p1 = projectedPoints[edge[0]]
                val p2 = projectedPoints[edge[1]]
                val avgZ = (vertexDepths[edge[0]] + vertexDepths[edge[1]]) / 2f
                val alpha = ((avgZ + 1.2f) / 2.4f).coerceIn(0.18f, 0.95f)

                drawLine(
                    color = accentColor.copy(alpha = alpha),
                    start = p1,
                    end = p2,
                    strokeWidth = if (avgZ > 0) 1.8f else 1.0f
                )
            }

            // Draw Vertex Nodes
            for (i in projectedPoints.indices) {
                val p = projectedPoints[i]
                val z = vertexDepths[i]
                val nodeAlpha = ((z + 1.2f) / 2.4f).coerceIn(0.3f, 1.0f)
                val nodeRadius = (3.5f * ((z + 1.5f) / 2.5f)).coerceIn(1.5f, 4.5f)

                drawCircle(
                    color = Color.White.copy(alpha = nodeAlpha),
                    radius = nodeRadius,
                    center = p
                )
                drawCircle(
                    color = accentColor.copy(alpha = nodeAlpha * 0.6f),
                    radius = nodeRadius * 1.8f,
                    center = p
                )
            }

            // 3. Dual Gyro Orbital Rings with 3D Depth
            val ringPoints1 = 36
            val ringPath1 = Path()
            val gyroRad1 = (gyroAngle * (PI / 180.0)).toFloat()

            for (i in 0..ringPoints1) {
                val theta = (i.toFloat() / ringPoints1) * 2f * PI.toFloat()
                val rx = cos(theta) * baseRadius * 1.5f
                val ry = sin(theta) * baseRadius * 1.5f

                // Rotate ring by gyro angle
                val ringX = rx * cos(gyroRad1) - ry * sin(gyroRad1) * 0.4f
                val ringY = rx * sin(gyroRad1) * 0.5f + ry * 0.55f

                val px = centerX + ringX
                val py = centerY + ringY

                if (i == 0) ringPath1.moveTo(px, py) else ringPath1.lineTo(px, py)
            }

            drawPath(
                path = ringPath1,
                color = accentColor.copy(alpha = 0.5f),
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), gyroAngle * 0.5f)
                )
            )

            // Inner Counter-Rotating Ring
            val ringPath2 = Path()
            val gyroRad2 = (-gyroAngle * 1.3f * (PI / 180.0)).toFloat()

            for (i in 0..ringPoints1) {
                val theta = (i.toFloat() / ringPoints1) * 2f * PI.toFloat()
                val rx = cos(theta) * baseRadius * 1.2f
                val ry = sin(theta) * baseRadius * 1.2f

                val ringX = rx * cos(gyroRad2) + ry * sin(gyroRad2) * 0.35f
                val ringY = -rx * sin(gyroRad2) * 0.35f + ry * 0.4f

                val px = centerX + ringX
                val py = centerY + ringY

                if (i == 0) ringPath2.moveTo(px, py) else ringPath2.lineTo(px, py)
            }

            drawPath(
                path = ringPath2,
                color = NeonCyanLight.copy(alpha = 0.35f),
                style = Stroke(
                    width = 1.0f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), -gyroAngle)
                )
            )

            // 4. Ambient Sparkle Nodes
            val particleCount = 8
            for (p in 0 until particleCount) {
                val angle = (p * (2 * PI / particleCount) + (rotationAngle * 0.02f)).toFloat()
                val dist = baseRadius * (1.1f + 0.3f * sin(p.toFloat() + rotationAngle * 0.05f))
                val px = centerX + cos(angle) * dist
                val py = centerY + sin(angle) * dist * 0.7f

                drawCircle(
                    color = accentColor.copy(alpha = 0.6f),
                    radius = 1.5f,
                    center = Offset(px, py)
                )
            }
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(CyberCardDark.copy(alpha = 0.85f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stateText,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Text(
                text = "CAMPUS·AI v2.6",
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        // Bottom HUD Grid Metric
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 8.dp)
        ) {
            Text(
                text = "NEURAL CORE // 60 FPS • TOUCH INTERACTIVE",
                color = accentColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}
