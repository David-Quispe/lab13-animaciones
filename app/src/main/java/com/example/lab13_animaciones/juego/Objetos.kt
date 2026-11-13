package com.example.lab13_animaciones.juego

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max

// Moneda que gira: se aplasta y se estira en horizontal como si diera vueltas
@Composable
fun MonedaView(giro: State<Float>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = max(abs(cos(giro.value * 2f * PI.toFloat())), 0.18f)
        }
    ) {
        val r = size.minDimension / 2f
        drawCircle(Color(0xFFFF8F00), r, center)
        drawCircle(Color(0xFFFFC107), r * 0.86f, center)
        drawCircle(Color(0xFFFFE082), r * 0.6f, center)
        drawCircle(Color(0xFFFFC107), r * 0.5f, center)
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = r * 0.12f,
            center = Offset(center.x - r * 0.35f, center.y - r * 0.35f)
        )
    }
}

// Valla de obra: tabla con franjas amarillas y negras sobre dos postes
@Composable
fun ValaView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val poste = Color(0xFF424242)
        drawRoundRect(poste, Offset(w * 0.12f, h * 0.35f), Size(w * 0.12f, h * 0.6f), CornerRadius(w * 0.03f))
        drawRoundRect(poste, Offset(w * 0.76f, h * 0.35f), Size(w * 0.12f, h * 0.6f), CornerRadius(w * 0.03f))

        val izquierda = w * 0.02f
        val anchoTabla = w * 0.96f
        val arriba = h * 0.15f
        val altoTabla = h * 0.38f
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(izquierda, arriba),
            size = Size(anchoTabla, altoTabla),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        val franjas = 8
        val anchoFranja = anchoTabla / franjas
        clipRect(
            left = izquierda + 3f,
            top = arriba + 3f,
            right = izquierda + anchoTabla - 3f,
            bottom = arriba + altoTabla - 3f
        ) {
            for (i in 0 until franjas step 2) {
                drawRect(
                    color = Color(0xFFFFC107),
                    topLeft = Offset(izquierda + i * anchoFranja, arriba),
                    size = Size(anchoFranja, altoTabla)
                )
            }
        }
        // Luces de advertencia
        drawCircle(Color(0xFFFF1744), w * 0.045f, Offset(w * 0.18f, arriba - h * 0.04f))
        drawCircle(Color(0xFFFF1744), w * 0.045f, Offset(w * 0.82f, arriba - h * 0.04f))
    }
}

// Cactus con dos brazos, espinas y una flor (este obstáculo se puede saltar)
@Composable
fun CactusView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val verde = Color(0xFF2E7D32)
        val verdeClaro = Color(0xFF43A047)

        drawOval(Color(0xFFD7B98E), Offset(w * 0.15f, h * 0.82f), Size(w * 0.7f, h * 0.16f))

        // Brazos
        drawRoundRect(verde, Offset(w * 0.12f, h * 0.5f), Size(w * 0.3f, h * 0.12f), CornerRadius(w * 0.06f))
        drawRoundRect(verde, Offset(w * 0.12f, h * 0.3f), Size(w * 0.13f, h * 0.32f), CornerRadius(w * 0.065f))
        drawRoundRect(verde, Offset(w * 0.58f, h * 0.42f), Size(w * 0.3f, h * 0.12f), CornerRadius(w * 0.06f))
        drawRoundRect(verde, Offset(w * 0.75f, h * 0.22f), Size(w * 0.13f, h * 0.32f), CornerRadius(w * 0.065f))

        // Tronco
        drawRoundRect(verdeClaro, Offset(w * 0.36f, h * 0.08f), Size(w * 0.28f, h * 0.82f), CornerRadius(w * 0.14f))

        // Espinas y flor
        for (i in 0 until 5) {
            val y = h * (0.2f + i * 0.13f)
            val lado = if (i % 2 == 0) 1f else -1f
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(w * 0.5f, y),
                end = Offset(w * 0.5f + w * 0.05f * lado, y - h * 0.02f),
                strokeWidth = 3f
            )
        }
        drawCircle(Color(0xFFFF4081), w * 0.06f, Offset(w * 0.5f, h * 0.08f))
    }
}

// Power-up de escudo
@Composable
fun EscudoView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val escudo = Path().apply {
            moveTo(w * 0.5f, h * 0.06f)
            lineTo(w * 0.88f, h * 0.2f)
            lineTo(w * 0.88f, h * 0.5f)
            quadraticBezierTo(w * 0.88f, h * 0.82f, w * 0.5f, h * 0.96f)
            quadraticBezierTo(w * 0.12f, h * 0.82f, w * 0.12f, h * 0.5f)
            lineTo(w * 0.12f, h * 0.2f)
            close()
        }
        drawPath(path = escudo, color = Color(0xFF0277BD))
        drawPath(path = escudo, color = Color(0xFF81D4FA), style = Stroke(width = w * 0.06f))
        drawRect(Color.White.copy(alpha = 0.9f), Offset(w * 0.46f, h * 0.25f), Size(w * 0.08f, h * 0.45f))
        drawRect(Color.White.copy(alpha = 0.9f), Offset(w * 0.3f, h * 0.4f), Size(w * 0.4f, h * 0.08f))
    }
}

// Power-up de imán en forma de herradura
@Composable
fun ImanView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rojo = Color(0xFFE53935)
        drawArc(
            color = rojo,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.15f),
            size = Size(w * 0.6f, w * 0.6f),
            style = Stroke(width = w * 0.18f)
        )
        drawRect(rojo, Offset(w * 0.11f, h * 0.45f), Size(w * 0.18f, h * 0.3f))
        drawRect(rojo, Offset(w * 0.71f, h * 0.45f), Size(w * 0.18f, h * 0.3f))
        drawRect(Color.White, Offset(w * 0.11f, h * 0.72f), Size(w * 0.18f, h * 0.12f))
        drawRect(Color.White, Offset(w * 0.71f, h * 0.72f), Size(w * 0.18f, h * 0.12f))
    }
}
