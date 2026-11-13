package com.example.lab13_animaciones.juego

// Pseudo-3D: lo que está lejos (arriba, junto al horizonte) se ve más pequeño y
// más cerca del centro; lo que está cerca (abajo) llega a tamaño completo.
const val ESCALA_LEJANA = 0.35f

// y va de 0 (horizonte) a 1 (parte baja del campo)
fun factorPerspectiva(y: Float): Float =
    ESCALA_LEJANA + (1f - ESCALA_LEJANA) * y.coerceIn(0f, 1f)
