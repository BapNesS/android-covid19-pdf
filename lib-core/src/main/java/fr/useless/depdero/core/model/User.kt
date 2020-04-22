package fr.useless.depdero.core.model

data class User (
    var userName: String,
    var userLastname: String,
    var birthCity: String,
    var birthDate: String,
    var addressStreet: String,
    var addressCity: String,
    var addressZipcode: String
) {

    companion object {
        fun emptyInstance() : User {
            return User("", "", "", "", "", "", "")
        }
    }

    val displayName : String
        get() = "$userLastname $userName"

    val fullAddress : String
        get() = "$addressStreet $addressZipcode $addressCity"
}