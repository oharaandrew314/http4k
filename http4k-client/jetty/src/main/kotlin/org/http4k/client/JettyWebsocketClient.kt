package org.http4k.client

import org.eclipse.jetty.util.BufferUtil
import org.eclipse.jetty.websocket.api.Callback
import org.eclipse.jetty.websocket.api.Callback.Completable
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.exceptions.WebSocketException
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.http4k.client.PreCannedJettyHttpClients.defaultJettyHttpClient
import org.http4k.core.Headers
import org.http4k.core.Request
import org.http4k.core.StreamBody
import org.http4k.core.toParametersMap
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import java.net.URI
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object JettyWebsocketClient {

    operator fun invoke(
        timeout: Duration = Duration.ZERO,
        client: WebSocketClient = WebSocketClient(defaultJettyHttpClient()),
        ) = object: WsHandler {
        override fun invoke(request: Request): WsResponse {
            val waitForConnect = CountDownLatch(1)
            val client = client(request, timeout, client) {
                waitForConnect.countDown()
            }

            if (timeout > Duration.ZERO) {
                waitForConnect.await(timeout.toMillis(), TimeUnit.MILLISECONDS)
            } else {
                waitForConnect.await()
            }

            return WsResponse.Accept(null, client) // TODO subprotocol, refuse
        }

    }
}

private fun client(
    request: Request,
    timeout: Duration,
    client: WebSocketClient,
//    onError: (Throwable) -> Unit,
    onConnect: () -> Unit
): Websocket = object : PushPullAdaptingWebSocket() {

    private val listener = Listener()

    init {
//        onError(onError)
        client.connect(listener, URI.create(request.uri.toString()), clientUpgradeRequest(request.headers, timeout))
            .whenComplete { _, error -> triggerError(error) }
    }

    override fun send(message: WsMessage) = with(listener) {
        if (!isOpen) {
            throw WebSocketException("Connection to ${request.uri} is closed.")
        }
        try {
            when (message.body) {
                is StreamBody -> Completable.with { session.sendBinary(message.body.payload, it) }.get()
                else -> Completable.with { session.sendText(message.body.toString(), it) }.get()
            }
        } catch (error: Throwable) {
            triggerError(error)
        }
        Unit
    }

    override fun close(status: WsStatus) = with(listener) {
        if (isOpen) {
            Completable.with { session.close(status.code, status.description, it) }.get()
        }
    }

    inner class Listener : Session.Listener.AbstractAutoDemanding() {
        override fun onWebSocketOpen(session: Session) {
            super.onWebSocketOpen(session)
            onConnect()
        }

        override fun onWebSocketClose(statusCode: Int, reason: String?) {
            triggerClose(WsStatus(statusCode, reason.orEmpty()))
        }

        override fun onWebSocketText(message: String) {
            try {
                triggerMessage(WsMessage(message))
            } catch (e: Throwable) {
                triggerError(e)
            }
        }

        override fun onWebSocketBinary(payload: ByteBuffer, callback: Callback) {
            try {
                triggerMessage(WsMessage(BufferUtil.toArray(payload).inputStream()))
                callback.succeed()
            } catch (e: Throwable) {
                triggerError(e)
                callback.fail(e)
            }
        }

        override fun onWebSocketError(cause: Throwable) {
            triggerError(cause)
        }
    }
}

private fun clientUpgradeRequest(headers: Headers, timeout: Duration) = ClientUpgradeRequest().apply {
    setHeaders(headers.toParametersMap())
    setTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
}
