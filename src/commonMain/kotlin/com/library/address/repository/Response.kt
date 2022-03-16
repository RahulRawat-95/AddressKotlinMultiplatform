package com.library.address.repository

sealed class Response<T> {
    class Failed<T>: Response<T>()
    data class Success<T>(val data: T): Response<T>()
}