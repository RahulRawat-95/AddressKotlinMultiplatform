package com.library.address.repository

import com.library.address.database.Address
import com.library.address.models.AddressApiModel
import com.library.address.utils.toRequestParams
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject

interface RemoteRepo {
    suspend fun getAddresses(token: String = apiToken): List<AddressApiModel>
    suspend fun createAddress(token: String = apiToken, address: Address): AddressApiModel
    suspend fun updateAddress(token: String = apiToken, addressId: Long, address: Address): Boolean
    suspend fun deleteAddress(token: String = apiToken, addressId: Long): Boolean

    companion object {
        private const val apiToken = "52e04d83e87e509f07982e6ac851e2d2c67d1d0eabc4fe78"
    }
}

internal class RemoteRepoImpl(
    private val client: HttpClient,
    private var baseUrl: String = "https://address-server-vinsol.herokuapp.com"
) : RemoteRepo {
    override suspend fun getAddresses(token: String): List<AddressApiModel> = client.get("$baseUrl/api/ams/user/addresses?token=$token")

    override suspend fun createAddress(token: String, address: Address): AddressApiModel = client.post("$baseUrl/api/ams/user/addresses?${address.toRequestParams(token)}")

    override suspend fun updateAddress(token: String, addressId: Long, address: Address): Boolean {
        val httpResponse: HttpResponse = client.put("$baseUrl/api/ams/user/addresses?${address.toRequestParams(token)}")
        return httpResponse.status.value in 200..299
    }

    override suspend fun deleteAddress(token: String, addressId: Long): Boolean {
        val httpResponse: HttpResponse = client.delete("$baseUrl/api/ams/user/addresses?token=$token&id=$addressId")
        return httpResponse.status.value in 200..299
    }
}