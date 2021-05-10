# Netty入门

## 1.Netty 背景介绍

### 1.1 发展历史

## 1.2 为什么要使用Netty

## 2.Netty 使用场景

## 3.Netty 主要架构，流程

### 3.1 架构

### 3.2 流程

### 3.3 为什么这么设计？

## 4.Netty 入门项目-Echo服务器

### 4.1 引入Netty依赖

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

### 4.2创建EchoServer

#### 4.2.1 BootStrap(服务配置、启动相关)

=====[**pom.xml**]=====

#### 4.2.2 EchoServerHandler(服务业务处理相关)

### 4.3创建EchoClient







## 5.Netty 项目-网络代理

