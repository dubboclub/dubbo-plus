##插件概述
该插件主要是完善Dubbo缓存方案，由于Dubbo提供的默认缓存方案比较基础，只是提供了简单的本地缓存方案，而不具备和第三方
缓存组件集成。本插件通过扩展实现了Dubbo的`Cache`和`CacheFactory`接口，和Redis,Memcached以及本地缓存组件Ehcache
进行集成。
##如何引用缓存
对于dubbo的配置之前写过一篇比较详细的内容进行介绍，这里不在对主要其内容进行重复介绍，如要了解可以到http://bbs.dubboclub.net/read-4.html去了解
这里只对如何引用缓存进行介绍。
###通过XML标签配置
    <!--lang:xml-->
    <!--表示服务提供端对所有暴露的服务都添加缓存功能-->
    <dubbo:provider cache="${cacheKey}"/>
    <!--表示服务消费端对所有订阅的服务都添加缓存功能-->
    <dubbo:consumer cache="${cacheKey}"/>
    <!--表示服务服务端对指定的服务添加缓存功能-->
    <dubbo:service ... cache="${cacheKey}"/>
    <!--表示服务消费端对指定的服务添加缓存功能-->
    <dubbo:reference ... cache="${cacheKey}"/>
     <!--表示对某个服务内的某个方法添加缓存功能-->
    <dubbo:method ... cache="${cacheKey}"/>
    
###通过Properties文件配置
    <!--lang:java-->
    #同<dubbo:provider..
    dubbo.service.cache=${cacheKey}
    #同<dubbo:consumer..
    dubbo.reference.cache=${cacheKey}
    #同<dubbo:service..
    dubbo.service.${beanId}.cache=${cacheKey}
    #同<dubbo:reference..
    dubbo.reference.${beanId}.cache=${cacheKey}

###补充
上面配置内容中都有一个${cacheKey}这个只是一个占位符，具体配置的是填入你想用的缓存组件即可，那么问题来了，dubbo提供哪些缓存实现呢？

####dubbo默认提供的缓存实现
       >threadlocal：它的实现类是ThreadLocalCacheFactory，一看就知道是一个线程级别的缓存了
       >lru：它的实现类是LruCacheFactory，是一个基于本地内存的LRU算法的缓存
       >jcache：它的实现类是JCacheFactory，是一个基于JCache的缓存实现

####当前cache插件提供哪些实现
        >redis：它的实现类是RedisCacheFactory，是集成了Redis的缓存实现
        >memcached：它的实现类是MCCacheFactory，是集成了Memcached缓存实现
        >ehcache：它的实现类是EhCacheFactory，是集成了Ehcache来做本地缓存实现
        >mixcache：它的实现类是MixCacheFactory，是一种组合缓存方案，可以从上面提供的几种缓存实现选择两种来实现一个二级缓存方案
如果你的项目引用了cache插件，那么${cacheKey}的可选值是threadlocal,lru,jcache,redis,memcached,ehcache,mixcache

##如何使用该缓存插件
该插件提供了四种缓存实现，那么针对这四种缓存实现需要做哪些配置呢？
###配置信息加载途径
该插件对缓存配置加载提供了三种途径
####第一种、集成到dubbo.properties配置文件中
####第二种、通过在dubbo.properties配置cache.properties.file属性来指明缓存配置文件路径，文件路径配置可以是:file:C:/cache/cache.properties文件系统格式，或者是:classpath:cache.properties的classpath路径
####第三种、通过-Dcache.properties.file方式来指定缓存配置文件存放路径

**下面所介绍的配置信息均是配置在上述配置文件中**

###使用Redis缓存需要配置的信息
    
    <!--lang-->
    cache.redis.connect    #【必填】配置redis连接地址，ip:port格式，如果是redis集群，可以是ip1:port1,ip2:port2....ipn:portn通过英文逗号将各个连接地址分开
    cache.redis.max.total  #【选填】配置redis客户端连接池最大连接数，默认8
    cache.redis.max.idle   #【选填】配置redis客户端连接池最大空闲连接数，默认8
    cache.redis.min.idle   #【选填】配置redis客户端连接池最小空闲连接数，默认0
    cache.redis.max.wait.millis  #【选填】配置redis客户端最大等待时间，单位毫秒,默认-1，表示无限等待
    cache.redis.min.evictable.idle.time.millis  #【选填】 默认值1000L * 60L * 30L
    cache.redis.soft.min.evictable.idle.time.millis #【选填】 默认值1000L * 60L * 30L
    cache.redis.num.tests.per.eviction.run   #【选填】 默认值3
    cache.redis.eviction.policy.class.name  #【选填】 默认值org.apache.commons.pool2.impl.DefaultEvictionPolicy
    cache.redis.test.on.borrow #【选填】 默认值false
    cache.redis.test.on.return #【选填】 默认值false
    cache.redis.test.while.idle #【选填】 默认值false
    cache.redis.time.between.eviction.runs.millis #【选填】 默认值-1L
    cache.redis.block.when.exhausted #【选填】 默认值true
    cache.redis.jmx.enabled #【选填】 默认值true
    cache.redis.jmx.name.prefix #【选填】 默认值pool
    
###使用memcached缓存需要配置的信息
    <!--lang-->
    cache.memcached.connect    #【必填】配置redis连接地址，ip:port格式，如果是redis集群，可以是ip1:port1,ip2:port2....ipn:portn通过英文逗号将各个连接地址分开
    cache.memcached.connect.timeout #【选填】 默认值60000
    cache.memcached.connection.pool.size #【选填】 默认值1
    cache.memcached.failure.mode #【选填】 默认值false
    cache.memcached.max.queued.no.repl.operations #【选填】 默认值40000或者(int) (40000 * (Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0))
    cache.memcached.session.locator #【选填】默认default，对应ArrayMemcachedSessionLocator，可选值：ketama对应KetamaMemcachedSessionLocator，election对应ElectionMemcachedSessionLocator，libmemcached对应LibmemcachedMemcachedSessionLocator，php对应PHPMemcacheSessionLocator
    cache.memcached.session.read.buffer.size #【选填】默认32 * 1024
    cache.memcached.so.timeout #【选填】默认0
    cache.memcached.write.thread.count #【选填】默认0
    cache.memcached.statistics.server #【选填】默认false
    cache.memcached.handle.read.write.concurrently #【选填】默认true
    cache.memcached.dispatch.message.thread.count #【选填】默认0
    cache.memcached.read.thread.count #【选填】默认1
    cache.memcached.check.session.timeout.interval  #【选填】默认1000l

###使用ehcache缓存需要配置信息
对于ehcache的配置除了上面配置外，由于ehcache自己有一套配置文件，所以也支持通过它自身的xml配置文件来配置
####通过properties来配置
    <!--lang-->
    cache.ehcache.update.check 
    cache.ehcache.default.transaction.timeout.in.seconds
    cache.ehcache.dynamic.config
    cache.ehcache.max.bytes.local.heap
    cache.ehcache.max.bytes.local.off.heap
    cache.ehcache.max.bytes.local.disk
    cache.ehcache.cache.loader.timeout.millis #默认值0
    cache.ehcache.max.elements.on.disk #默认值10000000
    cache.ehcache.max.entries.in.cache #默认值0
    cache.ehcache.clear.on.flush #默认值true
    cache.ehcache.eternal #默认值false
    cache.ehcache.time.to.idle.seconds #默认值120
    cache.ehcache.time.to.live.seconds #默认值120
    cache.ehcache.disk.spool.buffer.size.m.b #默认值30
    cache.ehcache.disk.access.stripes #默认值1
    cache.ehcache.disk.expiry.thread.interval.seconds #默认值120
    cache.ehcache.logging #默认值false
    cache.ehcache.statistics #默认值true
    cache.ehcache.max.bytes.local.heap #默认值null
    cache.ehcache.max.bytes.local.off.heap #默认值null
    cache.ehcache.max.bytes.local.disk #默认值null
    
####通过ehcache自带的XML方式配置
    <!--lang-->
    cache.ehcache.configuration #通过在配置文件中配置ehcache的XML文件路径 文件路径配置可以是:file:C:/cache/cache.xml文件系统格式，或者是:classpath:cache.xml的classpath路径

##使用mixcache缓存需要配置的信息
     <!--lang-->
     cache.mix#【必填】配置组合的缓存实现名称，可选值有threadlocal,lru,jcache,redis,memcached,ehcache，必须配置两个缓存实现，同英文逗号隔开，例如cache.mix=ehcache,redis
    
这种情况如果引用了两个缓存实现，那么需要对这两个缓存实现进行配置。，比如上面的cache.mix=ehcache,redis，那么你就需要对ehcache和redis两种缓存实现进行配置

    
##对于缓存方法黑/白名单
上面对开启dubbo缓存方式知道，可以对全局服务开启缓存，也可以对指定的接口来开启缓存，也可以对某个具体的方法开启缓存，为了能够更加智能的对某个方法的执行结果进行缓存
该插件添加了缓存方法黑/白名单的功能
    <!--lang-->
    cache.method.white.list:配置缓存方法白名单，如果多个，用英文逗号隔开，每一项是一个匹配方法的正则表达式，默认值为：^((select)|(get)|(query)|(load))[\w-]*$
    cache.method.black.list:配置缓存方法的黑名单，规则同上，默认值为：^((insert)|(add)|(save)|(update))[\w-]*$

当缓存插件拦截到当前执行方法不在黑名单里面，并且在白名单里面那么将对该方法的结果进行缓存，否则不进行缓存

##对缓存超时时间配置
###对全局缓存有效时间配置
    
    <!--lang-->
    cache.default.expire #单位秒 默认值60*60
    
###对某个接口的所有方法设置缓存有效时间
    <!--lang-->
    cache.interfacefullname.expire #单位秒 默认值cache.default.expire
###对某个接口的某个方法设置缓存有效时间
    <!--lang-->
    cache.interfacefullname.methodname.expire #单位秒 默认值cache.interfacefullname.expire