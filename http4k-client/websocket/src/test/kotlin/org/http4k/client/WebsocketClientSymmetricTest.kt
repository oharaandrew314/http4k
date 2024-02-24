package org.http4k.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.server.Jetty
import org.http4k.testing.toSymmetric
import org.http4k.websocket.SetHostFrom
import org.http4k.websocket.SymmetricWsFilter
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.asServer
import org.http4k.websocket.then
import org.junit.jupiter.api.Test
import java.time.Duration

class WebsocketClientSymmetricTest {

    private val messages = mutableListOf<String>()
    private val wsHandler = { _: Request ->
        WsResponse { ws ->
            ws.onMessage {
                messages += it.bodyString()
                ws.send(WsMessage("ACK: ${it.bodyString()}"))
            }
        }
    }.toSymmetric()

    private val server = wsHandler.asServer(Jetty(0)).start()

    private val client = SymmetricWsFilter.SetHostFrom(Uri.of("ws://localhost:${server.port()}"))
        .then(WebsocketClient.symmetric(timeout = Duration.ofSeconds(1)))

    @Test
    fun `open websocket through client`() {
        val ws = client(Request(Method.GET, "/"))

        val received = mutableListOf<String>()
        ws.onMessage { received += it.bodyString() }

        ws.send(WsMessage("hi"))

        // I don't get hamkrest assertions.  How do you do `messages.shouldContainExactly("hi", "bye")`?
        assertThat(messages, hasSize(equalTo(1)))
        assertThat(received, hasSize(equalTo(1)))
    }
}
