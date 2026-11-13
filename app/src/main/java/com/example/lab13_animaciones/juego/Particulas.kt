package com.example.lab13_animaciones.juego

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

// Dibuja todas las partículas (chispas, polvo, explosión). Las posiciones se leen
// dentro del dibujo, así que no se recompone nada en cada frame.
@Composable
fun CapaParticulas(juego: JuegoEstado, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in juego.particulas) {
            val vida = (p.vida / p.vidaMax).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = vida),
                radius = p.radio * (0.4f + 0.6f * vida),
                center = Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}
