package com.library.address

import com.library.address.caches.DatabaseWrapper
import com.library.address.database.Address
import com.library.address.database.AddressDatabase
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.ktor.client.*
import io.ktor.client.engine.java.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton

actual fun platformModule(builder: DI.Builder) {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AddressDatabase.Schema.create(driver)
    builder.bindSingleton {
        DatabaseWrapper(
            AddressDatabase(
                driver,
                AddressAdapter = Address.Adapter(EnumColumnAdapter())
            )
        )
    }
}

actual suspend fun getDatabaseWrapper(): DatabaseWrapper? = null

actual fun getHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Java) {
        block(this)
    }
}