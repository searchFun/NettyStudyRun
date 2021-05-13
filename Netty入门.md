# Netty入门

## 一、Netty 背景介绍

在学习一门框架的时候，适当了解其发展历史及背景是非常有必要的。

一门框架和技术的诞生，大多都是为了解决某个问题或者一系列的问题，而这些问题，对于框架受益者的我们可能感受不到，但是当我们将技术栈切换到历史版本，使用原生JDK去实现，那些问题可能会让我们非常头大，我们也许能提供一些问题的解决方案，但是随着项目逐渐庞大，我们的解决方案可能就会显得臃肿，不好用，改的很头疼。

所以适当了解历史，便是让我们了解大佬们在如何场景下，做出了什么样的决策。我们平常可能会去看源码，源码中包含了大佬们对于问题的设计思想，思维，学会这些，才能让我们站在现在的档口，去提出可能影响未来的解决方案，下面简单介绍一下Netty的背景。

### 1.1 Java IO发展历史

#### **第一个阶段：BIO-阶段**

**JDK1.0：**所有与输入相关的类都继承于InputStream，所有与输出相关的类都继承于OutputStream。

**JDK1.1：**增加了面向字符的IO类，包括Reader和Writer。

**同步阻塞模式**，简单来说，就是必须要有一个线程阻塞一直在那里等着，有数据来就处理，不能做其他的事，其他的事需要另起线程。这对于平常的传输也是没有什么问题的，我们日常代码也是使用的各种

**BIO存在的问题：** BIO的阻塞对于服务器来说，问题就非常严重了，因为我们都知道服务器是面对的多终端，对于每一个终端来说，服务器应该是完全服务于它的。所以对于BIO的服务器，需要每有一个客户端就给其分配一个线程，这样对每个客户才感觉不到差异性。

但是对于JVM而言，每创建一个线程，线程的切换，都比较耗费资源，所以当并发量较高时，资源可能就严重不足，性能较低。

#### **第二个阶段：NIO-阶段**

**JDK1.4：** 增加了NIO，下表是几种常用Channel

| IO类型 | 通道                              |
| ------ | --------------------------------- |
| TCP    | ServerSocketChannel/SocketChannel |
| UDP    | DatagramChannel /DatagramChannel  |
| File   | FileChannel                       |

**同步非阻塞模式**，简单来说，就是大家尽管连接，服务端只需要一个线程就能找到哪个客户端连接有消息来，有消息来再处理，或是交给业务处理线程处理，而不是启动多个线程干等在那里，浪费系统切换线程资源。

至于是怎么做到一个线程就能知道所有连接是不是有消息，这个就是多路复用，至于怎么实现的，便是操作系统提供的方法，有兴趣可以了解了解，内部是循环或是怎样，反正就是能获取所有连接注册的事件（或是有消息可读，或是有消息要写，等等）

#### **第三个阶段：AIO阶段**

**JDK1.7：**增加了AIO，但具体与系统有关，需要操作系统支持，目前windows是支持的，linux下还不是太成熟。

 异步非阻塞I/O模型（知乎中有人说java实现的不是异步非阻塞，而是异步阻塞，这里需要进一步考证）。 异步 IO 是基于事件和回调机制实现的，也就是应用操作之后会直接返回，不会阻塞在那里，当后台处理完成，操作系统会通知相应的线程进行后续的操作。

#### 总结：

NIO的出现是为了解决采用BIO的大量连接而实际未发生读写而造成线程阻塞的一项技术，使原本的同步阻塞变成了同步非阻塞。

AIO的出现是为了解决对于数据处理好后的及时知晓而形成的一项技术，是原本的同步变成了异步。

当然Netty的出现也有其出现的缘由，下面就是为什么会有Netty的出现。

### 1.2 为什么要有Netty

下面简单引入官网的介绍：

> Netty是**一个NIO客户端服务器框架**，可以快速轻松地开发网络应用程序，例如协议服务器和客户端。它**极大地简化了诸如TCP和UDP套接字服务器之类的网络编程**。
>
> “快速简便”并不意味着最终的应用程序将遭受可维护性或性能问题的困扰。 Netty经过精心设计，结合了许多协议（**例如FTP，SMTP，HTTP以及各种基于二进制和文本的旧式协议**）的实施经验。 最终，Netty成功地找到了一种无需妥协即可轻松实现开发，性能，稳定性和灵活性的方法。 

从官网描述可以简单了解到，Netty是一个基于NIO的框架，能够方便的、易维护进行网络编程。

那么传统的NIO又有哪些问题或者弊端，而导致Netty的产生呢？

**① NIO 原生库的复杂 :** 使用原生 `NIO` 开发服务器端与客户端 , 需要涉及到 服务器套接字通道 ( `ServerSocketChannel` ) , 套接字通道 ( `SocketChannel` ) , 选择器 ( `Selector` ) , 缓冲区 ( `ByteBuffer` ) 等组件 , 这些组件的原理 和API 都要熟悉 , 才能进行 `NIO` 的开发与调试 , 之后还需要针对应用进行调试优化

**② NIO 开发基础 :** `NIO` 门槛略高 , 需要开发者掌握多线程、网络编程等才能开发并且优化 `NIO` 网络通信的应用程序

**③ 原生 API 开发网络通信模块的基本的传输处理 :** 网络传输不光是实现服务器端和客户端的数据传输功能 , 还要处理各种异常情况 , 如 连接断开重连机制 , 网络堵塞处理 , 异常处理 , 粘包处理 , 拆包处理 , 缓存机制 等方面的问题 , 这是所有成熟的网络应用程序都要具有的功能 , 否则只能说是入门级的 Demo

**④ NIO BUG :** `NIO` 本身存在一些 BUG , 如 `Epoll` , 导致 选择器 ( `Selector` ) 空轮询 , 在 JDK 1.7 中还没有解决

正是由于这些一系列NIO的问题，就促成了一些大佬们忍不了了，造个Netty解决解决吧！

#### 总结

Netty是一个能够非常方便实现网络通信基于原生JDK的NIO框架，做了本来该JDK做的事。

## 二、Netty 使用场景

对于说Netty的使用场景,倒不如说TCP、UDP 协议可以拿来做什么,因为本质上Netty也只是基于现有进行的封装。

而说起TCP,UDP能拿来做什么,倒不如说什么地方需要使用到通信,什么地方需要传输数据。

### 2.1协议

针对于**熟知的网络模型：**

![img](https://upload-images.jianshu.io/upload_images/7541336-b87f3c6f5235b56d.png?imageMogr2/auto-orient/strip|imageView2/2/format/webp)

现已支持协议有：HTTP、HTTPS、FTP、TFTP、SMTP、POP3等等这些协议的服务器，都是基于TCP、UDP进行传输，那么也就是代表都可以使用Netty特性进行高效、稳定开发。

### 2.2 分布式框架

服务端离不开的关键字：分布式、消息队列、RPC等等；摆脱不了的热门框架关键字：Dubbo、Zookeeper、RocketMQ等等

这些分布式框架，必然对于多个主机节点需要保持最基本的数据一致性，也就是一处改动，多处需要同步，而数据的可靠，高效同步便需要基于稳定的网络信息传输，基于这些诉求，Netty 便可以轻松胜任。

### 2.3 总结

总得来说，Netty的使用场景多为应用层的协议实现或是自定义的协议实现，在分布式当道的现在，可谓是屠龙宝刀。

下面我们快步进入Netty的学习正式环节。

## 三、Netty 入门项目-TIME协议服务器

这里采用官网的引导项目：时间服务器进行Netty的入门探索。

TimeServer：监听客户端连接，返回服务器时间，断开此次连接。

TimeClient：连接服务器，打印从服务器获得的时间，结束此次连接。

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

#### 3.2.1 Bootstrap(服务配置、启动相关)

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

@Slf4j
public class TimeServer {
    //服务端监听端口
    int port;

    public TimeServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        //Nio连接的线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();						
        //Nio处理消息的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();					//(1)
        try {
            //服务启动程序
            ServerBootstrap bootstrap = new ServerBootstrap();					//(2)
            //配置服务端启动程序  设置线程组
            bootstrap.group(bossGroup, workerGroup)							   //(3)
                    //配置通道为Nio的ServerSocket通道
                    .channel(NioServerSocketChannel.class)						//(4)
                    /* 这个配置是 Socket的标准参数，非Netty,下面是配置项
                     BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    */
                    .option(ChannelOption.SO_BACKLOG, 1024)						
                    /* 这个配置是 Socket的标准参数，非Netty,下面是配置项
                   是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true)				//(5)
                    //配置处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {		//(6)
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //给通道在最后加一个处理器
                            socketChannel.pipeline().addLast(new TimeServerHandler());	
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();					//(7)
            log.debug("start: 服务启动成功！");
            //等待服务端监听端口的关闭
            future.channel().closeFuture().sync();							   //(8)
        } finally {
            //退出，释放线程资源
            bossGroup.shutdownGracefully();									  //(9)
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TimeServer timeServer = new TimeServer(8080);
        timeServer.start();
    }
}
```

##### (1) 设置EvenLoop 线程组

因为这里是服务器的部分，所以使用了2个EventLoopGroup ，boosGroup负责处理客户端连接的Accept连接，workGroup负责我们的工作任务，至于为什么要采用这样的模式，这个模式也就是Reactor模式，后面会详细介绍Reactor模式。

##### (2) 创建Server的引导实例

这里比较简单，就是新建了一个服务端的Bootstamp实例，什么都没有做，具体的配置等都在下面。

##### (3) 配置BootStrap实例

这里使用的多个多个点的这种方式配置(.group  .channel等等)，每一个再换一行，这样阅读看上去比较清晰，也避免了多个set set的调用，让代码看起来比较多。

##### (4) 设置通道为Nio的服务器通道

这里设置服务端的通道类型，NioServerSocketChannel  通过名字可以了解是采用Nio的Server版本的SocketChannel

##### (5) 设置Socket参数

这里是设置Socket的参数，这个部分不是Netty的，而是对底层Socket的配置。

##### (6) 设置子Handler

##### (7) 绑定端口

##### (8) 等待服务器关闭

##### (9) 关闭线程组

#### 3.2.2 TimeServerHandler(服务器业务逻辑处理相关)

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

#### 3.3.1 BootStrap(引导、配置相关)

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
    //服务端ip
    String ip;
    //服务端端口号
    int port;

    public TimeClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void start() throws InterruptedException, IOException {
         //Nio处理消息的线程组
        EventLoopGroup group = new NioEventLoopGroup();								//(1)
        try {
             //客户端引导实例
            Bootstrap bootstrap = new Bootstrap();									//(2)
            // 配置客户端的引导实例  设置线程组
            bootstrap.group(group)											       //(3)
                	//设置通道类型为Nio的客户端Channel
                    .channel(NioSocketChannel.class)								//(4)
                    //配置socket
                    .option(ChannelOption.SO_KEEPALIVE, true)						 //(5)
                    //设置handler
                    .handler(new ChannelInitializer<SocketChannel>() {				  //(6)
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            //连接到远程节点，阻塞等待直到连接完成
            ChannelFuture future = bootstrap.connect(ip, port).sync();			      //(7)

            //阻塞，直到channel 关闭
            future.channel().closeFuture().sync();									//(8)
        } finally {
            //关闭线程池并释放所有资源
            group.shutdownGracefully();												//(9)
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        TimeClient client = new TimeClient("localhost", 8080);
        client.start();
    }
}
```

##### (1) 设置EvenLoop 线程组

##### (2) 创建引导实例

##### (3) 给引导设置group信息

##### (4) 设置通道为Nio的通道

##### (5) 设置Socket参数

##### (6) 设置Handler

##### (7) 连接远端

##### (8) 等待通道关闭

##### (9) 关闭线程组

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
            //打印时间
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

![image-20210512205708524](.\images\timeServer.png)

![image-20210512210008421](.\images\timeClient.png)

## 四、Netty 主要组件

### 4.1 主要组件

- Bootstrap && ServerBootstrap （引导、启动）
- EventLoop && EventLoopGroup  
- ByteBuf
- Channel
- ChannelHandler
- ChannelFuture
- ChannelPipeline
- ChannelHandlerContext

### 4.2 Bootstrap && ServerBootstrap

### 4.3 EventLoop && EventLoopGroup

<img src=".\images\eventloop" alt="image-20210513142047110" style="zoom: 67%;" />



## 五、Netty 项目-网络代理

