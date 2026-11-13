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
private const val DURACION_LENTO = 5f
private const val DURACION_DOBLE = 8f
private const val CLAVE_MEJOR = "mejor_puntaje"
private const val CLAVE_MONEDAS_TOTALES = "monedas_totales"
private const val CLAVE_LOGROS = "logros"
private const val CLAVE_TUTORIAL = "tutorial_visto"
private const val CLAVE_PERSONAJE = "personaje"
private const val CLAVE_DESBLOQUEADOS = "desbloqueados"
private const val TIEMPO_PRIMERA_ERUPCION = 20f

private val COLORES_ROCOTO = listOf(Color(0xFFD50000), Color(0xFF66BB6A), Color(0xFFFFD54F), Color(0xFFFF6D00))
private val COLORES_LENTO = listOf(Color(0xFF29B6F6), Color(0xFF81D4FA), Color.White)
private val COLORES_MONEDA = listOf(Color(0xFFFFC107), Color(0xFFFFE082), Color.White)
private val COLORES_ESCUDO = listOf(Color(0xFF00E5FF), Color(0xFF81D4FA), Color.White)
private val COLORES_IMAN = listOf(Color(0xFFAB47BC), Color(0xFFE1BEE7), Color.White)
private val COLORES_GOLPE = listOf(Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEB3B))

enum class Pantalla { Inicio, Jugando, Pausa, GameOver, Personajes, Logros, Tutorial }

enum class TipoEntidad { ObstaculoAlto, ObstaculoBajo, Moneda, Escudo, Iman, RocaVolcanica, Rocoto, CamaraLenta, DoblePuntos }

// Cosas que pasan en el juego y que la pantalla convierte en sonido o vibración
enum class EventoJuego { Moneda, Salto, Golpe, PowerUp, GameOver }

// Todo lo que baja por la carretera. La posición y va de 0 (arriba) a 1 (abajo).
class Entidad(
    val id: Int,
    val tipo: TipoEntidad,
    val carril: Int,
    yInicial: Float,
    val factor: Float = 1f
) {
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

    // Erupción del Misti: carriles en peligro (un bit por carril) y mensaje en pantalla
    var lentoActivo by mutableStateOf(false)
        private set
    var dobleActivo by mutableStateOf(false)
        private set
    var progresoLento by mutableFloatStateOf(0f)
        private set
    var progresoDoble by mutableFloatStateOf(0f)
        private set
    // Progreso que se guarda entre partidas: monedas y personajes
    var monedasTotales by mutableIntStateOf(prefs.getInt(CLAVE_MONEDAS_TOTALES, 0))
        private set
    var personajeActivo by mutableStateOf(prefs.getString(CLAVE_PERSONAJE, "alpaca") ?: "alpaca")
        private set
    var desbloqueados by mutableStateOf(
        prefs.getStringSet(CLAVE_DESBLOQUEADOS, setOf("alpaca"))?.toSet() ?: setOf("alpaca")
    )
        private set
    var logrosObtenidos by mutableStateOf(prefs.getStringSet(CLAVE_LOGROS, emptySet())?.toSet() ?: emptySet())
        private set
    val especieActual: Especie
        get() = ESPECIES.firstOrNull { it.id == personajeActivo } ?: ESPECIES[0]

    var rocotos by mutableIntStateOf(0)
        private set
    var avisoMascara by mutableIntStateOf(0)
        private set
    var erupcionActiva by mutableStateOf(false)
        private set
    var anuncio by mutableStateOf<String?>(null)
        private set

    var sonidoActivo by mutableStateOf(true)
        private set
    var alEvento: ((EventoJuego) -> Unit)? = null

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
    private var saltosPartida = 0
    private var tiempoLento = 0f
    private var tiempoDoble = 0f
    private var puntosDistancia = 0f
    private var golpeEnErupcion = false
    private var tiempoParaErupcion = TIEMPO_PRIMERA_ERUPCION
    private var faseErupcion = 0 // 0 = nada, 1 = aviso, 2 = cayendo las rocas
    private var tiempoFase = 0f
    private var tiempoAnuncio = 0f

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
        puntosDistancia = 0f
        lentoActivo = false
        dobleActivo = false
        tiempoLento = 0f
        tiempoDoble = 0f
        saltosPartida = 0
        rocotos = 0
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
        avisoMascara = 0
        erupcionActiva = false
        anuncio = null
        golpeEnErupcion = false
        tiempoParaErupcion = TIEMPO_PRIMERA_ERUPCION
        faseErupcion = 0
        tiempoFase = 0f
        tiempoAnuncio = 0f
        pantalla = Pantalla.Jugando
    }

    // La primera vez que se juega se muestra el tutorial antes de empezar
    fun jugar() {
        if (prefs.getBoolean(CLAVE_TUTORIAL, false)) iniciar() else pantalla = Pantalla.Tutorial
    }

    fun verTutorial() {
        if (pantalla == Pantalla.Inicio) pantalla = Pantalla.Tutorial
    }

    fun terminarTutorial() {
        val primeraVez = !prefs.getBoolean(CLAVE_TUTORIAL, false)
        prefs.edit().putBoolean(CLAVE_TUTORIAL, true).apply()
        if (primeraVez) iniciar() else pantalla = Pantalla.Inicio
    }

    // Borra todo lo guardado: monedas, personajes, logros, récord y tutorial
    fun reiniciarProgreso() {
        prefs.edit().clear().apply()
        monedasTotales = 0
        personajeActivo = "alpaca"
        desbloqueados = setOf("alpaca")
        logrosObtenidos = emptySet()
        mejorPuntaje = 0
    }

    fun abrirLogros() {
        if (pantalla == Pantalla.Inicio) pantalla = Pantalla.Logros
    }

    // Registra el logro una sola vez y avisa con el cartel de arriba
    private fun desbloquear(id: String) {
        if (id in logrosObtenidos) return
        val logro = LOGROS.firstOrNull { it.id == id } ?: return
        logrosObtenidos = logrosObtenidos + id
        prefs.edit().putStringSet(CLAVE_LOGROS, logrosObtenidos).apply()
        anunciar("🏆 Logro: ${logro.titulo}", 2.5f)
        alEvento?.invoke(EventoJuego.PowerUp)
    }

    fun abrirPersonajes() {
        if (pantalla == Pantalla.Inicio) pantalla = Pantalla.Personajes
    }

    // Usa el personaje si ya está desbloqueado; si no, intenta comprarlo
    fun elegirPersonaje(especie: Especie) {
        if (especie.id !in desbloqueados) {
            if (monedasTotales < especie.costo) return
            monedasTotales -= especie.costo
            desbloqueados = desbloqueados + especie.id
        }
        personajeActivo = especie.id
        guardarProgreso()
    }

    private fun guardarProgreso() {
        prefs.edit()
            .putInt(CLAVE_MONEDAS_TOTALES, monedasTotales)
            .putString(CLAVE_PERSONAJE, personajeActivo)
            .putStringSet(CLAVE_DESBLOQUEADOS, desbloqueados)
            .apply()
    }

    fun alternarSonido() {
        sonidoActivo = !sonidoActivo
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
        lentoActivo = false
        dobleActivo = false
        avisoMascara = 0
        erupcionActiva = false
        anuncio = null
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
            saltosPartida++
            if (saltosPartida >= 15) desbloquear("saltarin")
            alEvento?.invoke(EventoJuego.Salto)
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
        // En cámara lenta el mundo corre a la mitad, pero el jugador sigue igual de ágil
        val dtMundo = if (lentoActivo) dt * 0.5f else dt
        distancia += velocidad * dtMundo
        puntosDistancia += velocidad * dtMundo * 20f * (if (dobleActivo) 2 else 1)
        puntaje = puntosDistancia.toInt() + puntosMonedas
        if (puntaje >= 1000) desbloquear("corredor")

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

        if (lentoActivo) {
            tiempoLento -= dt
            progresoLento = (tiempoLento / DURACION_LENTO).coerceIn(0f, 1f)
            if (tiempoLento <= 0f) lentoActivo = false
        }
        if (dobleActivo) {
            tiempoDoble -= dt
            progresoDoble = (tiempoDoble / DURACION_DOBLE).coerceIn(0f, 1f)
            if (tiempoDoble <= 0f) dobleActivo = false
        }

        if (tiempoAnuncio > 0f) {
            tiempoAnuncio -= dt
            if (tiempoAnuncio <= 0f) anuncio = null
        }
        actualizarErupcion(dt)

        tiempoGenerar -= dtMundo
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
            e.y += velocidad * e.factor * dtMundo

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
                    puntosMonedas += 10 * multiplicador * (if (dobleActivo) 2 else 1)
                    racha++
                    monedas++
                    monedasTotales++
                    if (monedas >= 50) desbloquear("coleccionista")
                    explosion(xDeCarril(e.carril), e.y, 8, COLORES_MONEDA, 0.3f)
                    alEvento?.invoke(EventoJuego.Moneda)
                }
                TipoEntidad.Escudo -> if (mismoCarril) {
                    e.consumida = true
                    escudoActivo = true
                    tiempoEscudo = DURACION_ESCUDO
                    progresoEscudo = 1f
                    explosion(xDeCarril(e.carril), e.y, 12, COLORES_ESCUDO, 0.35f)
                    alEvento?.invoke(EventoJuego.PowerUp)
                }
                TipoEntidad.Iman -> if (mismoCarril) {
                    e.consumida = true
                    imanActivo = true
                    tiempoIman = DURACION_IMAN
                    progresoIman = 1f
                    explosion(xDeCarril(e.carril), e.y, 12, COLORES_IMAN, 0.35f)
                    alEvento?.invoke(EventoJuego.PowerUp)
                }
                TipoEntidad.CamaraLenta -> if (mismoCarril) {
                    e.consumida = true
                    lentoActivo = true
                    tiempoLento = DURACION_LENTO
                    progresoLento = 1f
                    explosion(xDeCarril(e.carril), e.y, 12, COLORES_LENTO, 0.35f)
                    anunciar("⏳ ¡Cámara lenta!", 1.5f)
                    alEvento?.invoke(EventoJuego.PowerUp)
                }
                TipoEntidad.DoblePuntos -> if (mismoCarril) {
                    e.consumida = true
                    dobleActivo = true
                    tiempoDoble = DURACION_DOBLE
                    progresoDoble = 1f
                    explosion(xDeCarril(e.carril), e.y, 12, COLORES_MONEDA, 0.35f)
                    anunciar("⭐ ¡Puntos x2!", 1.5f)
                    alEvento?.invoke(EventoJuego.PowerUp)
                }
                TipoEntidad.Rocoto -> if (mismoCarril) {
                    e.consumida = true
                    puntosMonedas += 100
                    rocotos++
                    desbloquear("rocotero")
                    explosion(xDeCarril(e.carril), e.y, 30, COLORES_ROCOTO, 0.6f)
                    anunciar("🌶️ ¡Rocoto picante! +100", 2f)
                    alEvento?.invoke(EventoJuego.PowerUp)
                }
                TipoEntidad.ObstaculoAlto, TipoEntidad.RocaVolcanica -> if (mismoCarril) golpear(e)
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
        if (erupcionActiva) golpeEnErupcion = true
        explosion(xDeCarril(carril), POS_JUGADOR, 18, COLORES_GOLPE, 0.5f)
        alEvento?.invoke(EventoJuego.Golpe)
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
        guardarProgreso()
        pantalla = Pantalla.GameOver
        alEvento?.invoke(EventoJuego.GameOver)
    }

    // Cada cierto tiempo el Misti hace erupción: primero avisa qué carriles están
    // en peligro y después lanza rocas por ellos. Siempre queda un carril libre.
    private fun actualizarErupcion(dt: Float) {
        when (faseErupcion) {
            0 -> {
                tiempoParaErupcion -= dt
                if (tiempoParaErupcion <= 0f) {
                    val libre = Random.nextInt(3)
                    avisoMascara = 7 and (1 shl libre).inv()
                    erupcionActiva = true
                    golpeEnErupcion = false
                    faseErupcion = 1
                    tiempoFase = 1.8f
                    anunciar("¡El Misti hace erupción!", 2.2f)
                }
            }
            1 -> {
                tiempoFase -= dt
                if (tiempoFase <= 0f) {
                    for (c in 0..2) {
                        if (((avisoMascara shr c) and 1) == 1) {
                            for (i in 0 until 3) {
                                agregar(TipoEntidad.RocaVolcanica, c, -0.1f - i * 0.4f, 1.6f)
                            }
                        }
                    }
                    avisoMascara = 0
                    faseErupcion = 2
                    tiempoFase = 4.5f
                }
            }
            else -> {
                tiempoFase -= dt
                if (tiempoFase <= 0f) {
                    erupcionActiva = false
                    if (!golpeEnErupcion) desbloquear("superviviente")
                    faseErupcion = 0
                    tiempoParaErupcion = 22f + Random.nextFloat() * 10f
                }
            }
        }
    }

    private fun anunciar(texto: String, duracion: Float) {
        anuncio = texto
        tiempoAnuncio = duracion
    }

    private fun generar() {
        val carrilNuevo = Random.nextInt(3)
        // Durante la erupción solo salen monedas, para que siempre haya una salida
        val azar = if (erupcionActiva) 0.6f + Random.nextFloat() * 0.2f else Random.nextFloat()
        when {
            azar < 0.30f -> agregar(TipoEntidad.ObstaculoAlto, carrilNuevo, -0.1f)
            azar < 0.55f -> agregar(TipoEntidad.ObstaculoBajo, carrilNuevo, -0.1f)
            azar < 0.88f -> for (i in 0 until 3) {
                agregar(TipoEntidad.Moneda, carrilNuevo, -0.1f - i * 0.09f)
            }
            azar < 0.91f -> agregar(TipoEntidad.Rocoto, carrilNuevo, -0.1f)
            else -> agregar(
                listOf(TipoEntidad.Escudo, TipoEntidad.Iman, TipoEntidad.CamaraLenta, TipoEntidad.DoblePuntos).random(),
                carrilNuevo,
                -0.1f
            )
        }
    }

    private fun agregar(tipo: TipoEntidad, carril: Int, y: Float, factor: Float = 1f) {
        entidades.add(Entidad(siguienteId++, tipo, carril, y, factor))
    }
}
