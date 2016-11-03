package net.dubboclub.tracing.core.collector;

import com.alibaba.fastjson.JSON;
import net.dubboclub.tracing.core.SpanBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DefaultTracingCollector
 * Created by bieber.bibo on 16/11/2
 */

public class DefaultTracingCollector implements TracingCollector {

    private Logger logger = LoggerFactory.getLogger("tracing");

    @Override
    public void push(List<SpanBean> spanList) {
        for(SpanBean span:spanList){
            logger.info(JSON.toJSONString(span));
        }
    }
}
