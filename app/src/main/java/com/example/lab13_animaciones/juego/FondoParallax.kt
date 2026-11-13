package com.example.lab13_animaciones.juego

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate

// Colores del paisaje en cada momento del día
private class Paleta(
    val cesped: Color,
    val carretera: Color,
    val arboles: Color,
    val cieloArriba: Color,
    val cieloAbajo: Color,
    val astro: Color,
    val montana: Color,
    val tinte: Color
)

private val PALETA_DIA = Paleta(
    cesped = Color(0xFF2E7D32),
    carretera = Color(0xFF37474F),
    arboles = Color(0xFF1B5E20),
    cieloArriba = Color(0xFF4FC3F7),
    cieloAbajo = Color(0xFFB3E5FC),
    astro = Color(0xFFFFEB3B),
    montana = Color(0xFF78909C),
    tinte = Color(0x00000000)
)

private val PALETA_TARDE = Paleta(
    cesped = Color(0xFF6B8E3A),
    carretera = Color(0xFF45424D),
    arboles = Color(0xFF33691E),
    cieloArriba = Color(0xFFE64A19),
    cieloAbajo = Color(0xFFFFCC80),
    astro = Color(0xFFFF6D00),
    montana = Color(0xFF8D6E7A),
    tinte = Color(0x33FF6F00)
)

private val PALETA_NOCHE = Paleta(
    cesped = Color(0xFF0F2E19),
    carretera = Color(0xFF161C22),
    arboles = Color(0xFF06210E),
    cieloArriba = Color(0xFF050A1E),
    cieloAbajo = Color(0xFF1A237E),
    astro = Color(0xFFE0E0E0),
    montana = Color(0xFF0D1330),
    tinte = Color(0x66001133)
)

private val ESTRELLAS_X = floatArrayOf(0.08f, 0.2f, 0.52f, 0.64f, 0.9f, 0.45f)
private val ESTRELLAS_Y = floatArrayOf(0.2f, 0.55f, 0.15f, 0.5f, 0.3f, 0.7f)
private val LINEAS_X = floatArrayOf(0.05f, 0.15f, 0.28f, 0.4f, 0.55f, 0.68f, 0.8f, 0.92f)

@Composable
private fun animar(objetivo: Color): Color =
    animateColorAsState(
        targetValue = objetivo,
        animationSpec = tween(durationMillis = 2500),
        label = "paleta"
    ).value

// Fondo en capas: cielo con el volcán Misti, y un suelo cuyas capas se mueven a
// distinta velocidad (parallax). El paisaje pasa de día a atardecer y a noche.
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

    // 0 = día, 1 = atardecer, 2 = noche; cambia cada cierta distancia recorrida
    val momento by remember { derivedStateOf { (juego.distancia / 5f).toInt() % 3 } }
    val paleta = when (momento) {
        0 -> PALETA_DIA
        1 -> PALETA_TARDE
        else -> PALETA_NOCHE
    }
    val cesped = animar(paleta.cesped)
    val carretera = animar(paleta.carretera)
    val arboles = animar(paleta.arboles)
    val cieloArriba = animar(paleta.cieloArriba)
    val cieloAbajo = animar(paleta.cieloAbajo)
    val astro = animar(paleta.astro)
    val montana = animar(paleta.montana)
    val tinte = animar(paleta.tinte)
    val luzNoche by animateFloatAsState(
        targetValue = if (momento == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 2500),
        label = "luzNoche"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val ancho = size.width
        val alto = size.height
        val horizonte = alto * FRACCION_HORIZONTE
        val altoSuelo = alto - horizonte
        val anchoCarretera = ancho * FRACCION_CARRETERA
        val margen = (ancho - anchoCarretera) / 2f
        val anchoCarril = anchoCarretera / 3f
        val recorrido = juego.distancia * altoSuelo

        // ---------- Cielo ----------
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(cieloArriba, cieloAbajo),
                startY = 0f,
                endY = horizonte
            ),
            size = Size(ancho, horizonte)
        )
        for (i in ESTRELLAS_X.indices) {
            drawCircle(
                color = Color.White.copy(alpha = luzNoche * (0.3f + 0.7f * brillo)),
                radius = 2.5f,
                center = Offset(ancho * ESTRELLAS_X[i], horizonte * ESTRELLAS_Y[i])
            )
        }

        // Sol o luna
        val centroAstro = Offset(ancho * 0.8f, horizonte * 0.42f)
        drawCircle(
            color = astro.copy(alpha = 0.25f),
            radius = horizonte * 0.34f * (0.9f + 0.1f * brillo),
            center = centroAstro
        )
        drawCircle(color = astro, radius = horizonte * 0.2f, center = centroAstro)

        // Nubes que cruzan despacio
        val xNube1 = deriva * (ancho + 260f) - 130f
        val xNube2 = ((deriva + 0.5f) % 1f) * (ancho + 260f) - 130f
        drawOval(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(xNube1, horizonte * 0.22f),
            size = Size(150f, 34f)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(xNube2, horizonte * 0.5f),
            size = Size(110f, 26f)
        )

        // Volcán Misti con su cumbre nevada
        val cumbreX = ancho * 0.35f
        val cumbreY = horizonte * 0.18f
        val silueta = Path().apply {
            moveTo(-ancho * 0.05f, horizonte)
            lineTo(cumbreX - ancho * 0.28f, horizonte * 0.7f)
            lineTo(cumbreX - ancho * 0.07f, cumbreY + horizonte * 0.05f)
            lineTo(cumbreX - ancho * 0.02f, cumbreY)
            lineTo(cumbreX + ancho * 0.03f, cumbreY)
            lineTo(cumbreX + ancho * 0.08f, cumbreY + horizonte * 0.06f)
            lineTo(cumbreX + ancho * 0.35f, horizonte * 0.8f)
            lineTo(ancho * 1.05f, horizonte)
            close()
        }
        drawPath(path = silueta, color = montana)
        val nieve = Path().apply {
            moveTo(cumbreX - ancho * 0.02f, cumbreY)
            lineTo(cumbreX + ancho * 0.03f, cumbreY)
            lineTo(cumbreX + ancho * 0.08f, cumbreY + horizonte * 0.06f)
            lineTo(cumbreX + ancho * 0.05f, cumbreY + horizonte * 0.09f)
            lineTo(cumbreX + ancho * 0.025f, cumbreY + horizonte * 0.06f)
            lineTo(cumbreX, cumbreY + horizonte * 0.1f)
            lineTo(cumbreX - ancho * 0.03f, cumbreY + horizonte * 0.07f)
            lineTo(cumbreX - ancho * 0.07f, cumbreY + horizonte * 0.05f)
            close()
        }
        drawPath(path = nieve, color = Color.White.copy(alpha = 0.9f))

        // ---------- Suelo (bajo el horizonte) ----------
        translate(top = horizonte) {
            clipRect(left = 0f, top = 0f, right = ancho, bottom = altoSuelo) {
                // Capa lejana: césped con franjas, se mueve lento
                drawRect(color = cesped, size = Size(ancho, altoSuelo))
                val periodo1 = altoSuelo / 5f
                var y1 = -periodo1 + (recorrido * 0.35f) % periodo1
                while (y1 < altoSuelo) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.07f),
                        topLeft = Offset(0f, y1),
                        size = Size(ancho, periodo1 / 2f)
                    )
                    y1 += periodo1
                }

                // Capa intermedia: árboles a los lados
                val periodo2 = altoSuelo / 4f
                var y2 = -periodo2 + (recorrido * 0.7f) % periodo2
                while (y2 < altoSuelo + periodo2) {
                    drawCircle(
                        color = arboles,
                        radius = margen * 0.28f,
                        center = Offset(margen * 0.4f, y2)
                    )
                    drawCircle(
                        color = arboles,
                        radius = margen * 0.28f,
                        center = Offset(ancho - margen * 0.4f, y2 + periodo2 / 2f)
                    )
                    y2 += periodo2
                }

                // Capa cercana: la carretera, a la velocidad de los obstáculos
                drawRect(
                    color = carretera,
                    topLeft = Offset(margen, 0f),
                    size = Size(anchoCarretera, altoSuelo)
                )
                drawRect(Color.White, Offset(margen - 4f, 0f), Size(4f, altoSuelo))
                drawRect(Color.White, Offset(margen + anchoCarretera, 0f), Size(4f, altoSuelo))

                val largoRaya = altoSuelo / 8f
                val periodo3 = largoRaya * 2f
                for (i in 1..2) {
                    val x = margen + anchoCarril * i
                    var y3 = -periodo3 + recorrido % periodo3
                    while (y3 < altoSuelo) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(x, y3),
                            end = Offset(x, y3 + largoRaya),
                            strokeWidth = 6f
                        )
                        y3 += periodo3
                    }
                }

                // Faroles: titilan y de noche dejan un halo de luz
                var y4 = -periodo2 + recorrido % periodo2
                while (y4 < altoSuelo + periodo2) {
                    for (x in listOf(margen - 14f, margen + anchoCarretera + 14f)) {
                        drawCircle(
                            color = Color(0xFFFFEB3B).copy(alpha = 0.22f * luzNoche * brillo),
                            radius = 28f,
                            center = Offset(x, y4)
                        )
                        drawCircle(
                            color = Color(0xFFFFEB3B).copy(alpha = brillo),
                            radius = 7f,
                            center = Offset(x, y4)
                        )
                    }
                    y4 += periodo2
                }
            }
        }

        // Líneas de velocidad: aparecen cuando el juego va rápido
        val nivel = juego.nivelVelocidad
        if (nivel > 0.3f) {
            val alfa = ((nivel - 0.3f) / 0.7f).coerceIn(0f, 1f) * 0.35f
            val largoLinea = altoSuelo * 0.12f
            val periodoLinea = altoSuelo * 0.6f
            for (i in LINEAS_X.indices) {
                var yl = (recorrido * 2.2f + i * 97f) % periodoLinea - largoLinea
                while (yl < altoSuelo) {
                    val ini = yl.coerceAtLeast(0f)
                    val fin = yl + largoLinea
                    if (fin > ini) {
                        drawLine(
                            color = Color.White.copy(alpha = alfa),
                            start = Offset(ancho * LINEAS_X[i], horizonte + ini),
                            end = Offset(ancho * LINEAS_X[i], horizonte + fin),
                            strokeWidth = 3f
                        )
                    }
                    yl += periodoLinea
                }
            }
        }

        // Tinte general según la hora del día
        drawRect(color = tinte, size = Size(ancho, alto))
    }
}
