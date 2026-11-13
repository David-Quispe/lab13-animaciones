package com.example.lab13_animaciones.juego

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

// Alpaca dibujada con Canvas. "fase" va de 0 a 1 y se repite: con ella las patas
// se alternan y el cuerpo rebota al correr. La manta cambia de color con los golpes.
@Composable
fun Alpaca(
    fase: State<Float>,
    enSalto: Boolean,
    colorManta: Color,
    especie: Especie = ESPECIES[0],
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val angulo = fase.value * 2f * PI.toFloat()
        val paso = if (enSalto) 0f else sin(angulo)
        val rebote = if (enSalto) 0f else abs(sin(angulo)) * h * 0.04f

        val lana = especie.lana
        val lanaOscura = especie.lanaOscura
        val oscuro = Color(0xFF4E342E)

        // Patas: una sube mientras la otra baja
        drawRoundRect(
            color = oscuro,
            topLeft = Offset(w * 0.24f, h * 0.78f + paso * h * 0.07f),
            size = Size(w * 0.15f, h * 0.2f),
            cornerRadius = CornerRadius(w * 0.06f)
        )
        drawRoundRect(
            color = oscuro,
            topLeft = Offset(w * 0.61f, h * 0.78f - paso * h * 0.07f),
            size = Size(w * 0.15f, h * 0.2f),
            cornerRadius = CornerRadius(w * 0.06f)
        )

        translate(top = -rebote) {
            // Cuerpo
            drawOval(lana, Offset(w * 0.1f, h * 0.4f), Size(w * 0.8f, h * 0.5f))

            // Manta andina con franjas
            drawRoundRect(
                color = colorManta,
                topLeft = Offset(w * 0.18f, h * 0.52f),
                size = Size(w * 0.64f, h * 0.2f),
                cornerRadius = CornerRadius(w * 0.08f)
            )
            drawRect(Color(0xFFFFC107), Offset(w * 0.18f, h * 0.6f), Size(w * 0.64f, h * 0.035f))
            drawRect(Color.White, Offset(w * 0.18f, h * 0.66f), Size(w * 0.64f, h * 0.02f))

            // Orejas, cuello y cabeza
            drawOval(lana, Offset(w * 0.27f, h * 0.0f), Size(w * 0.1f, h * especie.orejaAlto))
            drawOval(lana, Offset(w * 0.63f, h * 0.0f), Size(w * 0.1f, h * especie.orejaAlto))
            drawRoundRect(
                color = lana,
                topLeft = Offset(w * 0.36f, h * 0.16f),
                size = Size(w * 0.28f, h * 0.42f),
                cornerRadius = CornerRadius(w * 0.12f)
            )
            drawOval(lana, Offset(w * 0.28f, h * 0.02f), Size(w * 0.44f, h * 0.3f))

            if (especie.pechoBlanco) {
                drawOval(Color.White, Offset(w * 0.4f, h * 0.34f), Size(w * 0.2f, h * 0.2f))
            }

            // Penacho de lana, hocico y ojos
            drawCircle(lanaOscura, w * 0.08f, Offset(w * 0.5f, h * 0.05f))
            drawOval(lanaOscura, Offset(w * 0.39f, h * 0.17f), Size(w * 0.22f, h * 0.12f))
            drawCircle(oscuro, w * 0.03f, Offset(w * 0.42f, h * 0.14f))
            drawCircle(oscuro, w * 0.03f, Offset(w * 0.58f, h * 0.14f))
            drawCircle(oscuro, w * 0.015f, Offset(w * 0.46f, h * 0.23f))
            drawCircle(oscuro, w * 0.015f, Offset(w * 0.54f, h * 0.23f))
        }
    }
}
