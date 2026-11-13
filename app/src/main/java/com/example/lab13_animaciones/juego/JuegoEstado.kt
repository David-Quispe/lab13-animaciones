package com.example.lab13_animaciones.juego

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

const val VIDAS_INICIALES = 3
const val FRACCION_CARRETERA = 0.8f
const val FRACCION_HORIZONTE = 0.12f
const val POS_JUGADOR = 0.78f

private const val VELOCIDAD_INICIAL = 0.45f
private const val VELOCIDAD_MAXIMA = 1.3f
private const val ACELERACION = 0.012f
private const val RADIO_COLISION = 0.07f
private const val DURACION_SALTO = 0.7f
private const val DURACION_INVULNERABLE = 1.2f
private const val DURACION_ESCUDO = 6f
private const val DURACION_IMAN = 6f
private const val INTERVALO_BASE = 1.1f
private const val CLAVE_MEJOR = "mejor_puntaje"

private val COLORES_MONEDA = listOf(Color(0xFFFFC107), Color(0xFFFFE082), Color.White)
private val COLORES_ESCUDO = listOf(Color(0xFF00E5FF), Color(0xFF81D4FA), Color.White)
private val COLORES_IMAN = listOf(Color(0xFFAB47BC), Color(0xFFE1BEE7), Color.White)
private val COLORES_GOLPE = listOf(Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEB3B))

enum class Pantalla { Inicio, Jugando, Pausa, GameOver }

enum class TipoEntidad { ObstaculoAlto, ObstaculoBajo, Moneda, Escudo, Iman }

// Todo lo que baja por la carretera. La posición y va de 0 (arriba) a 1 (abajo).
class Entidad(val id: Int, val tipo: TipoEntidad, val carril: Int, yInicial: Float) {
    var y by mutableFloatStateOf(yInicial)
    var consumida by mutableStateOf(false)
    var tiempoConsumida = 0f
}

// Partícula visual (chispa, polvo...). x e y van de 0 a 1 dentro del campo.
class Particula(
    xInicial: Float,
    yInicial: Float,
    val vx: Float,
    val vy: Float,
    val vidaMax: Float,
    val color: Color,
    val radio: Float
) {
    var x by mutableFloatStateOf(xInicial)
    var y by mutableFloatStateOf(yInicial)
    var vida by mutableFloatStateOf(vidaMax)
}

class JuegoEstado(private val prefs: SharedPreferences) {

    var pantalla by mutableStateOf(Pantalla.Inicio)
        private set
    var carril by mutableIntStateOf(1)
        private set
    var saltando by mutableStateOf(false)
        private set
    var vidas by mutableIntStateOf(VIDAS_INICIALES)
        private set
    var monedas by mutableIntStateOf(0)
        private set
    var puntaje by mutableIntStateOf(0)
        private set
    var mejorPuntaje by mutableIntStateOf(prefs.getInt(CLAVE_MEJOR, 0))
        private set
    var nuevoRecord by mutableStateOf(false)
        private set
    var invulnerable by mutableStateOf(false)
        private set
    var escudoActivo by mutableStateOf(false)
        private set
    var imanActivo by mutableStateOf(false)
        private set
    // Cuántas "alturas de pantalla" ha recorrido el jugador (la usa el fondo)
    var distancia by mutableFloatStateOf(0f)
        private set
    // Qué tan rápido va el juego, de 0 (inicio) a 1 (velocidad máxima)
    var nivelVelocidad by mutableFloatStateOf(0f)
        private set

    // Cantidad de golpes recibidos: la pantalla lo usa para sacudirse y destellar
    var golpes by mutableIntStateOf(0)
        private set

    // Racha de monedas seguidas (sin recibir golpes) y su multiplicador de puntos
    var racha by mutableIntStateOf(0)
        private set
    val multiplicador: Int
        get() = 1 + minOf(racha / 8, 4)

    // Cuánto le queda al escudo y al imán (de 1 a 0), para las barras del HUD
    var progresoEscudo by mutableFloatStateOf(0f)
        private set
    var progresoIman by mutableFloatStateOf(0f)
        private set

    val entidades = mutableStateListOf<Entidad>()
    val particulas = mutableStateListOf<Particula>()

    private var velocidad = VELOCIDAD_INICIAL
    private var tiempoSalto = 0f
    private var tiempoInvulnerable = 0f
    private var tiempoEscudo = 0f
    private var tiempoIman = 0f
    private var tiempoGenerar = 0f
    private var puntosMonedas = 0
    private var tiempoPolvo = 0f
    private var siguienteId = 0

    fun iniciar() {
        entidades.clear()
        particulas.clear()
        carril = 1
        saltando = false
        vidas = VIDAS_INICIALES
        monedas = 0
        puntaje = 0
        distancia = 0f
        puntosMonedas = 0
        racha = 0
        progresoEscudo = 0f
        progresoIman = 0f
        nuevoRecord = false
        invulnerable = false
        escudoActivo = false
        imanActivo = false
        velocidad = VELOCIDAD_INICIAL
        nivelVelocidad = 0f
        tiempoSalto = 0f
        tiempoInvulnerable = 0f
        tiempoEscudo = 0f
        tiempoIman = 0f
        tiempoGenerar = 0.6f
        pantalla = Pantalla.Jugando
    }

    fun pausar() {
        if (pantalla == Pantalla.Jugando) pantalla = Pantalla.Pausa
    }

    fun reanudar() {
        if (pantalla == Pantalla.Pausa) pantalla = Pantalla.Jugando
    }

    fun irAlMenu() {
        entidades.clear()
        particulas.clear()
        carril = 1
        saltando = false
        invulnerable = false
        escudoActivo = false
        imanActivo = false
        pantalla = Pantalla.Inicio
    }

    fun moverIzquierda() {
        if (pantalla == Pantalla.Jugando && carril > 0) carril--
    }

    fun moverDerecha() {
        if (pantalla == Pantalla.Jugando && carril < 2) carril++
    }

    fun saltar() {
        if (pantalla == Pantalla.Jugando && !saltando) {
            saltando = true
            tiempoSalto = DURACION_SALTO
        }
    }

    // Se llama en cada frame desde el bucle del juego. dt viene en segundos.
    fun actualizar(dt: Float) {
        if (pantalla == Pantalla.GameOver) {
            // Las partículas de la explosión terminan de caer
            actualizarParticulas(dt)
            return
        }
        if (pantalla != Pantalla.Jugando) return

        velocidad = (velocidad + ACELERACION * dt).coerceAtMost(VELOCIDAD_MAXIMA)
        nivelVelocidad = ((velocidad - VELOCIDAD_INICIAL) / (VELOCIDAD_MAXIMA - VELOCIDAD_INICIAL)).coerceIn(0f, 1f)
        distancia += velocidad * dt
        puntaje = (distancia * 20f).toInt() + puntosMonedas

        if (saltando) {
            tiempoSalto -= dt
            if (tiempoSalto <= 0f) saltando = false
        }
        if (invulnerable) {
            tiempoInvulnerable -= dt
            if (tiempoInvulnerable <= 0f) invulnerable = false
        }
        if (escudoActivo) {
            tiempoEscudo -= dt
            progresoEscudo = (tiempoEscudo / DURACION_ESCUDO).coerceIn(0f, 1f)
            if (tiempoEscudo <= 0f) escudoActivo = false
        }
        if (imanActivo) {
            tiempoIman -= dt
            progresoIman = (tiempoIman / DURACION_IMAN).coerceIn(0f, 1f)
            if (tiempoIman <= 0f) imanActivo = false
        }

        tiempoGenerar -= dt
        if (tiempoGenerar <= 0f) {
            generar()
            tiempoGenerar = (INTERVALO_BASE * VELOCIDAD_INICIAL / velocidad).coerceAtLeast(0.45f)
        }

        // Polvo detrás de la alpaca mientras corre
        tiempoPolvo -= dt
        if (tiempoPolvo <= 0f && !saltando) {
            tiempoPolvo = 0.05f - 0.03f * nivelVelocidad
            particulas.add(
                Particula(
                    xInicial = xDeCarril(carril) + (Random.nextFloat() - 0.5f) * 0.06f,
                    yInicial = POS_JUGADOR + 0.07f,
                    vx = (Random.nextFloat() - 0.5f) * 0.05f,
                    vy = velocidad * 0.9f,
                    vidaMax = 0.4f,
                    color = Color(0xFFD7CCC8),
                    radio = 3f + Random.nextFloat() * 3f
                )
            )
        }

        val eliminar = mutableListOf<Entidad>()
        for (e in entidades) {
            e.y += velocidad * dt

            if (e.consumida) {
                e.tiempoConsumida += dt
                if (e.tiempoConsumida > 0.35f) eliminar.add(e)
                continue
            }
            if (e.y > 1.15f) {
                eliminar.add(e)
                continue
            }
            if (abs(e.y - POS_JUGADOR) > RADIO_COLISION) continue

            val mismoCarril = e.carril == carril
            when (e.tipo) {
                TipoEntidad.Moneda -> if (mismoCarril || imanActivo) {
                    e.consumida = true
                    puntosMonedas += 10 * multiplicador
                    racha++
                    monedas++
                    explosion(xDeCarril(e.carril), e.y, 8, COLORES_MONEDA, 0.3f)
                }
                TipoEntidad.Escudo -> if (mismoCarril) {
                    e.consumida = true
                    escudoActivo = true
                    tiempoEscudo = DURACION_ESCUDO
                    progresoEscudo = 1f
                    explosion(xDeCarril(e.carril), e.y, 12, COLORES_ESCUDO, 0.35f)
                }
                TipoEntidad.Iman -> if (mismoCarril) {
                    e.consumida = true
                    imanActivo = true
                    tiempoIman = DURACION_IMAN
                    progresoIman = 1f
                    explosion(xDeCarril(e.carril), e.y, 12, COLORES_IMAN, 0.35f)
                }
                TipoEntidad.ObstaculoAlto -> if (mismoCarril) golpear(e)
                // El obstáculo bajo se puede saltar
                TipoEntidad.ObstaculoBajo -> if (mismoCarril && !saltando) golpear(e)
            }
        }
        entidades.removeAll(eliminar)
        actualizarParticulas(dt)
    }

    private fun actualizarParticulas(dt: Float) {
        val muertas = mutableListOf<Particula>()
        for (p in particulas) {
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vida -= dt
            if (p.vida <= 0f) muertas.add(p)
        }
        particulas.removeAll(muertas)
    }

    // Posición horizontal (de 0 a 1) del centro de un carril
    private fun xDeCarril(c: Int): Float =
        (1f - FRACCION_CARRETERA) / 2f + FRACCION_CARRETERA * (c + 0.5f) / 3f

    private fun explosion(x: Float, y: Float, cantidad: Int, colores: List<Color>, fuerza: Float) {
        repeat(cantidad) {
            val angulo = Random.nextFloat() * 6.2832f
            val rapidez = fuerza * (0.4f + Random.nextFloat() * 0.6f)
            particulas.add(
                Particula(
                    xInicial = x,
                    yInicial = y,
                    vx = cos(angulo) * rapidez,
                    vy = sin(angulo) * rapidez,
                    vidaMax = 0.35f + Random.nextFloat() * 0.3f,
                    color = colores.random(),
                    radio = 4f + Random.nextFloat() * 5f
                )
            )
        }
        while (particulas.size > 150) particulas.removeAt(0)
    }

    private fun golpear(e: Entidad) {
        if (invulnerable) return
        e.consumida = true
        invulnerable = true
        golpes++
        racha = 0
        explosion(xDeCarril(carril), POS_JUGADOR, 18, COLORES_GOLPE, 0.5f)
        if (escudoActivo) {
            // El escudo absorbe un golpe
            escudoActivo = false
            tiempoEscudo = 0f
            tiempoInvulnerable = 0.5f
            return
        }
        tiempoInvulnerable = DURACION_INVULNERABLE
        vidas--
        if (vidas <= 0) terminar()
    }

    private fun terminar() {
        if (puntaje > mejorPuntaje) {
            mejorPuntaje = puntaje
            nuevoRecord = true
            prefs.edit().putInt(CLAVE_MEJOR, puntaje).apply()
        }
        pantalla = Pantalla.GameOver
    }

    private fun generar() {
        val carrilNuevo = Random.nextInt(3)
        val azar = Random.nextFloat()
        when {
            azar < 0.30f -> agregar(TipoEntidad.ObstaculoAlto, carrilNuevo, -0.1f)
            azar < 0.55f -> agregar(TipoEntidad.ObstaculoBajo, carrilNuevo, -0.1f)
            azar < 0.92f -> for (i in 0 until 3) {
                agregar(TipoEntidad.Moneda, carrilNuevo, -0.1f - i * 0.09f)
            }
            else -> agregar(
                if (Random.nextBoolean()) TipoEntidad.Escudo else TipoEntidad.Iman,
                carrilNuevo,
                -0.1f
            )
        }
    }

    private fun agregar(tipo: TipoEntidad, carril: Int, y: Float) {
        entidades.add(Entidad(siguienteId++, tipo, carril, y))
    }
}
