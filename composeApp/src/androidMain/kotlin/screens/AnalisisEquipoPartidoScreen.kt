package screens

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import org.rauldiezmiguel.tfgfutbolbase.R
import network.CrearAlineacionEquipoRequest
import network.CuartosEquipoDTO
import network.JugadorDTO
import network.ModificarCuartosEquipoRequest
import viewModel.*


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@SuppressLint("MutableCollectionMutableState")
@Composable
fun AnalisisEquipoPartidoScreen(
    onGuardar: (FichaPartido) -> Unit,
    idEquipo: Int,
    idPartido: Int,
    jugadorViewModel: AndroidJugadorViewModel,
    cuartosEquipoViewModel: AndroidCuartosEquipoViewModel,
    alineacionEquipoViewModel: AndroidAlineacionEquipoViewModel
) {
    val cuartoEquipoState by cuartosEquipoViewModel.cuartoEquipoState.collectAsState()
    val jugadorState by jugadorViewModel.jugadorState.collectAsState()
    val alineacionesUI by alineacionEquipoViewModel.alineacionesUI.collectAsState()

    val funcionamientoMap = remember { mutableStateMapOf<Int, String>() }
    val danoRivalMap = remember { mutableStateMapOf<Int, String>() }
    val observacionesMap = remember { mutableStateMapOf<Int, String>() }
    var jugadorSeleccionadoParaAgregar by remember { mutableStateOf<JugadorDTO?>(null) }

    var campoWidthPx by remember { mutableFloatStateOf(1f) }
    var campoHeightPx by remember { mutableFloatStateOf(1f) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val centrosMap = remember { mutableStateMapOf<Int, Offset>() }

    val alineacionesMap = remember { mutableStateMapOf<Int, SnapshotStateMap<Int, JugadorConPosicion>>() }

    // Obtener lista de cuartos y ordenarlos desde el inicio
    val cuartos = if (cuartoEquipoState is CuartoEquipoState.Success) {
        (cuartoEquipoState as CuartoEquipoState.Success).cuarto
    } else emptyList()

    val cuartosOrdenados = remember(cuartoEquipoState) {
        cuartos.sortedBy { it.numero }
    }

    // Efecto inicial: obtener cuartos y jugadores
    LaunchedEffect(Unit) {
        cuartosEquipoViewModel.obtenerCuartosEquipoPorPartido(idPartido)
        jugadorViewModel.getJugadoresPorEquipo(idEquipo)
    }

    // Al recibir los cuartos, inicializar mapas y pedir alineaciones
    LaunchedEffect(cuartoEquipoState) {
        if (cuartoEquipoState is CuartoEquipoState.Success) {
            cuartosOrdenados.forEach { cuarto ->
                funcionamientoMap.putIfAbsent(cuarto.id, cuarto.funcionamiento ?: "")
                danoRivalMap.putIfAbsent(cuarto.id, cuarto.danoRival ?: "")
                observacionesMap.putIfAbsent(cuarto.id, cuarto.observaciones ?: "")
                alineacionEquipoViewModel.obtenerAlineaciones(cuarto.id)
            }
        }
    }

    // Sincroniza alineacionesMap con alineacionesUI
    LaunchedEffect(alineacionesUI) {
        println("Alineaciones recibidas: ${alineacionesUI.size}")
        alineacionesUI.groupBy { it.idCuarto }.forEach { (idCuarto, alineaciones) ->
            println("Cuarto $idCuarto tiene ${alineaciones.size} alineaciones")
            val jugadoresDeCuarto = alineacionesMap.getOrPut(idCuarto) { mutableStateMapOf() }
            alineaciones.forEach { alineacion ->
                jugadoresDeCuarto[alineacion.idAlineacion] = JugadorConPosicion(
                    jugador = alineacion.jugador,
                    offset = Offset(alineacion.posX, alineacion.posY),
                    idAlineacion = alineacion.idAlineacion,
                    idCuarto = alineacion.idCuarto
                )
            }
            jugadoresDeCuarto.keys.toList().forEach { idAlineacion ->
                if (alineaciones.none { it.idAlineacion == idAlineacion }) {
                    jugadoresDeCuarto.remove(idAlineacion)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
        /*
        Text(
            "Análisis Equipo Partido",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

         */

        when (jugadorState) {
            is JugadorState.Success -> {
                val jugadores = (jugadorState as JugadorState.Success).jugadores.sortedBy { it.dorsal }
                alineacionEquipoViewModel.actualizarJugadores(jugadores)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(jugadores) { jugador ->
                        Box(modifier = Modifier.clickable { jugadorSeleccionadoParaAgregar = jugador }) {
                            CamisetaJugador(jugador)
                        }
                    }
                }
            }
            is JugadorState.Loading -> CircularProgressIndicator()
            is JugadorState.Error -> Text("Error: ${(jugadorState as JugadorState.Error).message}")
            else -> {}
        }

        Spacer(Modifier.height(8.dp))

        when (cuartoEquipoState) {
            is CuartoEquipoState.Loading -> CircularProgressIndicator()
            is CuartoEquipoState.Error -> Text("Error: ${(cuartoEquipoState as CuartoEquipoState.Error).message}")
            is CuartoEquipoState.Success -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    cuartosOrdenados.forEachIndexed { index, cuarto ->
                        Text(
                            "Parte ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = secondaryColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        val jugadoresDeCuarto = alineacionesMap.getOrPut(cuarto.id) { mutableStateMapOf() }

                        CampoConJugadoresConFondo(
                            jugadores = jugadoresDeCuarto.values.toList(),
                            onJugadorMove = { jugador, newOffset ->
                                jugadoresDeCuarto[jugador.idAlineacion] = jugador.copy(offset = newOffset)
                            },
                            onRemoveJugador = { idAlineacion ->
                                jugadoresDeCuarto.remove(idAlineacion)
                                alineacionEquipoViewModel.eliminarAlineacion(idAlineacion)
                            },
                            onObtenerCentro = { centro -> centrosMap[cuarto.id] = centro },
                            onSizeChanged = { widthPx, heightPx ->
                                campoWidthPx = widthPx
                                campoHeightPx = heightPx
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = funcionamientoMap[cuarto.id] ?: "",
                            onValueChange = { funcionamientoMap[cuarto.id] = it },
                            label = { Text("Funcionamiento Parte ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = danoRivalMap[cuarto.id] ?: "",
                            onValueChange = { danoRivalMap[cuarto.id] = it },
                            label = { Text("Daño Rival Parte ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = observacionesMap[cuarto.id] ?: "",
                            onValueChange = { observacionesMap[cuarto.id] = it },
                            label = { Text("Observaciones Parte ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            else -> {}
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                cuartos.forEach { cuarto ->
                    val request = ModificarCuartosEquipoRequest(
                        funcionamiento = funcionamientoMap[cuarto.id] ?: "",
                        danoRival = danoRivalMap[cuarto.id] ?: "",
                        observaciones = observacionesMap[cuarto.id] ?: ""
                    )
                    cuartosEquipoViewModel.actualizarCuartoEquipo(cuarto.id, request)
                }

                alineacionesMap.forEach { (idCuarto, jugadoresMap) ->
                    jugadoresMap.values.forEach { jugadorConPosicion ->
                        val posXNormalizado = (jugadorConPosicion.offset.x / campoWidthPx) * 100f
                        val posYNormalizado = (jugadorConPosicion.offset.y / campoHeightPx) * 100f

                        val yaExiste = alineacionesUI.any {
                            it.idCuarto == idCuarto && it.jugador.id == jugadorConPosicion.jugador.id
                        }
                        if (!yaExiste) {
                            alineacionEquipoViewModel.crearAlineacion(
                                CrearAlineacionEquipoRequest(
                                    idCuarto = jugadorConPosicion.idCuarto,
                                    idJugador = jugadorConPosicion.jugador.id,
                                    posX = posXNormalizado,
                                    posY = posYNormalizado
                                )
                            )
                        } else {
                            alineacionEquipoViewModel.updatePlayerAlineacion(
                                id = jugadorConPosicion.idAlineacion,
                                posX = posXNormalizado,
                                posY = posYNormalizado
                            )
                        }
                    }
                }

                scope.launch { snackbarHostState.showSnackbar("Alineación guardada correctamente") }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
        ) {
            Text("Guardar", color = Color.White)
        }
    }

    if (jugadorSeleccionadoParaAgregar != null && cuartoEquipoState is CuartoEquipoState.Success) {
        AlertDialog(
            onDismissRequest = { jugadorSeleccionadoParaAgregar = null },
            title = { Text("Selecciona el cuarto") },
            text = {
                Column {
                    cuartos.forEachIndexed { index, cuarto ->
                        Button(
                            onClick = {
                                val centro = centrosMap[cuarto.id] ?: Offset(300f, 300f)
                                val jugadoresDeCuarto = alineacionesMap.getOrPut(cuarto.id) { mutableStateMapOf() }
                                val nuevoIdAlineacion = (jugadoresDeCuarto.keys.maxOrNull() ?: 0) + 1

                                jugadoresDeCuarto[nuevoIdAlineacion] = JugadorConPosicion(
                                    jugador = jugadorSeleccionadoParaAgregar!!,
                                    offset = centro,
                                    idAlineacion = nuevoIdAlineacion,
                                    idCuarto = cuarto.id
                                )

                                jugadorSeleccionadoParaAgregar = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                        ) {
                            Text("Parte ${index + 1}", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

// Reutiliza tus componentes auxiliares (CamisetaJugador, ManiquiDraggable, CampoConJugadoresConFondo, JugadorConPosicion)
// tal como los tienes, sin cambios.


@Composable
fun CamisetaJugador(jugador: JugadorDTO, color: Color = secondaryColor, size: Dp = 60.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.plain_white_football_shirt_svgrepo_com),
            contentDescription = "Camiseta de ${jugador.nombreJugador}",
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.fillMaxSize()
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = jugador.nombreJugador,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = jugador.dorsal.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ManiquiDraggable(
    jugador: JugadorConPosicion,
    onDragEnd: (Offset) -> Unit,
    onRemove: () -> Unit
) {
    var offset by remember(jugador.idAlineacion) { mutableStateOf(jugador.offset) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar jugador") },
            text = { Text("¿Deseas eliminar a ${jugador.jugador.nombreJugador}?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemove()
                    showDialog = false
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    },
                    onDragEnd = {
                        onDragEnd(offset)
                    }
                )
            }
            .combinedClickable(
                onClick = { showDialog = true },
                onLongClick = { showDialog = true }
            )
    ) {
        CamisetaJugador(jugador.jugador)
    }
}

@Composable
fun CampoConJugadoresConFondo(
    jugadores: List<JugadorConPosicion>,
    onJugadorMove: (JugadorConPosicion, Offset) -> Unit,
    onRemoveJugador: (Int) -> Unit,
    onObtenerCentro: ((Offset) -> Unit)? = null,
    onSizeChanged: ((Float, Float) -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clipToBounds()
            .onSizeChanged { size ->
                onSizeChanged?.invoke(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        //val campoWidthPx = constraints.maxWidth.toFloat()
        //val campoHeightPx = constraints.maxHeight.toFloat()

        // Centro del campo (en píxeles)
        val centro = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
        LaunchedEffect(Unit) {
            onObtenerCentro?.invoke(centro)
        }

        // Fondo del campo
        Image(
            painter = painterResource(id = R.drawable.campo_futbol_girado),
            contentDescription = "Campo de fútbol",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        jugadores.forEach { jugador ->
            // Convertir offset normalizado (0-100) a píxeles
            /*val offsetPx = Offset(
                x = (jugador.offset.x / 100f) * campoWidthPx,
                y = (jugador.offset.y / 100f) * campoHeightPx
            )
             */

            ManiquiDraggable(
                jugador = jugador,
                onDragEnd = { newPxOffset ->
                    onJugadorMove(jugador, newPxOffset)
                },
                onRemove = { onRemoveJugador(jugador.idAlineacion) }
            )
        }
    }
}


data class JugadorConPosicion(
    val jugador: JugadorDTO,
    val offset: Offset = Offset.Zero,
    val idAlineacion: Int,
    val idCuarto: Int
)

data class FichaPartido(
    val cuartos: List<CuartosEquipoDTO>,
    val jugadores: List<JugadorDTO>,
    val alineaciones: List<JugadorConPosicion>
)
