package screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.AndroidAsistenciaEntrenamientoViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.AsistenciaEntrenamientoJugadorState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val AzulGrisaceo = Color(0xFF546E7A)
private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerAsistenciaEntrenamientosJugadorScreen(
    idJugador: Int,
    nombreEquipo: String,
    asistenciaEntrenamientoViewModel: AndroidAsistenciaEntrenamientoViewModel,
    jugadorViewModel: AndroidJugadorViewModel
) {
    val state by asistenciaEntrenamientoViewModel.asistenciaJugadorState.collectAsState()
    val jugador by jugadorViewModel.jugadorSeleccionado.collectAsState()

    var filtroVisible by remember { mutableStateOf(false) }
    var fechaInicio by remember { mutableStateOf<Date?>(null) }
    var fechaFin by remember { mutableStateOf<Date?>(null) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val context = LocalContext.current

    val calendario = Calendar.getInstance()

    LaunchedEffect(idJugador) {
        asistenciaEntrenamientoViewModel.getAsistenciasPorJugador(idJugador)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SQUADRA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Equipo: $nombreEquipo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jugador: ${jugador?.nombreJugador}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryColor
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    "Asistencia Entrenamientos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = secondaryColor
                )
                IconButton(onClick = { filtroVisible = !filtroVisible }) {
                    Icon(
                        imageVector = if (!filtroVisible) Icons.Default.Tune else Icons.Default.Close,
                        contentDescription = if (filtroVisible) "Ocultar filtro" else "Mostrar filtro",
                        tint = secondaryColor
                    )
                }
            }

            if (filtroVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = {
                                android.app.DatePickerDialog(
                                    context, { _, year, month, dayOfMonth ->
                                        calendario.set(year, month, dayOfMonth)
                                        fechaInicio = calendario.time
                                    },
                                    calendario.get(Calendar.YEAR),
                                    calendario.get(Calendar.MONTH),
                                    calendario.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = secondaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = fechaInicio?.let { "Desde: ${sdf.format(it)}" } ?: "Desde")
                        }
                        if (fechaInicio != null) {
                            TextButton(onClick = { fechaInicio = null }) {
                                Text("Limpiar", fontSize = 12.sp, color = Color.Red)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = {
                                android.app.DatePickerDialog(
                                    context, { _, year, month, dayOfMonth ->
                                        calendario.set(year, month, dayOfMonth)
                                        fechaFin = calendario.time
                                    },
                                    calendario.get(Calendar.YEAR),
                                    calendario.get(Calendar.MONTH),
                                    calendario.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = secondaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = fechaFin?.let { "Hasta: ${sdf.format(it)}" } ?: "Hasta")
                        }
                        if (fechaFin != null) {
                            TextButton(onClick = { fechaFin = null }) {
                                Text("Limpiar", fontSize = 12.sp, color = Color.Red)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (state) {
                is AsistenciaEntrenamientoJugadorState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = secondaryColor)
                    }
                }

                is AsistenciaEntrenamientoJugadorState.Error -> {
                    val msg = (state as AsistenciaEntrenamientoJugadorState.Error).message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: $msg",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is AsistenciaEntrenamientoJugadorState.Success -> {
                    val asistenciasOriginal = (state as AsistenciaEntrenamientoJugadorState.Success).asistencias
                    val asistencias = asistenciasOriginal.filter { asistencia ->
                        val fecha = runCatching { sdf.parse(asistencia.fecha.toString()) }.getOrNull()
                        val desdeOk = fechaInicio?.let { fecha?.after(it) == true || fecha == it } ?: true
                        val hastaOk = fechaFin?.let { fecha?.before(it) == true || fecha == it } ?: true
                        desdeOk && hastaOk
                    }

                    val total = asistencias.size
                    val asistidos = asistencias.count { it.asistio }
                    val noAsistidos = total - asistidos
                    val porcentaje = if (total > 0) (asistidos * 100) / total else 0

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp)
                    ) {
                        items(asistencias) { asistencia ->
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, secondaryColor),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Fecha: ${asistencia.fecha}",
                                            color = secondaryColor,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Entrenamiento",
                                            color = secondaryColor.copy(alpha = 0.8f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (!asistencia.asistio && !asistencia.motivoInasistencia.isNullOrBlank()) {
                                            Text(
                                                text = "Motivo: ${asistencia.motivoInasistencia}",
                                                color = secondaryColor,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (asistencia.asistio) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.size(36.dp),
                                        tonalElevation = 2.dp
                                    ) {
                                        Icon(
                                            imageVector = if (asistencia.asistio) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = if (asistencia.asistio) "Asistió" else "No asistió",
                                            tint = Color.White,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(backgroundColor),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EstadisticaItem(label = "Totales", valor = total.toString())
                        EstadisticaItem(label = "Asistidos", valor = asistidos.toString(), icon = Icons.Default.Check, color = Color(0xFF4CAF50))
                        EstadisticaItem(label = "Fallados", valor = noAsistidos.toString(), icon = Icons.Default.Close, color = Color(0xFFF44336))
                        EstadisticaItem(label = "Asistencia", valor = "$porcentaje%")
                    }

                }
            }
        }
    }
}

@Composable
private fun EstadisticaItem(
    label: String,
    valor: String,
    icon: ImageVector? = null,
    color: Color = Color.Black
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}


