package guide.howto.testing_websocket_clients

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.client.WebsocketClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.websocket.JavaWebSocket
import org.http4k.testing.toSymmetric
import org.http4k.websocket.SetHostFrom
import org.http4k.websocket.SymmetricWsFilter
import org.http4k.websocket.SymmetricWsHandler
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.asServer
import org.http4k.websocket.then
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS

private val handler = websockets(
    "/ack" bind {
        WsResponse { ws ->
            ws.onMessage {
                println("Received ${it.bodyString()}")
                ws.send(WsMessage("ACK: ${it.bodyString()}"))
            }
        }
    }
).toSymmetric()


// The Websocket client we want to test
class AckClient(private val wsHandler: SymmetricWsHandler) {

    // send message and return ack
    fun send(message: String): String? {
        val waiter = CountDownLatch(1)
        var ack: String? = null

        val ws = wsHandler(Request(Method.GET, "/ack"))
        ws.onMessage {
            ack = it.bodyString()
            waiter.countDown()
        }
        ws.send(WsMessage(message))

        waiter.await(1000, MILLISECONDS)
        return ack
    }
}

// Run the client against a real server
fun main() {
    // convert handler to a server and start it
    handler.asServer(JavaWebSocket(0)).start().use { server ->
        val serverUri = Uri.of("ws://localhost:${server.port()}")

        // build a real Websocket client pointing to the real server
        val websocketClient = SymmetricWsFilter.SetHostFrom(serverUri)
            .then(WebsocketClient.symmetric())

        // Build an AckClient with the real Websocket client
        val client = AckClient(websocketClient)

        // send a message and print the ACK
        val ack = client.send("http4k")
        println(ack)
    }
}

class GreetingClientTest {

    @Test
    fun `send and verify ack`() {
        // Use the SymmetricWsHandler directly
        val client = AckClient(handler)

        // send a message and verify the ACK
        val ack = client.send("http4k")
        assertThat(ack, equalTo("ACK: http4k"))
    }
}


