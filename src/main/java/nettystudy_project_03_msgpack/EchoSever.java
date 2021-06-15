package nettystudy_project_03_msgpack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class EchoSever {

    int port;

    public EchoSever(int port) {
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

                     [配置参数]:
                     ChannelOption.SO_KEEPALIVE, true
                     [配置作用]:
                     是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。

                     [配置参数]:
                     ChannelOption.TCP_NODELAY, true
                     [配置作用]:
                     在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。为了尽可能的利用网络带宽，TCP总是希望尽
                     可能的发送足够大的数据。这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。TCP_NODELAY就是用于
                     启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等
                     累积一定大小后再发送。默认为false。

                     [配置参数]:
                     ChannelOption.SO_REUSEADDR, true
                     [配置作用]:
                     SO_REUSEADDR允许启动一个监听服务器并捆绑其众所周知端口，即使以前建立的将此端口用做他们的本地端口的连接仍存在。这通常是重启监听服务器时出现，若不设
                     置此选项，则bind时将出错。
                     SO_REUSEADDR允许在同一端口上启动同一服务器的多个实例，只要每个实例捆绑一个不同的本地IP地址即可。对于TCP，我们根本不可能启动捆绑相同IP地址和相同端
                     口号的多个服务器。
                     SO_REUSEADDR允许单个进程捆绑同一端口到多个套接口上，只要每个捆绑指定不同的本地IP地址即可。这一般不用于TCP服务器。
                     SO_REUSEADDR允许完全重复的捆绑：当一个IP地址和端口绑定到某个套接口上时，还允许此IP地址和端口捆绑到另一个套接口上。一般来说，这个特性仅在支持多播的
                     系统上才有，而且只对UDP套接口而言（TCP不支持多播）

                     [配置参数]:
                     ChannelOption.SO_RCVBUF / ChannelOption.SO_SNDBU
                     [配置作用]:
                     定义接收或者传输的系统缓冲区buf的大小，

                     [配置参数]:
                     ChannelOption.ALLOCATOR
                     [配置作用]:
                     Netty4使用对象池，重用缓冲区
                     bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                     bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

                    */
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //配置处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast( new MsgpackDecoder());
                            socketChannel.pipeline().addLast( new MsgpackEncoder());
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();

            //等待服务端监听端口的关闭
            future.channel().closeFuture().sync();
        } finally {
            //退出，释放线程资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        EchoSever echoSever = new EchoSever(8080);
        echoSever.start();
    }
}
