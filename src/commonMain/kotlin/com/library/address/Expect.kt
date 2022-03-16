package com.library.address

import com.library.address.caches.DatabaseWrapper
import io.ktor.client.*
import org.kodein.di.DI

const val databaseName = "AddressDatabase.db"

expect fun platformModule(builder: DI.Builder)

expect suspend fun getDatabaseWrapper(): DatabaseWrapper?

expect fun getHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient