package screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.CrearEvaluacionRequest
import viewModel.AndroidEvaluacionViewModel
import java.util.*
import kotlin.math.roundToInt

//private val AzulGrisaceo = Color(0xFF37474F)
//private val FondoColor   = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun CrearEvaluacionScreen(
    idJugador: Int,
    viewModel: AndroidEvaluacionViewModel,
    onCreated: () -> Unit
) {
    var fecha by remember { mutableStateOf("") }
    var comportamiento by remember { mutableIntStateOf(5) }
    var tecnica by remember { mutableIntStateOf(5) }
    var tactica by remember { mutableIntStateOf(5) }
    var observaciones by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendario = Calendar.getInstance()
    val picker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                fecha = "%04d-%02d-%02d".format(year, month + 1, day)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
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
            text = "Nueva Evaluación Semanal",
            style = MaterialTheme.typography.headlineSmall,
            color = primaryColor
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                // Fecha
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { },
                    label = { Text("Fecha", color = secondaryColor) },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { picker.show() },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.clickable { picker.show() }
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))

                // Comportamiento
                Rango1a10(
                    label = "Comportamiento",
                    value = comportamiento,
                    onValueChange = { comportamiento = it }
                )
                Spacer(Modifier.height(12.dp))

                // Técnica
                Rango1a10(
                    label = "Técnica",
                    value = tecnica,
                    onValueChange = { tecnica = it }
                )
                Spacer(Modifier.height(12.dp))

                // Táctica
                Rango1a10(
                    label = "Táctica",
                    value = tactica,
                    onValueChange = { tactica = it }
                )
                Spacer(Modifier.height(12.dp))

                // Observaciones
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones", color = secondaryColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val req = CrearEvaluacionRequest(
                            idJugador = idJugador,
                            fecha = fecha,
                            comportamiento = comportamiento,
                            tecnica = tecnica,
                            tactica = tactica,
                            observaciones = observaciones
                        )
                        viewModel.crearEvaluacion(idJugador, req)
                        onCreated()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) {
                    Text("Guardar Evaluación", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun Rango1a10(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("$label: $value", color = secondaryColor)
        Slider(
            // Slider usa valores Float, así que convertimos el Int a Float
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 1f..10f,
            steps = 8,            // 10 valores → 8 «steps» intermedios
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = secondaryColor,
                activeTrackColor = secondaryColor,
                inactiveTrackColor = secondaryColor.copy(alpha = 0.3f),
                activeTickColor = secondaryColor,
                inactiveTickColor = secondaryColor.copy(alpha = 0.3f)
            )
        )
    }
}
