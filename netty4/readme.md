##Dubbo Netty4通信层插件
由于dubbo官方当前版本是支持Netty3的，没有对Netty4的支持，要想使用Netty4的dubbo，只能使用dubbox，但是需要
将dubbo的整体都替换成dubbox，这样很不方便，于是就基于dubbo的extension方式实现了一个netty4的插件，不需要
对你当前的项目过多的调整只需要引入插件的jar以及加入相关配置即可。

##引用插件的jar包
    <!-- lang:xml-->
    <dependency>
         <groupId>net.dubboclub</groupId>
         <artifactId>netty4</artifactId>
         <version>0.0.2</version>
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

这样就对当前应用所有发布的服务都走了netty4通信

同样也可以采用dubbo.properties进行配置
添加 dubbo.provider.server=netty4即可。

