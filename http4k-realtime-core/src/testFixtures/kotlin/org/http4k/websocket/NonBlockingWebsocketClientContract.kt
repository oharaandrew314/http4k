package org.http4k.websocket

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CLIENT_TIMEOUT
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.StreamBody
import org.http4k.core.Uri
import org.http4k.server.PolyServerConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

abstract class NonBlockingWebsocketClientContract(
    serverConfig: PolyServerConfig,
    private val handler: WsHandler
) : BaseWebsocketClientContract(serverConfig) {

    @Test
    @Timeout(10, unit = TimeUnit.SECONDS)
    fun `send and receive in text mode`() {
        val queue = LinkedBlockingQueue<() -> WsMessage?>()
        val received = generateSequence { queue.take()() }

        val ws = handler(Uri.of("ws://localhost:$port/bob")).wsOrThrow()
        var sent = false
        ws.onMessage {
            if (!sent) {
                sent = true
                ws.send(WsMessage("hello"))
            }
            queue.add { it }
        }
        ws.onClose {
            queue.add { null }
        }

        assertThat(received.take(4).toList(), equalTo(listOf(WsMessage("bob"), WsMessage("hello"))))
    }

    @Test
    @Timeout(10, unit = TimeUnit.SECONDS)
    fun `send and receive in binary mode`() {
        val queue = LinkedBlockingQueue<() -> WsMessage?>()
        val received = generateSequence { queue.take()() }

        handler(Uri.of("ws://localhost:$port/bin")).wsOrThrow { ws ->
            ws.onMessage { message ->
                queue.add { message }
            }
            ws.onClose {
                queue.add { null }
            }
            ws.send(WsMessage("hello".byteInputStream()))
        }

        val messages = received.take(4).toList()
        assertThat(messages.all { it.body is StreamBody }, equalTo(true))
        assertThat(messages, equalTo(listOf(WsMessage("hello"))))
    }

    @Test
    fun `onConnect is called when connected`() {
        val connected = CountDownLatch(1)

        handler(Uri.of("ws://localhost:$port/bob")).wsOrThrow {
            connected.countDown()
        }

        assertThat(connected, isTrue)
    }

    @Test
    fun `refused to unknown host`() {
        assertThat(
            handler(Uri.of("ws://does-not-exist:12345")),
            equalTo(WsResponse.Refuse(Response(CLIENT_TIMEOUT)))
        )
    }

    @Test
    fun `refused on bad path`() {
        assertThat(
            handler(Uri.of("ws://localhost:$port/not_found")),
            equalTo(WsResponse.Refuse(Response(NOT_FOUND)))
        )
    }

    @Test
    fun `headers are sent to the server`() {
        val queue = LinkedBlockingQueue<() -> WsMessage?>()
        val received = generateSequence { queue.take()() }

        val ws = Request(Method.GET, Uri.of("ws://localhost:$port/headers"))
            .headers(listOf("testOne" to "1", "testTwo" to "2"))
            .let(handler)
            .wsOrThrow()

        ws.send(WsMessage(""))
        ws.onMessage {
            queue.add { it }
        }
        ws.onClose {
            queue.add { null }
        }

        assertThat(received.take(4).toList(), equalTo(listOf(WsMessage("testOne=1"), WsMessage("testTwo=2"))))
    }

    private val isTrue: Matcher<CountDownLatch> = has("counted down", { it.await(5, TimeUnit.SECONDS) }, equalTo(true))
}
