package com.example.lab13_animaciones.juego

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Medidas del campo calculadas una sola vez a partir del tamaño disponible
private class Geometria(
    val altoPx: Float,
    val margenPx: Float,
    val anchoCarrilPx: Float,
    val tamanoPx: Float,
    val tamanoDp: Dp
) {
    fun xCarril(carril: Int): Float =
        margenPx + carril * anchoCarrilPx + (anchoCarrilPx - tamanoPx) / 2f
}

@Composable
fun CampoDeJuego(juego: JuegoEstado, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val densidad = LocalDensity.current
        val anchoPx = constraints.maxWidth.toFloat()
        val altoPx = constraints.maxHeight.toFloat()
        val horizontePx = altoPx * FRACCION_HORIZONTE
        val anchoCarrilPx = anchoPx * FRACCION_CARRETERA / 3f
        val tamanoPx = anchoCarrilPx * 0.62f
        val geometria = Geometria(
            altoPx = altoPx - horizontePx,
            margenPx = anchoPx * (1f - FRACCION_CARRETERA) / 2f,
            anchoCarrilPx = anchoCarrilPx,
            tamanoPx = tamanoPx,
            tamanoDp = with(densidad) { tamanoPx.toDp() }
        )

        FondoParallax(juego)

        // Todo lo que corre por la carretera nace en el horizonte y se recorta ahí
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = with(densidad) { horizontePx.toDp() })
                .clipToBounds()
        ) {
            for (entidad in juego.entidades) {
                key(entidad.id) {
                    EntidadView(entidad, geometria)
                }
            }
            JugadorView(juego, geometria)
        }
    }
}

@Composable
private fun EntidadView(entidad: Entidad, g: Geometria) {
    // Al recogerla (o chocar con ella) crece y se desvanece
    val escala by animateFloatAsState(
        targetValue = if (entidad.consumida) 1.8f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "escalaEntidad"
    )
    val opacidad by animateFloatAsState(
        targetValue = if (entidad.consumida) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "opacidadEntidad"
    )
    val x = g.xCarril(entidad.carril)

    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), (entidad.y * g.altoPx - g.tamanoPx / 2f).roundToInt()) }
            .size(g.tamanoDp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
                alpha = opacidad
            },
        contentAlignment = Alignment.Center
    ) {
        when (entidad.tipo) {
            TipoEntidad.ObstaculoAlto -> Etiqueta("🚧", Color(0xFFB71C1C), RoundedCornerShape(10.dp))
            TipoEntidad.ObstaculoBajo -> Etiqueta("🌵", Color(0xFFEF6C00), RoundedCornerShape(10.dp))
            TipoEntidad.Moneda -> Etiqueta("$", Color(0xFFFFC107), CircleShape, 0.7f)
            TipoEntidad.Escudo -> Etiqueta("E", Color(0xFF00B0FF), CircleShape, 0.75f)
            TipoEntidad.Iman -> Etiqueta("I", Color(0xFFAB47BC), CircleShape, 0.75f)
        }
    }
}

@Composable
private fun Etiqueta(texto: String, fondo: Color, forma: Shape, fraccion: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxSize(fraccion)
            .background(fondo, forma),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun JugadorView(juego: JuegoEstado, g: Geometria) {
    // Cambio de carril con rebote
    val x by animateFloatAsState(
        targetValue = g.xCarril(juego.carril),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "xJugador"
    )
    // Salto: 0 en el suelo, 1 en lo más alto
    val altura by animateFloatAsState(
        targetValue = if (juego.saltando) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "alturaSalto"
    )
    // Se pone rojo cuando recibe un golpe
    val color by animateColorAsState(
        targetValue = if (juego.invulnerable) Color(0xFFE53935) else Color(0xFF29B6F6),
        animationSpec = tween(durationMillis = 150),
        label = "colorJugador"
    )
    val parpadeo by rememberInfiniteTransition(label = "parpadeoTransicion").animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "parpadeo"
    )

    val ySuelo = POS_JUGADOR * g.altoPx - g.tamanoPx / 2f

    // Sombra que se queda en el suelo mientras el jugador salta
    Box(
        modifier = Modifier
            .offset { IntOffset((x + g.tamanoPx * 0.1f).roundToInt(), (ySuelo + g.tamanoPx * 0.85f).roundToInt()) }
            .size(width = g.tamanoDp * 0.8f, height = 10.dp)
            .graphicsLayer {
                scaleX = 1f - 0.35f * altura
                alpha = 1f - 0.4f * altura
            }
            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), (ySuelo - altura * g.tamanoPx * 0.9f).roundToInt()) }
            .size(g.tamanoDp)
            .graphicsLayer {
                val s = 1f + 0.3f * altura
                scaleX = s
                scaleY = s
                alpha = if (juego.invulnerable) parpadeo else 1f
            }
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "😎", fontSize = 26.sp)

        AnimatedVisibility(
            visible = juego.escudoActivo,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(g.tamanoDp + 10.dp)
                    .border(4.dp, Color(0xFF00E5FF), CircleShape)
            )
        }

        AnimatedVisibility(
            visible = juego.imanActivo,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(g.tamanoDp + 22.dp)
                    .border(3.dp, Color(0xFFAB47BC), CircleShape)
            )
        }
    }
}
