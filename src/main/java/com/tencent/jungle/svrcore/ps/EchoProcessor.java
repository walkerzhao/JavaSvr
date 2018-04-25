package com.tencent.jungle.svrcore.ps;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.tencent.jungle.proto.EchoProto;
import com.tencent.jungle.svrcore.codec.AdaptorPacket;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EchoProcessor extends AdaptorBaseProcessor< EchoProto.EchoReq> {

    protected static final Logger logger = LoggerFactory.getLogger(EchoProcessor.class);

        
    protected Configuration configuration;
;


    @Inject
    public EchoProcessor(Injector injector) {
        super(injector,EchoProto.EchoReq.PARSER);
        configuration = injector.getInstance(Configuration.class);
        logger.debug("configuration:{}", configuration.toString());
    }

    @Override
    protected AdaptorPacket service(AdaptorPacket pkg, Channel ioChannel, EchoProto.EchoReq attach) throws Exception {
        logger.debug("hello,world");
        return null;
    }


}
