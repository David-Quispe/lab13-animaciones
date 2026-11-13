package com.example.lab13_animaciones.juego

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

// Fondo en capas que se mueven a distinta velocidad (efecto parallax).
// Las capas del suelo siguen la distancia recorrida del juego; las nubes y las
// luces usan una animación infinita que corre sola.
@Composable
fun FondoParallax(juego: JuegoEstado, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "fondo")
    val deriva by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "deriva"
    )
    val brillo by transicion.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brillo"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val ancho = size.width
        val alto = size.height
        val anchoCarretera = ancho * FRACCION_CARRETERA
        val margen = (ancho - anchoCarretera) / 2f
        val anchoCarril = anchoCarretera / 3f
        val recorrido = juego.distancia * alto

        // Capa 1 (la más lejana): césped con franjas, se mueve lento
        drawRect(color = Color(0xFF2E7D32))
        val periodo1 = alto / 5f
        var y1 = -periodo1 + (recorrido * 0.35f) % periodo1
        while (y1 < alto) {
            drawRect(
                color = Color.White.copy(alpha = 0.08f),
                topLeft = Offset(0f, y1),
                size = Size(ancho, periodo1 / 2f)
            )
            y1 += periodo1
        }

        // Capa 2: árboles a los lados, velocidad intermedia
        val periodo2 = alto / 4f
        var y2 = -periodo2 + (recorrido * 0.7f) % periodo2
        while (y2 < alto + periodo2) {
            drawCircle(
                color = Color(0xFF1B5E20),
                radius = margen * 0.28f,
                center = Offset(margen * 0.4f, y2)
            )
            drawCircle(
                color = Color(0xFF1B5E20),
                radius = margen * 0.28f,
                center = Offset(ancho - margen * 0.4f, y2 + periodo2 / 2f)
            )
            y2 += periodo2
        }

        // Capa 3: la carretera, a la misma velocidad que los obstáculos
        drawRect(
            color = Color(0xFF37474F),
            topLeft = Offset(margen, 0f),
            size = Size(anchoCarretera, alto)
        )
        drawRect(Color.White, Offset(margen - 4f, 0f), Size(4f, alto))
        drawRect(Color.White, Offset(margen + anchoCarretera, 0f), Size(4f, alto))

        val largoRaya = alto / 8f
        val periodo3 = largoRaya * 2f
        for (i in 1..2) {
            val x = margen + anchoCarril * i
            var y3 = -periodo3 + recorrido % periodo3
            while (y3 < alto) {
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(x, y3),
                    end = Offset(x, y3 + largoRaya),
                    strokeWidth = 6f
                )
                y3 += periodo3
            }
        }

        // Luces al borde de la carretera: se mueven con el suelo y titilan
        var y4 = -periodo2 + recorrido % periodo2
        while (y4 < alto + periodo2) {
            drawCircle(
                color = Color(0xFFFFEB3B).copy(alpha = brillo),
                radius = 7f,
                center = Offset(margen - 14f, y4)
            )
            drawCircle(
                color = Color(0xFFFFEB3B).copy(alpha = brillo),
                radius = 7f,
                center = Offset(margen + anchoCarretera + 14f, y4)
            )
            y4 += periodo2
        }

        // Capa del cielo: nubes que cruzan despacio por encima de todo
        val xNube1 = deriva * (ancho + 260f) - 130f
        val xNube2 = ((deriva + 0.5f) % 1f) * (ancho + 260f) - 130f
        drawOval(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(xNube1, alto * 0.18f),
            size = Size(230f, 70f)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(xNube2, alto * 0.55f),
            size = Size(180f, 55f)
        )
    }
}
