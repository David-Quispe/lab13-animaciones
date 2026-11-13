package com.example.lab13_animaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.juego.Ejercicio5Screen
import com.example.lab13_animaciones.ui.theme.Lab13animacionesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab13animacionesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var seleccionado by remember { mutableIntStateOf(0) }
                    val titulos = listOf("Ejercicio 1", "Ejercicio 2", "Ejercicio 3", "Ejercicio 4", "Final")

                    Column(modifier = Modifier.padding(innerPadding)) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            titulos.forEachIndexed { i, titulo ->
                                FilterChip(
                                    selected = seleccionado == i,
                                    onClick = { seleccionado = i },
                                    label = { Text(titulo) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                        when (seleccionado) {
                            0 -> Ejercicio1Screen()
                            1 -> Ejercicio2Screen()
                            2 -> Ejercicio3Screen()
                            3 -> Ejercicio4Screen()
                            4 -> Ejercicio5Screen()
                        }
                    }
                }
            }
        }
    }
}
