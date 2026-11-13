package com.example.lab13_animaciones.juego

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Franja roja intermitente sobre los carriles por donde van a caer las rocas.
// Se dibuja como trapecio para que siga la perspectiva de la carretera.
@Composable
fun AvisoErupcion(
    juego: JuegoEstado,
    anchoPx: Float,
    altoPx: Float,
    margenPx: Float,
    anchoCarrilPx: Float,
    modifier: Modifier = Modifier
) {
    val densidad = LocalDensity.current
    val parpadeo by rememberInfiniteTransition(label = "avisoTransicion").animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avisoParpadeo"
    )
    val centro = anchoPx / 2f
    fun xBorde(borde: Int, k: Float): Float = centro + (margenPx + borde * anchoCarrilPx - centro) * k

    Canvas(modifier = modifier.fillMaxSize()) {
        val kLejos = factorPerspectiva(0f)
        for (carril in 0..2) {
            if (((juego.avisoMascara shr carril) and 1) == 1) {
                val franja = Path().apply {
                    moveTo(xBorde(carril, kLejos), 0f)
                    lineTo(xBorde(carril + 1, kLejos), 0f)
                    lineTo(xBorde(carril + 1, 1f), altoPx)
                    lineTo(xBorde(carril, 1f), altoPx)
                    close()
                }
                drawPath(franja, Color(0xFFFF1744).copy(alpha = parpadeo))
            }
        }
    }

    val kSigno = factorPerspectiva(0.25f)
    for (carril in 0..2) {
        AnimatedVisibility(
            visible = ((juego.avisoMascara shr carril) and 1) == 1,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier
                .offset {
                    val mitad = (xBorde(carril, kSigno) + xBorde(carril + 1, kSigno)) / 2f
                    IntOffset((mitad - anchoCarrilPx * kSigno / 2f).roundToInt(), 0)
                }
                .width(with(densidad) { (anchoCarrilPx * kSigno).toDp() })
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = "!",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

// Cartel que baja desde arriba para dar avisos (erupción, logros, rocoto...)
@Composable
fun BannerAnuncio(juego: JuegoEstado, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = juego.anuncio,
        transitionSpec = {
            (slideInVertically { alto -> -alto } + fadeIn()) togetherWith
                (slideOutVertically { alto -> -alto } + fadeOut())
        },
        modifier = modifier,
        label = "anuncio"
    ) { texto ->
        if (texto != null) {
            Text(
                text = texto,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(0.dp))
        }
    }
}
