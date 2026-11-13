package com.example.lab13_animaciones.juego

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Hud(juego: JuegoEstado, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                repeat(VIDAS_INICIALES) { i ->
                    Corazon(activo = i < juego.vidas)
                }
            }

            TarjetaPuntaje(juego)

            Button(
                onClick = { juego.pausar() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Pausa")
            }
        }

        // Barras de duración de los power-ups (siempre reservan su espacio para
        // que el campo de juego no cambie de tamaño al aparecer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BarraPoder(
                visible = juego.escudoActivo,
                etiqueta = "Escudo",
                color = Color(0xFF00E5FF),
                progreso = { juego.progresoEscudo },
                modifier = Modifier.weight(1f)
            )
            BarraPoder(
                visible = juego.imanActivo,
                etiqueta = "Imán",
                color = Color(0xFFAB47BC),
                progreso = { juego.progresoIman },
                modifier = Modifier.weight(1f)
            )
            BarraPoder(
                visible = juego.lentoActivo,
                etiqueta = "Lento",
                color = Color(0xFF29B6F6),
                progreso = { juego.progresoLento },
                modifier = Modifier.weight(1f)
            )
            BarraPoder(
                visible = juego.dobleActivo,
                etiqueta = "x2",
                color = Color(0xFFFFC107),
                progreso = { juego.progresoDoble },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TarjetaPuntaje(juego: JuegoEstado) {
    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${juego.puntaje}",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            // El contador de monedas "rueda" hacia arriba cada vez que cambia
            AnimatedContent(
                targetState = juego.monedas,
                transitionSpec = {
                    (slideInVertically { altura -> -altura } + fadeIn()) togetherWith
                        (slideOutVertically { altura -> altura } + fadeOut())
                },
                label = "contadorMonedas"
            ) { cantidad ->
                Text(text = "Monedas: $cantidad", color = Color(0xFFFFC107), fontSize = 13.sp)
            }
            // El multiplicador crece con la racha de monedas y "salta" al subir
            AnimatedContent(
                targetState = juego.multiplicador,
                transitionSpec = { (scaleIn(initialScale = 2f) + fadeIn()) togetherWith fadeOut() },
                label = "multiplicador"
            ) { m ->
                Text(
                    text = "  x$m",
                    color = if (m > 1) Color(0xFFFF9800) else Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BarraPoder(
    visible: Boolean,
    etiqueta: String,
    color: Color,
    progreso: () -> Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(16.dp)) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = etiqueta,
                    color = color,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .drawBehind {
                            drawRect(color = color, size = Size(size.width * progreso(), size.height))
                        }
                )
            }
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
