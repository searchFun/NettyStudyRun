package nettystudy_befor_01_reactor;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SimpleServerReactor implements Runnable {
    int port;
    Selector selector;
    ServerSocketChannel serverSocketChannel;

    public SimpleServerReactor(int port) throws IOException {
        this.port = port;
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        SelectionKey acceptKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        acceptKey.attach(new Acceptor());
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    dispatch(next);
                    iterator.remove();
                }
            }
        }
    }

    private void dispatch(SelectionKey next) {
        Runnable runnable = (Runnable) next.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }

    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel accept = serverSocketChannel.accept();
                if (accept != null) {
                    new Handler(accept);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class Handler implements Runnable {

        SocketChannel socketChannel;
        SelectionKey selectionKey;

        ByteBuffer input = ByteBuffer.allocate(1024);
        ByteBuffer output = ByteBuffer.allocate(1024);

        static final int READING = 0, SENDING = 1;

        int state = READING;

        public Handler(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketChannel.configureBlocking(false);
            this.selectionKey = this.socketChannel.register(selector, 0);
            this.selectionKey.attach(this);

            //注册读事件
            this.selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        @Override
        public void run() {
            try {
                if (state == READING) {
                    read();
                } else if (state == SENDING) {
                    send();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        private void read() throws IOException {
            socketChannel.read(input);
            if (inputIsComplete()) {
                process();
                state = SENDING;
                //注册写事件
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
        }

        private boolean outputIsComplete() {
            return true;
        }

        private void process() {
        }

        private boolean inputIsComplete() {
            return true;
        }

        private void send() throws IOException {
            socketChannel.write(output);
            if (outputIsComplete()) {
                selectionKey.cancel();
            }
        }


    }
}
