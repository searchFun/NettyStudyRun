# ByteBuf

在NIO中，有**ByteBuffer**类进行缓冲，而在Netty中，针对ByteBuffer的诸多缺点上，Netty实现了自己的buffer。

在使用**ByteBuffer**我们需要实时关注读写的位置，**读写模式的切换也需要我们手动调用函数进行切换**，对于JDK来说，提供的方法可能更加灵活，可以更好的被上层使用者（框架）更好的使用与封装，但是对于普通程序高效率的开发来说，需要实时注意读写位置，否则便会出现问题，这样的要求对于开发人员来说，无异于是一种负担，对此，Netty实现了自己的解决方案。

当然上述只是实现Netty的一个原因，相比于Java Nio来说，Netty还有如下优势：

- 不需要调用flip()进行读写模式切换
- 读、写索引分开
- 可以链式调用
- Pooling池化（在代码通信编程中，少不了对buffer的创建，释放，而频繁的创建对象，释放内存，这无异于会提升系统的开销，通过池化，将暂不使用的对象放进去，而下次在需要的时候就直接取出来使用就行了，**这与线程池，数据库连接池类似，是一种内存换时间的策略**）
- 可以进行引用计数，方便重复使用（类似于JVM的垃圾回收机制，对buffer对象进行标记引用）
- 可以自定义缓冲区类型
- 复合缓冲区类型，支持零复制

具体ByteBuf的优势我们接下来进行分析


## 一、ByteBuf的属性

ByteBuf 内部数组结构如下:

![image-20210616110155509](C:\Users\10091\AppData\Roaming\Typora\typora-user-images\image-20210616110155509.png)

### （一） 逻辑划分

- **第一部分：** 表示已经使用完的废弃的无效字节

- **第二部分：** 表示保存在ByteBuf中的有效数据，从ByteBuf中读取到的数据就是这里

- **第三部分：** 表示可写字节，写入到ByteBuf中的数据就会保存在这里

- **第四部分：** 表示该ByteBuf最多还可以扩容的大小

### （二）重要属性

- **readerIndex : ** 表示读指针的起始位置，每读取一个字节，自动加1，**当readerIndex == writerIndex的时候，表示当前ByteBuf已经不可读了**
- **writerIndex :**  表示写指针的起始位置，每写入一个字节，自动加1，**当writerIndex == capacity的时候，表示当前ByteBuf已经不可写了，进行扩容。**
- **capacity :** 表示当前容量，也就是边界，真实的数据，当等于writerIndex时候，表示当前容量已满需要扩容。
- **maxCapacity :** 表示ByteBuf可以扩容的最大容量，当向ByteBuf写入数据容量不足，进行扩容，如果扩容容量超过maxCapacity，则会报错

## 二、ByteBuf的使用

对于ByteBuf的普遍使用，简单分为下面三类。容器类、写入类、读取类。

### （一）容量类

- capacity() : 表示该ByteBuf容量，废弃、可读、可写三部分容量字节数和
- maxCapacity() : 表示扩容最大字节数

### （二）写入类

- isWritable()：表示当前是否可写，即writerIndex > capacity，如果返回false，并不代表不能再写，因为会自动扩容
- writableBytes()：获取可写字节数，等于capacity - writerIndex
- maxWritableBytes()：获取最大可写字节数，等于maxCapacity - writerIndex
- writeBytes(byte[] src)：把src数组字节全部写入ByteBuf
- writeTYPE(TYPE value)：写入基础类型，writerIndex会根据类型调整如写入int后，writerIndex会+4
- setTYPE(TYPE value)：写入基础类型，但是不会改变writerIndex
- markWriterIndex()/resetWriterIndex()：mark表示记录当前writerIndex，reset表示将当前writerIndex恢复至mark时的值，一般成对使用

> 这里的**TYPE**包含基础类型：
>
> byte、boolean、char、short、int、long、float、double

### （三）读取类

- isReadable()：表示当前是否可读，即readerIndex < writerIndex
- readableBytes()：表示当前可读字节数，等于writerIndex - readerIndex
- readBytes(byte[] dest)：将ByteBuf中的数据读取至dest数组。
- readTYPE()：读取基本类型数据，readerIndex会根据类型调整如写入int后，readerIndex会+4
- getTYPE()：读取基本类型数据，但是不会改吧readerIndex
- markReaderIndex()/resetReaderIndex()：mark表示记录当前readerIndex，reset表示将当前readerIndex恢复至mark时的值，一般成对使用

## 三、ByteBuf 的创建

在Netty中ByteBuf不使用new 方式进行创建，而是通过ByteBufAllocator进行缓存区的创建与分配内存空间。

### （一）缓冲区类型（分配内存所在位置）

#### 1. 缓冲区分类

我们都知道缓冲区，其实是一个调用者与被调用者间的一个缓冲部分，通过一次加载，而将后续可能用到的数据提前加载好，下次再调用者需要这份数据时，就可以直接获取，以提升效率。

我们在应用中所提及的缓冲其实就是指的将数据放置内存，根据JVM的不同内存管理策略，Netty也进行了相关的区分。

| 类型                             | 说明                                  | 优点                                                        | 不足                                                         |
| -------------------------------- | ------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------ |
| Heap ByteBuf（Java堆内存缓存区） | 内部就是java数组，存储在jvm的堆内存中 | **不使用池化情况下**，可以快速创建与释放                    | **写入底层传输通道之前**，都会复制到直接缓存区               |
| Direct ByteBuf（直接内存缓存区） | 内部数据存放在操作系统的物理内存中    | 可以获取超过jvm堆限制外的内存，**写入传输通道比堆内存更快** | 释放和分配空间的代价更高（系统调用），在Java中操作需要先复制到堆内存中 |
| Composite Buf（组合缓存区）      | 多个缓存区的组合表示                  | 方便一次操作多个缓冲区                                      |                                                              |

**堆内存类型操作数据**

![img](C:\Users\10091\IdeaProjects\NettyStart\images\heap)

**直接内存操作数据**

![img](C:\Users\10091\IdeaProjects\NettyStart\images\nio)


> 如何区分：通过调用hasArray()，如果是Heap ByteBuf类型则返回true，否则false（false也不一定是Direct ByteBuf，而可能是Composite Buf）

2. Heap ByteBuf使用样例

因为是通过JVM进行管理创建的，所以对于Heap ByteBuf 可以直接通过array()方法获取数据，hasArray()方法返回true

```java
public class ByteBufTypeTest {
    @Test
    public void heapByteBuf() {
        ByteBuf heapBuf = ByteBufAllocator.DEFAULT.buffer();
        heapBuf.writeBytes("这里是JVM 堆内存".getBytes(StandardCharsets.UTF_8));
        if (heapBuf.hasArray()) {
            byte[] array = heapBuf.array();
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            int length = heapBuf.readableBytes();
            System.out.println(new String(array,offset,length));
            heapBuf.release();
        }
    }
}
=====================================================输出==============================================================
这里是JVM 堆内存
```

#### 3. Direct ByteBuf使用样例

因为是操作系统物理内存，而Java程序对其进行操作都需要进行复制一份至Jvm堆内存，所以直接通过array()是不能获取到的，而对于hasArray()方法返回的也是false，**获取数据需要通过getBytes()**

```java
public class ByteBufTypeTest {
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
}
=====================================================输出==============================================================
这里是直接内存
```

#### 4. Composite ByteBuf使用样例

组合缓存区可通过addComponent()/addComponents()进行添加Heap 、Direct 类型的ByteBuf，进行统一处理。

下面样例为当一个http请求过长，body需要分段发送情况下，复用header情况。

```java
public class ByteBufTypeTest {
    @Test
    public void compositeByteBuf() {
        CompositeByteBuf httpByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        //复用header
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

=====================================================输出==============================================================
我这里是Http消息头
这里是Http消息体1
我这里是Http消息头
这里是Http消息体2
```

#### 5. 小结

对于我们所需要的ByteBuf其所在内存位置可以通过调用分配器的不同方法进行创建

Heap（Jvm堆内存）: 分配器.buffer()方法

Direct（操作系统物理内存）：分配器.directBuffer()方法

Composite （组合）:分配器.compositeBuffer()方法

### （二）池化&&非池化（Pooled &&Unpooled）（分配内存方式）

***池***，很多地方有引用到这个概念，如**线程池**，**数据库连接池**等等，这些池出现的原因就是因为其对象的创建、销毁对系统有太多损耗，而提出来以空间换取时间的一种容器策略。

借鉴上述概念，理解ByteBuf的池化与非池化概念应该就比较简单了，对于池化ByteBuf来说，创建与释放交于ByteBuf池来进行管理，而对于非池化ByteBuf来说，其创建与释放交于JVM进行创建与释放内存。

> Netty对于不同缓存区类型(Heap、Direct、Composite )都可以通过池化(Pooled)与非池化(Unpooled)进行创建与分配。

#### 1.ByteBufAllocator分配器

Netty中提供了两种实现，**PooledByteBufAllocator**（池化），**UnpolledByteBufAllocator**（非池化）

<img src="C:\Users\10091\IdeaProjects\NettyStart\images\image-20210616103105506.png" alt="image-20210616103105506" style="zoom: 80%;" />

***PooledByteBufAllocator:***(池化)

将ByteBuf实例放入池中，提高了性能，将内存碎片减少到最小；池化分配器采用了jemalloc高效内存分配策略，该策略被好几种现代操作系统所采用。

***UnpooledByteBufAllocator:***(非池化)

普通的分配器，每次调用都会返回一个新的ByteBuf实例，通过Jvm进行垃圾回收管理。

> 在Netty中，默认的分配器为ByteBufAllocator.DEFAULT，可以通过Java系统参数选项: io.netty.allocator.type进行配置："unpooled"，"pooled"。
>
> 另外可以在设置启动器Bootstrap的时候，进行设置
>
> ```java
>  ServerBootstrap bootstrap = new ServerBootstrap();
>  bootstrap.option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
>           .childOption(ChannelOption.ALLOCATOR,UnpooledByteBufAllocator.DEFAULT);
> ```

#### 2. Pooled ByteBuf(池化)

对于池化的ByteBuf创建，可以通过池化分配器进行创建

```java
ByteBuf pooledBuf = PooledByteBufAllocator.DEFAULT.buffer();
```

#### 3. Unpooled ByteBuf(非池化)

对于非池化的ByteBuf创建，可以通过非池化分配器进行创建

```java
ByteBuf unPooledBuf = UnpooledByteBufAllocator.DEFAULT.buffer();
```

### （三）总结

根据缓冲区的**内存位置**，分为了堆内存、直接内存、混合

根据缓冲区的**创建方式**，分为了池化与非池化

通过选择内置ByteBufAllocator分配器，选择池化（PooledByteBufAllocator）还是非池化（UnpooledByteBufAllocator），调用buffer()、directBuffer()和compositeBuffer()方法选择缓冲区内存所在位置进行创建ByteBuf。

## 四、ByteBuf的回收

### （一）引用记数法

Netty实现了自己对ByteBuf的回收管理，方式与JVM管理对象类似，采用引用记数法记录ByteBuf的生命周期，能够更快发现那些非池化的ByteBuf，以便提升ByteBuf的回收与销毁效率。

***ByteBuf计数法规则：***

1. 刚创建的ByteBuf，其引用为1
2. 每次调用retain()方法，引用+1
3. 调用release()方法，引用-1
4. 如果引用为0，再次访问，则会抛出异常
5. 如果引用为0，表示这个ByteBuf没有被使用，则它占用的内存需要被回收

> **关于retian()/release():**
>
> 1. 在Netty的业务处理器(handler)使用中，retain()/release()方法应当成对使用，调用了retain()，就应当调用一次release()。
>
> 2. 如果这两个方法一次都不调用，则会由流水线的最后一个Handler进行调用release()释放缓冲区内存

### （二）回收策略

#### **1.池化的ByteBuf**

放入可重新分配的ByteBuf池，等待下一次分配。

#### **2.非池化的ByteBuf**

**Heap类型：**由JVM的垃圾回收机制回收

**Direct类型：**调用本地方法释放内存(unsafe.freeMemory)

### （三）入站消息自动回收

#### 1.TailHanlder

Netty默认会在ChannelPipline通道流水线最后添加一个TailHanler末尾处理器，它就实现了这些内存的释放。

如果所有入站InboundHandler都将ByteBuf数据包一直向下传递，那么就会由TailHandler进行释放。

> 如何让ByteBuf数据包一直向下传？
>
> 对于我们自定义实现的handler业务处理器，调用父类的channelRead，即可完成向后传递msg
>
> ```java
> public class DemoInboundHandler extends ChannelHandlerAdapter {
>     @Override
>     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
>         ByteBuf buf= (ByteBuf) msg;
>         //...业务处理
>         //调用父类的入站方法，将msg向后传递
>         super.channelRead(ctx, msg);
>     }
> }
> ```

#### 2.SimpleChannelInboundHandler

如果Handler需要截断流水线的处理过程，不再继续向后传递ByteBuf，这时流失线末端的TailHandler就失效了，

这种情况下，有如下两个选择

- 手动调用release()
- 继承**SimpleChannelInboundHandler**，业务代码放在**messageReceived**（原channelRead0）中

下面是SimpleChannelInboundHandler.class源码：

```java
 @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;
        try {
            if (acceptInboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I imsg = (I) msg;
                //这里是继承实现的方法
                messageReceived(ctx, imsg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            //自动释放
            if (autoRelease && release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }
```

可以看到，完成我们自己实现的messageReceived业务处理代码后，会在finally中完成释放

### （四）出站消息自动回收

对于出站消息的ByteBuf，通常是由Handler创建的，通过调用ctx.writeAndFlush(ByteBuf msg)，ByteBuf将会写入的出站处理的流水线，当到最后一个出站处理器，HeadHandler发送消息完成后，会进行释放一次，如果计数为0，将会被回收。

