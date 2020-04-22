package fr.useless.depdero.core.tool

import androidx.core.text.isDigitsOnly
import java.text.SimpleDateFormat
import java.util.*


//////////////////////////// HEURE
// de [22, 42] à [22:42]
fun formatHhmmToString(hours: Int, minutes: Int): String {
    return "$hours".padStart(2, '0') +
            ":" +
            "$minutes".padStart(2, '0')
}

// de [22:42] à [22, 42]
fun formatStringToHhmm(dateAsString: String): Pair<Int, Int> {
    dateAsString
        .split(":")
        .filter {
            it.isDigitsOnly()
        }
        .takeIf{ it.count() == 2 }
        ?.let {
            return Pair( it[0].toInt(), it[1].toInt() )
        }
    return Pair(12, 0)
}


//////////////////////////// DATE
// de [2020, 04, 03] à [03/04/2020]
fun formatYmdToString(year: Int, month: Int, day: Int): String {
    return "$day".padStart(2, '0') +
            "/" +
            "$month".padStart(2, '0') +
            "/" +
            "$year".padStart(4, '0')
}

// de [03/04/2020] à [2020, 04, 03]
fun formatStringToYmd(dateAsString: String): Triple<Int, Int, Int>? {
    dateAsString
        .split("/")
        .filter {
            it.isDigitsOnly()
        }
        .takeIf{ it.count() == 3 }
        ?.let {
            return Triple( it[2].toInt(), it[1].toInt(), it[0].toInt())
        }
    return null
}

fun Date.toDMY() : String {
    val formatter = SimpleDateFormat("dd/MM/YYYY", Locale.FRANCE)
    return formatter.format(this)
}

fun Date.toH() : String {
    val formatter = SimpleDateFormat("HH", Locale.FRANCE)
    return formatter.format(this)
}

fun Date.toHm() : String {
    val formatter = SimpleDateFormat("HH:mm", Locale.FRANCE)
    return formatter.format(this)
}

fun Date.tom() : String {
    val formatter = SimpleDateFormat("mm", Locale.FRANCE)
    return formatter.format(this)
}

fun Date.tos() : String {
    val formatter = SimpleDateFormat("ss", Locale.FRANCE)
    return formatter.format(this)
}

fun dateFrom(dmY : String, hm: String) : Date? {

    val cal = Calendar.getInstance()
    val dayComponents = formatStringToYmd(dmY)
    val hoursComponents = formatStringToHhmm(hm)

    return dayComponents?.let { day ->
        cal.set(Calendar.YEAR, day.first)
        cal.set(Calendar.MONTH, day.second - 1)
        cal.set(Calendar.DAY_OF_MONTH, day.third)
        cal.set(Calendar.HOUR_OF_DAY, hoursComponents.first)
        cal.set(Calendar.MINUTE, hoursComponents.second)

        cal.time
    }
}
