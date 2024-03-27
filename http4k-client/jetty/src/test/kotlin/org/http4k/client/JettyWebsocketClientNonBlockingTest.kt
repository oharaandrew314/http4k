package org.http4k.client

import org.http4k.server.Jetty
import org.http4k.server.ServerConfig
import org.http4k.websocket.NonBlockingWebsocketClientContract
import java.time.Duration

class JettyWebsocketClientNonBlockingTest : NonBlockingWebsocketClientContract(
    serverConfig = Jetty(0, ServerConfig.StopMode.Immediate),
    handler = JettyWebsocketClient(timeout = Duration.ofMillis(10))
)
