package com.example.lab13_animaciones.juego

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Hud(juego: JuegoEstado, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            repeat(VIDAS_INICIALES) { i ->
                Corazon(activo = i < juego.vidas)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${juego.puntaje}",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            // El contador de monedas "rueda" hacia arriba cada vez que cambia
            AnimatedContent(
                targetState = juego.monedas,
                transitionSpec = {
                    (slideInVertically { altura -> -altura } + fadeIn()) togetherWith
                        (slideOutVertically { altura -> altura } + fadeOut())
                },
                label = "contadorMonedas"
            ) { cantidad ->
                Text(text = "Monedas: $cantidad", color = Color(0xFFFFC107), fontSize = 14.sp)
            }
        }

        Button(
            onClick = { juego.pausar() },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("Pausa")
        }
    }
}

@Composable
private fun Corazon(activo: Boolean) {
    val escala by animateFloatAsState(
        targetValue = if (activo) 1f else 0.55f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaCorazon"
    )
    Text(
        text = "❤",
        color = if (activo) Color(0xFFFF5252) else Color.Gray,
        fontSize = 26.sp,
        modifier = Modifier
            .padding(end = 4.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
    )
}
