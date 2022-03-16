package com.library.address

import io.ktor.client.*
import io.ktor.client.engine.ios.*

actual fun getHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Ios) {
        block(this)
    }
}