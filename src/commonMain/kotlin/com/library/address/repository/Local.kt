package com.library.address.repository

import com.library.address.caches.DatabaseWrapper
import com.library.address.database.Address
import com.library.address.models.AddressApiModel
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal interface LocalRepo {
    fun getAddressesFlow(crudStates: List<CrudState>): Flow<List<Address>>?
    suspend fun getAddresses(crudStates: List<CrudState>): List<Address>?
    suspend fun insertOrReplaceAddresses(addresses: List<AddressApiModel>)
    suspend fun createAddress(address: Address)
    suspend fun updateAddress(address: Address)
    suspend fun markAddressForDeletion(id: Long)
    suspend fun markAddressAsSynced(id: Long)
    suspend fun makeDefault(id: Long)
    suspend fun removeDefault()
    suspend fun updateId(oldId: Long, newId: Long)
    suspend fun deleteAddress(id: Long)
}

internal class LocalRepoImpl(private val databaseWrapper: DatabaseWrapper) : LocalRepo {
    override fun getAddressesFlow(crudStates: List<CrudState>): Flow<List<Address>>? =
        addressesQuery(crudStates = crudStates)?.asFlow()?.map {
            withContext(Dispatchers.Default) {
                val addresses = it.executeAsList()

                val localAddress = mutableListOf<Address>()
                val remoteAddress = mutableListOf<Address>()

                for (address in addresses) {
                    if (address.id < 0) {
                        localAddress.add(address)
                    } else {
                        remoteAddress.add(address)
                    }
                }

                val list = mutableListOf<Address>()
                list.addAll(remoteAddress)
                list.addAll(localAddress.reversed())

                list
            }
        }

    override suspend fun getAddresses(crudStates: List<CrudState>): List<Address>? = addressesQuery(crudStates = crudStates)?.executeAsList()

    private fun addressesQuery(crudStates: List<CrudState>): Query<Address>? {
        val queries = databaseWrapper.instance?.addressDatabaseQueries
        return queries?.getAllAddress(crudStates = crudStates)
    }

    override suspend fun insertOrReplaceAddresses(addresses: List<AddressApiModel>) {
        val queries = databaseWrapper.instance?.addressDatabaseQueries

        if (queries != null) {
            val allLocalAddresses = queries.getAllAddress(
                crudStates = listOf(
                    CrudState.SYNCED,
                    CrudState.CREATE,
                    CrudState.UPDATE,
                    CrudState.DELETE
                )
            ).executeAsList()

            val allLocalAddressesMap = allLocalAddresses.associateBy { it.id }
            val updatedIds = allLocalAddresses.filter { it.crud_state == CrudState.UPDATE }.map { it.id }.toMutableSet()

            queries.transaction {
                queries.deleteAllAddressWithStates(crudStates = listOf(CrudState.SYNCED))

                for (address in addresses) {
                    updatedIds.remove(address.id)
                    val localAddress = allLocalAddressesMap[address.id]
                    if (localAddress == null || (localAddress.crud_state != CrudState.DELETE && localAddress.crud_state != CrudState.UPDATE)) {
                        address.apply {
                            queries.insertOrReplace(
                                id = id,
                                firstname = firstName,
                                lastname = lastName,
                                address1 = address1,
                                address2 = address2,
                                city = city,
                                zipcode = zipCode,
                                phone = phone,
                                state_name = stateName,
                                alternative_phone = alternativePhone,
                                company = company,
                                state_id = stateId.toLong(),
                                country_id = countryId.toLong(),
                                crud_state = CrudState.SYNCED,
                                is_default = localAddress?.is_default ?: false
                            )
                        }
                    }
                }

                for (removedUpdatedId in updatedIds) {
                    queries.deleteAddressById(id = removedUpdatedId)
                }
            }
        }
    }

    override suspend fun createAddress(address: Address) {
        createOrUpdateAddress(address, isUpdate = false)
    }

    override suspend fun updateAddress(address: Address) {
        createOrUpdateAddress(address, isUpdate = true)
    }

    private suspend fun createOrUpdateAddress(address: Address, isUpdate: Boolean) {
        val queries = databaseWrapper.instance?.addressDatabaseQueries

        if (queries != null) {
            var id: Long = address.id
            if (!isUpdate) {
                try {
                    val latestLocalId = queries.fetchLatestLocalId().executeAsOne()
                    id = latestLocalId - 1
                    queries.updateLocalId(newId = id)
                } catch (e: Exception) {
                    queries.insertLocalId(-1)
                    id = -1
                }
            }

            address.apply {
                queries.insertOrReplace(
                    id = id,
                    firstname = firstname,
                    lastname = lastname,
                    address1 = address1,
                    address2 = address2,
                    city = city,
                    zipcode = zipcode,
                    phone = phone,
                    state_name = state_name,
                    alternative_phone = alternative_phone,
                    company = company,
                    state_id = state_id,
                    country_id = country_id,
                    crud_state = if (isUpdate && id > 0) CrudState.UPDATE else CrudState.CREATE,
                    is_default = is_default
                )
                if (is_default == true)
                    makeDefault(id = id)
            }
        }
    }

    override suspend fun markAddressForDeletion(id: Long) {
        databaseWrapper.instance?.addressDatabaseQueries?.setCrudState(id = id, crudState = CrudState.DELETE)
    }

    override suspend fun markAddressAsSynced(id: Long) {
        databaseWrapper.instance?.addressDatabaseQueries?.setCrudState(id = id, crudState = CrudState.SYNCED)
    }

    override suspend fun makeDefault(id: Long) {
        val queries = databaseWrapper.instance?.addressDatabaseQueries
        queries?.transaction {
            queries.removeCurrentDefault()
            queries.setDefault(id = id)
        }
    }

    override suspend fun removeDefault() {
        databaseWrapper.instance?.addressDatabaseQueries?.removeCurrentDefault()
    }

    override suspend fun updateId(oldId: Long, newId: Long) {
        databaseWrapper.instance?.addressDatabaseQueries?.updateId(newId, oldId)
    }

    override suspend fun deleteAddress(id: Long) {
        databaseWrapper.instance?.addressDatabaseQueries?.deleteAddressById(id)
    }
}