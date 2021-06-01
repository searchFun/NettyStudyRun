package nettystudy_project_02_timeserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
public class TimeServerHandler extends ChannelHandlerAdapter {

    private int count;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channelActive: 有客户端连接成功");
//        //为了发送消息，我们需要产生一个新的buffer用于放消息
//        //分配一个可以容纳long的bytebuf
//        //通过ctx.alloc()获得ByteBufAllocator接口的实现,ByteBufAllocator用于分配产生buffers
//        final ByteBuf time = ctx.alloc().buffer(Long.BYTES);
//        //将系统时间写入
//        time.writeLong(System.currentTimeMillis() / 1000L + 2208988800L);
//
//        //ChannelFuture代表一个还未发生的I/O操作
//        //ChannelFuture意味着任何请求操作可能未被执行，因为Netty是异步的
//        log.debug("channelActive: 准备调用写数据动作");
//        final ChannelFuture f = ctx.writeAndFlush(time);
//        log.debug("channelActive: 写数据动作已完成");
//
//        //当调用write()方法返回ChannelFuture并且已经是完成状态时，你需要调用close方法
//        //那么我们怎么知道请求已经完成了呢?最简单的方式就是为ChannelFuture添加一个ChannelFutureListener监听
//        //它会告诉我们这个ChannelFuture是否已经完成
//        f.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                log.debug("operationComplete: 这里数据才真正的写完了");
//                assert f == future;
//                ctx.close();
//            }
//        });
//        //如果嫌上面麻烦，可以使用Netty的实现
//        //f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("The time server receive order : " + body + " ; the counter is : " + ++count);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString() : "BAD ORDER";
        currentTime = currentTime + System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("exceptionCaught: 报错");
        cause.printStackTrace();
        ctx.close();
    }
}
