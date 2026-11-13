package com.example.lab13_animaciones.juego

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

// Ejercicio final: prototipo de videojuego (corredor de tres carriles)
@Composable
fun Ejercicio5Screen(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val juego = remember {
        JuegoEstado(contexto.getSharedPreferences("lab13_juego", Context.MODE_PRIVATE))
    }

    // Sonidos con ToneGenerator (no necesita archivos de audio) y vibración al chocar
    val haptico = LocalHapticFeedback.current
    DisposableEffect(juego) {
        val tonos = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 55)
        } catch (e: RuntimeException) {
            null
        }
        juego.alEvento = { evento ->
            if (juego.sonidoActivo) {
                when (evento) {
                    EventoJuego.Moneda -> tonos?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
                    EventoJuego.Salto -> tonos?.startTone(ToneGenerator.TONE_DTMF_5, 70)
                    EventoJuego.PowerUp -> tonos?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                    EventoJuego.Golpe -> tonos?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
                    EventoJuego.GameOver -> tonos?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 400)
                }
            }
            if (evento == EventoJuego.Golpe) {
                haptico.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
        onDispose {
            juego.alEvento = null
            tonos?.release()
        }
    }

    // Bucle del juego: mientras la pantalla sea "Jugando" se actualiza en cada frame.
    // Al cambiar de pantalla el efecto se cancela solo, y por eso la pausa congela el juego.
    LaunchedEffect(juego.pantalla) {
        if (juego.pantalla == Pantalla.Jugando || juego.pantalla == Pantalla.GameOver) {
            var anterior = withFrameNanos { it }
            while (true) {
                val ahora = withFrameNanos { it }
                val dt = ((ahora - anterior) / 1_000_000_000f).coerceAtMost(0.05f)
                anterior = ahora
                juego.actualizar(dt)
            }
        }
    }

    // Efectos al recibir un golpe: la pantalla se sacude y destella en rojo
    val sacudida = remember { Animatable(0f) }
    val destello = remember { Animatable(0f) }
    LaunchedEffect(juego.golpes) {
        if (juego.golpes > 0) {
            launch {
                destello.snapTo(0.45f)
                destello.animateTo(0f, tween(450))
            }
            repeat(6) { i ->
                sacudida.animateTo(if (i % 2 == 0) 1f else -1f, tween(45))
            }
            sacudida.animateTo(0f, tween(45))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B12))
    ) {
        Hud(juego)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(juego) {
                    // Deslizar a los lados cambia de carril, deslizar hacia arriba salta
                    var dx = 0f
                    var dy = 0f
                    detectDragGestures(
                        onDragStart = {
                            dx = 0f
                            dy = 0f
                        },
                        onDragEnd = {
                            if (abs(dx) > abs(dy)) {
                                if (dx > 60f) juego.moverDerecha()
                                else if (dx < -60f) juego.moverIzquierda()
                            } else if (dy < -60f) {
                                juego.saltar()
                            }
                        }
                    ) { cambio, arrastre ->
                        cambio.consume()
                        dx += arrastre.x
                        dy += arrastre.y
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = sacudida.value * 16f }
            ) {
                CampoDeJuego(juego, Modifier.fillMaxSize())
                // Destello rojo al recibir un golpe (no bloquea los gestos)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = destello.value }
                        .background(Color(0xFFD50000))
                )
            }
            BannerAnuncio(juego, Modifier.align(Alignment.TopCenter))
            Superposicion(juego)
        }

        Controles(juego)
    }
}

// Capa que aparece encima del campo según la pantalla actual
@Composable
private fun Superposicion(juego: JuegoEstado) {
    AnimatedContent(
        targetState = juego.pantalla,
        transitionSpec = {
            (fadeIn(animationSpec = tween(350)) +
                scaleIn(initialScale = 0.85f, animationSpec = tween(350))) togetherWith
                fadeOut(animationSpec = tween(200))
        },
        modifier = Modifier.fillMaxSize(),
        label = "pantallas"
    ) { pantalla ->
        when (pantalla) {
            Pantalla.Jugando -> Spacer(modifier = Modifier.fillMaxSize())
            Pantalla.Inicio -> PanelInicio(juego)
            Pantalla.Pausa -> PanelPausa(juego)
            Pantalla.GameOver -> PanelGameOver(juego)
            Pantalla.Personajes -> PanelPersonajes(juego)
        }
    }
}

@Composable
fun PanelBase(contenido: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido
        )
    }
}

@Composable
private fun PanelInicio(juego: JuegoEstado) {
    PanelBase {
        Text("CORREDOR ANDINO", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Mejor puntaje: ${juego.mejorPuntaje}", color = Color(0xFFFFC107), fontSize = 16.sp)
        Text(
            text = "Desliza a los lados para cambiar de carril y hacia arriba para saltar. " +
                "Salta los cactus, esquiva las vallas y junta monedas.",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Button(onClick = { juego.iniciar() }) {
            Text("Jugar")
        }
        Button(onClick = { juego.abrirPersonajes() }) {
            Text("Personajes (${juego.monedasTotales} monedas)")
        }
        Button(onClick = { juego.alternarSonido() }) {
            Text(if (juego.sonidoActivo) "Sonido: sí" else "Sonido: no")
        }
    }
}

@Composable
private fun PanelPausa(juego: JuegoEstado) {
    PanelBase {
        Text("Pausa", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Button(onClick = { juego.reanudar() }) {
            Text("Reanudar")
        }
        Button(onClick = { juego.alternarSonido() }) {
            Text(if (juego.sonidoActivo) "Sonido: sí" else "Sonido: no")
        }
        Button(onClick = { juego.irAlMenu() }) {
            Text("Menú")
        }
    }
}

@Composable
private fun PanelGameOver(juego: JuegoEstado) {
    PanelBase {
        Text("GAME OVER", color = Color(0xFFFF5252), fontSize = 30.sp, fontWeight = FontWeight.Bold)
        if (juego.nuevoRecord) {
            Text("¡Nuevo récord!", color = Color(0xFFFFC107), fontSize = 18.sp)
        }
        Text("Puntaje: ${juego.puntaje}", color = Color.White, fontSize = 18.sp)
        Text("Monedas: ${juego.monedas}", color = Color.White, fontSize = 16.sp)
        Text("Mejor: ${juego.mejorPuntaje}", color = Color.White, fontSize = 16.sp)
        Button(onClick = { juego.iniciar() }) {
            Text("Reintentar")
        }
        Button(onClick = { juego.irAlMenu() }) {
            Text("Menú")
        }
    }
}

// Botones por si no se quiere usar el deslizamiento
@Composable
private fun Controles(juego: JuegoEstado) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { juego.moverIzquierda() }, modifier = Modifier.weight(1f)) {
            Text("◀")
        }
        Button(onClick = { juego.saltar() }, modifier = Modifier.weight(1.4f)) {
            Text("Saltar")
        }
        Button(onClick = { juego.moverDerecha() }, modifier = Modifier.weight(1f)) {
            Text("▶")
        }
    }
}
