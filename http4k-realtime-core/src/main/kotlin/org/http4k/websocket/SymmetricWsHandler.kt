package org.http4k.websocket

import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.server.Http4kServer
import org.http4k.server.PolyServerConfig

typealias SymmetricWsHandler = (Request) -> Websocket

fun SymmetricWsHandler.asServer(config: PolyServerConfig): Http4kServer {
    return config.toWsServer { request ->
        val server = invoke(request)
        WsResponse { client ->
            server.onMessage(client::send)
            server.onClose(client::close)

            client.onMessage(server::send)
            client.onClose(server::close)
        }
    }
}

fun interface SymmetricWsFilter : (SymmetricWsHandler) -> SymmetricWsHandler {
    companion object
}

fun SymmetricWsFilter.then(next: SymmetricWsFilter): SymmetricWsFilter = SymmetricWsFilter { this(next(it)) }
fun SymmetricWsFilter.then(next: SymmetricWsHandler): SymmetricWsHandler = { this(next)(it) }

val SymmetricWsFilter.Companion.NoOp: SymmetricWsFilter get() = SymmetricWsFilter { next -> { next(it) } }
fun SymmetricWsFilter.Companion.SetHostFrom(uri: Uri): SymmetricWsFilter = SymmetricWsFilter { next ->
    {
        next(it.uri(it.uri.scheme(uri.scheme).host(uri.host).port(uri.port))
            .replaceHeader("Host", "${uri.host}${uri.port?.let { port -> ":$port" } ?: ""}"))
    }
}

