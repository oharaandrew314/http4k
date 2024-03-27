package org.http4k.websocket

import org.http4k.websocket.WsStatus.Companion.NORMAL

/**
 * Represents a connected Websocket instance, and can be passed around an application. This is configured
 * to react to events on the WS event stream by attaching listeners.
 */
interface Websocket {
    fun send(message: WsMessage)
    fun close(status: WsStatus = NORMAL)

    fun onError(fn: (Throwable) -> Unit)
    fun onClose(fn: (WsStatus) -> Unit)
    fun onMessage(fn: (WsMessage) -> Unit)
}

