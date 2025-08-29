package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import network.EventoCalendario

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.JugadorDTO
import viewModel.AndroidAlineacionEquipoViewModel
import viewModel.AndroidAlineacionRivalViewModel
import viewModel.AndroidCuartosEquipoViewModel
import viewModel.AndroidCuartosRivalViewModel
import viewModel.AndroidEstadisticasViewModel
import viewModel.AndroidJugadorViewModel


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerPartidoTabsScreen(
    idEquipo: Int,
    idPartido: Int,
    partido: EventoCalendario.Partido,
    jugadorViewModel: AndroidJugadorViewModel,
    cuartosEquipoViewModel: AndroidCuartosEquipoViewModel,
    alineacionEquipoViewModel: AndroidAlineacionEquipoViewModel,
    cuartosRivalViewModel: AndroidCuartosRivalViewModel,
    alineacionRivalViewModel: AndroidAlineacionRivalViewModel,
    estadisticasViewModel: AndroidEstadisticasViewModel,
    onBack: () -> Unit
) {
    val tabs = listOf("Resumen", "Análisis Equipo", "Análisis Rival")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

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
            containerColor = Color.White,
            contentColor = selectedColor,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
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

        when (selectedTabIndex) {
            0 -> VerPartidoScreen(
                partido = partido,
                jugadorViewModel = jugadorViewModel,
                estadisticasViewModel = estadisticasViewModel
            )
            1 -> VerAnalisisEquipoPartidoScreen(
                idEquipo = idEquipo,
                idPartido = idPartido,
                jugadorViewModel = jugadorViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel
            )
            2 -> VerAnalisisRivalPartidoScreen(
                idEquipo = idEquipo,
                idPartido = idPartido,
                cuartosRivalViewModel = cuartosRivalViewModel,
                alineacionRivalViewModel = alineacionRivalViewModel,
                partido = partido
            )
        }
    }
}
