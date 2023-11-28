package org.http4k.boost

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.server.ServerConfig
import org.http4k.server.asServer

class Http4kApplication(
    private val app: HttpHandler,
    private val server: ServerConfig
): HttpHandler {

    override fun invoke(request: Request) = app(request)

    fun start() = asServer(server).start()
}
