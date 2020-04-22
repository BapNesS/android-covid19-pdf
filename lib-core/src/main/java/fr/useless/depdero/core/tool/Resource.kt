package fr.useless.depdero.core.tool

sealed class Resource<out T> {

    class Loading<out T> : Resource<T>()

    data class Success<T>(var data: T) : Resource<T>()

    data class Failure<out T>(val errorCode: Int) : Resource<T>()

}