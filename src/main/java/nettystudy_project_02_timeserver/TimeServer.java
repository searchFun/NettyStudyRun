package nettystudy_project_02_timeserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeServer {
    //服务端监听端口
    int port;

    public TimeServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        //Nio连接的线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //Nio处理消息的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //服务启动程序
            ServerBootstrap bootstrap = new ServerBootstrap();
            //配置服务端启动程序
            bootstrap.group(bossGroup, workerGroup)
                    //配置通道为Nio的ServerSocket通道
                    .channel(NioServerSocketChannel.class)
                    /* 这个配置是 Socket的标准参数，非Netty,下面是配置项
                     [配置参数]:
                     ChannelOption.SO_BACKLOG, 1024
                     [配置作用]:
                     BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置
                     的值小于1，Java将使用默认值50。
                    */
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    /* 这个配置是 Socket的标准参数，非Netty,下面是配置项
                     [配置参数]:
                     ChannelOption.SO_KEEPALIVE, true
                     [配置作用]:
                     是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //配置处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //给通道在最后加一个处理器
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new TimeServerHandler());
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            log.debug("start: 服务启动成功！");
            //等待服务端监听端口的关闭
            future.channel().closeFuture().sync();
        } finally {
            //退出，释放线程资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TimeServer timeServer = new TimeServer(8080);
        timeServer.start();
    }
}
