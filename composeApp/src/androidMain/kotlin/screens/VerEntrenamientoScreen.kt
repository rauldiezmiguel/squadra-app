package screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import network.EventoCalendario

//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)


@Composable
fun VerEntrenamientoScreen(
    entrenamiento: EventoCalendario.Entrenamiento,
    onBack: () -> Unit
) {
    val storage = FirebaseStorage.getInstance().reference
    val context = LocalContext.current

    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isImageLoading by remember { mutableStateOf(false) }
    var showFullScreen by remember { mutableStateOf(false) }
    var showFullScreenUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(entrenamiento) {
        val paths = entrenamiento.entrenamientoUrl
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        isImageLoading = true

        val resolvedUrls = paths.map { path ->
            if (path.startsWith("https://")) {
                path
            } else {
                try {
                    storage.child(path).downloadUrl.await().toString()
                } catch (e: Exception) {
                    ""
                }
            }
        }.filter { it.isNotBlank() }

        imageUrls = resolvedUrls
        isImageLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        if (isImageLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material.Text(
                        text = "SQUADRA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Entrenamiento del ${entrenamiento.fecha}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = primaryColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                if (imageUrls.isEmpty()) {
                    Text(
                        text = "No hay imágenes disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        imageUrls.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Imagen del entrenamiento",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showFullScreenUrl = url },
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Compartir imagen del entrenamiento")
                                        )
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Compartir imagen",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Descripción
                entrenamiento.descripcion
                    ?.takeIf { it.isNotBlank() }
                    ?.let { desc ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            InfoCardScrollable(
                                icon = Icons.Filled.Info,
                                title = "Descripción",
                                content = desc,
                                titleColor = secondaryColor
                            )
                        }
                    }
            }
        }

        // Imagen a pantalla completa
        if (showFullScreenUrl != null) {
            Dialog(onDismissRequest = { showFullScreenUrl = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showFullScreenUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(showFullScreenUrl),
                        contentDescription = "Imagen en pantalla completa",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCardScrollable(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    title: String,
    content: String,
    titleColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 250.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = title,
                        tint = titleColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 160.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
