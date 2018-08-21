package com.tencent.dolphin.svrcore.io;

import java.net.SocketAddress;

import com.tencent.dolphin.svrcore.packet.CodecService;
import com.tencent.dolphin.svrcore.ps.ProcessorService;
import com.tencent.dolphin.svrcore.ws.WorkerService;


/**
 * 管理链接资源，进行IO操作。
 */
public interface ServerIoService {
    void start();

    /**
     * 绑定网卡。本方法与{@link com.tencent.dolphin.svrcore.ServerIoService#setBindAddr}只需调任意一个
     * @param nic 网卡标识，eth1
     * @param port
     * @return
     */
    ServerIoService setBindNic(String nic, int port);
    /**
     * 绑定地址。本方法与{@link com.tencent.dolphin.svrcore.ServerIoService#setBindNic}只需调任意一个
     * @param addr
     * @return
     */
    ServerIoService setBindAddr(SocketAddress addr);

    ServerIoService setBindIpPort(String ip, int port);

    /**
     * 编解码器服务
     * @param codec
     * @return
     */
    ServerIoService setCodecService(CodecService codec);

    /**
     * 工作线程池
     * @param worker
     * @return
     */
    ServerIoService setWorkerService(WorkerService worker);

    /**
     * 上行包处理服务
     * @param mapper
     * @return
     */
    ServerIoService setProcessorService(ProcessorService mapper);
}

