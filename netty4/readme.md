##Dubbo Netty4通信层插件
由于dubbo官方当前版本是支持Netty3的，没有对Netty4的支持，要想使用Netty4的dubbo，只能使用dubbox，但是需要
将dubbo的整体都替换成dubbox，这样很不方便，于是就基于dubbo的extension方式实现了一个netty4的插件，不需要
对你当前的项目过多的调整只需要引入插件的jar以及加入相关配置即可。

##引用插件的jar包
    <!-- lang:xml-->
    <dependency>
         <groupId>net.dubboclub</groupId>
         <artifactId>netty4</artifactId>
         <version>0.0.4</version>
    </dependency>

##客户端加入Netty4插件
可以在dubbo的xml中对标签dubbo:reference 添加client属性，值为netty4

    <!-- lang:xml-->
    <dubbo:reference client="netty4" ..../>

上面这种方式需要在定于服务标签都加上对应的属性，这样可能比较麻烦，也可以在dubbo.properties文件中添加

dubbo.reference.client=netty4

这样就对所有的订阅服务都走了netty4的通信了。

##服务端加入Netty4插件

同样可以通过xml进行配置,在标签dubbo:provider添加server属性，值为netty4

    <!-- lang:xml-->
    <dubbo:provider server="netty4"..../>
    <dubbo:provider transporter="netty4".../>
    <dubbo:protocol transporter="netty4".../>


这样就对当前应用所有发布的服务都走了netty4通信

同样也可以采用dubbo.properties进行配置
添加 dubbo.provider.server[transporter]=netty4或者dubbo.provider.transporter=netty4即可。

> **注意：**对于服务端开启netty4，需要注意一点，通过上面配置知道开启方式可以通过配置server参数和transporter参数，合适配置server，那么合适配置transporter呢？
> 加入你当前系统中所有子应用都全体切换成netty4，那么可以随便配置一个即可，如果只是部分系统切换了，就需要注意，因为通过server配置发布的服务会将这个参数带到客户端，并且客户端会获取这个参数，
> 除非客户端通过上面的方式设置了client的类型，否则将会把这个server参数来进行远程通信。如果此时客户端并没有引用netty4插件的包，会导致连接失败，导致服务不可用，
> 而transporter参数并不会被客户端使用。所以这里需要特别注意。

##和Netty3压测对比数据报告

下面测试是每个线程连续调用1000次得到的数据

![](https://raw.githubusercontent.com/dubboclub/dubbo-plus/master/netty4/performance-test-report/1kb.png)


![](https://raw.githubusercontent.com/dubboclub/dubbo-plus/master/netty4/performance-test-report/2kb.png)


![](https://raw.githubusercontent.com/dubboclub/dubbo-plus/master/netty4/performance-test-report/4kb.png)


![](https://raw.githubusercontent.com/dubboclub/dubbo-plus/master/netty4/performance-test-report/8kb.png)

硬件配置：

CPU:Intel(R) Core(TM) i7-4702MQ CPU @2.20GHz 2.20GHz

内存:8GB