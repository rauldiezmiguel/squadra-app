package screens

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.IntOffset
import network.EventoCalendario
import network.JugadorDTO
import org.rauldiezmiguel.tfgfutbolbase.R
import viewModel.*


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@SuppressLint("RememberReturnType")
@Composable
fun VerAnalisisRivalPartidoScreen(
    idEquipo: Int,
    idPartido: Int,
    cuartosRivalViewModel: AndroidCuartosRivalViewModel,
    alineacionRivalViewModel: AndroidAlineacionRivalViewModel,
    partido: EventoCalendario.Partido
){
    val cuartoRivalState by cuartosRivalViewModel.cuartoRivalState.collectAsState()
    val alineacionesPorCuarto by alineacionRivalViewModel.alineacionesPorCuarto.collectAsState()
    val alineacionesMap = remember { mutableStateMapOf<Int, SnapshotStateMap<Int, JugadorRivalConPosicion>>() }

    LaunchedEffect(Unit) {
        cuartosRivalViewModel.obtenerCuartosRivalPorPartido(idPartido)
    }

    val cuartos = if (cuartoRivalState is CuartoRivalState.Success) {
        (cuartoRivalState as CuartoRivalState.Success).cuarto
    } else emptyList()

    val cuartosOrdenados = cuartos.sortedBy { it.numero }

    LaunchedEffect(cuartosOrdenados) {
        cuartosOrdenados.forEach { cuarto ->
            alineacionRivalViewModel.obtenerAlineaciones(cuarto.id)
        }
    }

    val alineacionesUI = remember(cuartos, alineacionesPorCuarto) {
        alineacionesPorCuarto.mapValues { (_, alineacionesList) ->
            alineacionesList.map { alineacion ->
                JugadorRivalConPosicion(
                    dorsalJugador = alineacion.dorsalJugador,
                    offset = Offset(alineacion.posX, alineacion.posY),
                    idAlineacion = alineacion.id,
                    idCuarto = alineacion.idCuarto
                )
            }
        }.flatMap { it.value }
    }

    LaunchedEffect(alineacionesUI.size) {
        alineacionesUI.groupBy { it.idCuarto }.forEach { (idCuarto, alineaciones) ->
            val jugadoresDeCuarto = alineacionesMap.getOrPut(idCuarto) { mutableStateMapOf() }
            alineaciones.forEach { alineacion ->
                jugadoresDeCuarto[alineacion.idAlineacion] = alineacion
            }
            jugadoresDeCuarto.keys.toList().forEach { idAlineacion ->
                if (alineaciones.none { it.idAlineacion == idAlineacion }) {
                    jugadoresDeCuarto.remove(idAlineacion)
                }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        when (cuartoRivalState) {
            is CuartoRivalState.Success -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ){
                    partido.jugadoresDestacados
                        ?.takeIf { it.isNotBlank() }
                        ?.let {

                            InfoCard(
                                icon = Icons.Filled.CheckCircle,
                                title = "Jugadores Destacados",
                                content = it,
                                titleColor = secondaryColor,
                                borderColor = secondaryColor
                            )
                        }

                    cuartosOrdenados.forEachIndexed { index, cuarto ->
                        Text(
                            "Parte ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = secondaryColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        val jugadoresDeCuarto = alineacionesMap.getOrPut(cuarto.id) { mutableStateMapOf() }

                        CampoConJugadoresRivalesSoloVista(
                            jugadores = jugadoresDeCuarto.values.toList()
                        )

                        Spacer(Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Analisis Rival",
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = cuarto.analisisRival.orEmpty().ifBlank { "Sin información" },
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Observaciones",
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = cuarto.observaciones.orEmpty().ifBlank { "Sin información" },
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            is CuartoRivalState.Loading -> CircularProgressIndicator()
            is CuartoRivalState.Error -> Text("Error: ${(cuartoRivalState as CuartoRivalState.Error).message}")
            else -> {}
        }
    }
}

@Composable
fun CampoConJugadoresRivalesSoloVista(
    jugadores: List<JugadorRivalConPosicion>
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
    ) {
        val campoWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val campoHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        Image(
            painter = painterResource(id = R.drawable.campo_futbol_girado),
            contentDescription = "Campo de fútbol",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        jugadores.forEach { jugador ->
            val posX = jugador.offset.x.coerceIn(0f, 100f)
            val posY = jugador.offset.y.coerceIn(0f, 100f)

            val xPx = (posX / 100f) * campoWidthPx
            val yPx = (posY / 100f) * campoHeightPx

            Box(
                modifier = Modifier.offset {
                    IntOffset(xPx.toInt(), yPx.toInt())
                }
            ) {
                CamisetaRivalJugador(jugador.dorsalJugador)
            }
        }
    }
}