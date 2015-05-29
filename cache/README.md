#Dubbo缓存插件


##一、插件概述
该插件主要是完善Dubbo缓存方案，由于Dubbo提供的默认缓存方案比较基础，只是提供了简单的本地缓存方案，而不具备和第三方
缓存组件集成。本插件通过扩展实现了Dubbo的`Cache`和`CacheFactory`接口，和Redis,Memcached以及本地缓存组件Ehcache
进行集成。
##二、如何引用缓存
对于dubbo的配置之前写过一篇比较详细的内容进行介绍，这里不在对主要其内容进行重复介绍，如要了解可以到[http://bbs.dubboclub.net/read-4.html](http://bbs.dubboclub.net/read-4.html)去了解
这里只对如何引用缓存进行介绍。
###1、通过XML标签配置
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
    
###2、通过Properties文件配置
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
> threadlocal：它的实现类是ThreadLocalCacheFactory，一看就知道是一个线程级别的缓存了<br>
  lru：它的实现类是LruCacheFactory，是一个基于本地内存的LRU算法的缓存<br>
  jcache：它的实现类是JCacheFactory，是一个基于JCache的缓存实现<br>

####当前cache插件提供哪些实现
        
> redis：它的实现类是RedisCacheFactory，是集成了Redis的缓存实现<br>
  memcached：它的实现类是MCCacheFactory，是集成了Memcached缓存实现<br>
  ehcache：它的实现类是EhCacheFactory，是集成了Ehcache来做本地缓存实现<br>
  mixcache：它的实现类是MixCacheFactory，是一种组合缓存方案，可以从上面提供的几种缓存实现选择两种来实现一个二级缓存方案<br>

**如果你的项目引用了cache插件，那么${cacheKey}的可选值是threadlocal,lru,jcache,redis,memcached,ehcache,mixcache**

##三、如何使用该缓存插件
该插件提供了四种缓存实现，那么针对这四种缓存实现需要做哪些配置呢？
###1、配置信息加载途径
该插件对缓存配置加载提供了三种途径<br>
- **第一种、集成到dubbo.properties配置文件中**<br>
- **第二种、通过在dubbo.properties配置cache.properties.file属性来指明缓存配置文件路径**<br>
- **第三种、通过-Dcache.properties.file方式来指定缓存配置文件存放路径**<br>

> 文件路径配置可以是:file:C:/cache/cache.properties文件系统格式，或者是:classpath:cache.properties的classpath路径，默认是根据classpath路径来查找资源

**下面所介绍的配置信息均是配置在上述配置文件中**

###2、使用Redis缓存需要配置的信息
    
    <!--lang-->
	#【必填】配置redis连接地址，ip:port格式，如果是redis集群，可以是ip1:port1,ip2:port2....ipn:portn通过英文逗号将各个连接地址分开
    cache.redis.connect    
	#【选填】配置redis客户端连接池最大连接数，默认8
    cache.redis.maxTotal
	#【选填】配置redis客户端连接池最大空闲连接数，默认8
    cache.redis.maxIdle
	#【选填】配置redis客户端连接池最小空闲连接数，默认0
    cache.redis.minIdle
	#【选填】配置redis客户端最大等待时间，单位毫秒,默认-1，表示无限等待
    cache.redis.maxWaitMillis
	#【选填】 默认值1000L * 60L * 30L
    cache.redis.minEvictableIdleTimeMillis
	#【选填】 默认值1000L * 60L * 30L
    cache.redis.soft.minEvictableIdleTimeMillis
	#【选填】 默认值3
    cache.redis.num.testsPerEvictionRun
	#【选填】 默认值org.apache.commons.pool2.impl.DefaultEvictionPolicy
    cache.redis.evictionPolicyClassName
	#【选填】 默认值false
    cache.redis.testOnBorrow
	#【选填】 默认值false
    cache.redis.testOnReturn
	#【选填】 默认值false
    cache.redis.testWhileIdle
	#【选填】 默认值-1L
    cache.redis.timeBetweenEvictionRunsMillis
	#【选填】 默认值true
    cache.redis.blockWhenExhausted
    #【选填】默认值true
    cache.redis.jmxEnabled
	#【选填】默认值pool
    cache.redis.jmxNamePrefix
    
###3、使用memcached缓存需要配置的信息
    <!--lang-->
	#【必填】配置redis连接地址，ip:port格式，如果是redis集群，可以是ip1:port1,ip2:port2....ipn:portn通过英文逗号将各个连接地址分开
    cache.memcached.connect    
	#【选填】 默认值60000
    cache.memcached.connectTimeout
	#【选填】 默认值1
    cache.memcached.connectionPoolSize
	#【选填】 默认值false
    cache.memcached.failureMode
	#【选填】 默认值40000或者(int) (40000 * (Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0))
    cache.memcached.maxQueuedNoReplOperations
	#【选填】默认default，对应ArrayMemcachedSessionLocator，可选值：ketama对应KetamaMemcachedSessionLocator，election对应ElectionMemcachedSessionLocator，libmemcached对应LibmemcachedMemcachedSessionLocator，php对应PHPMemcacheSessionLocator
    cache.memcached.session.locator
	#【选填】默认32 * 1024
    cache.memcached.sessionReadBufferSize
	#【选填】默认0
    cache.memcached.soTimeout
	#【选填】默认0
    cache.memcached.writeThreadCount
	#【选填】默认false
    cache.memcached.statisticsServer
    #【选填】默认true
    cache.memcached.handleReadWriteConcurrently
	#【选填】默认0
    cache.memcached.dispatchMessageThreadCount
	#【选填】默认1
    cache.memcached.readThreadCount
	#【选填】默认1000l
    cache.memcached.checkSessionTimeoutInterval

###5、使用ehcache缓存需要配置信息
对于ehcache的配置除了上面配置外，由于ehcache自己有一套配置文件，所以也支持通过它自身的xml配置文件来配置
####通过properties来配置
    <!--lang-->
    #默认值true
    cache.ehcache.updateCheck
    #默认值15
    #默认值default
    cache.ehcache.name
    #默认值0
    cache.ehcache.cacheLoaderTimeoutMillis
    #默认值10000
    cache.ehcache.maxEntriesLocalHeap
    #默认值10000000
    cache.ehcache.maxElementsOnDisk
    #默认值0
    cache.ehcache.maxEntriesInCache
    #默认值true
    cache.ehcache.clearOnFlush
    #默认值false
    cache.ehcache.eternal
    #默认值120
    cache.ehcache.timeToIdleSeconds
    #默认值120
    cache.ehcache.timeToLiveSeconds
    #默认值null
    cache.ehcache.overflowToDisk
    #默认值null
    cache.ehcache.diskPersistent
    #默认值30
    cache.ehcache.diskSpoolBufferSizeMB
    #默认值1
    cache.ehcache.diskAccessStripes
    #默认值120
    cache.ehcache.diskExpiryThreadIntervalSeconds
    #默认值false
    cache.ehcache.logging
    #默认值null
    cache.ehcache.overflowToOffHeap
    #默认值null
    cache.ehcache.transactionalMode
    #默认值true
    cache.ehcache.statistics
    #默认值null
    cache.ehcache.copyOnRead
    #默认值null
    cache.ehcache.copyOnWrite
    #默认值null
    cache.ehcache.maxBytesLocalHeap
    #默认值null
    cache.ehcache.maxBytesLocalOffHeap
    #默认值null
    cache.ehcache.maxBytesLocalDisk
    
####通过ehcache自带的XML方式配置
    <!--lang-->
	#通过在配置文件中配置ehcache的XML文件路径 文件路径配置可以是:file:C:/cache/cache.xml文件系统格式，或者是:classpath:cache.xml的classpath路径
    cache.ehcache.configuration 

###6、使用mixcache缓存需要配置的信息
     <!--lang-->
	#【必填】配置组合的缓存实现名称，可选值有threadlocal,lru,jcache,redis,memcached,ehcache，必须配置两个缓存实现，同英文逗号隔开，例如cache.mix=ehcache,redis
     cache.mix

    
> 这种情况如果引用了两个缓存实现，那么需要对这两个缓存实现进行配置。，比如上面的cache.mix=ehcache,redis，那么你就需要对ehcache和redis两种缓存实现进行配置

    
##四、对于缓存方法黑/白名单
上面对开启dubbo缓存方式知道，可以对全局服务开启缓存，也可以对指定的接口来开启缓存，也可以对某个具体的方法开启缓存，为了能够更加智能的对某个方法的执行结果进行缓存
该插件添加了缓存方法黑/白名单的功能

    <!--lang-->
	#配置缓存方法白名单，如果多个，用英文逗号隔开，每一项是一个匹配方法的正则表达式，默认值为：^((select)|(get)|(query)|(load))[\w-]*$
    cache.method.white.list
	#配置缓存方法的黑名单，规则同上，默认值为：^((insert)|(add)|(save)|(update))[\w-]*$
    cache.method.black.list

> 当缓存插件拦截到当前执行方法不在黑名单里面，并且在白名单里面那么将对该方法的结果进行缓存，否则不进行缓存

##五、对缓存超时时间配置
###1、对全局缓存有效时间配置
    
    <!--lang-->
	#单位秒 默认值60*10,十分钟
    cache.default.expire 
    
###2、对某个接口的所有方法设置缓存有效时间
    <!--lang-->
	#单位秒 默认值cache.${cacheKey}.default.expire
    cache.interfacefullname.expire 
###3、对某个接口的某个方法设置缓存有效时间
    <!--lang-->
	##单位秒 默认值cache.${cacheKey}.interfacefullname.expire
    cache.interfacefullname.methodname.expire 

##六、对某个缓存实现设置属于其私有的有效时间
下面对cacheKey可取的值有redis,memcached,ehcache

redis：是对redis的缓存实现设置缓存有效时间
memcached:是对memcached的缓存实现设置缓存有效时间
ehcached:是对ehcache的缓存实现设置有效时间

###1、对全局缓存有效时间配置

    <!--lang-->
	#单位秒 默认值cache.default.expire
    cache.${cacheKey}.default.expire 

###2、对某个接口的所有方法设置缓存有效时间
    <!--lang-->
	#单位秒 默认值cache.interfacefullname.expire
    cache.${cacheKey}.interfacefullname.expire 
###3、对某个接口的某个方法设置缓存有效时间
    <!--lang-->
	#单位秒 默认值cache.interfacefullname.methodname.expire
    cache.${cacheKey}.interfacefullname.methodname.expire 