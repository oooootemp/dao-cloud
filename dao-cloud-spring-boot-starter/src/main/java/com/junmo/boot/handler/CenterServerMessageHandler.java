package com.junmo.boot.handler;

import com.google.gson.Gson;
import com.junmo.boot.bootstrap.RegistryManager;
import com.junmo.core.exception.DaoException;
import com.junmo.core.model.ProviderModel;
import com.junmo.core.model.ProxyProviderModel;
import com.junmo.core.model.ProxyProviderServerModel;
import com.junmo.core.model.ServerNodeModel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: sucf
 * @date: 2022/11/19 09:11
 * @description: poll server node
 */
@Slf4j
public class CenterServerMessageHandler extends SimpleChannelInboundHandler<ProxyProviderServerModel> {

    public static final Map<ProxyProviderModel, Promise<Set<ServerNodeModel>>> PROMISE_MAP = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyProviderServerModel proxyProviderServerModel) {
        String proxy = proxyProviderServerModel.getProxy();
        ProviderModel providerModel = proxyProviderServerModel.getProviderModel();
        Promise<Set<ServerNodeModel>> pollPromise = PROMISE_MAP.remove(new ProxyProviderModel(proxy, providerModel));
        Set<ServerNodeModel> serverNodeModes = proxyProviderServerModel.getServerNodeModes();
        String errorMessage = proxyProviderServerModel.getErrorMessage();
        if (StringUtils.hasLength(errorMessage)) {
            log.error(">>>>>>>>>>>> poll (proxy = {}, provider = {}, server node = {}) error. <<<<<<<<<<<<", proxyProviderServerModel.getProxy(), proxyProviderServerModel.getProviderModel(), new Gson().toJson(proxyProviderServerModel.getServerNodeModes()));
            pollPromise.setFailure(new DaoException(errorMessage));
        } else {
            log.info(">>>>>>>>>>>> poll (proxy = {}, provider = {}, server node = {}) success. <<<<<<<<<<<<", proxyProviderServerModel.getProxy(), proxyProviderServerModel.getProviderModel(), new Gson().toJson(proxyProviderServerModel.getServerNodeModes()));
            pollPromise.setSuccess(serverNodeModes);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        RegistryManager.reconnect();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            RegistryManager.reconnect();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}