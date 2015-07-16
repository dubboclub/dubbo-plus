package net.dubboclub.akka.remoting.actor;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.*;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;

/**
 * Created by bieber on 2015/7/10.
 */
public abstract class RouterActor extends UntypedActor {

    protected Router router;

    protected static final int MAX_WORKER_SIZE = 10;

    protected LoggingAdapter logging = Logging.getLogger(getContext().system(),this);

    public RouterActor(URL url){
        String loadBalance = url.getParameter(Constants.LOADBALANCE_KEY,Constants.DEFAULT_LOADBALANCE);
        RoutingLogic routingLogic = null;
        if("random".equals(loadBalance)){
            routingLogic = new RandomRoutingLogic();
        }else if("roundrobin".equals(loadBalance)){
            routingLogic = new RoundRobinRoutingLogic();
        }else if("consistenthash".equals(loadBalance)){
            routingLogic = new ConsistentHashingRoutingLogic(getContext().system());
        }else if("leastactive".equals(loadBalance)){
            routingLogic = new SmallestMailboxRoutingLogic();
        }else{
            routingLogic = new SmallestMailboxRoutingLogic();
        }
        router = new Router(routingLogic);
    }
}
