package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import network.ActualizarJugadoresDestacadosRequest
import network.ActualizarResultadoPartidoRequest
import network.CrearEstadisticasRequest
import viewModel.AndroidAlineacionEquipoViewModel
import viewModel.AndroidAlineacionRivalViewModel
import viewModel.AndroidCuartosEquipoViewModel
import viewModel.AndroidCuartosRivalViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.AndroidUsuarioViewModel

// Tu color base
//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)


@Composable
fun ModificarPartidoTabsScreen(
    idEquipo: Int,
    idPartido: Int,
    onDismiss: () -> Unit,
    onUpdatePartido: (ActualizarResultadoPartidoRequest, (Boolean, String?) -> Unit) -> Unit,
    onUpdateJugadoresDestacados: (ActualizarJugadoresDestacadosRequest, (Boolean, String?) -> Unit) -> Unit,
    onCreateEstadistica: (CrearEstadisticasRequest) -> Unit,
    onGuardar: (FichaPartido) -> Unit,
    onGuardarRival: () -> Unit,
    jugadorViewModel: AndroidJugadorViewModel,
    viewModel: AndroidUsuarioViewModel,
    cuartosEquipoViewModel: AndroidCuartosEquipoViewModel,
    alineacionEquipoViewModel: AndroidAlineacionEquipoViewModel,
    cuartosRivalViewModel: AndroidCuartosRivalViewModel,
    alineacionRivalViewModel: AndroidAlineacionRivalViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val tabs = listOf("Actualizar", "Análisis Equipo", "Análisis Rival")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Paleta de colores para tabs
    val selectedColor = primaryColor
    val unselectedColor = primaryColor.copy(alpha = 0.6f)
    val indicatorColor = primaryColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
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
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = backgroundColor, // Fondo de la barra de pestañas
            contentColor = selectedColor,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = indicatorColor
                )
            },
            divider = {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = secondaryColor.copy(alpha = 0.3f)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (isSelected) selectedColor else unselectedColor,
                            style = if (isSelected) {
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                )
            }
        }

        // Contenido de cada pestaña
        when (selectedTabIndex) {
            0 -> ActualizarPartidoScreen(
                idPartido = idPartido,
                idEquipo = idEquipo,
                jugadorViewModel = jugadorViewModel,
                onDismiss = onDismiss,
                onUpdatePartido = onUpdatePartido,
                onCreateEstadistica = onCreateEstadistica
            )
            1 -> AnalisisEquipoPartidoScreen(
                onGuardar = onGuardar,
                idEquipo = idEquipo,
                idPartido = idPartido,
                jugadorViewModel = jugadorViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel

            )
            2 -> AnalisisRivalPartidoScreen(
                onGuardar = onGuardarRival,
                idEquipo = idEquipo,
                idPartido = idPartido,
                cuartosRivalViewModel = cuartosRivalViewModel,
                alineacionRivalViewModel = alineacionRivalViewModel,
                onUpdateJugadoresDestacados = onUpdateJugadoresDestacados,
                onDismiss = onDismiss
            )
        }
    }
}
