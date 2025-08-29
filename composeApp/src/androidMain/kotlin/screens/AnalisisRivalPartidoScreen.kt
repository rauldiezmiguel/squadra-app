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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import network.*
import org.rauldiezmiguel.tfgfutbolbase.R
import viewModel.*


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@SuppressLint("MutableCollectionMutableState")
@Composable
fun AnalisisRivalPartidoScreen(
    onGuardar: () -> Unit,
    idEquipo: Int,
    idPartido: Int,
    onUpdateJugadoresDestacados: (ActualizarJugadoresDestacadosRequest, (Boolean, String?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    cuartosRivalViewModel: AndroidCuartosRivalViewModel,
    alineacionRivalViewModel: AndroidAlineacionRivalViewModel
) {
    val cuartoRivalState by cuartosRivalViewModel.cuartoRivalState.collectAsState()
    val alineacionesUI by alineacionRivalViewModel.alineaciones.collectAsState()

    val analisisRivalMap = remember { mutableStateMapOf<Int, String>() }
    val observacionesMap = remember { mutableStateMapOf<Int, String>() }
    val dorsalesDisponibles = remember { mutableStateListOf<JugadorRivalDTO>().apply { addAll((1..12).map { JugadorRivalDTO(it) }) } }
    var showAddDorsalDialog by remember { mutableStateOf(false) }
    var nuevoDorsal by remember { mutableStateOf("") }
    var jugadorSeleccionadoParaAgregar by remember { mutableStateOf<JugadorRivalDTO?>(null) }
    var jugadoresDestacados by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var campoWidthPx by remember { mutableFloatStateOf(1f) }
    var campoHeightPx by remember { mutableFloatStateOf(1f) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val centrosMap = remember { mutableStateMapOf<Int, Offset>() }
    val alineacionesMap = remember { mutableStateMapOf<Int, SnapshotStateMap<Int, JugadorRivalConPosicion>>() }


    val cuartos = if (cuartoRivalState is CuartoRivalState.Success) {
        (cuartoRivalState as CuartoRivalState.Success).cuarto
    } else emptyList()

    val cuartosOrdenados = cuartos.sortedBy { it.numero }

    LaunchedEffect(Unit) {
        cuartosRivalViewModel.obtenerCuartosRivalPorPartido(idPartido)
    }

    LaunchedEffect(cuartoRivalState) {
        if (cuartoRivalState is CuartoRivalState.Success) {
            cuartosOrdenados.forEach { cuarto ->
                analisisRivalMap.putIfAbsent(cuarto.id, cuarto.analisisRival ?: "")
                observacionesMap.putIfAbsent(cuarto.id, cuarto.observaciones ?: "")
            }
        }
    }

    // Sincroniza alineaciones locales con backend
    LaunchedEffect(alineacionesUI) {
        alineacionesUI.groupBy { it.idCuarto }.forEach { (idCuarto, alineaciones) ->
            val jugadoresDeCuarto = alineacionesMap.getOrPut(idCuarto) { mutableStateMapOf() }
            alineaciones.forEach { alineacion ->
                jugadoresDeCuarto[alineacion.id] = JugadorRivalConPosicion(
                    dorsalJugador = alineacion.dorsalJugador,
                    offset = Offset(alineacion.posX, alineacion.posY),
                    idAlineacion = alineacion.id,
                    idCuarto = alineacion.idCuarto
                )
            }
            jugadoresDeCuarto.keys.toList().forEach { idAlineacion ->
                if (alineaciones.none { it.id == idAlineacion }) {
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
            "Análisis Rival Partido",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

         */

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(dorsalesDisponibles) { jugador ->
                Box(modifier = Modifier.clickable { jugadorSeleccionadoParaAgregar = jugador }) {
                    CamisetaRivalJugador(jugador.dorsalJugador)
                }
            }

            // Botón extra al final para agregar dorsal nuevo
            item {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(2.dp, secondaryColor, RoundedCornerShape(8.dp))
                        .clickable { showAddDorsalDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = secondaryColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAddDorsalDialog) {
            AlertDialog(
                onDismissRequest = { showAddDorsalDialog = false },
                title = { Text("Agregar dorsal nuevo") },
                text = {
                    OutlinedTextField(
                        value = nuevoDorsal,
                        onValueChange = { nuevoDorsal = it },
                        label = { Text("Dorsal") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val dorsalInt = nuevoDorsal.toIntOrNull()
                        if (dorsalInt != null && dorsalesDisponibles.none { it.dorsalJugador == dorsalInt }) {
                            dorsalesDisponibles.add(JugadorRivalDTO(dorsalInt))
                        }
                        nuevoDorsal = ""
                        showAddDorsalDialog = false
                    }) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDorsalDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        when (cuartoRivalState) {
            is CuartoRivalState.Loading -> CircularProgressIndicator()
            is CuartoRivalState.Error -> Text("Error: ${(cuartoRivalState as CuartoRivalState.Error).message}")
            is CuartoRivalState.Success -> {
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

                        CampoRivalConJugadoresConFondo(
                            jugadores = jugadoresDeCuarto.values.toList(),
                            onJugadorMove = { jugador, newOffset ->
                                jugadoresDeCuarto[jugador.idAlineacion] = jugador.copy(offset = newOffset)
                            },
                            onRemoveJugador = { idAlineacion ->
                                jugadoresDeCuarto.remove(idAlineacion)
                                alineacionRivalViewModel.eliminarAlineacion(idAlineacion)
                            },
                            onObtenerCentro = { centro -> centrosMap[cuarto.id] = centro },
                            onSizeChanged = { widthPx, heightPx ->
                                campoWidthPx = widthPx
                                campoHeightPx = heightPx
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = analisisRivalMap[cuarto.id] ?: "",
                            onValueChange = { analisisRivalMap[cuarto.id] = it },
                            label = { Text("Análisis Rival Parte ${index + 1}") },
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

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 2.dp,
                        color = secondaryColor
                    )

                    Text(
                        text = "Jugadores Destacados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = jugadoresDestacados,
                        onValueChange = { jugadoresDestacados = it },
                        label = { Text("Ej: Dorsal 10 -  Cualidades, Dorsal 7 - Cualidades ...") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            else -> {}
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {

                cuartos.forEach { cuarto ->
                    val request = ModificarCuartosRivalRequest(
                        analisisRival = analisisRivalMap[cuarto.id],
                        observaciones = observacionesMap[cuarto.id]
                    )
                    cuartosRivalViewModel.actualizarCuartoRival(cuarto.id, request)
                }

                isUpdating = true
                val partidoRequest = ActualizarJugadoresDestacadosRequest(
                    jugadoresDestacados = jugadoresDestacados
                )
                onUpdateJugadoresDestacados(partidoRequest) { success, error ->
                    isUpdating = false
                    if (success) onDismiss() else errorMessage = error
                }

                // Guarda alineaciones solo con dorsalJugador
                alineacionesMap.forEach { (idCuarto, jugadoresMap) ->
                    jugadoresMap.values.forEach { jugador ->
                        val posXNormalizado = (jugador.offset.x / campoWidthPx) * 100f
                        val posYNormalizado = (jugador.offset.y / campoHeightPx) * 100f

                        val yaExiste = alineacionesUI.any {
                            it.idCuarto == idCuarto && it.dorsalJugador == jugador.dorsalJugador
                        }
                        if (!yaExiste) {
                            alineacionRivalViewModel.crearAlineacion(
                                CrearAlineacionRivalRequest(
                                    idCuarto = jugador.idCuarto,
                                    dorsalJugador = jugador.dorsalJugador,
                                    posX = posXNormalizado,
                                    posY = posYNormalizado
                                )
                            )
                        } else {
                            alineacionRivalViewModel.updatePlayerRival(
                                id = jugador.idAlineacion,
                                posX = posXNormalizado,
                                posY = posYNormalizado
                            )
                        }
                    }
                }

                scope.launch { snackbarHostState.showSnackbar("Análisis del rival guardado correctamente") }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
        ) {
            Text("Guardar", color = Color.White)
        }
    }

    // Diálogo para elegir cuarto
    if (jugadorSeleccionadoParaAgregar != null && cuartoRivalState is CuartoRivalState.Success) {
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

                                jugadoresDeCuarto[nuevoIdAlineacion] = JugadorRivalConPosicion(
                                    dorsalJugador = jugadorSeleccionadoParaAgregar!!.dorsalJugador,
                                    offset = centro,
                                    idAlineacion = nuevoIdAlineacion,  // null porque es nuevo
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

@Composable
fun CamisetaRivalJugador(dorsalJugador: Int, color: Color = secondaryColor, size: Dp = 60.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.plain_white_football_shirt_svgrepo_com),
            contentDescription = "Camiseta de $dorsalJugador",
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.fillMaxSize()
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = dorsalJugador.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ManiquiRivalDraggable(
    jugador: JugadorRivalConPosicion,
    onDragEnd: (Offset) -> Unit,
    onRemove: () -> Unit
) {
    var offset by remember(jugador.idAlineacion) { mutableStateOf(jugador.offset) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar jugador") },
            text = { Text("¿Deseas eliminar a ${jugador.dorsalJugador}?") },
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
        CamisetaRivalJugador(jugador.dorsalJugador)
    }
}

@Composable
fun CampoRivalConJugadoresConFondo(
    jugadores: List<JugadorRivalConPosicion>,
    onJugadorMove: (JugadorRivalConPosicion, Offset) -> Unit,
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

        val centro = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
        LaunchedEffect(Unit) {
            onObtenerCentro?.invoke(centro)
        }

        Image(
            painter = painterResource(id = R.drawable.campo_futbol_girado),
            contentDescription = "Campo de fútbol",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        jugadores.forEach { jugador ->
            /*val offsetPx = Offset(
                x = (jugador.offset.x / 100f) * campoWidthPx,
                y = (jugador.offset.y / 100f) * campoHeightPx
            )
             */

            ManiquiRivalDraggable(
                jugador = jugador,
                onDragEnd = { newPxOffset ->
                    onJugadorMove(jugador, newPxOffset) },
                onRemove = { onRemoveJugador(jugador.idAlineacion) }
            )
        }
    }
}

// Clave única: dorsalJugador
data class JugadorRivalConPosicion(
    val dorsalJugador: Int,
    val offset: Offset = Offset.Zero,
    val idAlineacion: Int,  // Puede ser null si es nuevo
    val idCuarto: Int
)

@Serializable
data class JugadorRivalDTO(
    val dorsalJugador: Int
)
