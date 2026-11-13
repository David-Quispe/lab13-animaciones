package com.example.lab13_animaciones.juego

import androidx.compose.ui.graphics.Color

// Cada animal andino que se puede usar para correr. Se desbloquean con las
// monedas que se van acumulando entre partidas.
class Especie(
    val id: String,
    val nombre: String,
    val costo: Int,
    val lana: Color,
    val lanaOscura: Color,
    val manta: Color,
    val orejaAlto: Float,
    val pechoBlanco: Boolean
)

val ESPECIES = listOf(
    Especie("alpaca", "Alpaca", 0, Color(0xFFF5E6CC), Color(0xFFE0C9A0), Color(0xFF29B6F6), 0.18f, false),
    Especie("llama", "Llama", 100, Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFF43A047), 0.26f, false),
    Especie("vicuna", "Vicuña", 250, Color(0xFFD9A55B), Color(0xFFB07A33), Color(0xFFFF7043), 0.14f, true)
)
