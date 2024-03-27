package org.http4k.client

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import org.http4k.core.Uri
import org.http4k.server.websocket.JavaWebSocket
import org.http4k.websocket.BlockingWebsocketClientContract
import org.http4k.websocket.WsMessage
import org.http4k.websocket.blocking
import org.http4k.websocket.invoke
import org.http4k.websocket.wsOrThrow
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.junit.jupiter.api.Test
import java.time.Duration

class WebsocketClientBlockingTest : BlockingWebsocketClientContract(
    serverConfig = JavaWebSocket(0),
    wsHandler = JavaWebSocketClient(timeout = Duration.ofMillis(10))
) {
    override fun <T : Throwable> connectErrorMatcher(): Matcher<T> = isA<WebsocketNotConnectedException>()

    override fun <T : Throwable> connectionClosedErrorMatcher(): Matcher<T> = isA<WebsocketNotConnectedException>()

    @Test
    fun foo() = `send and receive in text mode`()

    @Test
    fun `blocking with auto-reconnection (closed by server)`() {
        val wsHandler = JavaWebSocketClient(timeout = Duration.ofMillis(10), autoReconnect = true)
        val client = wsHandler(Uri.of("ws://localhost:$port/bob")).wsOrThrow().blocking()
        client.send(WsMessage("hello"))

        assertThat(client.received().take(3).toList(), equalTo(listOf(WsMessage("bob"), WsMessage("hello"))))

        Thread.sleep(100)

        client.send(WsMessage("hi"))
        assertThat(client.received().take(3).toList(), equalTo(listOf(WsMessage("bob"), WsMessage("hi"))))
    }

    @Test
    fun `blocking with auto-reconnection (closed by client)`() {
        val wsHandler = JavaWebSocketClient(timeout = Duration.ofMillis(10), autoReconnect = true)
        val client = wsHandler(Uri.of("ws://localhost:$port/long-living/bob")).wsOrThrow().blocking()

        client.send(WsMessage("hello"))
        Thread.sleep(1000) // wait until the message comes back

        client.close()

        assertThat(client.received().take(3).toList(), equalTo(listOf(WsMessage("bob"), WsMessage("hello"))))

        client.send(WsMessage("hi"))
        Thread.sleep(1000)
        client.close()

        assertThat(client.received().take(3).toList(), equalTo(listOf(WsMessage("bob"), WsMessage("hi"))))
    }
}
