#Dubbo Restful插件文档说明

##目的
dubbo的扩展版本dubbox有支持restful，但是它对dubbo框架整体改动比较大，导致使用了dubbox需要把整个dubbo框架
进行升级替换，个人觉得这个不符合dubbo的微内核目的，于是就做了一个restful插件，主要是扩展了`Protocol`接口来实现
restful相关内容。

##将项目支持Restful

只需要依赖本插件，那么代码层面就不需要任何改动，唯一需要变动的地方就是对dubbo配置进行变更

###通过properties文件来配置restful

在服务提供端配置`dubbo.protocol.name=restful`，那么就告诉dubbo框架需要通过restful协议来发布服务如果只存在这个配置，那么就会默认采用jetty以及端口为8080来启动一个java进程，并且此时的`contextpath`为`/`
当然可以通过其他参数来指定启动方式以及调整端口和`contextpath`

`dubbo.protocol.server=jetty/servlet`其中`servlet`是将restful协议整合到你项目当前的servlet容器中

针对servlet容器，需要在你项目的web.xml中配置dubbo的`DispatcherServlet`的servlet，这部分配置可以参考dubbo官方的`webservice`配置

###通过xml来配置restful

通过`<dubbo:protocol name='restful' server='jetty/servlet' contextpath='xxxx'/>`来配置


###关于contextpath配置

如果熟悉dubbo的同学或者踩过坑的通讯，知道在webservice并且采用servlet发布服务，会有一个contextpath的bug，为了避免这个问题

需要你们在dubbo.properties里面添加dubbo.protocol.restful.contextpath配置来告知restful插件你当前的contextpath的值

##查看所有发布的服务信息

访问：http://ip:port/contextpath/services

##服务调用
###服务调用地址：
restful插件是按照:`http://ip:port/contextpath/${path}[/${method}][/${version}][/${group}]`
其中`method`和`version`以及`group`可以不用在路径上体现，可以在请求报文
###服务调用报文
整个报文都是json格式，格式如下
`{
method:"请求的方法",
version:"请求的版本",
group:"请求的服务分组",
arg1:"方法第一个参数值，如果是对象，那么就是json对象",
arg2:"方法第二个参数值",
.....
argn:"第n个参数值"
}`

###注意：
`method`,`version`和`group`必须至少在上面报文或者请求路径上出现一次，如果两个地方都出现，那么路径上出现的信息是最终结果

如果默认没有对`version`和`group`配置，那么对应的值为`all`

其中`path`可以是配置的`<dubbo:service path='xxx'`path属性，也可以是接口类全名，可以是类名

比如接口com.dubboclub.RestfulService有方法helloWorld，该接口的某个服务实现没有配置版本和分组,配置的path属性为`restfulService`

可以通过`http://ip:port/contextpath/com.dubboclub.RestfulService/helloWorld/all/all`或者`http://ip:port/contextpath/restfulService/helloWorld/all/all`或者`http://ip:port/contextpath/RestfulService/helloWorld/all/all`访问！
