package org.http4k.websocket

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri

typealias WsHandler = (Request) -> WsResponse
typealias WsConsumer = (Websocket) -> Unit

operator fun WsHandler.invoke(uri: Uri) = this(Request(Method.GET, uri))
operator fun WsHandler.invoke(uri: String) = this(Uri.of(uri))
