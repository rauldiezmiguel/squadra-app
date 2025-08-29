package repository

import com.kizitonwose.calendar.core.YearMonth
import network.EventoCalendario
import kotlinx.datetime.LocalDate
import network.CalendarioApi
//import conversionDate.toYearMonthConversion

// Extensión: convierte de kotlinx.datetime.LocalDate a YearMonth
fun LocalDate.toYearMonthConversion(): YearMonth {
    return YearMonth(this.year, this.monthNumber)
}

object CalendarioRepository {
    // Cache de eventos, donde la clave es un YearMonth (ej. 2025-04)
    private val cacheEventos = mutableMapOf<Int, MutableMap<YearMonth, List<EventoCalendario>>>()

    /**
     * Obtiene los eventos de un equipo para un mes específico.
     * Si no están en caché, los descarga y los guarda.
     */
    suspend fun getEventosForMonth(idEquipo: Int, month: YearMonth): List<EventoCalendario>{
        /*
        val eventosMesEquipo = cacheEventos[idEquipo]?.get(month)
        if (eventosMesEquipo != null) return eventosMesEquipo
         */

        val todosLosEventos = CalendarioApi.getEventosPorEquipo(idEquipo)
        val eventosDelMes = todosLosEventos.filter { it.fecha.toYearMonthConversion() == month }

        val eventosPorMes = cacheEventos.getOrPut(idEquipo) { mutableMapOf() }
        eventosPorMes[month] = eventosDelMes
        return eventosDelMes
    }

    /**
     * Fuerza la recarga de todos los eventos para el equipo, y actualiza la caché.
     */
    suspend fun refrescarEventosEquipo(idEquipo: Int) {
        val todosLosEventos = CalendarioApi.getEventosPorEquipo(idEquipo)

        // Agrupar por YearMonth
        val agrupadosPorMes = todosLosEventos.groupBy { it.fecha.toYearMonthConversion() }
        cacheEventos[idEquipo] = agrupadosPorMes.toMutableMap()
    }

    // Limpia el cache completo (puedes implementar uno que limpie solo el mes afectado)
    fun limpiarCache() {
        cacheEventos.clear()
    }
}