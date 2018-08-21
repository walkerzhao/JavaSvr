package com.tencent.dolphin.svrcore.ps;

import com.tencent.dolphin.svrcore.packet.IoPacket;
import io.netty.channel.Channel;

public interface Processor<T_REQ extends IoPacket, T_RSP extends IoPacket> {
    T_RSP process(T_REQ req, Channel ioChannel) throws Exception;
}
