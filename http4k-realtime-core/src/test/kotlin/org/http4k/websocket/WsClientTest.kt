package org.http4k.websocket

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.throws
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.routing.ws.bind
import org.http4k.websocket.WsStatus.Companion.NEVER_CONNECTED
import org.http4k.websocket.WsStatus.Companion.NORMAL
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference

class WsClientTest {

    private val message = WsMessage("hello")
    private val error = RuntimeException("foo") as Throwable

    private class TestConsumer: WsConsumer {
        lateinit var websocket: PushPullAdaptingWebSocket
        val messages = mutableListOf<WsMessage>()
        val throwable = mutableListOf<Throwable>()
        val closed = AtomicReference<WsStatus>()

        override fun invoke(p1: Websocket) {
            websocket = p1 as PushPullAdaptingWebSocket
            p1.onMessage {
                messages += it
            }
            p1.onClose {
                closed.set(it)
            }
            p1.onError {
                throwable.add(it)
            }
        }
    }

    @Test
    fun `when match, passes a consumer with the matching request`() {
        var r: Request? = null
        val wsHandler: WsHandler = {
            r = it
            WsResponse {}
        }

        wsHandler(Request(GET, "/"))

        assertThat(r!!, equalTo(Request(GET, "/")))
    }

    @Test
    fun `sends outbound messages to the websocket`() {
        val consumer = TestConsumer()

        val client = WsResponse(null, consumer).wsOrThrow().blocking()

        client.send(message)
        assertThat(consumer.messages, equalTo(listOf(message)))
        consumer.websocket.triggerError(error)
        assertThat(consumer.throwable, equalTo(listOf(error)))
        client.close(NEVER_CONNECTED)
        assertThat(consumer.closed.get(), equalTo(NEVER_CONNECTED))
    }

    @Test
    fun `sends inbound messages to the client`() {
        val client = WsResponse { ws: Websocket ->
            ws.send(message)
            ws.send(message)
            ws.close(NEVER_CONNECTED)
        }.wsOrThrow().blocking()

        val received = client.received()
        assertThat(received.take(2).toList(), equalTo(listOf(message, message)))
    }

    @Test
    fun `closed websocket throws when read attempted`() {
        val client =  WsResponse { ws: Websocket ->
            ws.close(NEVER_CONNECTED)
        }.wsOrThrow().blocking()

        assertThat(
            { client.received().take(2).toList() },
            throws(equalTo(IllegalStateException("foo")))
        )
    }

    @Test
    fun `throws for no match`() {
        val handler = "/foo" bind {
            WsResponse {}
        }

        assertThat(
            handler("/"),
            equalTo(WsResponse.Refuse(Response(NOT_FOUND)))
        )
    }

    @Test
    fun `when no messages`() {
        val client = WsResponse { ws: Websocket ->
            ws.close(NORMAL)
        }.wsOrThrow().blocking()

        assertThat(client.received().none(), equalTo(true))
        assertThat(client.received().toList(), isEmpty) // verify NoSuchElement not thrown during iteration
    }
}
