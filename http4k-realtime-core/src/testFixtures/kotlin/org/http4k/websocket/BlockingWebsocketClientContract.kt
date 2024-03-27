package org.http4k.websocket

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CLIENT_TIMEOUT
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.UNKNOWN_HOST
import org.http4k.core.StreamBody
import org.http4k.core.Uri
import org.http4k.server.PolyServerConfig
import org.junit.jupiter.api.Test

abstract class BlockingWebsocketClientContract(
    serverConfig: PolyServerConfig,
    protected val wsHandler: WsHandler,
) : BaseWebsocketClientContract(serverConfig) {

    abstract fun <T: Throwable> connectErrorMatcher(): Matcher<T>
    abstract fun <T: Throwable> connectionClosedErrorMatcher(): Matcher<T>

    @Test
    fun `send and receive in text mode`() {
        val ws = wsHandler(Uri.of("ws://localhost:$port/bob")).wsOrThrow().blocking()
        ws.send(WsMessage("hello"))

        val messages = ws.received().take(4).toList()

        assertThat(messages, equalTo(listOf(WsMessage("bob"), WsMessage("hello"))))
    }

    @Test
    fun `send and receive in binary mode`() {
        val ws = wsHandler(Uri.of("ws://localhost:$port/bin")).wsOrThrow().blocking()
        ws.send(WsMessage("hello".byteInputStream()))

        val messages = ws.received().take(4).toList()

        assertThat(messages.all { it.body is StreamBody }, equalTo(true))
        assertThat(messages, equalTo(listOf(WsMessage("hello"))))
    }


    @Test
    fun `websocket is refused when path not found`() {
        assertThat(
            wsHandler(Uri.of("ws://localhost:$port/not-found")),
            equalTo(WsResponse.Refuse(Response(NOT_FOUND)))
        )
    }

    @Test
    fun `websocket is refused when host not found`() {
        assertThat(
            wsHandler(Uri.of("ws://locahost:12345/bin")),
            equalTo(WsResponse.Refuse(Response(CLIENT_TIMEOUT)))
        )
    }

    @Test
    fun `exception is thrown on sending after connection is closed`() {
        val ws = wsHandler(Uri.of("ws://localhost:$port/bob")).wsOrThrow().blocking()
        ws.send(WsMessage("hello"))

        val messages = ws.received().take(3).toList()

        assertThat(messages, equalTo(listOf(WsMessage("bob"), WsMessage("hello"))))
        assertThat({ ws.send(WsMessage("hi")) }, throws(connectionClosedErrorMatcher()))
    }

    @Test
    fun `headers are sent to the server`() {
        val ws = Request(GET, Uri.of("ws://localhost:$port/headers"))
            .headers(listOf("testOne" to "1", "testTwo" to "2"))
            .let(wsHandler)
            .wsOrThrow()
            .blocking()

        ws.send(WsMessage(""))

        val messages = ws.received().take(4).toList()

        assertThat(messages, equalTo(listOf(WsMessage("testOne=1"), WsMessage("testTwo=2"))))
    }
}
