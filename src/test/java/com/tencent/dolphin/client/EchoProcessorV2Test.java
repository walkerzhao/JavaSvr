package com.tencent.dolphin.client;

import com.tencent.dolphin.proto.EchoProto;
import com.tencent.dolphin.svrcore.client.ClientIoService;
import com.tencent.dolphin.svrcore.client.DefaultTimeoutManager;
import com.tencent.dolphin.svrcore.client.StaticRouterServiceBuilder;
import com.tencent.dolphin.svrcore.client.TcpClientIoService;
import com.tencent.dolphin.svrcore.qapp.QAppClientCodecService;
import com.tencent.dolphin.svrcore.qapp.QAppMsg;
import com.tencent.dolphin.svrcore.qapp.QAppReqPacket;
import com.tencent.dolphin.svrcore.qapp.QAppRspPacket;
import com.tencent.dolphin.svrcore.ws.ThreadPoolWorkerService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by andy on 2018/8/22.
 */
public class EchoProcessorV2Test {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    ClientIoService<QAppReqPacket, QAppRspPacket> bonClient = null;

    @Test
    public void testEchoProcessor() throws Exception {
        System.out.println("hello,world");

        //server ip port
        String[][] remote = {{"routeid", "192.168.2.190", "22054"}};
        //创建client
        bonClient = getTcpClient(new QAppClientCodecService(), remote);
        //组包,发包
        TestEcho();
        System.exit(0);
    }

    private ClientIoService<QAppReqPacket, QAppRspPacket> getTcpClient(QAppClientCodecService qAppClientCodecService, String[][] router) {
        ClientIoService<QAppReqPacket, QAppRspPacket> client = new TcpClientIoService<QAppReqPacket, QAppRspPacket>();

        if(router.length <= 0) {
            throw new RuntimeException("router error");
        }


        StaticRouterServiceBuilder staticRouterServiceBuilder = new StaticRouterServiceBuilder();

        for (int i =0; i< router.length ; i++ ) {
            if(router[i].length < 3 ){
                throw new RuntimeException("router error");
            }


            String routerId = router[i][0];
            String targetHost = router[i][1];
            String targetPort = router[i][2];
            staticRouterServiceBuilder.add(routerId, targetHost, Integer.parseInt(targetPort));
        }

        client.setCodecService(qAppClientCodecService)
                .setRouterService(staticRouterServiceBuilder.build())
                .setTimeoutManager(new DefaultTimeoutManager())
                .setWorkerService(new ThreadPoolWorkerService() );
        if(client instanceof TcpClientIoService){
            ((TcpClientIoService)client).setChannelsPerAddr(4);
        }

        client.start();

        return client;
    }

    public  void TestEcho() throws Exception {
        try {
            EchoProto.EchoReq.Builder reqBuilder = EchoProto.EchoReq.newBuilder();
            reqBuilder.setUin(416548283L);


            QAppMsg.QAppRequest.Builder qappmsg = QAppMsg.QAppRequest.newBuilder()
                    .setAppName("routeid")
                    .setBusiness("test")
                    .setClientIp("127.0.0.1")
                    .setCmd("1")
                    .setSeq(1)
                    .setServiceIp("0.0.0.0")/*此处获取不到所发往的IP*/
                    .setUid(10000)
                    .setVersion(1)
                    .setBody(reqBuilder.build().toByteString());

            QAppReqPacket req = new QAppReqPacket(qappmsg.build());

            QAppRspPacket rsp = bonClient.sync(req, 3000L);

            System.out.println("rsp=" + rsp);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("", e);
        }

    }
}
