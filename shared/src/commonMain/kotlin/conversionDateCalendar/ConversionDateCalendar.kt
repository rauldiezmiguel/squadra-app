package conversionDateCalendar

import com.kizitonwose.calendar.core.YearMonth

/*
// Extensión: convierte de kotlinx.datetime.LocalDate a YearMonth
fun LocalDate.toYearMonthConversion(): YearMonth {
    return YearMonth(this.year, this.monthNumber)
}
*/

/*
// Extensión: convierte de YearMonth a una fecha específica del mes
fun YearMonth.atDay(day: Int): LocalDate {
    val maxDay = this.lengthOfMonth()
    require(day in 1..maxDay) { "Día fuera del rango para este mes" }
    return LocalDate(this.year, this.month, day)
}
*/

// Longitud del mes (funciona sin java.time)
fun YearMonth.lengthOfMonth(): Int {
    // Febrero: validamos si es bisiesto
    return when (this.monthNumber) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(this.year)) 29 else 28
        else -> throw IllegalArgumentException("Mes inválido: ${this.month}")
    }
}

// Verificar año bisiesto (útil para febrero)
fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}