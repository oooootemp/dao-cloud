package com.junmo.boot.handler;

import com.junmo.core.model.HeartbeatModel;
import com.junmo.core.netty.protocol.HeartbeatPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: sucf
 * @date: 2023/1/15 12:29
 * @description: 接收客户端ping pong
 */
@Slf4j
public class ServerPingPongMessageHandler extends SimpleChannelInboundHandler<HeartbeatModel> {

    /**
     * 处理来自rpc client长连接心跳, 返回一个心跳包给服务端
     *
     * @param ctx            the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                       belongs to
     * @param heartbeatModel the message to handle
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartbeatModel heartbeatModel) {
        log.debug(">>>>>>>>>>> receive client (connect address = {}) heart beat packet <<<<<<<<<<<", ctx.channel().remoteAddress());
        ctx.channel().writeAndFlush(new HeartbeatPacket()).addListener(future -> {
            if (!future.isSuccess()) {
                log.error("<<<<<<<<<<< send back client (connect address = {}) heart beat packet error >>>>>>>>>>>", ctx.channel().remoteAddress(), future.cause());
            }
        });
    }
}