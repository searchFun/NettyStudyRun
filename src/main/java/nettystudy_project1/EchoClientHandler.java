package nettystudy_project1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EchoClientHandler extends ChannelHandlerAdapter {


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("exceptionCaught: ");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String helloServer = "Hello Server!";
        ByteBuf byteBuf = Unpooled.copiedBuffer(helloServer.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("channelRead: ");
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String msgString = new String(bytes, StandardCharsets.UTF_8);
        log.info("Received msg from server: {}", msgString);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("channelReadComplete: ");
    }
}
