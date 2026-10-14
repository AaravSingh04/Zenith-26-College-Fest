package com.zenith26.fest.data.remote

/**
 * Sealed class representing network call states.
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val statusCode: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
