package org.http4k.client

import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.http4k.client.PreCannedOkHttpClients.defaultOkHttpClient
import org.http4k.core.Request
import org.http4k.core.StreamBody
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object OkHttpWebsocketClient {

    operator fun invoke(
        timeout: Duration = Duration.ofSeconds(5),
        client: OkHttpClient = defaultOkHttpClient()
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
    client: OkHttpClient,
    onConnect: () -> Unit,
): Websocket = object : PushPullAdaptingWebSocket() {

    private val ws = client.newBuilder().connectTimeout(timeout).build()
        .newWebSocket(request.asOkHttp(), Listener())

    override fun send(message: WsMessage) {
        val messageSent = when (message.body) {
            is StreamBody -> ws.send(message.body.payload.toByteString())
            else -> ws.send(message.body.toString())
        }
        check(messageSent) {
            "Connection to ${request.uri} is closed."
        }
    }

    override fun close(status: WsStatus) {
        ws.close(status.code, status.description)
    }

    inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            onConnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            triggerClose(WsStatus(code, reason))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            triggerError(t)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            triggerMessage(WsMessage(text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            triggerMessage(WsMessage(bytes.asByteBuffer()))
        }
    }
}
