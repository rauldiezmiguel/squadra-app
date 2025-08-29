package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.toKotlinLocalDate
import network.CrearPartidoRequest
import java.time.format.DateTimeFormatter

//private val AzulGrisaceo = Color(0xFF37474F)
//private val FondoColor   = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun CrearPartidoScreen(
    onDismiss: () -> Unit,
    onCreate: (CrearPartidoRequest) -> Unit,
    idEquipo: Int,
    fechaSeleccionada: LocalDate
) {
    var nombreRival by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .fillMaxSize()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material.Text(
                    text = "SQUADRA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        Text(
            "Nuevo Partido",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                OutlinedTextField(
                    value = nombreRival,
                    onValueChange = { nombreRival = it },
                    label = { Text("Nombre del Rival", color = secondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Fecha seleccionada: ${fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = secondaryColor)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isCreating = true
                        val partido = CrearPartidoRequest(
                            idEquipo = idEquipo,
                            nombreRival = nombreRival,
                            fecha = fechaSeleccionada.toKotlinLocalDate()
                        )
                        onCreate(partido)
                        onDismiss()  // Cerramos la pantalla al crear el partido
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating && nombreRival.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) {
                    Text(if (isCreating) "Creando..." else "Crear Partido", color = Color.White)
                }
            }
        }
    }
}