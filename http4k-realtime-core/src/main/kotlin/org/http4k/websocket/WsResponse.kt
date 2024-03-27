package org.http4k.websocket

import org.http4k.core.Response

sealed interface WsResponse {
    data class Accept(val subprotocol: String?, val websocket: Websocket): WsResponse
    data class Refuse(val response: Response): WsResponse

    companion object {
        operator fun invoke(subprotocol: String? = null, consumer: WsConsumer) = Accept(
            subprotocol = subprotocol,
            websocket = object: PushPullAdaptingWebSocket() {
                private val client = this
                private val server = object: PushPullAdaptingWebSocket() {
                    override fun send(message: WsMessage) = client.triggerMessage(message)
                    override fun close(status: WsStatus) = client.triggerClose(status)
                }.also(consumer)

                override fun send(message: WsMessage) = server.triggerMessage(message)
                override fun close(status: WsStatus) = server.triggerClose(status)
            }
        )
    }
}

fun WsConsumer.asWsHandler(subprotocol: String? = null): WsHandler = {
    WsResponse(subprotocol, this)
}

fun WsResponse.onRefuse(fn: (WsResponse.Refuse) -> Nothing) = when(this) {
    is WsResponse.Accept -> this
    is WsResponse.Refuse -> fn(this)
}

fun WsResponse.wsOrThrow() = onRefuse {
    error("Websocket connection was refused: ${it.response.status}")
}.websocket

fun WsResponse.wsOrThrow(fn: (Websocket) -> Unit): Websocket = wsOrThrow().also(fn)
