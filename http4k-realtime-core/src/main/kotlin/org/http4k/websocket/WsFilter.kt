package org.http4k.websocket

import org.http4k.routing.RoutingWsHandler

fun interface WsFilter : (WsHandler) -> WsHandler {
    companion object
}

val WsFilter.Companion.NoOp: WsFilter get() = WsFilter { next -> { next(it) } }

fun WsFilter.then(next: WsFilter): WsFilter = WsFilter { this(next(it)) }

fun WsFilter.then(next: WsHandler): WsHandler = { this(next)(it) }

fun WsFilter.then(routingWsHandler: RoutingWsHandler): RoutingWsHandler = routingWsHandler.withFilter(this)
