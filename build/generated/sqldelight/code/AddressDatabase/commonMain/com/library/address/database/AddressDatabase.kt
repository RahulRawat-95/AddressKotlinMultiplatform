package com.library.address.database

import com.library.address.database.AddressLibrary.newInstance
import com.library.address.database.AddressLibrary.schema
import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlDriver

public interface AddressDatabase : Transacter {
  public val addressDatabaseQueries: AddressDatabaseQueries

  public companion object {
    public val Schema: SqlDriver.Schema
      get() = AddressDatabase::class.schema

    public operator fun invoke(driver: SqlDriver, AddressAdapter: Address.Adapter): AddressDatabase
        = AddressDatabase::class.newInstance(driver, AddressAdapter)
  }
}
