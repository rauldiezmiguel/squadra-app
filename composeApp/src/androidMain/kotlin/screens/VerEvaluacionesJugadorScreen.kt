package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.AndroidEvaluacionViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.EvaluacionState
import viewModel.PromedioMensualState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Tune


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun EvaluacionesJugadorScreen(
    idJugador: Int,
    nombreEquipo: String,
    viewModel: AndroidEvaluacionViewModel,
    jugadorViewModel: AndroidJugadorViewModel
) {
    val evaluacionState by viewModel.evaluacionState.collectAsState()
    val promedioState by viewModel.promediosMensualesState.collectAsState()
    val jugador by jugadorViewModel.jugadorSeleccionado.collectAsState()

    var filtroVisible by remember { mutableStateOf(false) }
    var fechaInicio by remember { mutableStateOf<Date?>(null) }
    var fechaFin by remember { mutableStateOf<Date?>(null) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val context = LocalContext.current

    val calendario = Calendar.getInstance()

    LaunchedEffect(idJugador) {
        viewModel.getEvaluaciones(idJugador)
        viewModel.getPromediosMensuales(idJugador)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.InsertChart,
                contentDescription = "Promedios",
                tint = secondaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Promedios Mensuales",
                style = MaterialTheme.typography.titleLarge,
                color = secondaryColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (promedioState) {
            is PromedioMensualState.Loading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = secondaryColor)
                }
            }

            is PromedioMensualState.Error -> {
                Text("Error al cargar promedios", color = MaterialTheme.colorScheme.error)
            }

            is PromedioMensualState.Success -> {
                val lista = (promedioState as PromedioMensualState.Success).promedios
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                        .height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista) { dto ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .padding(vertical = 8.dp)
                                .border(width = 2.dp, color = secondaryColor, shape = MaterialTheme.shapes.medium),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "Mes: ${dto.mes}/${dto.año}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("Comportamiento: ${"%.2f".format(dto.comportamiento)}", color = secondaryColor)
                                Text("Técnica: ${"%.2f".format(dto.tecnica)}", color = secondaryColor)
                                Text("Táctica: ${"%.2f".format(dto.tactica)}", color = secondaryColor)
                            }
                        }

                    }
                }
            }

            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Parte donde muestras título y botón mostrar/ocultar filtro alineados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Evaluaciones Semanales",
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Evaluaciones Semanales",
                    style = MaterialTheme.typography.titleLarge,
                    color = secondaryColor,
                )
            }

            IconButton(onClick = { filtroVisible = !filtroVisible }) {
                Icon(
                    imageVector = if (!filtroVisible) Icons.Filled.Tune else Icons.Filled.Close,
                    contentDescription = if (filtroVisible) "Ocultar filtro" else "Mostrar filtro",
                    tint = secondaryColor
                )
            }
        }

        if (filtroVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                calendario.set(year, month, dayOfMonth)
                                fechaInicio = calendario.time
                            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = fechaInicio?.let { "Desde: ${sdf.format(it)}" } ?: "Desde")
                    }
                    if (fechaInicio != null) {
                        TextButton(
                            onClick = { fechaInicio = null },
                            modifier = Modifier.padding(top = 4.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Limpiar", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                calendario.set(year, month, dayOfMonth)
                                fechaFin = calendario.time
                            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = fechaFin?.let { "Hasta: ${sdf.format(it)}" } ?: "Hasta")
                    }
                    if (fechaFin != null) {
                        TextButton(
                            onClick = { fechaFin = null },
                            modifier = Modifier.padding(top = 4.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Limpiar", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            when (evaluacionState) {
                is EvaluacionState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = secondaryColor)
                    }
                }

                is EvaluacionState.Error -> {
                    Text("Error al cargar evaluaciones", color = MaterialTheme.colorScheme.error)
                }

                is EvaluacionState.Success -> {
                    val evals = (evaluacionState as EvaluacionState.Success).evaluaciones
                        .filter { eval ->
                            val evalDate = runCatching { eval.fecha?.let { sdf.parse(it) } }.getOrNull()
                            val desdeOk = fechaInicio?.let { evalDate?.after(it) == true || evalDate == it } ?: true
                            val hastaOk = fechaFin?.let { evalDate?.before(it) == true || evalDate == it } ?: true
                            desdeOk && hastaOk
                        }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(evals) { e ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 2.dp, color = secondaryColor, shape = MaterialTheme.shapes.medium),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Fecha: ${e.fecha}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = secondaryColor
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text("Comportamiento: ${e.comportamiento}", color = secondaryColor)
                                    Text("Técnica: ${e.tecnica}", color = secondaryColor)
                                    Text("Táctica: ${e.tactica}", color = secondaryColor)
                                    e.observaciones?.takeIf { it.isNotBlank() }?.let { obs ->
                                        Spacer(Modifier.height(4.dp))
                                        Text("Observación: $obs", color = secondaryColor)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}


