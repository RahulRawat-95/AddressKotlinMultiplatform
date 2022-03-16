package com.library.address.database

import com.library.address.repository.CrudState
import com.squareup.sqldelight.ColumnAdapter
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public data class Address(
  public val id: Long,
  public val firstname: String?,
  public val lastname: String?,
  public val address1: String,
  public val address2: String?,
  public val city: String,
  public val zipcode: String,
  public val phone: String?,
  public val state_name: String?,
  public val alternative_phone: String?,
  public val company: String?,
  public val state_id: Long,
  public val country_id: Long,
  public val crud_state: CrudState?,
  public val is_default: Boolean?
) {
  public override fun toString(): String = """
  |Address [
  |  id: $id
  |  firstname: $firstname
  |  lastname: $lastname
  |  address1: $address1
  |  address2: $address2
  |  city: $city
  |  zipcode: $zipcode
  |  phone: $phone
  |  state_name: $state_name
  |  alternative_phone: $alternative_phone
  |  company: $company
  |  state_id: $state_id
  |  country_id: $country_id
  |  crud_state: $crud_state
  |  is_default: $is_default
  |]
  """.trimMargin()

  public class Adapter(
    public val crud_stateAdapter: ColumnAdapter<CrudState, String>
  )
}
