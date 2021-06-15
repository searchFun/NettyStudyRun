package nettystudy_project_99_webproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Test {
    public static void main(String[] args) {
        ByteBuf buf = Unpooled.buffer(10);
        for (int i = 0; i < 10; i++) {
            buf.writeInt(i);
        }
        for (int i = 0; i < 40; i += 4) {
            System.out.println(buf.getInt(i));
        }
    }
}
