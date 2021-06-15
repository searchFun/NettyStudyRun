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

##  一、缓冲区类型

### 1. 缓冲区分类

我们都知道缓冲区，其实是一个调用者与被调用者间的一个缓冲部分，通过一次加载，而将后续可能用到的数据提前加载好，下次再调用者需要这份数据时，就可以直接获取，以提升效率。

我们在应用中所提及的缓冲其实就是指的将数据放置内存，根据JVM的不同内存管理策略，Netty也进行了相关的区分。

| 类型                             | 说明                                  | 优点                                                        | 不足                                                         |
| -------------------------------- | ------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------ |
| Heap ByteBuf（Java堆内存缓存区） | 内部就是java数组，存储在jvm的堆内存中 | **不使用池化情况下**，可以快速创建与释放                    | **写入底层传输通道之前**，都会复制到直接缓存区               |
| Direct ByteBuf（直接内存缓存区） | 内部数据存放在操作系统的物理内存中    | 可以获取超过jvm堆限制外的内存，**写入传输通道比堆内存更快** | 释放和分配空间的代价更高（系统调用），在Java中操作需要先复制到堆内存中 |
| Composite Buf（组合缓存区）      | 多个缓存区的组合表示                  | 方便一次操作多个缓冲区                                      |                                                              |

对于上述三种类型的缓冲区形成的原因，本也是因为多方面的权衡而提供的多种解决方案以灵活应对应用中的复杂需求。

> 如何区分：通过调用hasArray()，如果是Heap ByteBuf类型则返回true，否则false（false也不一定是Direct ByteBuf，而可能是Composite Buf）

### 2. Heap ByteBuf使用样例

因为是通过JVM进行管理的，所以对于Heap ByteBuf 可以直接通过array()方法获取数据

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

### 3. Direct ByteBuf使用样例

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

### 4. Composite ByteBuf使用样例

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

## 二、池化&&非池化（Pooled &&Unpooled）

***池***，很多地方有引用到这个概念，如**线程池**，**数据库连接池**等等，这些池出现的原因就是因为其对象的创建、销毁对系统有太多损耗，而提出来以空间换取时间的一种容器策略。

借鉴上述概念，理解ByteBuf的池化与非池化概念应该就比较简单了，对于池化ByteBuf来说，创建与释放交于ByteBuf池来进行管理，而对于非池化ByteBuf来说，其创建与释放交于JVM进行创建与释放内存。

### 1. Unpooled ByteBuf(非池化)

对于非池化的ByteBuf创建，Netty提供了一个比较直接的创建方式：

```java
ByteBuf buffer = Unpooled.buffer();
```

Unpooled源码：

```java
public final class Unpooled {
	
    private static final ByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;
    
    public static ByteBuf buffer() {
        return ALLOC.heapBuffer();
    }
}
```



