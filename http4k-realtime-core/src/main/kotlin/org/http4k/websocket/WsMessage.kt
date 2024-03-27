package org.http4k.websocket

import org.http4k.core.Body
import java.io.InputStream
import java.nio.ByteBuffer

data class WsMessage(val body: Body, val mode: Mode) {
    constructor(value: String) : this(Body(value), Mode.Text)
    constructor(value: ByteBuffer): this(Body(value), Mode.Binary)
    constructor(value: InputStream, mode: Mode = Mode.Binary) : this(Body(value), mode)

    fun body(new: Body): WsMessage = copy(body = new)
    fun bodyString(): String = String(body.payload.array())

    enum class Mode { Text, Binary }

    companion object
}
