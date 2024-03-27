package org.http4k.websocket

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.routing.ws.bind
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.concurrent.atomic.AtomicReference

class WebsocketTest {

    private val message = WsMessage("hello")

    private class TestConsumer {
        lateinit var websocket: Websocket
        val messages = mutableListOf<WsMessage>()
        val throwable = mutableListOf<Throwable>()
        val closed = AtomicReference<WsStatus>()

        operator fun invoke(websocket: Websocket) {
            this.websocket = websocket
            websocket.onMessage { messages += it }
            websocket.onClose { closed.set(it) }
            websocket.onError { throwable.add(it) }
        }
    }

    @Test
    fun `when match, passes a consumer with the matching request`() {
        var r: Request? = null
        val wsHandler = "/" bind {
            r = it
            WsResponse { }
        }

        wsHandler(Request(GET, "/")).wsOrThrow()

        assertThat(r, equalTo(Request(GET, "/")))
    }

    @Test
    fun `sends outbound messages to the websocket`() {
        val consumer = TestConsumer()
        val wsHandler: WsHandler = {
            WsResponse {
                consumer(it)
            }
        }

        val client = wsHandler(Request(GET, "/")).wsOrThrow()
        client.onMessage {
            fail("Client must not receive messages it sends")
        }

        client.send(message)
        assertThat(consumer.messages, equalTo(listOf(message)))
        client.close(WsStatus.NEVER_CONNECTED)
        assertThat(consumer.closed.get(), equalTo(WsStatus.NEVER_CONNECTED))
    }

    @Test
    fun `sends inbound messages to the client`() {
        val consumer = TestConsumer()
        val wsHandler: WsHandler = {
            WsResponse {
                consumer(it)
            }
        }

        val client = wsHandler(Request(GET, "/")).wsOrThrow()
        consumer.websocket.onMessage {
            fail("Server must not receive messages it sends")
        }

        val received = mutableListOf<WsMessage>()
        client.onMessage { received += it }

        consumer.websocket.send(message)
        consumer.websocket.send(message)

        assertThat(received, equalTo(listOf(message, message)))
    }
}
