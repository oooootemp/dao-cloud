package com.junmo.center.core.handler;

import com.junmo.center.core.CenterClusterManager;
import com.junmo.core.model.HeartbeatModel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: sucf
 * @date: 2023/4/16 23:03
 * @description:
 */
@Slf4j
public class AcceptHeartbeatClusterCenterHandler extends SimpleChannelInboundHandler<HeartbeatModel> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartbeatModel msg) {
        // get cluster ip
        String ip = ctx.channel().remoteAddress().toString();
        CenterClusterManager.joinCluster(ip);
    }
}