package screens

import viewModel.AuthViewModel
import viewModel.LoginState
import viewModel.AndroidAuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de colores profesional
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)
private val errorColor = Color(0xFFD32F2F)

@Composable
fun LoginScreen(viewModel: AndroidAuthViewModel, onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.NavigateToMainScreen) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a Squadra",
                    fontSize = 24.sp,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "Usuario")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = secondaryColor,
                        cursorColor = primaryColor,
                        focusedTextColor = primaryColor,
                        unfocusedTextColor = secondaryColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = "Contraseña")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = secondaryColor,
                        cursorColor = primaryColor,
                        focusedTextColor = primaryColor,
                        unfocusedTextColor = secondaryColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar Sesión", fontSize = 16.sp, color = Color.White)
                    }
                }

                if (loginState is LoginState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Error: ${(loginState as LoginState.Error).message}",
                        color = errorColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
