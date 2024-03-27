package blog.nanoservices

import org.http4k.client.JavaWebSocketClient
import org.http4k.core.Uri
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.invoke
import org.http4k.websocket.wsOrThrow
import java.nio.file.FileSystems.getDefault
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY

fun `file watcher`()  =
    { ws: Websocket ->
        val w = getDefault().newWatchService()
        Paths.get("").register(w, ENTRY_MODIFY)
        val key = w.take()
        while (true)
            key.pollEvents()
                .forEach { ws.send(WsMessage(it.context().toString())) }
    }.asServer(Jetty()).start()

fun main() {
    `file watcher`()
    JavaWebSocketClient()(Uri.of("http://localhost:8000")).wsOrThrow().onMessage { println(it) }
}
