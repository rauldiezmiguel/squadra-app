package network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

// emulador http://10.0.2.2:9090
// dispositivo físico http://localhost:8080
// https://ktor-server-vhqi.onrender.com

actual fun getHttpEngine(): HttpClientEngine = OkHttp.create()
actual fun getApiBaseUrl(): String = "https://ktor-server-vhqi.onrender.com"