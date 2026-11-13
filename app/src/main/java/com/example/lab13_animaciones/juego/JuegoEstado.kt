package com.example.lab13_animaciones.juego

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.random.Random

const val VIDAS_INICIALES = 3
const val FRACCION_CARRETERA = 0.8f
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

enum class Pantalla { Inicio, Jugando, Pausa, GameOver }

enum class TipoEntidad { ObstaculoAlto, ObstaculoBajo, Moneda, Escudo, Iman }

// Todo lo que baja por la carretera. La posición y va de 0 (arriba) a 1 (abajo).
class Entidad(val id: Int, val tipo: TipoEntidad, val carril: Int, yInicial: Float) {
    var y by mutableFloatStateOf(yInicial)
    var consumida by mutableStateOf(false)
    var tiempoConsumida = 0f
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

    // Cantidad de golpes recibidos: la pantalla lo usa para sacudirse y destellar
    var golpes by mutableIntStateOf(0)
        private set

    val entidades = mutableStateListOf<Entidad>()

    private var velocidad = VELOCIDAD_INICIAL
    private var tiempoSalto = 0f
    private var tiempoInvulnerable = 0f
    private var tiempoEscudo = 0f
    private var tiempoIman = 0f
    private var tiempoGenerar = 0f
    private var siguienteId = 0

    fun iniciar() {
        entidades.clear()
        carril = 1
        saltando = false
        vidas = VIDAS_INICIALES
        monedas = 0
        puntaje = 0
        distancia = 0f
        nuevoRecord = false
        invulnerable = false
        escudoActivo = false
        imanActivo = false
        velocidad = VELOCIDAD_INICIAL
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
        if (pantalla != Pantalla.Jugando) return

        velocidad = (velocidad + ACELERACION * dt).coerceAtMost(VELOCIDAD_MAXIMA)
        distancia += velocidad * dt
        puntaje = (distancia * 20f).toInt() + monedas * 10

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
            if (tiempoEscudo <= 0f) escudoActivo = false
        }
        if (imanActivo) {
            tiempoIman -= dt
            if (tiempoIman <= 0f) imanActivo = false
        }

        tiempoGenerar -= dt
        if (tiempoGenerar <= 0f) {
            generar()
            tiempoGenerar = (INTERVALO_BASE * VELOCIDAD_INICIAL / velocidad).coerceAtLeast(0.45f)
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
                    monedas++
                }
                TipoEntidad.Escudo -> if (mismoCarril) {
                    e.consumida = true
                    escudoActivo = true
                    tiempoEscudo = DURACION_ESCUDO
                }
                TipoEntidad.Iman -> if (mismoCarril) {
                    e.consumida = true
                    imanActivo = true
                    tiempoIman = DURACION_IMAN
                }
                TipoEntidad.ObstaculoAlto -> if (mismoCarril) golpear(e)
                // El obstáculo bajo se puede saltar
                TipoEntidad.ObstaculoBajo -> if (mismoCarril && !saltando) golpear(e)
            }
        }
        entidades.removeAll(eliminar)
    }

    private fun golpear(e: Entidad) {
        if (invulnerable) return
        e.consumida = true
        invulnerable = true
        golpes++
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
