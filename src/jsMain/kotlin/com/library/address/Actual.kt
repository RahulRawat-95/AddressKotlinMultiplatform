package com.library.address

import com.library.address.caches.DatabaseWrapper
import com.library.address.database.Address
import com.library.address.database.AddressDatabase
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.drivers.sqljs.initSqlDriver
import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.coroutines.await
import org.kodein.di.DI

actual fun platformModule(builder: DI.Builder) = Unit

actual suspend fun getDatabaseWrapper(): DatabaseWrapper? {
    return DatabaseWrapper(
        AddressDatabase(
            initSqlDriver(AddressDatabase.Schema).await(), AddressAdapter = Address.Adapter(
                EnumColumnAdapter()
            )
        )
    )
}

actual fun getHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Js) {
        block(this)
    }
}