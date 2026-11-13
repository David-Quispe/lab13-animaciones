package com.example.lab13_animaciones

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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

// Ejercicio 4: AnimatedContent
// Los tres estados posibles de una pantalla que carga datos.
enum class EstadoPantalla { Cargando, Contenido, Error }

@Composable
fun Ejercicio4Screen(modifier: Modifier = Modifier) {
    var estado by remember { mutableStateOf(EstadoPantalla.Cargando) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EstadoPantalla.entries.forEach { e ->
                Button(
                    onClick = { estado = e },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(e.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(targetState = estado, label = "estadoPantalla") { actual ->
            when (actual) {
                EstadoPantalla.Cargando -> TarjetaEstado(Color(0xFF607D8B)) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando datos...", color = Color.White)
                }

                EstadoPantalla.Contenido -> TarjetaEstado(Color(0xFF4CAF50)) {
                    Text("Datos cargados", color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Todo salió bien", color = Color.White)
                }

                EstadoPantalla.Error -> TarjetaEstado(Color(0xFFE53935)) {
                    Text("Ocurrió un error", color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No se pudo conectar", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEstado(
    fondo: Color,
    contenido: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 160.dp)
            .background(fondo, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            contenido()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Ejercicio4Preview() {
    Ejercicio4Screen()
}
