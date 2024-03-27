package org.http4k.client

import org.http4k.server.Undertow
import org.http4k.websocket.NonBlockingWebsocketClientContract
import java.time.Duration

class OkHttpWebsocketClientNonBlockingTest : NonBlockingWebsocketClientContract(
    serverConfig = Undertow(0),
    handler = OkHttpWebsocketClient(timeout = Duration.ofMillis(10))
)
