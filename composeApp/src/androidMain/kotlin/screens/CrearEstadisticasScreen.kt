package screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import network.CrearEstadisticasRequest
import network.EventoCalendario
import network.PartidoDTO
import viewModel.AndroidCalendarioViewModel

private val AzulGrisaceo = Color(0xFF37474F)
private val FondoColor = Color(0xFFF5F5F5)

@Composable
fun CrearEstadisticasScreen(
    idJugador: Int,
    idEquipo: Int,
    calendarioViewModel: AndroidCalendarioViewModel,
    onDismiss: () -> Unit,
    onCreate: (CrearEstadisticasRequest) -> Unit
) {
    // Estados de los campos
    var minutosJugados by remember { mutableStateOf(0) }
    var goles by remember { mutableStateOf(0) }
    var asistencias by remember { mutableStateOf(0) }
    var titular by remember { mutableStateOf(false) }
    var tarjetasAmarillas by remember { mutableStateOf(0) }
    var tarjetasRojas by remember { mutableStateOf(0) }
    var partidoJugado by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    // Estado para la selección de partido
    var selectedPartido by remember { mutableStateOf<EventoCalendario.Partido?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(idEquipo) {
        calendarioViewModel.getPartidos(idEquipo)
    }

    val partidos by calendarioViewModel.partidosParaEstadisticas.collectAsState()
    val arrowRotation by animateFloatAsState(
        targetValue = if (menuExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Column(
        modifier = Modifier
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .fillMaxSize()
            .background(FondoColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FondoColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Nueva Estadística",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AzulGrisaceo
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (showError) {
                    Text("Por favor, selecciona un partido", color = Color.Red)
                }

                // Selector de partido o mensaje si no hay ninguno
                if (partidos.isEmpty()) {
                    Text("No hay partidos disponibles para este equipo.", color = AzulGrisaceo)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.fillMaxWidth(0.95f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (menuExpanded) AzulGrisaceo.copy(alpha = 0.1f) else Color.White,
                                    contentColor = AzulGrisaceo
                                )
                            ) {
                                Text(
                                    text = selectedPartido
                                        ?.let { "${it.nombreRival} — ${it.fecha}" }
                                        ?: "Seleccionar Partido",
                                    color = AzulGrisaceo,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar menú",
                                    tint = AzulGrisaceo,
                                    modifier = Modifier.rotate(arrowRotation)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .heightIn(max = 300.dp)
                                    .background(Color.White)
                            ) {
                                partidos.forEach { partido ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${partido.nombreRival} — ${partido.fecha}", color = AzulGrisaceo)
                                        },
                                        onClick = {
                                            selectedPartido = partido
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))


                OutlinedTextField(
                    value = minutosJugados.toString(),
                    onValueChange = { minutosJugados = it.toIntOrNull() ?: 0 },
                    label = { Text("Minutos Jugados", color = AzulGrisaceo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = goles.toString(),
                    onValueChange = { goles = it.toIntOrNull() ?: 0 },
                    label = { Text("Goles", color = AzulGrisaceo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = asistencias.toString(),
                    onValueChange = { asistencias = it.toIntOrNull() ?: 0 },
                    label = { Text("Asistencias", color = AzulGrisaceo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = titular,
                        onCheckedChange = { titular = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Titular", color = AzulGrisaceo)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = partidoJugado,
                        onCheckedChange = { partidoJugado = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Partido jugado", color = AzulGrisaceo)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tarjetasAmarillas.toString(),
                    onValueChange = { tarjetasAmarillas = it.toIntOrNull() ?: 0 },
                    label = { Text("Tarjetas Amarillas", color = AzulGrisaceo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tarjetasRojas.toString(),
                    onValueChange = { tarjetasRojas = it.toIntOrNull() ?: 0 },
                    label = { Text("Tarjetas Rojas", color = AzulGrisaceo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedPartido != null) {
                            isCreating = true
                            val request = CrearEstadisticasRequest(
                                idJugador = idJugador,
                                idPartido = selectedPartido!!.idPartido, // Aquí ya sabemos que no es null
                                minutosJugados = minutosJugados,
                                goles = goles,
                                asistencias = asistencias,
                                titular = titular,
                                tarjetasAmarillas = tarjetasAmarillas,
                                tarjetasRojas = tarjetasRojas,
                                partidoJugado = partidoJugado
                            )
                            showError = false
                            onCreate(request)
                            onDismiss()
                        } else {
                            // Maneja el caso en que no se ha seleccionado un partido
                            // Puede ser un mensaje de error o un aviso al usuario
                            println("No se ha seleccionado un partido")
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulGrisaceo)
                ) {
                    Text(if (isCreating) "Guardando..." else "Guardar Estadística", color = Color.White)
                }
            }
        }
    }
}