package com.example.lab13_animaciones

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Ejercicio 3: animación de tamaño y posición
// animateDpAsState anima el tamaño y el desplazamiento (x, y) que usa Modifier.offset.
@Composable
fun Ejercicio3Screen(modifier: Modifier = Modifier) {
    var activo by remember { mutableStateOf(false) }

    val tamano by animateDpAsState(
        targetValue = if (activo) 120.dp else 60.dp,
        animationSpec = tween(durationMillis = 800),
        label = "tamano"
    )
    val desplazX by animateDpAsState(
        targetValue = if (activo) 140.dp else 0.dp,
        animationSpec = tween(durationMillis = 800),
        label = "desplazX"
    )
    val desplazY by animateDpAsState(
        targetValue = if (activo) 80.dp else 0.dp,
        animationSpec = tween(durationMillis = 800),
        label = "desplazY"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { activo = !activo }) {
            Text("Mover y cambiar tamaño")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = desplazX, y = desplazY)
                    .size(tamano)
                    .background(Color(0xFFE91E63), RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Orden invertido: el fondo se dibuja antes del offset, así que el cuadro
        // solo cambia de tamaño y lo que se desplaza es el contenido (el texto).
        Text("Orden invertido: background -> size -> offset")
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF009688), RoundedCornerShape(12.dp))
                    .size(tamano)
                    .offset(x = desplazX, y = desplazY)
            ) {
                Text("offset", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Ejercicio3Preview() {
    Ejercicio3Screen()
}
