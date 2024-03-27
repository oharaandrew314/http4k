package org.http4k.websocket

import org.http4k.websocket.WsStatus.Companion.NORMAL
import java.util.concurrent.LinkedBlockingQueue

interface WsClient {
    fun received(): Sequence<WsMessage>
    fun close(status: WsStatus = NORMAL)
    fun send(message: WsMessage)
}

fun Websocket.blocking() = object: WsClient {
    private val inner = this@blocking
    private val queue = LinkedBlockingQueue<() -> WsMessage?>()

    init {
        onMessage { queue += { it } }
        onClose { queue += { null } }
    }

    override fun received() = generateSequence { queue.take()() }
    override fun close(status: WsStatus) = inner.close(status)
    override fun send(message: WsMessage) = inner.send(message)
}
