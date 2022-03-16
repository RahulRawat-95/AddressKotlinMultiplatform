package com.library.address.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressApiModel(
    @SerialName("id") var id: Long,
    @SerialName("firstname") var firstName: String? = null,
    @SerialName("lastname") var lastName: String? = null,
    @SerialName("address1") var address1: String = "",
    @SerialName("address2") var address2: String? = "",
    @SerialName("city") var city: String = "",
    @SerialName("zipCode") var zipCode: String = "",
    @SerialName("phone") var phone: String? = null,
    @SerialName("state_name") var stateName: String? = "",
    @SerialName("alternative_phone") var alternativePhone: String? = null,
    @SerialName("company") var company: String? = null,
    @SerialName("state_id") var stateId: Int = -1,
    @SerialName("country_id") var countryId: Int = -1
) {
    override fun toString(): String {
        return "$firstName $lastName\n$address1 $address2 $city"
    }
}