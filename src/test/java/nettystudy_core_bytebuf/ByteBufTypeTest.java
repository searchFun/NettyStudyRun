package nettystudy_core_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ByteBufTypeTest {
    @Test
    public void heapByteBuf() {
        ByteBuf heapBuf = ByteBufAllocator.DEFAULT.buffer();
        heapBuf.writeBytes("这里是JVM 堆内存".getBytes(StandardCharsets.UTF_8));
        if (heapBuf.hasArray()) {
            int length = heapBuf.readableBytes();
            byte[] array = heapBuf.array();
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            System.out.println(new String(array, offset, length, StandardCharsets.UTF_8));
            heapBuf.release();
        }
    }

    @Test
    public void directByteBuf() {
        ByteBuf directBuf = ByteBufAllocator.DEFAULT.directBuffer();
        directBuf.writeBytes("这里是直接内存".getBytes(StandardCharsets.UTF_8));
        if (!directBuf.hasArray()) {
            int length = directBuf.readableBytes();
            byte[] array = new byte[length];
            directBuf.getBytes(directBuf.readerIndex(), array);
            System.out.println(new String(array, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void compositeByteBuf() {
        CompositeByteBuf httpByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        ByteBuf headerBuf = Unpooled.copiedBuffer("我这里是Http消息头".getBytes(StandardCharsets.UTF_8));
        httpByteBuf.addComponent(headerBuf);
        //send body 1
        ByteBuf body1Buf = Unpooled.copiedBuffer("这里是Http消息体1".getBytes(StandardCharsets.UTF_8));
        httpByteBuf.addComponent(body1Buf);
        sendMsg(httpByteBuf);
        httpByteBuf.removeComponent(1);

        //send body 2
        ByteBuf body2Buf = Unpooled.copiedBuffer("这里是Http消息体2".getBytes(StandardCharsets.UTF_8));
        httpByteBuf.addComponent(body2Buf);
        sendMsg(httpByteBuf);
        httpByteBuf.removeComponent(1);
    }

    private void sendMsg(CompositeByteBuf httpByteBuf) {
        Iterator<ByteBuf> iterator = httpByteBuf.iterator();
        while (iterator.hasNext()) {
            ByteBuf next = iterator.next();
            int length = next.readableBytes();
            byte[] array = new byte[length];
            next.getBytes(next.readerIndex(), array);
            System.out.println(new String(array, StandardCharsets.UTF_8));
        }
    }
}
