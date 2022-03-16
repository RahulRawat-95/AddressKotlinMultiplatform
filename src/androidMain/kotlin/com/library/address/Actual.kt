package com.library.address

import com.library.address.caches.DatabaseWrapper
import com.library.address.database.Address
import com.library.address.database.AddressDatabase
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import io.ktor.client.*
import io.ktor.client.engine.android.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual fun platformModule(builder: DI.Builder) {
    builder.bindSingleton {
        DatabaseWrapper(
            AddressDatabase(
                driver = AndroidSqliteDriver(AddressDatabase.Schema, instance(), databaseName),
                AddressAdapter = Address.Adapter(
                    EnumColumnAdapter()
                )
            )
        )
    }
}

actual suspend fun getDatabaseWrapper(): DatabaseWrapper? = null

actual fun getHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Android) {
        block(this)
    }
}