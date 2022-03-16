package com.library.address

import com.library.address.caches.DatabaseWrapper
import com.library.address.database.Address
import com.library.address.database.AddressDatabase
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import org.kodein.di.DI
import org.kodein.di.bindSingleton

actual fun platformModule(builder: DI.Builder) {
    val driver = NativeSqliteDriver(AddressDatabase.Schema, databaseName)
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