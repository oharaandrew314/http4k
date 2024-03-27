package org.http4k.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.HttpHeaderNames.CONNECTION
import io.netty.handler.codec.http.HttpHeaderValues.UPGRADE
import io.netty.handler.codec.http.HttpHeaderValues.WEBSOCKET
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestSource
import org.http4k.core.Uri
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.wsOrThrow
import java.net.InetSocketAddress

class WebSocketServerHandler(private val wsHandler: WsHandler) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is HttpRequest) {
            if (requiresWsUpgrade(msg)) {
                val address = ctx.channel().remoteAddress() as InetSocketAddress
                val upgradeRequest = msg.asRequest(address)
                val websocket = wsHandler(upgradeRequest).wsOrThrow() as PushPullAdaptingWebSocket // TODO handle rejection

                val config = WebSocketServerProtocolConfig.newBuilder()
                    .handleCloseFrames(false)
                    .websocketPath(upgradeRequest.uri.toString())
                    .checkStartsWith(true)
                    .build()

                ctx.pipeline().addAfter(
                    ctx.name(),
                    "handshakeListener",
                    object : ChannelInboundHandlerAdapter() {
                        override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
                            if (evt is WebSocketServerProtocolHandler.HandshakeComplete) {
                                ctx.pipeline().addAfter(
                                    ctx.name(),
                                    Http4kWsChannelHandler::class.java.name,
                                    Http4kWsChannelHandler(websocket)
                                )
                            }
                        }
                    }
                )

                ctx.pipeline().addAfter(
                    ctx.name(),
                    WebSocketServerProtocolHandler::class.java.name,
                    WebSocketServerProtocolHandler(config)
                )

                ctx.fireChannelRead(msg)
            } else {
                ctx.fireChannelRead(msg)
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    private fun requiresWsUpgrade(httpRequest: HttpRequest) =
        httpRequest.headers().containsValue(CONNECTION, UPGRADE, true) &&
            httpRequest.headers().containsValue(UPGRADE, WEBSOCKET, true)

    private fun HttpRequest.asRequest(address: InetSocketAddress) =
        Request(Method.valueOf(method().name()), Uri.of(uri()))
            .headers(headers().map { it.key to it.value })
            .source(RequestSource(address.address.hostAddress, address.port))
}
