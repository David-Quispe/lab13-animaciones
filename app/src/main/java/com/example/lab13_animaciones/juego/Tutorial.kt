package com.example.lab13_animaciones.juego

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tutorial animado: una mano muestra el deslizamiento lateral y el salto
@Composable
fun PanelTutorial(juego: JuegoEstado) {
    val transicion = rememberInfiniteTransition(label = "tutorial")
    val lado by transicion.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lado"
    )
    val subida by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "subida"
    )

    PanelBase {
        Text("Cómo jugar", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

        // Demostración 1: cambiar de carril
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👆",
                fontSize = 40.sp,
                modifier = Modifier.offset(x = (lado * 90).dp)
            )
        }
        Text(
            text = "Desliza a los lados para cambiar de carril",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        // Demostración 2: saltar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👆",
                fontSize = 40.sp,
                modifier = Modifier
                    .offset(y = (20 - subida * 40).dp)
                    .graphicsLayer { alpha = 1f - subida * 0.7f }
            )
        }
        Text(
            text = "Desliza hacia arriba para saltar los cactus. Las combis y las rocas del Misti no se saltan: esquívalas.",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Button(onClick = { juego.terminarTutorial() }) {
            Text("¡A correr!")
        }
    }
}
