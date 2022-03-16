package com.library.address.database.AddressLibrary

import com.library.address.database.Address
import com.library.address.database.AddressDatabase
import com.library.address.database.AddressDatabaseQueries
import com.library.address.repository.CrudState
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.`internal`.copyOnWriteList
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.Collection
import kotlin.collections.MutableList
import kotlin.reflect.KClass

internal val KClass<AddressDatabase>.schema: SqlDriver.Schema
  get() = AddressDatabaseImpl.Schema

internal fun KClass<AddressDatabase>.newInstance(driver: SqlDriver,
    AddressAdapter: Address.Adapter): AddressDatabase = AddressDatabaseImpl(driver, AddressAdapter)

private class AddressDatabaseImpl(
  driver: SqlDriver,
  internal val AddressAdapter: Address.Adapter
) : TransacterImpl(driver), AddressDatabase {
  public override val addressDatabaseQueries: AddressDatabaseQueriesImpl =
      AddressDatabaseQueriesImpl(this, driver)

  public object Schema : SqlDriver.Schema {
    public override val version: Int
      get() = 1

    public override fun create(driver: SqlDriver): Unit {
      driver.execute(null, """
          |CREATE TABLE Address (
          |    id INTEGER PRIMARY KEY,
          |    firstname TEXT DEFAULT NULL,
          |    lastname TEXT DEFAULT NULL,
          |    address1 TEXT NOT NULL,
          |    address2 TEXT DEFAULT NULL,
          |    city TEXT NOT NULL,
          |    zipcode TEXT NOT NULL,
          |    phone TEXT DEFAULT NULL,
          |    state_name TEXT DEFAULT NULL,
          |    alternative_phone TEXT DEFAULT NULL,
          |    company TEXT DEFAULT NULL,
          |    state_id INTEGER NOT NULL,
          |    country_id INTEGER NOT NULL,
          |    crud_state TEXT,
          |    is_default INTEGER DEFAULT 0
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE LocalAddressId (
          |    id INTEGER PRIMARY KEY
          |)
          """.trimMargin(), 0)
    }

    public override fun migrate(
      driver: SqlDriver,
      oldVersion: Int,
      newVersion: Int
    ): Unit {
    }
  }
}

private class AddressDatabaseQueriesImpl(
  private val database: AddressDatabaseImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), AddressDatabaseQueries {
  internal val getAllAddress: MutableList<Query<*>> = copyOnWriteList()

  internal val fetchLatestLocalId: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> getAllAddress(crudStates: Collection<CrudState?>, mapper: (
    id: Long,
    firstname: String?,
    lastname: String?,
    address1: String,
    address2: String?,
    city: String,
    zipcode: String,
    phone: String?,
    state_name: String?,
    alternative_phone: String?,
    company: String?,
    state_id: Long,
    country_id: Long,
    crud_state: CrudState?,
    is_default: Boolean?
  ) -> T): Query<T> = GetAllAddressQuery(crudStates) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1),
      cursor.getString(2),
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7),
      cursor.getString(8),
      cursor.getString(9),
      cursor.getString(10),
      cursor.getLong(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13)?.let { database.AddressAdapter.crud_stateAdapter.decode(it) },
      cursor.getLong(14)?.let { it == 1L }
    )
  }

  public override fun getAllAddress(crudStates: Collection<CrudState?>): Query<Address> =
      getAllAddress(crudStates) { id, firstname, lastname, address1, address2, city, zipcode, phone,
      state_name, alternative_phone, company, state_id, country_id, crud_state, is_default ->
    Address(
      id,
      firstname,
      lastname,
      address1,
      address2,
      city,
      zipcode,
      phone,
      state_name,
      alternative_phone,
      company,
      state_id,
      country_id,
      crud_state,
      is_default
    )
  }

  public override fun fetchLatestLocalId(): Query<Long> = Query(-2030377411, fetchLatestLocalId,
      driver, "AddressDatabase.sq", "fetchLatestLocalId", "SELECT id FROM LocalAddressId") {
      cursor ->
    cursor.getLong(0)!!
  }

  public override fun insertOrReplace(
    id: Long?,
    firstname: String?,
    lastname: String?,
    address1: String,
    address2: String?,
    city: String,
    zipcode: String,
    phone: String?,
    state_name: String?,
    alternative_phone: String?,
    company: String?,
    state_id: Long,
    country_id: Long,
    crud_state: CrudState?,
    is_default: Boolean?
  ): Unit {
    driver.execute(943236384, """
    |INSERT OR REPLACE INTO Address(id, firstname, lastname, address1, address2, city, zipcode, phone, state_name, alternative_phone, company, state_id, country_id, crud_state, is_default)
    |    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimMargin(), 15) {
      bindLong(1, id)
      bindString(2, firstname)
      bindString(3, lastname)
      bindString(4, address1)
      bindString(5, address2)
      bindString(6, city)
      bindString(7, zipcode)
      bindString(8, phone)
      bindString(9, state_name)
      bindString(10, alternative_phone)
      bindString(11, company)
      bindLong(12, state_id)
      bindLong(13, country_id)
      bindString(14, crud_state?.let { database.AddressAdapter.crud_stateAdapter.encode(it) })
      bindLong(15, is_default?.let { if (it) 1L else 0L })
    }
    notifyQueries(943236384, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun deleteAllAddressWithStates(crudStates: Collection<CrudState?>): Unit {
    val crudStatesIndexes = createArguments(count = crudStates.size)
    driver.execute(null, """DELETE FROM Address WHERE crud_state IN $crudStatesIndexes""",
        crudStates.size) {
      crudStates.forEachIndexed { index, crudStates_ ->
          bindString(index + 1, crudStates_?.let {
              database.AddressAdapter.crud_stateAdapter.encode(it) })
          }
    }
    notifyQueries(202220190, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun deleteAddressById(id: Long): Unit {
    driver.execute(2038737347, """DELETE FROM Address WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(2038737347, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun setDefault(id: Long): Unit {
    driver.execute(2006403575, """UPDATE Address SET is_default = 1 WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(2006403575, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun removeCurrentDefault(): Unit {
    driver.execute(1615792292, """UPDATE Address SET is_default = 0 WHERE is_default = 1""", 0)
    notifyQueries(1615792292, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun setCrudState(crudState: CrudState?, id: Long): Unit {
    driver.execute(-1109930455, """UPDATE Address SET crud_state = ? WHERE id = ?""", 2) {
      bindString(1, crudState?.let { database.AddressAdapter.crud_stateAdapter.encode(it) })
      bindLong(2, id)
    }
    notifyQueries(-1109930455, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun updateId(newId: Long, oldId: Long): Unit {
    driver.execute(1069702204, """UPDATE Address SET id = ? WHERE id = ?""", 2) {
      bindLong(1, newId)
      bindLong(2, oldId)
    }
    notifyQueries(1069702204, {database.addressDatabaseQueries.getAllAddress})
  }

  public override fun insertLocalId(id: Long?): Unit {
    driver.execute(-1618993291, """INSERT INTO LocalAddressId(id) VALUES(?)""", 1) {
      bindLong(1, id)
    }
    notifyQueries(-1618993291, {database.addressDatabaseQueries.fetchLatestLocalId})
  }

  public override fun updateLocalId(newId: Long): Unit {
    driver.execute(1691531109, """UPDATE LocalAddressId SET id = ?""", 1) {
      bindLong(1, newId)
    }
    notifyQueries(1691531109, {database.addressDatabaseQueries.fetchLatestLocalId})
  }

  private inner class GetAllAddressQuery<out T : Any>(
    public val crudStates: Collection<CrudState?>,
    mapper: (SqlCursor) -> T
  ) : Query<T>(getAllAddress, mapper) {
    public override fun execute(): SqlCursor {
      val crudStatesIndexes = createArguments(count = crudStates.size)
      return driver.executeQuery(null,
          """SELECT * FROM Address WHERE crud_state IN $crudStatesIndexes ORDER BY id""",
          crudStates.size) {
        crudStates.forEachIndexed { index, crudStates_ ->
            bindString(index + 1, crudStates_?.let {
                database.AddressAdapter.crud_stateAdapter.encode(it) })
            }
      }
    }

    public override fun toString(): String = "AddressDatabase.sq:getAllAddress"
  }
}
