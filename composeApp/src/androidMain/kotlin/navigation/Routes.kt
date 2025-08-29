package navigation

import kotlinx.datetime.LocalDate

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object Main : Routes("main")
    data object Equipo : Routes("equipo/{idEquipo}/{nombreEquipo}/{idTemporada}") {
        fun createRoute(idEquipo: Int, nombreEquipo: String, idTemporada: Int) = "equipo/$idEquipo/$nombreEquipo/$idTemporada"
    }
    data object CrearEntrenamiento : Routes("crearEntrenamiento/{idEquipo}/{selectDate}/{idTemporada}"){
        fun createRoute(idEquipo: Int, selectDate: LocalDate, idTemporada: Int) = "crearEntrenamiento/$idEquipo/${selectDate}/$idTemporada"
    }
    data object VerEntrenamiento : Routes("verEntrenamiento")
    data object CrearPartido : Routes("crearPartido/{idEquipo}/{selectDate}/{idTemporada}"){
        fun createRoute(idEquipo: Int, selectDate: LocalDate, idTemporada: Int) = "crearPartido/$idEquipo/${selectDate}/$idTemporada"
    }
    data object VerPartido : Routes("verPartido")
    data object ActualizarPartido : Routes("actualizarPartido/{idEquipo}/{idPartido}/{idTemporada}/{selectDate}"){
        fun createRoute(idEquipo: Int, idPartido: Int, idTemporada: Int, selectDate: LocalDate) = "actualizarPartido/$idEquipo/$idPartido/$idTemporada/$selectDate"
    }
    data object JugadoresEquipo : Routes("jugadoresEquipo/{idEquipo}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, nombreEquipo: String) = "jugadoresEquipo/$idEquipo/$nombreEquipo"
    }
    data object CrearJugador : Routes("crearJugador/{idEquipo}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, nombreEquipo: String) = "crearJugador/$idEquipo/$nombreEquipo"
    }
    data object EditarJugador : Routes("editarJugador/{idJugador}") {
        fun createRoute(idJugador: Int) = "editarJugador/$idJugador"
    }
    data object Jugador : Routes("jugador/{idEquipo}/{nombreEquipo}"){
        fun createRoute(idEquipo: Int, nombreEquipo: String) = "jugador/$idEquipo/$nombreEquipo"
    }
    data object CrearEstadisticas : Routes("crearEstadisticas/{idJugador}/{idEquipo}") {
        fun createRoute(idJugador: Int, idEquipo: Int) = "crearEstadisticas/$idJugador/$idEquipo"
    }
    data object VerEstadisticasTotalesJugador : Routes("verEstadisticasTotalesJugador/{nombreEquipo}/{idJugador}/{idTemporada}") {
        fun createRoute(nombreEquipo: String, idJugador: Int, idTemporada: Int) = "verEstadisticasTotalesJugador/$nombreEquipo/$idJugador/$idTemporada"
    }
    data object VerDetalleEstadistica : Routes("verDetalleEstadisticas/{idEquipo}/{idTemporada}/{nomEstadistica}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, idTemporada: Int, nomEstadistica: String, nombreEquipo: String) = "verDetalleEstadisticas/$idEquipo/$idTemporada/$nomEstadistica/$nombreEquipo"
    }
    data object AsistenciaEntrenamiento :
        Routes("asistencia-entrenamiento/{idEntrenamiento}/{nombreEquipo}") {
        fun createRoute(idEntrenamiento: Int, nombreEquipo: String) = "asistencia-entrenamiento/$idEntrenamiento/$nombreEquipo"
    }
    data object VerAsistenciaEntrenamientoJugador :
        Routes("verAsistenciaEntrenamientoJugador/{idJugador}/{nombreEquipo}") {
        fun createRoute(idJugador: Int, nombreEquipo: String) = "verAsistenciaEntrenamientoJugador/$idJugador/$nombreEquipo"
    }
    data object CrearEvaluacion : Routes("crearEvaluacion/{idJugador}/{idEquipo}") {
        fun createRoute(idJugador: Int, idEquipo: Int) = "crearEvaluacion/$idJugador/$idEquipo"
    }
    data object VerEvaluacionesJugador : Routes("verEvaluacionesJugador/{idJugador}/{nombreEquipo}") {
        fun createRoute(idJugador: Int, nombreEquipo: String) = "verEvaluacionesJugador/$idJugador/$nombreEquipo"
    }
    data object VerEstadisticasTotalesEquipo : Routes("verEstadisticasTotalesEquipo/{idEquipo}/{idTemporada}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, idTemporada: Int, nombreEquipo: String) =
            "verEstadisticasTotalesEquipo/$idEquipo/$idTemporada/$nombreEquipo"
    }
    data object VerDetalleEstadisticaEquipo : Routes("verDetalleEstadisticaEquipo/{idEquipo}/{idTemporada}/{nomEstadistica}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, idTemporada: Int, nomEstadistica: String, nombreEquipo: String) =
            "verDetalleEstadisticaEquipo/$idEquipo/$idTemporada/$nomEstadistica/$nombreEquipo"
    }
    data object VerPerfilUsuario : Routes("verPerfilUsuario")
    data object VerListaPartidos : Routes("verListaPartido")
    data object CrearFichaJugador : Routes("crearFichaJugador/{idJugador}/{idEquipo}"){
        fun createRoute(idJugador: Int, idEquipo: Int) = "crearFichaJugador/$idJugador/$idEquipo"
    }
    data object VerFichasJugadores : Routes("verFichasJugadores/{idEquipo}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, nombreEquipo: String) = "verFichasJugadores/$idEquipo/$nombreEquipo"
    }
    data object VerListaEntrenamiento : Routes("verListaEntrenamiento")
    data object AnalisisEquipoPartido : Routes("analisisEquipoPartido/{idEquipo}/{nombreEquipo}/{idPartido}") {
        fun createRoute(idEquipo: Int, nombreEquipo: String, idPartido: Int) = "analisisEquipoPartido/$idEquipo/$nombreEquipo/$idEquipo"
    }
    data object AnalisisRivalPartido : Routes("analisisRivalPartido/{idEquipo}/{nombreEquipo}/{idPartido}") {
        fun createRoute(idEquipo: Int, nombreEquipo: String, idPartido: Int) = "analisisRivalPartido/$idEquipo/$nombreEquipo/$idEquipo"
    }
    data object ModificarPartidoTabs : Routes("modificarPartidoTabs/{idEquipo}/{idPartido}/{idTemporada}/{selectDate}/{nombreEquipo}") {
        fun createRoute(idEquipo: Int, idPartido: Int, idTemporada: Int, selectDate: LocalDate, nombreEquipo: String) = "modificarPartidoTabs/$idEquipo/$idPartido/$idTemporada/$selectDate/$nombreEquipo"
    }
    data object VerAnalisisEquipoPartido : Routes("verAnalisisEquipoPartido/{idEquipo}/{idPartido}") {
        fun createRoute(idEquipo: Int, idPartido: Int) = "verAnalisisEquipoPartido/$idEquipo/$idPartido"
    }
    data object VerAnalisisRivalPartido : Routes("verAnalisisRivalPartido/{idEquipo}/{idPartido}") {
        fun createRoute(idEquipo: Int, idPartido: Int) = "verAnalisisRivalPartido/$idEquipo/$idPartido"
    }
    data object VerPartidoTabs : Routes("verPartidoTabs/{idEquipo}") {
        fun createRoute(idEquipo: Int) = "verPartidoTabs/$idEquipo"
    }
    data object ChangePassword : Routes("changePassword")
}