package com.library.address.repository

import com.library.address.database.Address
import io.ktor.client.features.logging.*
import kotlinx.coroutines.flow.Flow

interface Repo {
    fun fetchAddressFlow(): CFlow<List<Address>>?
    suspend fun fetchAddresses()
    suspend fun sync()
    suspend fun createAddress(address: Address)
    suspend fun updateAddress(address: Address)
    suspend fun makeDefault(id: Long)
    suspend fun removeDefault()
    suspend fun deleteAddress(id: Long)
}

internal class RepoImpl(private val remoteRepo: RemoteRepo, private val localRepo: LocalRepo) : Repo {
    override fun fetchAddressFlow(): CFlow<List<Address>>? {
        return localRepo.getAddressesFlow(listOf(CrudState.SYNCED, CrudState.CREATE, CrudState.UPDATE))?.wrap()
    }

    override suspend fun fetchAddresses() {
        try {
            Logger.Companion.DEFAULT.log("dexter Fetching Address")
            val addresses = remoteRepo.getAddresses()
            Logger.Companion.DEFAULT.log("dexter Fetching Address ${addresses.size}")
            localRepo.insertOrReplaceAddresses(addresses)
            Logger.Companion.DEFAULT.log("dexter Fetching Address end")
        } catch (e: Exception) {
            Logger.Companion.DEFAULT.log("dexter Fetching Address ${e.message} ${e.cause}")
        }
    }

    override suspend fun sync() {
        val modifiedAddresses = localRepo.getAddresses(
            crudStates = listOf(CrudState.UPDATE, CrudState.DELETE)
        )
        val createdAddresses = localRepo.getAddresses(crudStates = listOf(CrudState.CREATE))?.reversed()

        val addresses = mutableListOf<Address>()
        if (modifiedAddresses != null)
            addresses.addAll(modifiedAddresses)
        if (createdAddresses != null)
            addresses.addAll(createdAddresses)

        for (address in addresses) {
            try {
                when (address.crud_state) {
                    CrudState.CREATE -> {
                        val addressApiModel = remoteRepo.createAddress(address = address)
                        localRepo.markAddressAsSynced(id = address.id)
                        localRepo.updateId(oldId = address.id, newId = addressApiModel.id)
                    }
                    CrudState.DELETE -> {
                        if (remoteRepo.deleteAddress(addressId = address.id))
                            localRepo.deleteAddress(id = address.id)
                        else
                            throw Exception()
                    }
                    CrudState.UPDATE -> {
                        if (!remoteRepo.updateAddress(addressId = address.id, address = address))
                            throw Exception()
                        localRepo.markAddressAsSynced(id = address.id)
                    }
                    else -> break
                }
            } catch (e: Exception) {
                e.message
            }
        }
    }

    override suspend fun createAddress(address: Address) {
        localRepo.createAddress(address)
    }

    override suspend fun updateAddress(address: Address) {
        localRepo.updateAddress(address)
    }

    override suspend fun makeDefault(id: Long) {
        localRepo.makeDefault(id)
    }

    override suspend fun removeDefault() {
        localRepo.removeDefault()
    }

    override suspend fun deleteAddress(id: Long) {
        localRepo.markAddressForDeletion(id = id)
    }
}