package com.library.address.di

import com.library.address.getHttpClient
import com.library.address.platformModule
import com.library.address.repository.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance

var repo: Repo? = null
var remoteRepo: RemoteRepo? = null

fun initKodein(enableNetworkLogs: Boolean = false, appDeclaration: (DI.Builder) -> Unit = {}): DI {
    val container = DI {
        appDeclaration(this)
        platformModule(this)
        commonModule(this, enableNetworkLogs)
    }

    repo = container.direct.instance()

    return container
}

fun initKodeinJS(enableNetworkLogs: Boolean = false): DI {
    val container = DI {
        platformModule(this)

        bindSingleton { createJson() }
        bindSingleton { createHttpClient(json = instance(), enableNetworkLogs = enableNetworkLogs) }

        bindSingleton { CoroutineScope(Dispatchers.Default + SupervisorJob() ) }

        bindSingleton<RemoteRepo> { RemoteRepoImpl(client = instance()) }
    }

    remoteRepo = container.direct.instance()

    return container
}

private fun commonModule(builder: DI.Builder, enableNetworkLogs: Boolean) {
    builder.bindSingleton { createJson() }
    builder.bindSingleton { createHttpClient(json = instance(), enableNetworkLogs = enableNetworkLogs) }

    builder.bindSingleton { CoroutineScope(Dispatchers.Default + SupervisorJob() ) }

    builder.bindSingleton<RemoteRepo> { RemoteRepoImpl(client = instance()) }
    builder.bindSingleton<LocalRepo> { LocalRepoImpl(databaseWrapper = instance()) }

    builder.bindSingleton<Repo> { RepoImpl(remoteRepo = instance(), localRepo = instance()) }
}

private fun createJson() = Json { isLenient = true; ignoreUnknownKeys = true }

private fun createHttpClient(json: Json, enableNetworkLogs: Boolean) = getHttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }
    if (enableNetworkLogs) {
        install(Logging) {
            logger = Logger.EMPTY
            level = LogLevel.NONE
        }
    }
}