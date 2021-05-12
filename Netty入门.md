# Netty入门

## 一、Netty 背景介绍

在学习一门框架的时候，适当了解其发展历史及背景是非常有必要的。

一门框架和技术的诞生，大多都是为了解决某个问题或者一系列的问题，而这些问题，对于框架受益者的我们可能感受不到，但是当我们将技术栈切换到历史版本，使用原生JDK去实现，那些问题可能会让我们非常头大，我们也许能提供一些问题的解决方案，但是随着项目逐渐庞大，我们的解决方案可能就会显得臃肿，不好用，改的很头疼。

所以适当了解历史，便是让我们了解大佬们在如何场景下，做出了什么样的决策。我们平常可能会去看源码，源码中包含了大佬们对于问题的设计思想，思维，学会这些，才能让我们站在现在的档口，去提出可能影响未来的解决方案，下面简单介绍一下Netty的背景。

### 1.1 Java Io发展历史

#### **第一个阶段：BIO-阶段**

**JDK1.0：**所有与输入相关的类都继承于InputStream，所有与输出相关的类都继承于OutputStream。

**JDK1.1：**增加了面向字符的IO类，包括Reader和Writer。

| IO类型 | 通道                          |
| ------ | ----------------------------- |
| TCP    | ServerSocket/Socket           |
| UDP    | DatagramSocket/DatagramSocket |
| File   | FileChannel                   |

**同步阻塞模式**，简单来说，就是必须要有一个线程阻塞一直在那里等着，有数据来就处理，不能做其他的事，其他的事需要另起线程。这对于平常的传输也是没有什么问题的，我们日常代码也是使用的各种InputStream/OutputStream,Reader/Writer，很是便于理解，一个输入一个输出，一个read一个write。

**BIO存在的问题：** BIO的阻塞对于服务器来说，问题就非常严重了，因为我们都知道服务器是面对的多终端，对于每一个终端来说，服务器应该是完全服务于它的。所以对于BIO的服务器，需要每有一个客户端就给其分配一个线程，这样对每个客户才感觉不到差异性。

但是对于JVM而言，每创建一个线程，线程的切换，都比较耗费资源，所以当并发量较高时，资源可能就严重不足，性能较低。

#### **第二个阶段：NIO-阶段**

**JDK1.4：** 增加了NIO，下表是几种常用Channel

| IO类型 | 通道                              |
| ------ | --------------------------------- |
| TCP    | ServerSocketChannel/SocketChannel |
| UDP    | DatagramChannel /DatagramChannel  |
| File   | FileChannel                       |

同步非阻塞模式，简单来说，就是大家尽管连接，服务端只需要一个线程就能找到哪个客户端连接有消息来，有消息来再处理，或是交给业务处理线程处理，而不是启动多个线程干等在那里，浪费系统切换线程资源。

至于是怎么做到一个线程就能知道所有连接是不是有消息，这个就是多路复用，至于怎么实现的，便是操作系统提供的方法，有兴趣可以了解了解，内部是循环或是怎样，反正就是能获取所有连接注册的事件（或是有消息可读，或是有消息要写，等等）

#### **第三个阶段：AIO阶段**

**JDK1.7：**增加了AIO，但具体与系统有关，需要操作系统支持，目前windows是支持的。

 异步非阻塞I/O模型（知乎中有人说java实现的不是异步非阻塞，而是异步阻塞，这里需要进一步考证）。 异步 IO 是基于事件和回调机制实现的，也就是应用操作之后会直接返回，不会阻塞在那里，当后台处理完成，操作系统会通知相应的线程进行后续的操作。

总结下来：

NIO的出现是为了解决采用BIO的大量连接而实际未发生读写而造成线程阻塞的一项技术，使原本的同步阻塞变成了同步非阻塞。

AIO的出现是为了解决对于数据处理好后的及时知晓而形成的一项技术，是原本的同步变成了异步。

### 1.2 Netty介绍(Netty官网-谷歌翻译)

Netty是一个NIO客户端服务器框架，可以快速轻松地开发网络应用程序，例如协议服务器和客户端。它极大地简化和简化了诸如TCP和UDP套接字服务器之类的网络编程。

“快速简便”并不意味着最终的应用程序将遭受可维护性或性能问题的困扰。 Netty经过精心设计，结合了许多协议（例如FTP，SMTP，HTTP以及各种基于二进制和文本的旧式协议）的实施经验。 最终，Netty成功地找到了一种无需妥协即可轻松实现开发，性能，稳定性和灵活性的方法。 

#### 特征(下面是官网的描述-谷歌翻译)

##### 设计好!

- 针对各种传输类型的统一 API - 阻塞和非阻塞Socket
- 基于灵活和可扩展的事件模型，允许明确分离关注事项
- 高度可自定义的线程模型 - 单线程、一个或多个线程池（如 SEDA）
- 真正的无连接数据UDP支持（自3.1）

##### 简单易用!

- 记录良好的 Javadoc、用户指南和示例
- 没有额外的依赖关系，JDK 5（内置3.x）或6（网4.x）就足够了
  - 注意：某些组件（如 HTTP/2）可能有更多的要求。有关更多信息，请参阅["要求"页面](https://netty.io/wiki/requirements.html)。

##### 高性能!

- 更好的吞吐量，更低的延迟
- 减少资源消耗
- 最小化不必要的内存副本

##### 安全!

- 完成 SSL/TLS 和启动支持

##### 社区活跃!

- 提前发布，经常发布
- 作者自2003年以来一直在写类似的框架，他仍然发现你的反馈珍贵！

## 二、Netty 使用场景

对于说Netty的使用场景,倒不如说TCP、UDP 协议可以拿来做什么,因为本质上Netty也只是基于现有进行的封装。

而说起TCP,UDP能拿来做什么,倒不如说什么地方需要使用到通信,什么地方需要传输数据。

### 2.1协议

针对于**熟知的网络模型：**

![img](https://upload-images.jianshu.io/upload_images/7541336-b87f3c6f5235b56d.png?imageMogr2/auto-orient/strip|imageView2/2/format/webp)

现已支持协议有：HTTP、HTTPS、FTP、TFTP、SMTP、POP3等等这些协议的服务器，都是基于TCP、UDP进行传输，那么也就是代表都可以使用Netty特性进行高效、稳定开发。

### 2.2 框架

服务端离不开的关键字：分布式、消息队列、RPC等等；摆脱不了的热门框架关键字：Dubbo、Zookeeper、RocketMQ等等

这些分布式框架，必然对于多个主机节点需要保持最基本的数据一致性，也就是一处改动，多处需要同步，而数据的可靠，高效同步便需要基于稳定的网络信息传输，基于这些诉求，Netty 便可以轻松胜任。

## 三、Netty 入门项目-TIME协议服务器

这里采用官网的引导项目：TimeServer，时间服务器进行Netty的入门探索。

### 3.1 引入Netty依赖

这里使用的是Netty5.0版本，为方便引入依赖，采用Maven管理项目

在新建的空白maven项目中，pom.xml引入如下依赖

=====[**pom.xml**]=====

```xml
<!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>5.0.0.Alpha2</version>
</dependency>
```

### 3.2创建TimeServer

#### 3.2.1 BootStrap(服务配置、启动相关)

=====[**TimeServer.java**]=====

```java
package nettystudy_project_02_timeserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import nettystudy_project_01_echo.EchoServerHandler;

@Slf4j
public class TimeServer {
    int port;

    public TimeServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        //Nio连接的线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //Nio处理消息的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //服务启动程序
            ServerBootstrap bootstrap = new ServerBootstrap();
            //配置服务端启动程序
            bootstrap.group(bossGroup, workerGroup)
                    //配置通道为Nio的ServerSocket通道
                    .channel(NioServerSocketChannel.class)
                    /* 这个配置是 Socket的标准参数，非Netty,下面是配置项
                     [配置参数]:
                     ChannelOption.SO_BACKLOG, 1024
                     [配置作用]:
                     BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置
                     的值小于1，Java将使用默认值50。
                    */
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    /* 这个配置是 Socket的标准参数，非Netty,下面是配置项
                     [配置参数]:
                     ChannelOption.SO_KEEPALIVE, true
                     [配置作用]:
                     是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //配置处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //给通道在最后加一个处理器
                            socketChannel.pipeline().addLast(new TimeServerHandler());
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            log.debug("start: 服务启动成功！");
            //等待服务端监听端口的关闭
            future.channel().closeFuture().sync();
        } finally {
            //退出，释放线程资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TimeServer timeServer = new TimeServer(8080);
        timeServer.start();
    }
}
```

#### 3.2.2 TimeServerHandler(服务业务处理相关)

=====[**TimeServerHandler.java**]=====

```java
package nettystudy_project_02_timeserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channelActive: 有客户端连接成功");
        //为了发送消息，我们需要产生一个新的buffer用于放消息
        //分配一个可以容纳long的bytebuf
        //通过ctx.alloc()获得ByteBufAllocator接口的实现,ByteBufAllocator用于分配产生buffers
        final ByteBuf time = ctx.alloc().buffer(Long.BYTES);
        //将系统时间写入
        time.writeLong(System.currentTimeMillis() / 1000L + 2208988800L);

        //ChannelFuture代表一个还未发生的I/O操作
        //ChannelFuture意味着任何请求操作可能未被执行，因为Netty是异步的
        log.debug("channelActive: 准备调用写数据动作");
        final ChannelFuture f = ctx.writeAndFlush(time);
        log.debug("channelActive: 写数据动作已完成");

        //当调用write()方法返回ChannelFuture并且已经是完成状态时，你需要调用close方法
        //那么我们怎么知道请求已经完成了呢?最简单的方式就是为ChannelFuture添加一个ChannelFutureListener监听
        //它会告诉我们这个ChannelFuture是否已经完成
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.debug("operationComplete: 这里数据才真正的写完了");
                assert f == future;
                ctx.close();
            }
        });
        //如果嫌上面麻烦，可以使用Netty的实现
        //f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("exceptionCaught: 报错");
        cause.printStackTrace();
        ctx.close();
    }
}
```

### 3.3创建TimeClient

#### 3.3.1 BootStrap(服务配置、启动相关)

=====[**TimeClient.java**]=====

```java
package nettystudy_project_02_timeserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

public class TimeClient {
    String ip;
    int port;

    public TimeClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void start() throws InterruptedException, IOException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            //连接到远程节点，阻塞等待直到连接完成
            ChannelFuture future = bootstrap.connect(ip, port).sync();

            //阻塞，直到channel 关闭
            future.channel().closeFuture().sync();
        } finally {
            //关闭线程池并释放所有资源
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        TimeClient client = new TimeClient("localhost", 8080);
        client.start();
    }
}
```

#### 3.3.2 TimeClientHandler()

=====[**TimeClientHandler.java**]=====

```java
package nettystudy_project_02_timeserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class TimeClientHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf) msg;
        try {
            long currentTimeMillis = (m.readLong() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

分别启动TimeServer.java TimeClient.java，结果如下：

![TimeServer](C:\Users\10091\AppData\Roaming\Typora\typora-user-images\image-20210512180929630.png)

![TimeClient](C:\Users\10091\AppData\Roaming\Typora\typora-user-images\image-20210512180952392.png)

### 3.4 ChannelOption

```
[配置参数]:
ChannelOption.SO_BACKLOG, 1024
[配置作用]:
BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置
的值小于1，Java将使用默认值50。

[配置参数]:
ChannelOption.SO_KEEPALIVE, true
[配置作用]:
是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。

[配置参数]:
ChannelOption.TCP_NODELAY, true
[配置作用]:
在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。为了尽可能的利用网络带宽，TCP总是希望尽
可能的发送足够大的数据。这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。TCP_NODELAY就是用于
启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等
累积一定大小后再发送。默认为false。

[配置参数]:
ChannelOption.SO_REUSEADDR, true
[配置作用]:
SO_REUSEADDR允许启动一个监听服务器并捆绑其众所周知端口，即使以前建立的将此端口用做他们的本地端口的连接仍存在。这通常是重启监听服务器时出现，若不设
置此选项，则bind时将出错。
SO_REUSEADDR允许在同一端口上启动同一服务器的多个实例，只要每个实例捆绑一个不同的本地IP地址即可。对于TCP，我们根本不可能启动捆绑相同IP地址和相同端
口号的多个服务器。
SO_REUSEADDR允许单个进程捆绑同一端口到多个套接口上，只要每个捆绑指定不同的本地IP地址即可。这一般不用于TCP服务器。
SO_REUSEADDR允许完全重复的捆绑：当一个IP地址和端口绑定到某个套接口上时，还允许此IP地址和端口捆绑到另一个套接口上。一般来说，这个特性仅在支持多播的
系统上才有，而且只对UDP套接口而言（TCP不支持多播）

[配置参数]:
ChannelOption.SO_RCVBUF / ChannelOption.SO_SNDBU
[配置作用]:
定义接收或者传输的系统缓冲区buf的大小，

[配置参数]:
ChannelOption.ALLOCATOR
[配置作用]:
Netty4使用对象池，重用缓冲区
bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
```



## 四、Netty 主要架构，流程

### 4.1 架构

### 4.2 流程

### 4.3 为什么这么设计？

## 五、Netty 项目-网络代理

