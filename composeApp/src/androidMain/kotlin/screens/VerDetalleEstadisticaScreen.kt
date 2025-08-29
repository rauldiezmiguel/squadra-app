package screens

import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import viewModel.*
import java.io.File
import java.io.FileOutputStream

private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerDetalleEstadisticaScreen(
    idTemporada: Int,
    idEquipo: Int,
    nomEstadistica: String,
    nombreEquipo: String,
    estadisticasViewModel: AndroidEstadisticasViewModel,
    jugadorViewModel: AndroidJugadorViewModel,
) {
    val state   by estadisticasViewModel.jugadorDetalleState.collectAsState()
    val jugador by jugadorViewModel.jugadorSeleccionado.collectAsState()
    val context = LocalContext.current
    val chartRefLine = remember { mutableStateOf<LineChart?>(null) }
    val chartRefPie = remember { mutableStateOf<PieChart?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jugador?.id, idEquipo, idTemporada, nomEstadistica) {
        jugador?.id?.let { idJ ->
            estadisticasViewModel.cargarEstadisticaJugadorPartidos(
                idJ, idEquipo, idTemporada, nomEstadistica
            )
        }
    }
    LaunchedEffect(Unit) {
        estadisticasViewModel.getEstadisticasTotalesEquipo(idEquipo, idTemporada)
    }

    Scaffold(
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
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


                when (state) {
                    is EstadisticaJugadorDetalleState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    is EstadisticaJugadorDetalleState.Error -> {
                        val msg = (state as EstadisticaJugadorDetalleState.Error).message
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(msg, color = Color.Red)
                        }
                    }
                    is EstadisticaJugadorDetalleState.Success -> {
                        val listaDto = (state as EstadisticaJugadorDetalleState.Success).data
                        if (listaDto.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay datos de $nomEstadistica para este jugador")
                            }
                        } else {
                            when (nomEstadistica) {
                                "Goles", "Asistencias", "Min. Jugados", "Tarjetas Amarillas", "Tarjetas Rojas" -> {
                                    val entries = listaDto.mapIndexed { idx, dto ->
                                        Entry(idx.toFloat(), dto.valorEstadistica.toFloat())
                                    }
                                    val labels = listaDto.map { it.nombreRival }

                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp),
                                        factory = { ctx ->
                                            LineChart(ctx).apply {
                                                chartRefLine.value = this
                                                setBackgroundColor(backgroundColor.toArgb())
                                                description.isEnabled = false

                                                axisLeft.apply {
                                                    granularity = 1f
                                                    axisMinimum = 0f
                                                    textColor = Color.Black.toArgb()
                                                }

                                                xAxis.apply {
                                                    position = XAxis.XAxisPosition.BOTTOM
                                                    granularity = 1f
                                                    valueFormatter = IndexAxisValueFormatter(labels)
                                                    labelRotationAngle = -45f
                                                    textColor = Color.Black.toArgb()
                                                }
                                                axisLeft.textColor = Color.Black.toArgb()
                                                axisRight.isEnabled = false
                                                legend.isEnabled = false

                                                data = LineData(
                                                    LineDataSet(entries, nomEstadistica).apply {
                                                        color = Color(0xFF1E88E5).toArgb()
                                                        valueTextColor = Color.Black.toArgb()
                                                        setDrawCircles(true)
                                                        setDrawValues(false)
                                                        lineWidth = 2.5f
                                                        setCircleColor(Color(0xFF42A5F5).toArgb())
                                                    }
                                                )
                                                invalidate()
                                            }
                                        }
                                    )
                                }
                                "Titularidades" -> {
                                    val titularidades = listaDto.count { it.valorEstadistica > 0 }
                                    val noTitularidades = listaDto.count { it.valorEstadistica == 0}

                                    val entries = listOf(
                                        PieEntry(titularidades.toFloat(), "Titular"),
                                        PieEntry(noTitularidades.toFloat(), "Suplente")
                                    )

                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp),
                                        factory = { ctx ->
                                            PieChart(ctx).apply {
                                                chartRefPie.value = this
                                                setBackgroundColor(backgroundColor.toArgb())
                                                description.isEnabled = false
                                                isDrawHoleEnabled = true
                                                setEntryLabelColor(Color.Black.toArgb())
                                                setUsePercentValues(true)

                                                data = PieData(
                                                    PieDataSet(entries, "Titularidades (%)").apply {
                                                        colors = listOf(
                                                            Color(0xFF4CAF50).toArgb(), // Verde
                                                            Color(0xFFFFC107).toArgb()  // Amarillo
                                                        )
                                                        valueTextColor = Color.Black.toArgb()
                                                        valueTextSize = 14f
                                                    }
                                                )
                                                invalidate()
                                            }
                                        }
                                    )
                                }
                                "Partidos Jugados" -> {
                                    val estadoEquipo by estadisticasViewModel.estadisticasTotalesEquipoState.collectAsState()
                                    if (estadoEquipo is EstadisticasTotalesEquipoState.Success) {
                                        val totalPartidosEquipo = (estadoEquipo as EstadisticasTotalesEquipoState.Success).data.partidosTotales
                                        val jugados = listaDto.count { it.valorEstadistica > 0 }
                                        val noJugados = totalPartidosEquipo - jugados

                                        val entries = listOf(
                                            PieEntry(jugados.toFloat(), "Jugados"),
                                            PieEntry(noJugados.toFloat(), "No Jugados")
                                        )

                                        AndroidView(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp),
                                            factory = { ctx ->
                                                PieChart(ctx).apply {
                                                    chartRefPie.value = this
                                                    setBackgroundColor(backgroundColor.toArgb())
                                                    description.isEnabled = false
                                                    isDrawHoleEnabled = true
                                                    setEntryLabelColor(Color.Black.toArgb())
                                                    setUsePercentValues(true)

                                                    data = PieData(
                                                        PieDataSet(entries, "Partidos Jugados (%)").apply {
                                                            colors = listOf(
                                                                Color(0xFF2196F3).toArgb(), // Azul
                                                                Color(0xFFBDBDBD).toArgb()  // Gris claro
                                                            )
                                                            valueTextColor = Color.Black.toArgb()
                                                            valueTextSize = 14f
                                                        }
                                                    )
                                                    invalidate()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val filename = "${nomEstadistica}_${jugador?.nombreJugador}_${System.currentTimeMillis()}.pdf"

                            chartRefLine.value?.let { chart ->
                                saveChartPdfToDownloads(chart, filename, context)
                            }
                            chartRefPie.value?.let { chart ->
                                saveChartPdfToDownloads(chart, filename, context)
                            }

                            showDialog = false
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("Confirmar descarga") },
                    text = { Text("¿Estás seguro de que deseas descargar el gráfico en PDF?") }
                )
            }

            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = secondaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Descargar gráfico en PDF")
            }
        }
    }
}

private fun saveChartPdfToDownloads(chart: Any, filename: String, context: Context) {
    val bitmap = when (chart) {
        is LineChart -> chart.chartBitmap
        is PieChart -> chart.chartBitmap
        else -> return
    }

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = document.startPage(pageInfo)
    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
    document.finishPage(page)

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it).use { out ->
                    document.writeTo(out!!)
                }
                Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            }
        } else {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloads, filename)
            FileOutputStream(outFile).use { document.writeTo(it) }
            Toast.makeText(context, "PDF guardado en: ${outFile.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }

    document.close()
}
