package org.http4k.filter

import org.http4k.core.RequestContext
import org.http4k.core.Store
import org.http4k.core.Uri
import org.http4k.sse.SseFilter
import org.http4k.websocket.WsFilter
import org.http4k.websocket.WsResponse

fun ServerFilters.InitialiseSseRequestContext(contexts: Store<RequestContext>) = SseFilter { next ->
    {
        val context = RequestContext()
        try {
            next(contexts(context, it))
        } finally {
            contexts.remove(context)
        }
    }
}

fun ServerFilters.InitialiseWsRequestContext(contexts: Store<RequestContext>) = WsFilter { next ->
    {
        val context = RequestContext()
        try {
            next(contexts(context, it))
        } finally {
            contexts.remove(context)
        }
    }
}

fun ServerFilters.SetWsSubProtocol(subprotocol: String) = WsFilter { next ->
    {
        when(val response = next(it)) {
            is WsResponse.Accept -> response.copy(subprotocol = subprotocol)
            is WsResponse.Refuse -> response
        }
    }
}

fun ClientFilters.SetWsHostFrom(uri: Uri) = WsFilter { next ->
    {
        next(it.uri(it.uri.scheme(uri.scheme).host(uri.host).port(uri.port))
            .replaceHeader("Host", "${uri.host}${uri.port?.let { port -> ":$port" } ?: ""}"))
    }
}

