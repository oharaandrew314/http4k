package org.http4k.server

import io.netty.buffer.Unpooled
import io.netty.buffer.Unpooled.EMPTY_BUFFER
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelFutureListener.CLOSE
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import org.http4k.websocket.PushPullAdaptingWebSocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import org.http4k.websocket.WsStatus.Companion.NOCODE

class Http4kWsChannelHandler(private val websocket: PushPullAdaptingWebSocket) : SimpleChannelInboundHandler<WebSocketFrame>() {
    private var normalClose = false

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        websocket.onMessage { message ->
            when(message.mode) {
                WsMessage.Mode.Binary -> ctx.writeAndFlush(BinaryWebSocketFrame(message.body.stream.use {
                    Unpooled.wrappedBuffer(
                        it.readBytes()
                    )
                }))
                WsMessage.Mode.Text -> ctx.writeAndFlush(TextWebSocketFrame(message.bodyString()))
            }
        }
        websocket.onClose { status ->
            ctx.writeAndFlush(CloseWebSocketFrame(status.code, status.description))
                .addListeners(ChannelFutureListener {
                    normalClose = true
                    websocket.triggerClose(status)
                }, CLOSE)
        }
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        if (!normalClose) {
            ctx.writeAndFlush(EMPTY_BUFFER).addListeners(ChannelFutureListener {
                websocket.triggerClose(NOCODE)
            }, CLOSE)
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame) {
        when (msg) {
            is TextWebSocketFrame -> websocket.triggerMessage(WsMessage(msg.text()))
            is BinaryWebSocketFrame -> websocket.triggerMessage(WsMessage(msg.content().nioBuffer()))
            is CloseWebSocketFrame -> {
                msg.retain()
                ctx.writeAndFlush(msg).addListeners(ChannelFutureListener {
                    normalClose = true
                    websocket.triggerClose(WsStatus(msg.statusCode(), msg.reasonText()))
                }, CLOSE)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        websocket.triggerError(cause)
    }
}
