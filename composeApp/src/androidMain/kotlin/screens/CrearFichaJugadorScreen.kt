package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import network.FichaJugadorRequest
import viewModel.AndroidJugadorViewModel

//private val AzulGrisaceo = Color(0xFF37474F)
//private val FondoColor   = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun CrearFichaJugadorScreen(
    idJugador: Int,
    idEquipo: Int,
    viewModel: AndroidJugadorViewModel,
    onCreated: () -> Unit
) {
    var piernaHabil by remember { mutableStateOf("") }
    var caracteristicasFisicas by remember { mutableStateOf("") }
    var caracteristicasTacticas by remember { mutableStateOf("") }
    var caracteristicasTecnicas by remember { mutableStateOf("") }
    var conductaEntrenamiento by remember { mutableStateOf("") }
    var conductaConCompañeros by remember { mutableStateOf("") }
    var observacionFinal by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            // Título
            Text(
                text = "Ficha del Jugador",
                style = MaterialTheme.typography.headlineSmall,
                color = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Contenido scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                FichaTextField("Pierna hábil", piernaHabil) { piernaHabil = it }
                Spacer(Modifier.height(12.dp))

                FichaTextField("Características físicas", caracteristicasFisicas) { caracteristicasFisicas = it }
                Spacer(Modifier.height(12.dp))

                FichaTextField("Características tácticas", caracteristicasTacticas) { caracteristicasTacticas = it }
                Spacer(Modifier.height(12.dp))

                FichaTextField("Características técnicas", caracteristicasTecnicas) { caracteristicasTecnicas = it }
                Spacer(Modifier.height(12.dp))

                FichaTextField("Conducta en entrenamiento", conductaEntrenamiento) { conductaEntrenamiento = it }
                Spacer(Modifier.height(12.dp))

                FichaTextField("Conducta con compañeros", conductaConCompañeros) { conductaConCompañeros = it }
                Spacer(Modifier.height(20.dp))

                FichaTextField("Observacion final", observacionFinal) { observacionFinal = it }
                Spacer(Modifier.height(20.dp))
            }

            // Botón fijo al final
            Button(
                onClick = {
                    val ficha = FichaJugadorRequest(
                        idJugador = idJugador,
                        idEquipo = idEquipo,
                        piernaHabil = piernaHabil,
                        caracteristicasFisicas = caracteristicasFisicas,
                        caracteristicasTacticas = caracteristicasTacticas,
                        caracteristicasTecnicas = caracteristicasTecnicas,
                        conductaEntrenamiento = conductaEntrenamiento,
                        conductaConCompañeros = conductaConCompañeros,
                        observacionFinal = observacionFinal
                    )
                    viewModel.crearFichaJugador(ficha)
                    onCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
            ) {
                Text("Guardar Ficha", color = Color.White)
            }
        }
    }
}

@Composable
fun FichaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = secondaryColor) },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        maxLines = 4
    )
}