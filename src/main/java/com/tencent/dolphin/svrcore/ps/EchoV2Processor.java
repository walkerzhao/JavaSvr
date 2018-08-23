package com.tencent.dolphin.svrcore.ps;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.tencent.dolphin.proto.EchoProto;
import com.tencent.dolphin.svrcore.qapp.QAppMsg;
import com.tencent.dolphin.svrcore.qapp.QAppReqPacket;
import com.tencent.dolphin.svrcore.qapp.QAppRspPacket;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EchoV2Processor extends BaseProcessor<QAppReqPacket, QAppRspPacket, EchoProto.EchoReq> {

    protected static final Logger logger = LoggerFactory.getLogger(EchoV2Processor.class);


    protected Configuration configuration;


    @Inject
    public EchoV2Processor(Injector injector) {
        super(injector);
        configuration = injector.getInstance(Configuration.class);
        logger.debug("configuration:{}", configuration.toString());
    }

    @Override
    protected QAppRspPacket service(QAppReqPacket pkg, Channel ioChannel, EchoProto.EchoReq attach) throws Exception {
        logger.info("req:{}", attach);

        QAppMsg.QAppResponse.Builder qAppResponse = QAppMsg.QAppResponse.newBuilder();
        qAppResponse.setErrCode(0);
        qAppResponse.setSeq(1);
        qAppResponse.setErrMsg("suc");

        EchoProto.EchoRsp.Builder echoBuilder = EchoProto.EchoRsp.newBuilder();

        echoBuilder.setUin(416548283);
        qAppResponse.setBody(echoBuilder.build().toByteString());
        QAppRspPacket qAppRspPacket = new QAppRspPacket(qAppResponse.build());
        qAppRspPacket.setCodeAndMsg(0, "suc");
        logger.info("resp:{}", qAppRspPacket);
        return qAppRspPacket;
    }

    @Override
    protected StringBuilder onFormatFlowLog(QAppReqPacket reqPkg, QAppRspPacket rspPkg, Channel ioChannel, EchoProto.EchoReq attach, Exception ex) throws Exception {
        return null;
    }

    @Override
    protected EchoProto.EchoReq beforeService(QAppReqPacket pkg, Channel ioChannel) throws Exception {
        return null;
    }

}
