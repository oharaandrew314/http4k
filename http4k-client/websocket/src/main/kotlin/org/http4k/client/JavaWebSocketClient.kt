package org.http4k.client

import org.http4k.core.Headers
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object JavaWebSocketClient {
    operator fun invoke(
        timeout: Duration = Duration.ZERO,
        draft: Draft = Draft_6455(),
        autoReconnect: Boolean = false
    ): WsHandler = { request ->
        val waitForConnect = CountDownLatch(1)
        val client = client(request, timeout, draft, autoReconnect) {
            waitForConnect.countDown()
        }

        if (timeout > Duration.ZERO) {
            if (waitForConnect.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                WsResponse.Accept(null, client) // TODO subprotocol, refuse
            } else {
                WsResponse.Refuse(Response(Status.CLIENT_TIMEOUT))
            }
        } else {
            waitForConnect.await()
            WsResponse.Accept(null, client) // TODO subprotocol, refuse
        }
    }
}

private fun client(
    request: Request,
    timeout: Duration,
    draft: Draft,
    autoReconnect: Boolean,
    onConnect: () -> Unit
) = object : PushPullAdaptingWebSocket() {
    private val client = RemoteWebSocketClient().also { it.connect() }

    override fun send(message: WsMessage){
        when(message.mode) {
            WsMessage.Mode.Binary -> client.send(message.body.payload)
            WsMessage.Mode.Text -> client.send(message.bodyString())
        }
    }

    override fun close(status: WsStatus) = client.close(status.code, status.description)

    inner class RemoteWebSocketClient: WebSocketClient(
        URI.create(request.uri.toString()),
        draft,
        request.headers.combineToMap(),
        timeout.toMillis().toInt()
    ) {
        override fun onClosing(code: Int, reason: String?, remote: Boolean) {
            if (autoReconnect) reconnect()
        }
        override fun onOpen(handshakedata: ServerHandshake?) = onConnect()
        override fun onClose(code: Int, reason: String, remote: Boolean) = triggerClose(WsStatus(code, reason))
        override fun onMessage(message: String) = triggerMessage(WsMessage(message))
        override fun onMessage(bytes: ByteBuffer) = triggerMessage(WsMessage(bytes))
        override fun onError(e: Exception) = triggerError(e)
    }
}

private fun Headers.combineToMap() = groupBy { it.first }.mapValues { it.value.map { it.second }.joinToString(", ") }
