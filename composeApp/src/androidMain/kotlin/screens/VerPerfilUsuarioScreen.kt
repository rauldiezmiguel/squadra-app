package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.AndroidUsuarioViewModel
import viewModel.PerfilState

// Colores de la app
private val CardColor = Color.White
private val ButtonColor = Color(0xFFD32F2F)
//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerPerfilUsuarioScreen(
    viewModel: AndroidUsuarioViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
    val perfilState by viewModel.perfilState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPerfilUsuario()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        fontSize = 24.sp,
                        color = primaryColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(ButtonColor)
                ) {
                    Text("Cerrar Sesión", color = Color.White)
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (perfilState) {
                is PerfilState.Loading -> {
                    CircularProgressIndicator(color = secondaryColor)
                }
                is PerfilState.Error -> {
                    val message = (perfilState as PerfilState.Error).message
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.cargarPerfilUsuario() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is PerfilState.Success -> {
                    val perfil = (perfilState as PerfilState.Success).perfil
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sección Usuario
                        item {
                            Text(
                                text = "Usuario",
                                style = MaterialTheme.typography.titleMedium,
                                color = secondaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = perfil.nombreUsuario,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = secondaryColor
                                )
                            }
                        }
                        // Sección Tipo de usuario
                        item {
                            Text(
                                text = "Tipo",
                                style = MaterialTheme.typography.titleMedium,
                                color = secondaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = perfil.tipoUsuario.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = secondaryColor
                                )
                            }
                        }
                        // Sección Club
                        perfil.club?.let { club ->
                            item {
                                Text(
                                    text = "Club",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = secondaryColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = club.nombre,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = secondaryColor
                                    )
                                }
                            }
                        }
                        // Sección Equipos, solo si hay datos
                        if (perfil.equipos.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Equipos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = secondaryColor
                                )
                            }
                            items(perfil.equipos) { equipo ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = equipo.nombreEquipo,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Texto clicable para cambiar contraseña
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Cambiar contraseña",
                                color = secondaryColor,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChangePassword() }
                                    .padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
