package org.http4k.client

import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.http4k.client.PreCannedOkHttpClients.defaultOkHttpClient
import org.http4k.core.Body
import org.http4k.core.Headers
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.StreamBody
import org.http4k.core.Uri
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsClient
//import org.http4k.websocket.WsConsumer
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object OkHttpWebsocketClient {

//    fun blocking(
//        uri: Uri,
//        headers: Headers = emptyList(),
//        timeout: Duration = Duration.of(5, ChronoUnit.SECONDS),
//        client: OkHttpClient = defaultOkHttpClient()
//    ): WsClient = OkHttpBlockingWebsocket(uri, headers, timeout, client).awaitConnected()
//
//    fun nonBlocking(
//        uri: Uri,
//        headers: Headers = emptyList(),
//        timeout: Duration = Duration.ZERO,
//        client: OkHttpClient = defaultOkHttpClient(),
//        onError: (Throwable) -> Unit = {},
//        onConnect: WsConsumer = {}
//    ): Websocket = OkHttpNonBlockingWebsocket(uri, headers, timeout, client, onError, onConnect)

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

//private class OkHttpBlockingWebsocket(
//    uri: Uri,
//    headers: Headers,
//    timeout: Duration,
//    client: OkHttpClient
//) : WsClient {
//    private val connected = CompletableFuture<WsClient>()
//
//    private val queue = LinkedBlockingQueue<() -> WsMessage?>()
//
//    private val websocket = OkHttpNonBlockingWebsocket(uri, headers, timeout, client, connected::completeExceptionally) { ws ->
//        ws.onMessage { queue += { it } }
//        ws.onError { queue += { throw it } }
//        ws.onClose { queue += { null } }
//        connected.complete(this)
//    }
//
//    fun awaitConnected(): WsClient = try {
//        connected.get()
//    } catch (e: ExecutionException) {
//        throw (e.cause ?: e)
//    }
//
//    override fun received(): Sequence<WsMessage> = generateSequence { queue.take()() }
//
//    override fun close(status: WsStatus) = websocket.close(status)
//
//    override fun send(message: WsMessage) = websocket.send(message)
//}

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
