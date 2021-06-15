package nettystudy_core_bytebuf;

import io.netty.buffer.*;

public class ByteBufType {
    public static void main(String[] args) {
        //heap
        ByteBuf heapBuf = ByteBufAllocator.DEFAULT.buffer();
        //direct
        ByteBuf directBuffer = ByteBufAllocator.DEFAULT.directBuffer();
        //composite
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeByteBuf.addComponents(heapBuf,directBuffer);
    }

}
