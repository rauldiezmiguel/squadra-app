package visual

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.EventoCalendario
import com.kizitonwose.calendar.core.YearMonth
import conversionDateCalendar.lengthOfMonth
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate
import java.time.Month
import java.util.Locale

// Obtener una fecha (java.time.LocalDate) a partir de un día de este YearMonth.
fun YearMonth.atDayConversionDateConversion(day: Int): LocalDate {
    return LocalDate.of(this.year, this.month, day)
}

// Operaciones aritméticas: restar y sumar meses
fun YearMonth.minusMonths(months: Int): YearMonth {
    val javaYM = java.time.YearMonth.of(this.year, this.month).minusMonths(months.toLong())
    return YearMonth(javaYM.year, javaYM.monthValue)
}

fun YearMonth.plusMonths(months: Int): YearMonth {
    val javaYM = java.time.YearMonth.of(this.year, this.month).plusMonths(months.toLong())
    return YearMonth(javaYM.year, javaYM.monthValue)
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun CalendarioEquipo(
    currentMonth: YearMonth = YearMonth.now(),
    onChangeMonth: (YearMonth) -> Unit = {},
    onDayClick: (LocalDate) -> Unit = {},
    eventos: List<EventoCalendario> = emptyList()
) {

    //eventos.forEach { println("Evento: ${it} - Fecha: ${it.fecha}") }
    eventos.forEach { println("Evento en fecha: ${it.fecha}") }

    val today = LocalDate.now()
    val firstDayOfMonth = currentMonth.atDayConversionDateConversion(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) // Lunes = 1 → 1 % 7 = 1 (Lunes), Domingo = 7 → 0
    val totalCells = (daysInMonth + firstDayOfWeek)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cellSize = (screenWidth - 32.dp) / 7  // 32.dp para márgenes/padding

    // Usamos el Locale español
    val monthInt = (currentMonth.month.ordinal + 1) // ordinal: 0..11, +1 para tener 1..12
    val monthName = Month.of(monthInt).getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))
    val formattedMonth = "$monthName ${currentMonth.year}"

    // Colores neutros base (sin transparencia) para los eventos:
    val trainingColor = Color(0xFFA5D6A7)   // Gris azulado neutro para entrenamientos
    val partidoColor = Color(0xFFEF9A9A)    // Otro gris azulado para partidos

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB))
            .padding(16.dp)
    ) {
        // Encabezado del mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onChangeMonth(currentMonth.minusMonths(1)) }) {
                Text("<", fontSize = 20.sp, color = Color(0xFF5D5D5D))
            }
            Text(
                text = formattedMonth.replaceFirstChar { it.uppercase() },
                fontSize = 22.sp,
                color = Color(0xFF3C3C3C),
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { onChangeMonth(currentMonth.plusMonths(1)) }) {
                Text(">", fontSize = 20.sp, color = Color(0xFF5D5D5D))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contenedor centrado para los días y las celdas del calendario
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Días de la semana
                val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    diasSemana.forEach { dia ->
                        Box(
                            modifier = Modifier
                                .size(41.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dia,
                                color = Color(0xFF888888),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Grid del calendario
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(totalCells) { index ->
                        if (index < firstDayOfWeek) {
                            Box(modifier = Modifier.size(48.dp))
                        } else {
                            val day = index - firstDayOfWeek + 1
                            val date = currentMonth.atDayConversionDateConversion(day)
                            val isToday = date == today

                            // Verificar si existe algun evento para la fecha
                            val eventosDelDia = eventos.filter { it.fecha == date.toKotlinLocalDate() }

                            // Determinar el color de fondo según el tipo de evento.
                            // Se emplea una transparencia (alpha) para que el fondo sea sutil.
                            val cellBackground = when {
                                // Si hay eventos de ambos tipos, se usa un gris neutro
                                eventosDelDia.any { it is EventoCalendario.Entrenamiento } &&
                                        eventosDelDia.any { it is EventoCalendario.Partido } ->
                                    Color.Gray.copy(alpha = 0.5f)
                                // Si solo hay entrenamientos:
                                eventosDelDia.all { it is EventoCalendario.Entrenamiento } &&
                                        eventosDelDia.isNotEmpty() ->
                                    trainingColor.copy(alpha = 0.5f)
                                // Si solo hay partidos:
                                eventosDelDia.all { it is EventoCalendario.Partido } &&
                                        eventosDelDia.isNotEmpty() ->
                                    partidoColor.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            }

                            // Borde: resaltado si es hoy, de lo contrario un color neutro
                            val borderWidth = if (isToday) 3.dp else 1.dp
                            val borderColor = if (isToday) Color(0xFF4DB6AC) else Color(0xFFE0E0E0)

                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .padding(2.dp)
                                    .clickable { onDayClick(date) }
                                    .background(cellBackground, shape = MaterialTheme.shapes.small)
                                    .border(borderWidth, borderColor, shape = MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        color = Color(0xFF3C3C3C)
                                    )

                                    // Punto si hay entrenamiento con imagen
                                    val tieneImagen = eventosDelDia.any {
                                        it is EventoCalendario.Entrenamiento && !it.entrenamientoUrl.isNullOrBlank()
                                    }

                                    if (tieneImagen) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color(0xFF4CAF50), shape = CircleShape)  // verde
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leyenda eventos
                    Text(
                        text = "Leyenda de eventos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFA5D6A7), label = "Entrenamiento")
                    LegendItem(color = Color(0xFFEF9A9A), label = "Partido")
                    LegendDoubleDotItem(
                        baseColor = Color(0xFFA5D6A7),  // color entrenamiento
                        overlayColor = Color(0xFF4CAF50), // verde
                        label = "Entr. con imagen"
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun LegendDoubleDotItem(baseColor: Color, overlayColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(baseColor, shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(overlayColor, shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Black)
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun CalendarioEquipoPreview() {
    MaterialTheme {
        var mesActual by remember { mutableStateOf(YearMonth.now()) }
        CalendarioEquipo(
            currentMonth = mesActual,
            onChangeMonth = { nuevoMes -> mesActual = nuevoMes },
            onDayClick = { fecha -> println("Día clicado: $fecha") }
        )
    }
}