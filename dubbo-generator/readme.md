## dubbo客户端自动生成器
在大部分业务场景和产线部署的时候会发现要引用一个dubbo远程服务只需要进行如下的配置即可：

    <!--lang:xml-->
    <dubbo:reference interface="com.xxx.xxx.xxxFacade" id="clientId" />

发现在原有的系统代码里面会多出一个spring的xml配置，容易让人误解dubbo和spring之间存在不可拆分的关系
所以我这里要做的就是把dubbo从spring中剥离出来，提供一种更加便捷的方式获取远程服务。同时提供更加便捷的方式对远程服务的增强
提供AOP的方式切入的服务的具体执行中。

## Getting start

### 引入工具包
    
     <!--lang:xml-->
     <dependency>
         <groupId>net.dubboclub</groupId>
         <artifactId>dubbo-generator</artifactId>
         <version>${dubboclub.generator.version}</version>
      </dependency>
### 引入远程服务新方式

#### 延迟加载方式
    <!--lang:java-->
    ClientFacade client = DubboClientWrapper.getWrapper(ClientFacade.class);
    ClientFacade client = DubboClientWrapper.getWrapper(ClientFacade.class, InvokeHandler.class);
    ClientFacade client = DubboClientWrapper.getWrapper(ClientFacade.class,"clientId");
    ClientFacade client = DubboClientWrapper.getWrapper(ClientFacade.class,"clientId",InvokeHandler.class);
    
DubboClientWrapper提供了上面四种方式获取dubbo远程服务。其中"clientId"这是指定该服务的id(和通过spring的<dubbo:reference id=""/> 中id一样)，InvokeHandler(这是一个接口，必须传入这个类的实现类)是指定对这个client的增强类。
clientId的默认值是"[FacadeClassFullName]",InvokeHandler的默认值是DefaultInvokeHandler,该handler只进行简单的请求日志记录，记录在logger name为DUBBO_INTEGRATION的日志中。

#### 预加载
上面的方式是在对某个Client第一次使用的时候才会加载这个远程服务，并且创建对应的服务实体。这样可能对于第一次请求的时候效率上会有影响。也可以通过该配置spring的方式来预先加载

    <!--lang:xml-->
       <bean class="net.dubboclub.dubbogenerator.InitializingFacade">
            <property name="clientClassList">
                <list>
                    <value>com.xx.xx.ClientFacade</value>
                </list>
            </property>
        </bean>
        
可以通过上面的方式先告知FacadeInitialization需要预先加载的ClientFacade，那么FacadeInitialization会在spring初始化阶段（一般是项目启动阶段）初始化这些服务。
通过上面方式注入，对于引用服务可以通过两种方式来引用dubbo的远程服务。第一种还是通过DubboClientWrapper的静态方法获取对应远程服务实体。也可以通过spring的@Atowire注解
需要注意的是，这种预加载，并没地方设置clientid以及invokeHandler，此处全部采用默认的设置方式，当然也提供其他设置途径，下面会进行介绍。

## InvokeHandler设置
某个InvokeHandler类只会实例化一次，每个dubbo的客户端可以设置不同的InvokeHandler以满足不同接口不同的需求。如果设置呢？

### 全局InvokeHandler设置
全局的设置可以通过`DubboClientWrapper.setDefaultHandler(Class<? extends InvokeHandler> handlerClass)`设置全局的默认handler，该方法只能调用一次，如果调用两次则会出现异常。
除了上面编程方式指定默认全局的handler，还可以通过在dubbo.properties中通过配置`dubbo.wrapper.default.handler`参数配置Handler的类全名来指定全局默认的handler。
如果通过dubbo.properties配置了`dubbo.wrapper.default.handler`这个参数，也通过`DubboClientWrapper.setDefaultHandler(Class<? extends InvokeHandler> handlerClass)`
方法设置了默认handler，最终是以调用`DubboClientWrapper.setDefaultHandler`为准，在dubbo.properties则会被覆盖。

### 给某个dubbo接口指定Handler
这里同样也提供两种方式，第一种方式则是上面延迟加载方式通过调用`DubboClientWrapper.getWrapper`带有InvokeHandler参数的方法来给某个client指定某一个handler。

同样也可以通过在dubbo.properties中通过配置来指定。
通过配置dubbo.wrapper.{clientId}.handler参数配置Handler的类全名，其中clientId默认是"[facadeclassfullname]"。

## 最佳实践
比如现在有一个net.dubboclub.QueryUserInfoFacade接口

    <!--lang:java-->
    <bean class="net.dubboclub.dubbogenerator.InitializingFacade">
        <property name="clientClassList">
            <list>
                <value>net.dubboclub.QueryUserInfoFacade</value>
            </list>
         </property>
    </bean>
    
    #dubbo.properties中的配置
    #设置全局的handler
    dubbo.wrapper.default.handler=net.dubboclub.MobileDefaultInvokeHandler
    #给QueryUserInfoFacade制定某个具体的handler
    dubbo.wrapper.[net.dubboclub.QueryUserInfoFacade].handler=net.dubboclub.QueryUserInfoFacadeInvokeHandler

    //引用远程服务
    QueryUserInfoFacade client = DubboClientWrapper.getWrapper(QueryUserInfoFacade.class);//或者@AutoWire QueryUserInfoFacade queryUserInfoFacade






