package fr.useless.depdero.core.model

import androidx.annotation.StringRes
import fr.useless.depdero.core.R

/**
 * qrCodeValue = valeur du formulaire sur le site
 */
enum class Reason(val id: Int, val qrCodeValue: String, @StringRes val description: Int) {

    TRAVAIL     (0, "travail", R.string.reason_travail),
    COURSES     (1, "courses", R.string.reason_courses),
    SANTE       (2, "sante", R.string.reason_sante),
    FAMILLE     (3, "famille", R.string.reason_famille),
    SPORT       (4, "sport", R.string.reason_sport),
    JUDICIAIRE  (5, "judiciaire", R.string.reason_judiciaire),
    MISSIONS    (6, "missions", R.string.reason_missions)

}