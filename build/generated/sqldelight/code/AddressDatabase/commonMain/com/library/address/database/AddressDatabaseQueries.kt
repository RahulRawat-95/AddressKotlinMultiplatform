package com.library.address.database

import com.library.address.repository.CrudState
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.Collection

public interface AddressDatabaseQueries : Transacter {
  public fun <T : Any> getAllAddress(crudStates: Collection<CrudState?>, mapper: (
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
  ) -> T): Query<T>

  public fun getAllAddress(crudStates: Collection<CrudState?>): Query<Address>

  public fun fetchLatestLocalId(): Query<Long>

  public fun insertOrReplace(
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
  ): Unit

  public fun deleteAllAddressWithStates(crudStates: Collection<CrudState?>): Unit

  public fun deleteAddressById(id: Long): Unit

  public fun setDefault(id: Long): Unit

  public fun removeCurrentDefault(): Unit

  public fun setCrudState(crudState: CrudState?, id: Long): Unit

  public fun updateId(newId: Long, oldId: Long): Unit

  public fun insertLocalId(id: Long?): Unit

  public fun updateLocalId(newId: Long): Unit
}
