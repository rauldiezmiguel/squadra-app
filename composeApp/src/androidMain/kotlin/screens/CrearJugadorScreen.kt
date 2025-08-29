package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.CrearJugadorRequest

//private val AzulGrisaceo = Color(0xFF37474F)
//private val FondoColor   = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun CrearJugadorScreen(
    idEquipo: Int,
    onDismiss: () -> Unit,
    onCreate: (CrearJugadorRequest) -> Unit
) {
    var nombreJugador by remember { mutableStateOf("") }
    var dorsalJugador by remember { mutableStateOf("") }
    var posicion by remember { mutableStateOf("") }
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
            text = "Nuevo Jugador",
            style = MaterialTheme.typography.headlineSmall,
            color = primaryColor
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                OutlinedTextField(
                    value = nombreJugador,
                    onValueChange = { nombreJugador = it },
                    label = { Text("Nombre del Jugador", color = secondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dorsalJugador,
                    onValueChange = { dorsalJugador = it.filter { c -> c.isDigit() } },
                    label = { Text("Dorsal", color = secondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = posicion,
                    onValueChange = { posicion = it },
                    label = { Text("Posición", color = secondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        isCreating = true
                        onCreate(
                            CrearJugadorRequest(
                                nombre = nombreJugador,
                                dorsal = dorsalJugador.toIntOrNull() ?: 0,
                                posicion = posicion,
                                idEquipo = idEquipo
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating && nombreJugador.isNotEmpty() && dorsalJugador.isNotEmpty() && posicion.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) {
                    Text(if (isCreating) "Creando..." else "Crear Jugador", color = Color.White)
                }
            }
        }
    }
}
