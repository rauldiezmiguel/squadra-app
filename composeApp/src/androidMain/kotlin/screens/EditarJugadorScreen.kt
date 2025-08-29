package screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.AndroidJugadorViewModel

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun EditarJugadorScreen(
    idJugador: Int,
    jugadorViewModel: AndroidJugadorViewModel,
    onBack: () -> Unit,
    onGuardar: (Int, String) -> Unit // dorsal, posicion
) {
    val jugador by jugadorViewModel.jugadorSeleccionado.collectAsState()

    var dorsal by remember { mutableStateOf("") }
    var posicion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Llamamos a la función para obtener el jugador cuando entra a la pantalla
    LaunchedEffect(idJugador) {
        jugadorViewModel.getJugadoresPorId(idJugador)
    }

    // Asignamos los valores del jugador solo cuando se carga por primera vez
    LaunchedEffect(jugador) {
        jugador?.let {
            if (dorsal.isEmpty() && posicion.isEmpty()) {
                dorsal = it.dorsal.toString()
                posicion = it.posicion
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .background(backgroundColor),
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
            "Editar Jugador",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = dorsal,
                    onValueChange = { dorsal = it.filter { c -> c.isDigit() } },
                    label = { Text("Dorsal", color = secondaryColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = posicion,
                    onValueChange = { posicion = it },
                    label = { Text("Posición", color = secondaryColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                isLoading = true
                onGuardar(dorsal.toInt(), posicion)
                isLoading = false

                Toast.makeText(context, "Jugador actualizado correctamente", Toast.LENGTH_SHORT).show()
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
            enabled = dorsal.isNotBlank() && posicion.isNotBlank()
        ) {
            Text(text = if (isLoading) "Guardando..." else "Guardar", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Cancelar", color = secondaryColor)
        }
    }
}
