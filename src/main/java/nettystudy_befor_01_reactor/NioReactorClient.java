package nettystudy_befor_01_reactor;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

@Slf4j
public class NioReactorClient {
    String host;
    int port;

    public NioReactorClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        // 1、获取Selector选择器
        Selector selector = Selector.open();
        // 2、获取通道
        SocketChannel socketChannel = SocketChannel.open();
        // 3.设置为非阻塞
        socketChannel.configureBlocking(false);
        // 4、将通道注册到选择器上,并注册的操作为：“接收”操作
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        // 5、连接服务器
        socketChannel.connect(new InetSocketAddress(host, port));
        // 6、采用轮询的方式，查询获取“准备就绪”的注册过的操作
        while (selector.select() > 0) {
            // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的操作”）
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                // 8、获取“准备就绪”的时间
                SelectionKey selectedKey = selectedKeys.next();
                // 9、判断key是具体的什么事件
                if (selectedKey.isConnectable()) {
                    onConnectable(selectedKey);
                } else if (selectedKey.isReadable()) {
                    onReadable(selectedKey);
                }
                // 15、移除选择键
                selectedKeys.remove();
            }
        }
    }

    private void onConnectable(SelectionKey selectionKey) throws IOException {
        log.debug("onAcceptable: 连接成功");
        // 1、获取该选择器上的“连接就绪”状态的通道
        SocketChannel clientSocket = (SocketChannel) selectionKey.channel();
        // 2、连接成功后需要将选择器设置为对该通道的读事件感兴趣（不然同样会无限循环）
        if (clientSocket.finishConnect()) {
            selectionKey.interestOps(SelectionKey.OP_READ);
            sendMessage(clientSocket, "Hello Server!".getBytes());
        }
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String s = scanner.nextLine();
                try {
                    sendMessage(clientSocket, s.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onReadable(SelectionKey selectedKey) throws IOException {
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
            System.out.println("Rec from server: " + new String(dataBytes));
        }
    }

    private void sendMessage(SocketChannel socketChannel, byte[] bytes) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }


    public static void main(String[] args) throws IOException {
        NioReactorClient nioReactorClient = new NioReactorClient("localhost", 8080);
        nioReactorClient.start();
    }
}
