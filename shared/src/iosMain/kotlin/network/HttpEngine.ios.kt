package network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

//http://192.168.0.10:8080
// Darwin.create() "https://ktor-server-vhqi.onrender.com"

actual fun getHttpEngine(): HttpClientEngine = Darwin.create {
    configureRequest {
        // Permite acceso a redes restringidas como redes móviles o Wi-Fi lentas
        setAllowsConstrainedNetworkAccess(true)

        // También puedes permitir conexiones costosas si aplica
        setAllowsExpensiveNetworkAccess(true)

        // (Opcional) Permitir conexiones en segundo plano
        setAllowsCellularAccess(true)
    }
}
actual fun getApiBaseUrl(): String = "https://ktor-server-vhqi.onrender.com"