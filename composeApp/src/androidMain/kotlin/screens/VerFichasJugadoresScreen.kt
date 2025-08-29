package screens

import android.content.ContentValues
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.FichaJugadorDTO
import viewModel.AndroidJugadorViewModel
import viewModel.FichasJugadoresState

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import java.io.File
import java.io.FileOutputStream

//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerFichasJugadoresScreen(
    idEquipo: Int,
    nombreEquipo: String,
    viewModel: AndroidJugadorViewModel
) {
    val fichasState by viewModel.fichasJugadoresState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var lastFichas by remember { mutableStateOf<List<FichaJugadorDTO>>(emptyList()) }
    val context = LocalContext.current


    LaunchedEffect(idEquipo) {
        viewModel.obtenerFichaDeJugadorPorEquipo(idEquipo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        // Fila con menú y título
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Spacer(modifier = Modifier.width(58.dp))

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
            // Icono de menú
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 8.dp)
            ) {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Download, contentDescription = "Descargar PDF", modifier = Modifier.size(32.dp))
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "Fichas Jugadores",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryColor
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Equipo: $nombreEquipo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryColor
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (fichasState) {
                    is FichasJugadoresState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is FichasJugadoresState.Error -> {
                        val message = (fichasState as FichasJugadoresState.Error).message
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Error: $message", color = Color.Red)
                        }
                    }

                    is FichasJugadoresState.Success -> {
                        val fichas = (fichasState as FichasJugadoresState.Success).jugadores
                        lastFichas = fichas
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(fichas) { ficha ->
                                FichaCard(ficha)
                            }
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                generarPDF(context, lastFichas, nombreEquipo)
                            }) {
                                Text("Sí")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancelar")
                            }
                        },
                        title = { Text("Descargar PDF") },
                        text = { Text("¿Estás seguro de que quieres descargar el PDF?") }
                    )
                }
            }
        }
    }
}

@Composable
fun FichaCard(ficha: FichaJugadorDTO) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(ficha.nombreJugador ?: "Jugador desconocido", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ficha.piernaHabil?.let { InfoLine("Pierna hábil", it) }
            ficha.caracteristicasFisicas?.let { InfoLine("Características físicas", it) }
            ficha.caracteristicasTacticas?.let { InfoLine("Características tácticas", it) }
            ficha.caracteristicasTecnicas?.let { InfoLine("Características técnicas", it) }
            ficha.conductaEntrenamiento?.let { InfoLine("Conducta en entrenamiento", it) }
            ficha.conductaConCompañeros?.let { InfoLine("Conducta con compañeros", it) }
            ficha.observacionFinal?.let { InfoLine("Observacion final", it) }
        }
    }
}

@Composable
fun InfoLine(titulo: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp).background(backgroundColor)) {
        Text(text = "$titulo:", fontWeight = FontWeight.SemiBold)
        Text(text = valor, fontSize = 14.sp)
    }
}

fun generarPDF(context: Context, fichas: List<FichaJugadorDTO>, nombreEquipo: String) {
    val document = PdfDocument()

    val pageWidth = 595
    val pageHeight = 842
    var pageNumber = 1

    val titlePaint = Paint().apply {
        textSize = 18f
        color = AndroidColor.BLACK
        isFakeBoldText = true
    }
    val headerPaint = Paint().apply {
        textSize = 16f
        color = AndroidColor.DKGRAY
        isFakeBoldText = true
    }
    val bodyPaint = Paint().apply {
        textSize = 14f
        color = AndroidColor.BLACK
    }
    val separatorPaint = Paint().apply {
        color = AndroidColor.GRAY
        strokeWidth = 2f
    }

    var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas
    var y = 40

    fun nuevaPagina() {
        document.finishPage(page)
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page.canvas
        y = 40
    }

    canvas.drawText("Ficha de Jugadores - Equipo: $nombreEquipo", 10f, y.toFloat(), titlePaint)
    y += 30
    canvas.drawLine(10f, y.toFloat(), (pageWidth - 10).toFloat(), y.toFloat(), separatorPaint)
    y += 10

    fichas.forEach { ficha ->
        if (y + 120 > pageHeight) nuevaPagina()

        canvas.drawText("Jugador: ${ficha.nombreJugador ?: "Desconocido"}", 10f, y.toFloat(), headerPaint)
        y += 25

        ficha.piernaHabil?.let {
            y = drawLabeledText(canvas, "Pierna hábil: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        ficha.caracteristicasFisicas?.let {
            y = drawLabeledText(canvas, "Físico: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        ficha.caracteristicasTacticas?.let {
            y = drawLabeledText(canvas, "Táctica: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        ficha.caracteristicasTecnicas?.let {
            y = drawLabeledText(canvas, "Técnica: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        ficha.conductaEntrenamiento?.let {
            y = drawLabeledText(canvas, "Comportamiento entrenamiento: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        ficha.conductaConCompañeros?.let {
            y = drawLabeledText(canvas, "Comportamiento compañeros: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        ficha.observacionFinal?.let {
            y = drawLabeledText(canvas, "Observación final: ", it, 10f, y, headerPaint, bodyPaint, pageWidth)
        }

        y += 10
        canvas.drawLine(10f, y.toFloat(), (pageWidth - 10).toFloat(), y.toFloat(), separatorPaint)
        y += 20
    }

    document.finishPage(page)

    val filename = "fichas_jugadores_${nombreEquipo}_${System.currentTimeMillis()}.pdf"

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
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloadsDir, filename)
            FileOutputStream(outFile).use { document.writeTo(it) }
            Toast.makeText(context, "PDF guardado en: ${outFile.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }

    document.close()
}

fun dividirTextoPorAncho(texto: String, paint: Paint, maxWidth: Float): List<String> {
    val palabras = texto.split(" ")
    val lineas = mutableListOf<String>()
    var lineaActual = ""

    for (palabra in palabras) {
        val posibleLinea = if (lineaActual.isEmpty()) palabra else "$lineaActual $palabra"
        if (paint.measureText(posibleLinea) <= maxWidth) {
            lineaActual = posibleLinea
        } else {
            lineas.add(lineaActual)
            lineaActual = palabra
        }
    }

    if (lineaActual.isNotEmpty()) {
        lineas.add(lineaActual)
    }

    return lineas
}

fun drawLabeledText(
    canvas: android.graphics.Canvas,
    label: String,
    text: String,
    x: Float,
    yStart: Int,
    labelPaint: Paint,
    bodyPaint: Paint,
    pageWidth: Int
): Int {
    val labelWidth = labelPaint.measureText(label)
    val maxTextWidth = pageWidth - x - 10 - labelWidth
    var y = yStart

    canvas.drawText(label, x, y.toFloat(), labelPaint)

    val lines = dividirTextoPorAncho(text, bodyPaint, maxTextWidth)
    lines.forEachIndexed { index, line ->
        val xOffset = if (index == 0) x + labelWidth else x
        canvas.drawText(line, xOffset, y.toFloat(), bodyPaint)
        y += 20
    }

    return y
}