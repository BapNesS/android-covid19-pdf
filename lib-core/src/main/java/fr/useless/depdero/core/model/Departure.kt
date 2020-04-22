package fr.useless.depdero.core.model

import fr.useless.depdero.core.tool.toDMY
import fr.useless.depdero.core.tool.toHm
import java.util.*

class Departure {
    var reason: Reason? = null
    var date: Date? = null

    val dateDay : String? get() = date?.toDMY()
    val dateHours: String? get() = date?.toHm()
}