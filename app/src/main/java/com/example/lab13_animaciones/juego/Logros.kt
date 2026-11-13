package com.example.lab13_animaciones.juego

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Logro(val id: String, val titulo: String, val descripcion: String)

val LOGROS = listOf(
    Logro("coleccionista", "Coleccionista", "Junta 50 monedas en una partida"),
    Logro("saltarin", "Saltarín", "Salta 15 veces en una partida"),
    Logro("corredor", "Corredor andino", "Llega a 1000 puntos"),
    Logro("superviviente", "Superviviente del Misti", "Sobrevive a una erupción sin recibir golpes"),
    Logro("rocotero", "Rocotero", "Atrapa un rocoto")
)

@Composable
fun PanelLogros(juego: JuegoEstado) {
    PanelBase {
        Text("Logros", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "${juego.logrosObtenidos.size} de ${LOGROS.size}",
            color = Color(0xFFFFC107),
            fontSize = 16.sp
        )
        for (logro in LOGROS) {
            val obtenido = logro.id in juego.logrosObtenidos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = if (obtenido) 0.25f else 0.08f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(if (obtenido) "🏆" else "🔒", fontSize = 24.sp)
                Column {
                    Text(
                        text = logro.titulo,
                        color = Color.White.copy(alpha = if (obtenido) 1f else 0.6f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(logro.descripcion, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
        }
        Button(onClick = { juego.irAlMenu() }) {
            Text("Volver")
        }
    }
}
