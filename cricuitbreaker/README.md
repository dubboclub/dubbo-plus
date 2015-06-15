#服务降级方案实现
##当前情况
Dubbo支持服务降级，并且支持当服务出现异常的时候进行服务降级处理，但是存在一下几个缺陷


1. 当调用远程服务出真的宕机的时候，Dubbo依然会重试调用一次远程服务，再尝试进行服务降级，这样就浪费了一次没必要的远程请求<br/>
2. Dubbo支持强制性的服务降级，就是不进行远程调用就进行服务降级处理，但是每次降级处理的上线和下线都需要人为的去干涉<br/>


针对上面两个问题，我们需要达到的目的是：当远程服务真的出现宕机的时候，消费端不要再进行远程调用，而是直接降级处理，并且消费端能够定时的检查服务是否恢复，从而自动下线降级服务，恢复调用远程服务。

##具体实现
通过Dubbo的Filter对Dubbo进行扩展，从而使得每次服务发起调用都可以得到监控，从而可以监控每次服务的调用。

**对自动判断服务提供端是否宕机**：通过一个记录器对每个方法出现RPC异常进行记录，并且可以配置在某个时间段内连续出现都少个异常可判定为服务提供端出现了宕机，从而进行服务降级。

**自动恢复远程服务调用**：通过配置检查服务的频率来达到定时检查远程服务是否可用，从而去除服务降级。

##降级相关配置
降级配置分配为应用级别，接口级别，方法级别
###一、应用级别
    <!--lang:java-->
    dubbo.reference.default.break.limit：该参数是配置一个方法在指定时间内出现多少个异常则判断为服务提供方宕机
    dubbo.reference.default.retry.frequency：该参数配置重试频率，比如配置100，则表示没出现一百次异常则尝试一下远程服务是否可用
    dubbo.reference.circuit.break:服务降级功能开关，默认是false，表示关闭状态，可以配置为true
###二、接口级别
    <!--lang:java-->
    dubbo.reference.${fullinterfacename}.break.limit：同上面dubbo.reference.default-break-limit，指定某个接口
    dubbo.reference.${fullinterfacename}.retry.frequency:同上面
    dubbo.reference.${fullinterfacename}.circuit.break:服务降级功能开关，默认是false，表示关闭状态，可以配置为true
###三、方法级别
    <!--lang:java-->
    dubbo.reference.${fullinterfacename}.${methodName}.break.limit：同上面dubbo.reference.default-break-limit，指定某个接口的某个方法
    dubbo.reference.${fullinterfacename}.${methodName}.retry.frequency:同上面dubbo.reference.default-retry-frequency，指定某个接口的某个方法
    dubbo.reference.${fullinterfacename}.${methodName}.circuit.break:服务降级功能开关，默认是false，表示关闭状态，可以配置为true
    
上面这些参数均是配置在dubbo.properties中

dubbo.properties具体在哪里，默认是在classpath根目录，也可以通过-Ddubbo.properties.file来指定该文件路径。



##降级服务实现
默认情况的服务降级是直接返回一个异常，也可以在消费端对某个远程接口提供一个默认实现，作为降级方案。

具体规则如下：

例如当前远程提供一个接口com.foo.FooService的实现，如何在消费端提供一个降级实现，作为降级方案呢？只需要在消费端创建一个类com.foo.FooServiceCircuitBreak并且该类实现com.foo.FooService接口，那么当调用远程com.foo.FooService接口实现某个方法出现异常并且满足了该方法降级规则（通过上面配置的参数进行判断）那么就会调用com.foo.FooServiceCircuitBreak类进行服务降级处理，而不是返回一个异常。






