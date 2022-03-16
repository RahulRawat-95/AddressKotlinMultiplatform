package com.library.address.utils

import com.library.address.database.Address

fun Address.toRequestParams(token: String): String {
    val requestParams = StringBuilder("token=$token&")

    requestParams.append("id=$id&")
    requestParams.append("firstname=$firstname&")
    requestParams.append("lastname=$lastname&")
    requestParams.append("address1=$address1&")
    requestParams.append("address2=$address2&")
    requestParams.append("city=$city&")
    requestParams.append("zipCode=$zipcode&")
    requestParams.append("phone=$phone&")
    requestParams.append("state_name=$state_name&")
    requestParams.append("alternative_phone=$alternative_phone&")
    requestParams.append("company=$company&")
    requestParams.append("state_id=$state_id&")
    requestParams.append("country_id=$country_id")

    return requestParams.toString()
}