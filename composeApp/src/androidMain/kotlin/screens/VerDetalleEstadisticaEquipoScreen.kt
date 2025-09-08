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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import viewModel.AndroidEstadisticasViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.TeamDetalleState
import java.io.File
import java.io.FileOutputStream

//private val AzulGrisaceo = Color(0xFF37474F)
//private val FondoColor   = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerDetalleEstadisticaEquipoScreen(
    idEquipo: Int,
    idTemporada: Int,
    nomEstadistica: String,
    nombreEquipo: String,
    viewModel: AndroidEstadisticasViewModel
) {
    val state by viewModel.teamDetalleState.collectAsState()
    val context = LocalContext.current
    val chartRefPie = remember { mutableStateOf<PieChart?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Trigger load
    LaunchedEffect(idEquipo, idTemporada, nomEstadistica) {
        viewModel.cargarDetalleEstadisticaEquipo(idEquipo, idTemporada, nomEstadistica)
    }

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "SQUADRA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Equipo: $nombreEquipo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryColor
                )

                when (state) {
                    is TeamDetalleState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is TeamDetalleState.Error -> {
                        val msg = (state as TeamDetalleState.Error).message
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(msg ?: "Error desconocido", color = Color.Red)
                        }
                    }
                    is TeamDetalleState.Success -> {
                        val dto = (state as TeamDetalleState.Success).data

                        val detallesJugadores = dto.detallesPorJugador
                        val totalEquipo = dto.totalEquipo

                        if (detallesJugadores.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay datos de $nomEstadistica para este equipo")
                            }
                        } else {
                            val colores = listOf(
                                Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0),
                                Color(0xFF29B6F6), Color(0xFF66BB6A), Color(0xFFFFCA28),
                                Color(0xFFFFA726), Color(0xFF8D6E63), Color(0xFF78909C),
                                Color(0xFFD81B60)
                            )

                            // Construir PieEntries con cada jugador
                            val entries = detallesJugadores.map {
                                PieEntry(it.valorEstadistica.toFloat(), it.nombreJugador)
                            }

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                factory = { ctx ->
                                    PieChart(ctx).apply {
                                        chartRefPie.value = this
                                        setBackgroundColor(backgroundColor.toArgb())
                                        description.isEnabled = false
                                        setUsePercentValues(true)
                                        isDrawHoleEnabled = true
                                        setEntryLabelColor(Color.Black.toArgb())
                                        data = PieData(
                                            PieDataSet(entries, "").apply {
                                                valueTextColor = Color.Black.toArgb()
                                                valueFormatter = PercentFormatter()
                                                colors = colores.map { it.toArgb() }.take(entries.size)
                                            }
                                        )
                                        invalidate()
                                    }
                                }
                            )

                            Spacer(Modifier.height(16.dp))

                            // Leyenda personalizada
                            Column(
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                detallesJugadores.forEach { jugador ->
                                    val porcentaje = jugador.valorEstadistica * 100f / totalEquipo
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "${jugador.nombreJugador}: ${"%.1f".format(porcentaje)}% (${jugador.valorEstadistica})",
                                            style = MaterialTheme.typography.bodyMedium
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
                            val filename = "${nomEstadistica}_${nombreEquipo}_${System.currentTimeMillis()}.pdf"

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

// Diálogo de confirmación
@Composable
fun ConfirmDownloadDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Descargar gráfico") },
        text = { Text("¿Descargar la estadística en PDF?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Sí") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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