package com.tencent.dolphin.svrcore.comm;

import java.net.InetSocketAddress;

import com.tencent.dolphin.svrcore.packet.IoPacket;

public abstract class BaseIoPacket implements IoPacket {
    protected final long createTime = System.currentTimeMillis();
    protected final long createNanoTime = System.nanoTime();
    protected InetSocketAddress routerAddr;
    /** 用以存储在整个请求生命周期使用的对象。包体PB解析一次后缓存在此 */
    protected Object ioAttach = null;
    private Object zmqAttach = null;

    private int errCode;
    private String errMsg;


    public IoPacket newResponsePacket(IoPacket reqPacket, int ec,
                                      String message, Object body) {
        setCodeAndMsg(ec, message);
        return null;
    }
    public void setCodeAndMsg(int ec, String message){
        errCode = ec;
        errMsg = message;
    }


    @Override
    public long getCreateTime() {
        return createTime;
    }

    public long getCreateNanoTime() {
        return createNanoTime;
    }

    public void setRouterAddr(InetSocketAddress routerAddr) {
        this.routerAddr = routerAddr;
    }

    @Override
    public InetSocketAddress getRouterAddr() {
        return routerAddr;
    }

    @Override
    public int getEstimateSize() {
        return 0;
    }

    @Override
    public long getReqUid() {
        // TODO Auto-generated method stub
        return 0;
    }

    public <T> T getIoAttach() {
        return (T)ioAttach;
    }

    public void setIoAttach(Object ioAttach) {
        this.ioAttach = ioAttach;
    }

    /**
     * 获取返回包中的错误码,用于智能监控上报
     * @return
     */
    public long getRetCode() {
        return errCode;
    }
    /**
     * 获取返回包中的错误消息,用于智能监控上报
     * @return
     */
    public String getErrorMsg() {
        return errMsg;
    }

}
