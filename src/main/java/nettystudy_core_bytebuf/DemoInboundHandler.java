package nettystudy_core_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class DemoInboundHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf= (ByteBuf) msg;
        //...业务处理
        //调用父类的入站方法，将msg向后传递
        super.channelRead(ctx, msg);
    }
}
