package org.http4k.websocket

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.http4k.client.JavaWebSocketClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.filter.ClientFilters
import org.http4k.filter.SetWsHostFrom
import org.http4k.hamkrest.hasBody
import org.http4k.lens.string
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Http4kServer
import org.http4k.server.PolyHandler
import org.http4k.server.PolyServerConfig
import org.http4k.server.asServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import org.http4k.routing.bind as hbind

abstract class WebsocketServerContract(
    private val serverConfig: (Int) -> PolyServerConfig,
    private val client: HttpHandler,
    private val httpSupported: Boolean = true
) {
    private lateinit var server: Http4kServer

    private val lens = WsMessage.string().map(String::toInt).toLens()

    private val wsClient by lazy {
        ClientFilters.SetWsHostFrom(Uri.of("ws://localhost:${server.port()}"))
            .then(JavaWebSocketClient())
    }

    @BeforeEach
    fun before() {
        val routes = routes(
            "/hello/{name}" hbind { r: Request -> Response(OK).body(r.path("name")!!) }
        )
        val ws = websockets(
            "/hello" bind websockets(
                "/{name}" bind { req: Request ->
                    WsResponse { ws ->
                        println("invoke endpoint")
                        val name = req.path("name")!!
                        ws.send(WsMessage(name))
                        ws.onMessage {
                            println("server received $it")
                            ws.send(WsMessage("goodbye $name".byteInputStream()))
                        }
                        ws.onClose { println("$name is closing") }
                    }
                }
            ),
            "/errors" bind { _: Request ->
                WsResponse { ws ->
                    ws.onMessage { lens(it) }
                    ws.onError {
                        ws.send(WsMessage(it.localizedMessage))
                    }
                }
            },
            "/queries" bind { req: Request ->
                WsResponse { ws ->
                    ws.onMessage { ws.send(WsMessage(req.query("query") ?: "not set")) }
                    ws.onError { ws.send(WsMessage(it.localizedMessage)) }
                }
            },
            "/echo" bind { _: Request ->
                WsResponse { ws ->
                    ws.onMessage { ws.send(it) }
                }
            })

        server = PolyHandler(routes.takeIf { httpSupported }, ws).asServer(serverConfig(0)).start()
    }

    @AfterEach
    fun after() {
        server.stop()
    }

    @Test
    fun `can do standard http traffic`() {
        if (!httpSupported) return
        assertThat(client(Request(GET, "/hello/bob")), hasBody("bob"))
    }

    @Test
    fun `can send and receive messages from socket`() {
        val ws = wsClient(Uri.of("/hello/bob")).wsOrThrow().blocking()

        ws.send(WsMessage("hello"))
        assertThat(
            ws.received().take(2).toList(),
            equalTo(listOf(WsMessage("bob"), WsMessage("goodbye bob".byteInputStream())))
        )
    }

    @Test
    fun `errors are propagated to the 'on error' handler`() {
        val client = wsClient(Uri.of("/errors")).wsOrThrow().blocking()
        client.send(WsMessage("hello"))
        assertThat(
            client.received().take(1).toList(),
            equalTo(listOf(WsMessage("websocket 'message' must be object")))
        )
    }

    @Test
    fun `should propagate close on client close`() {
        val latch = CountDownLatch(1)
        var closeStatus: WsStatus? = null

        val server = websockets(
            "/closes" bind { _: Request ->
                WsResponse { ws ->
                    ws.onClose {
                        closeStatus = it
                        latch.countDown()
                    }
                }
            }).asServer(serverConfig(0)).start()
        val client = wsClient(Uri.of("/closes")).wsOrThrow()
        client.close()

        latch.await()
        assertThat(closeStatus, present())
        server.close()
    }

    @Test
    fun `should propagate close on server close`() {
        val latch = CountDownLatch(1)
        var closeStatus: WsStatus? = null

        val server = websockets(
            "/closes" bind { _: Request ->
                WsResponse { ws ->
                    ws.onMessage {
                        ws.close()
                    }
                    ws.onClose {
                        closeStatus = it
                        latch.countDown()
                    }
                }
            }).asServer(serverConfig(0)).start()
        val client = wsClient(Uri.of("/closes")).wsOrThrow()
        client.send(WsMessage("message"))

        latch.await()
        assertThat(closeStatus, present())
        client.close()
        server.close()
    }

    @Test
    fun `should propagate close on server stop`() {
        val latch = CountDownLatch(1)
        var closeStatus: WsStatus? = null

        val server = websockets(
            "/closes" bind { _: Request ->
                WsResponse { ws ->
                    ws.onClose {
                        closeStatus = it
                        latch.countDown()
                    }
                }
            }).asServer(serverConfig(0)).start()
        val client = wsClient(Uri.of("/closes")).wsOrThrow()
        client.send(WsMessage("message"))
        server.close()

        latch.await()
        assertThat(closeStatus, present())
        client.close()
    }

    @Test
    fun `should correctly set query parameters on upgrade request passed into the web socket`() {
        val client = wsClient(Uri.of("/queries?query=foo")).wsOrThrow().blocking()
        client.send(WsMessage("hello"))
        assertThat(client.received().take(1).toList(), equalTo(listOf(WsMessage("foo"))))
    }

    @Test
    fun `can connect with non-blocking client`() {
        val client = wsClient(Uri.of("/hello/bob")).wsOrThrow()
        val latch = CountDownLatch(1)
        client.onMessage {
            latch.countDown()
        }

        latch.await()
    }

    @Test
    fun `should fail on invalid url`() {
        assertThat(
            wsClient(Uri.of("/aaa")),
            equalTo(WsResponse.Refuse(Response(NOT_FOUND)))
        )
    }

    @Test
    fun `can send and receive multi-frame messages from socket`() {
        val client = wsClient(Uri.of("/echo")).wsOrThrow().blocking()

        val longMessage = WsMessage((1..10000).joinToString("") { "a" })
        client.send(longMessage)

        val anotherMessage = WsMessage("another message")
        client.send(anotherMessage)

        assertThat(client.received().take(2).toList(), equalTo(listOf(longMessage, anotherMessage)))
    }
}
