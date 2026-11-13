package com.example.lab13_animaciones.juego

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// Combi vista de frente, como las que recorren Arequipa. Ya no se puede saltar:
// hay que cambiar de carril.
@Composable
fun CombiView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ruedas
        val llanta = Color(0xFF212121)
        drawRoundRect(llanta, Offset(w * 0.08f, h * 0.78f), Size(w * 0.16f, h * 0.2f), CornerRadius(w * 0.04f))
        drawRoundRect(llanta, Offset(w * 0.76f, h * 0.78f), Size(w * 0.16f, h * 0.2f), CornerRadius(w * 0.04f))

        // Carrocería con franjas azul y amarilla
        drawRoundRect(Color(0xFFF5F5F5), Offset(w * 0.05f, h * 0.1f), Size(w * 0.9f, h * 0.75f), CornerRadius(w * 0.1f))
        drawRect(Color(0xFF1565C0), Offset(w * 0.05f, h * 0.6f), Size(w * 0.9f, h * 0.12f))
        drawRect(Color(0xFFFFC107), Offset(w * 0.05f, h * 0.72f), Size(w * 0.9f, h * 0.04f))

        // Letrero del techo
        drawRoundRect(Color(0xFFFFC107), Offset(w * 0.3f, h * 0.0f), Size(w * 0.4f, h * 0.12f), CornerRadius(w * 0.03f))

        // Parabrisas con reflejo
        drawRoundRect(Color(0xFF263238), Offset(w * 0.14f, h * 0.17f), Size(w * 0.72f, h * 0.3f), CornerRadius(w * 0.06f))
        drawRoundRect(Color.White.copy(alpha = 0.25f), Offset(w * 0.18f, h * 0.2f), Size(w * 0.2f, h * 0.12f), CornerRadius(w * 0.03f))

        // Faros y parrilla
        drawCircle(Color(0xFFFFEB3B), w * 0.07f, Offset(w * 0.2f, h * 0.55f))
        drawCircle(Color(0xFFFFEB3B), w * 0.07f, Offset(w * 0.8f, h * 0.55f))
        drawRoundRect(Color(0xFF424242), Offset(w * 0.38f, h * 0.5f), Size(w * 0.24f, h * 0.08f), CornerRadius(w * 0.02f))
    }
}
