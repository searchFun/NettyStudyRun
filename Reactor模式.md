# Reactor模式与Netty的线程模型

## 一、 Reactor(反应器)模式

### 1. 传统Web服务器

如传统BIO：优点是比较直观，缺点是不适合并发量不大情况，因为线程会占用一定的系统资源，包括内存与线程切换开销。



![img](C:\Users\10091\IdeaProjects\NettyStart\images\thread-based)

**2.Reactor模式下服务器**

根据接受到的不同事件进行事件分离（dispatch），Accept，Connect，Read，Write

![img](C:\Users\10091\IdeaProjects\NettyStart\images\reactor1)

这