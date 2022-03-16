package com.library.address.database

import kotlin.Long
import kotlin.String

public data class LocalAddressId(
  public val id: Long
) {
  public override fun toString(): String = """
  |LocalAddressId [
  |  id: $id
  |]
  """.trimMargin()
}
