package org.http4k.server

import org.eclipse.jetty.websocket.api.Callback
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.exceptions.WebSocketException
import org.http4k.core.Request
import org.http4k.core.StreamBody
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import java.nio.ByteBuffer

class Http4kJettyServerWebSocketEndpoint(
    private val websocket: PushPullAdaptingWebSocket,
    private val request: Request
) : Session.Listener.AbstractAutoDemanding() {

    override fun onWebSocketOpen(session: Session) {
        super.onWebSocketOpen(session)
        websocket.onMessage { message ->
            if (!isOpen) {
                throw WebSocketException("Connection to ${request.uri} is closed.")
            }
            try {
                when (message.body) {
                    is StreamBody -> Callback.Completable.with { session.sendBinary(message.body.payload, it) }.get()
                    else -> Callback.Completable.with { session.sendText(message.body.toString(), it) }.get()
                }
            } catch (error: Throwable) {
                websocket.triggerError(error)
            }
        }
        websocket.onClose { status ->
            if (isOpen) {
                Callback.Completable.with { session.close(status.code, status.description, it) }.get()
            }
        }
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        super.onWebSocketClose(statusCode, reason)
        websocket.close(WsStatus(statusCode, reason.orEmpty()))
    }

    override fun onWebSocketError(cause: Throwable) {
        super.onWebSocketError(cause)
        websocket.triggerError(cause)
    }

    override fun onWebSocketText(message: String) {
        super.onWebSocketText(message)
        try {
            websocket.send(WsMessage(message))
        } catch (e: Throwable) {
            websocket.triggerError(e)
        }
    }

    override fun onWebSocketBinary(payload: ByteBuffer, callback: Callback) {
        super.onWebSocketBinary(payload, callback)
        try {
            websocket.triggerMessage(WsMessage(payload))
            callback.succeed()
        } catch (e: Throwable) {
            websocket.triggerError(e)
            callback.fail(e)
        }
    }
}
