package nettystudy_project1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EchoClient {
    String ip;
    int port;

    public EchoClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void start() throws InterruptedException, IOException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            //连接到远程节点，阻塞等待直到连接完成
            ChannelFuture future = bootstrap.connect(ip, port).sync();

            Channel channel = future.channel();
            while (true) {
                //向服务端发送内容
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String input = reader.readLine();
                if (input != null) {
                    if ("quit".equals(input)) {
                        System.exit(1);
                    }
                    channel.writeAndFlush(Unpooled.copiedBuffer(input.getBytes(StandardCharsets.UTF_8)));
                }
            }

            //阻塞，直到channel 关闭
            //  future.channel().closeFuture().sync();
        } finally {
            //关闭线程池并释放所有资源
            // group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        EchoClient client = new EchoClient("localhost", 8080);
        client.start();
    }
}
