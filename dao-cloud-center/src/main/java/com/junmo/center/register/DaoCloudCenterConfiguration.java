package com.junmo.center.register;

import com.junmo.center.register.handler.PollServerHandler;
import com.junmo.center.register.handler.ServerRegisterMessageHandler;
import com.junmo.center.web.RegisterController;
import com.junmo.core.netty.protocol.DaoMessageCoder;
import com.junmo.core.netty.protocol.ProtocolFrameDecoder;
import com.junmo.core.util.NetUtil;
import com.junmo.core.util.ThreadPoolFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author: sucf
 * @date: 2022/11/13 23:14
 * @description: register center configuration
 */
@Slf4j
public class DaoCloudCenterConfiguration implements ApplicationListener<ApplicationEvent> {

    /**
     * default hessian serialize
     */
    public static byte SERIALIZE_TYPE = 0;

    private final int port = 5551;

    @Value(value = "${server.servlet.context-path:null}")
    private String contextPath;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            ThreadPoolFactory.GLOBAL_THREAD_POOL.submit(() -> {
                NioEventLoopGroup boss = new NioEventLoopGroup();
                NioEventLoopGroup worker = new NioEventLoopGroup(4);
                try {
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.channel(NioServerSocketChannel.class);
                    serverBootstrap.group(boss, worker);
                    serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(new DaoMessageCoder());
                            ch.pipeline().addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new PollServerHandler());
                            ch.pipeline().addLast(new ServerRegisterMessageHandler());
                        }
                    });
                    Channel channel = serverBootstrap.bind(port).sync().channel();
                    log.info(">>>>>>>>>>>> dao-cloud-center port:{} start success <<<<<<<<<<<", port);
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    log.error("server interrupted error", e);
                } finally {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                }
            });
        } else if (applicationEvent instanceof WebServerInitializedEvent) {
            WebServerInitializedEvent event = (WebServerInitializedEvent) applicationEvent;
            if (contextPath == null) {
                log.info("====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> open web dao-cloud page address: http://{}:{}/dao-cloud/index.html <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<====", NetUtil.getLocalIp(), event.getWebServer().getPort());
            } else {
                log.info("====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> open web dao-cloud page address: http://{}:{}{}/dao-cloud/index.html <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<====", NetUtil.getLocalIp(), event.getWebServer().getPort(), contextPath);
            }
        }
    }

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(prefix = "dao-cloud.center.dashboard", name = "enabled", matchIfMissing = true)
    public RegisterController registerController() {
        return new RegisterController();
    }
}



