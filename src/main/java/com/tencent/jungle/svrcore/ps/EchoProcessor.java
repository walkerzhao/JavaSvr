package com.tencent.jungle.svrcore.ps;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tencent.jungle.proto.EchoProto;
import com.tencent.jungle.svrcore.client.RouterService.RouterInfo;
import com.tencent.jungle.svrcore.codec.AdaptorPacket;
import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.qapp.QAppReqPacket;
import com.tencent.jungle.svrcore.qapp.QAppRspPacket;
import com.tencent.jungle.videohub.proto.CommProtocolProto;
import io.netty.channel.Channel;
import kilim.Pausable;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import java.util.*;

@Singleton
public class EchoProcessor extends AdaptorBaseProcessor< EchoProto.EchoReq> {

    protected static final Logger logger = LoggerFactory.getLogger(EchoProcessor.class);

        
    protected Configuration configuration;
;


    @Inject
    public EchoProcessor(Injector injector) {
        super(injector,EchoProto.EchoReq.PARSER);
        logger.debug("configuration:{}", configuration.toString());
    }

    @Override
    protected AdaptorPacket service(AdaptorPacket pkg, Channel ioChannel, EchoProto.EchoReq attach) throws Pausable, Exception {
        logger.debug("hello,world");
        return null;
    }


}
