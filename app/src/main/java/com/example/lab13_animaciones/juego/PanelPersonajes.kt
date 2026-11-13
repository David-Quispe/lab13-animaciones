package com.example.lab13_animaciones.juego

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PanelPersonajes(juego: JuegoEstado) {
    // Los tres se animan con la misma fase para no crear tres transiciones
    val fase: State<Float> = rememberInfiniteTransition(label = "vitrina").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vitrinaFase"
    )

    PanelBase {
        Text("Personajes", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Monedas guardadas: ${juego.monedasTotales}",
            color = Color(0xFFFFC107),
            fontSize = 16.sp
        )
        for (especie in ESPECIES) {
            val desbloqueada = especie.id in juego.desbloqueados
            val activa = especie.id == juego.personajeActivo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = if (activa) 0.25f else 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Alpaca(
                    fase = fase,
                    enSalto = false,
                    colorManta = especie.manta,
                    especie = especie,
                    modifier = Modifier.size(64.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(especie.nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (desbloqueada) "Desbloqueada" else "Cuesta ${especie.costo} monedas",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
                Button(
                    onClick = { juego.elegirPersonaje(especie) },
                    enabled = !activa && (desbloqueada || juego.monedasTotales >= especie.costo)
                ) {
                    Text(
                        when {
                            activa -> "En uso"
                            desbloqueada -> "Usar"
                            else -> "Comprar"
                        }
                    )
                }
            }
        }
        Button(onClick = { juego.irAlMenu() }) {
            Text("Volver")
        }
    }
}
