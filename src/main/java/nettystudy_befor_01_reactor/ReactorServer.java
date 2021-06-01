package nettystudy_befor_01_reactor;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
public class ReactorServer {
    int port;

    public ReactorServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // 1、获取Selector选择器
        Selector selector = Selector.open();
        // 2、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 4、绑定连接
        serverSocketChannel.bind(new InetSocketAddress(port));
        // 5、将通道注册到选择器上,并注册的操作为：“连接”操作
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 6、采用轮询的方式，查询获取“准备就绪”的注册过的操作
        while (selector.select() > 0) {
            // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的操作”）
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                // 8、获取“准备就绪”的时间
                SelectionKey selectedKey = selectedKeys.next();
                // 9、判断key是具体的什么事件
                if (selectedKey.isAcceptable()) {
                    // 10、处理就绪状态
                    selectedKey.attach(serverSocketChannel.accept());
                    onAcceptable(selectedKey);
                } else if (selectedKey.isReadable()) {
                    // 11、处理可读状态
                    onReadable(selectedKey);
                }
                // 12、移除选择键
                selectedKeys.remove();
            }
        }
    }

    public void onAcceptable(SelectionKey selectedKey) throws IOException {
        log.debug("onAcceptable: 收到连接");
        // 1.获取通道
        SocketChannel socketChannel = (SocketChannel) selectedKey.attachment();
        // 2、设置为非阻塞模式
        socketChannel.configureBlocking(false);
        // 3、将该通道注册到selector选择器上,并注册的操作为：“读”操作
        socketChannel.register(selectedKey.selector(), SelectionKey.OP_READ);
    }

    public void onReadable(SelectionKey selectedKey) throws IOException {
        log.debug("onReadable: 收到读事件");
        // 1、获取该选择器上的“读就绪”状态的通道
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
        // 2、读取数据
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            //分配一个1024大小缓存buffer,避免重复分配
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            //读取到的长度
            int readLength = 0;
            //有读到内容
            while ((readLength = socketChannel.read(byteBuffer)) != 0) {
                //切换读模式
                byteBuffer.flip();
                arrayOutputStream.write(byteBuffer.array(), 0, readLength);
                byteBuffer.clear();
            }
            //结果
            byte[] dataBytes = arrayOutputStream.toByteArray();
            System.out.println("Rec from client: " + new String(dataBytes));
            sendMessage(socketChannel, (new String(dataBytes) + "-server").getBytes());
        }
    }

    private void sendMessage(SocketChannel socketChannel, byte[] bytes) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

    public static void main(String[] args) throws IOException {
        ReactorServer server = new ReactorServer(8080);
        server.start();
    }
}
