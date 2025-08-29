package conversionDate

import com.kizitonwose.calendar.core.YearMonth
import java.time.YearMonth as JavaYearMonth
import java.time.LocalDate

import kotlinx.datetime.LocalDate as KtLocalDate
import java.time.LocalDate as JavaLocalDate

fun KtLocalDate.toJavaLocalDate(): JavaLocalDate {
    return JavaLocalDate.of(this.year, this.monthNumber, this.dayOfMonth)
}

fun JavaLocalDate.toKotlinLocalDateConversion(): KtLocalDate {
    return KtLocalDate(this.year, this.monthValue, this.dayOfMonth)
}

// Crea un YearMonth “actual” usando java.time.LocalDate
fun YearMonth.Companion.now(): YearMonth {
    val today = LocalDate.now()
    return YearMonth(today.year, today.monthValue)
}

/*
// Operaciones aritméticas: restar y sumar meses
fun YearMonth.minusMonths(months: Int): YearMonth {
    val javaYM = JavaYearMonth.of(this.year, this.month).minusMonths(months.toLong())
    return YearMonth(javaYM.year, javaYM.monthValue)
}

fun YearMonth.plusMonths(months: Int): YearMonth {
    val javaYM = JavaYearMonth.of(this.year, this.month).plusMonths(months.toLong())
    return YearMonth(javaYM.year, javaYM.monthValue)
}
*/

// Longitud del mes
fun YearMonth.lengthOfMonth(): Int {
    return JavaYearMonth.of(this.year, this.month).lengthOfMonth()
}

/*
// Obtener una fecha (java.time.LocalDate) a partir de un día de este YearMonth.
fun YearMonth.atDayConversionDateConversion(day: Int): LocalDate {
    return LocalDate.of(this.year, this.month, day)
}

 */


/*
// Extensión: convierte de kotlinx.datetime.LocalDate a YearMonth
fun kotlinx.datetime.LocalDate.toYearMonthConversion(): YearMonth {
    return YearMonth(this.year, this.monthNumber)
}*/