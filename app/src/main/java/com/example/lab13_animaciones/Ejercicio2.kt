package com.example.lab13_animaciones

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Ejercicio 2: animateColorAsState
// Un botón cambia el color de un cuadro entre azul y verde con una transición suave.
@Composable
fun Ejercicio2Screen(modifier: Modifier = Modifier) {
    var esAzul by remember { mutableStateOf(true) }

    val colorTween by animateColorAsState(
        targetValue = if (esAzul) Color(0xFF2196F3) else Color(0xFF4CAF50),
        animationSpec = tween(durationMillis = 1000),
        label = "colorTween"
    )

    // Mismo cambio de color, pero con spring para comparar la sensación
    val colorSpring by animateColorAsState(
        targetValue = if (esAzul) Color(0xFF2196F3) else Color(0xFF4CAF50),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "colorSpring"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { esAzul = !esAzul }) {
            Text("Cambiar color")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .background(colorTween, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "tween", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .background(colorSpring, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "spring", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Ejercicio2Preview() {
    Ejercicio2Screen()
}
