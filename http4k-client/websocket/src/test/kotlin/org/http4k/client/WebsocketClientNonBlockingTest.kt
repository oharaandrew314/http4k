package org.http4k.client

import org.http4k.server.websocket.JavaWebSocket
import org.http4k.websocket.NonBlockingWebsocketClientContract
import org.junit.jupiter.api.Test
import java.time.Duration

class WebsocketClientNonBlockingTest : NonBlockingWebsocketClientContract(
    serverConfig = JavaWebSocket(0),
    handler = JavaWebSocketClient(timeout = Duration.ofSeconds(10))
) {
    @Test
    fun foo() = `send and receive in text mode`()
}
