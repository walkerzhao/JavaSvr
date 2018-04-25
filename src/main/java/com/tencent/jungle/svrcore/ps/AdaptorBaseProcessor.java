package com.tencent.jungle.svrcore.ps;

import com.google.inject.Injector;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.tencent.jungle.api.APIException;
import com.tencent.jungle.svrcore.codec.AdaptorPacket;
import com.tencent.jungle.svrcore.comm.BaseProcessor;
import com.tencent.jungle.svrcore.utils.BusinessException;
import com.tencent.jungle.svrcore.utils.NicUtil;
import com.tencent.jungle.svrcore.utils.U;
import io.netty.channel.Channel;
import kilim.KLog;
import kilim.Pausable;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @see BaseProcessor
 */
public abstract class AdaptorBaseProcessor<REQ_MSG extends Message> extends BaseProcessor<AdaptorPacket, AdaptorPacket, REQ_MSG> {
    final Parser<REQ_MSG> parser;

    static final KLog kflow = new KLog(LoggerFactory.getLogger("ServerFlow"));
	static final KLog kwriteFlow = new KLog(LoggerFactory.getLogger("ServerWriteFlow"));

    public AdaptorBaseProcessor(Injector injector, Parser<REQ_MSG> parser) {
        super(injector);
        this.parser = parser;
    }

    @Override
    protected REQ_MSG beforeService(AdaptorPacket pkg, Channel ioChannel) throws Exception, Pausable {
        ByteString body = (ByteString) (pkg != null && pkg.getBody() != null ? pkg.getBody() : null);
        REQ_MSG req = parser == null ? null : (body == null ? parser.parseFrom(ByteString.EMPTY) : parser.parseFrom(body));
        if (logger.isDebugEnabled()) {
            String remote = ioChannel != null && ioChannel.remoteAddress() != null ? ioChannel.remoteAddress().toString() : "<UnknownHost>";
            if (req != null)
                logger.debug(remote + " " + (pkg == null ? "" : pkg.getIoCmd()) + " get request {}", U.proto2string(req));
            else
                logger.debug(remote + " " + (pkg == null ? "" : pkg.getIoCmd()) + " get request " + (body == null ? "<NULL>" : new String(Base64.encodeBase64(body.toByteArray()))));
        }
        if (pkg != null)
            pkg.setIoAttach(req);
        return req;
    }

    /**
     * 异常处理。默认返回空包体
     *
     * @throws Pausable
     */
    protected AdaptorPacket onException(AdaptorPacket req, Channel ioChannel,
                                        Exception ex, REQ_MSG attach) throws Pausable {
        logger.error(ioChannel.remoteAddress() + " processing " + req.getIoCmd() + " error", ex);
        int ec = 100003;
        if (ex instanceof BusinessException)
            ec = ((BusinessException) ex).getCode();
        else if (ex instanceof APIException)
            ec = ((APIException) ex).getErrorCode();
        else if (ex.getCause() instanceof APIException)
            ec = ((APIException) ex.getCause()).getErrorCode();
//        else if (ex instanceof DolphinRunException){
//        	ec = ((DolphinRunException) ex).getCode();
//        }
        
        return (AdaptorPacket) req.newResponsePacket(req, ec, ex.getMessage(), U.EMPTY_BYTES);
    }
    
    /** 默认打本地日志 
	 * @throws Pausable */
	protected void doFlowLog(AdaptorPacket reqPkg, AdaptorPacket rspPkg, Channel ioChannel, REQ_MSG attach, Exception ex, StringBuilder log) throws Pausable{
		if(isWriteProcessor()){
			if(rspPkg.getRetCode() !=0){
				kwriteFlow.error(log.toString());
			}else{
				kwriteFlow.info(log.toString());
			}
		}else{
			if(rspPkg.getRetCode() !=0){
				kflow.error(log.toString());
			}else{
				kflow.info(log.toString());
			}
		}
	}

    @Override
    protected StringBuilder onFormatFlowLog(AdaptorPacket reqPkg, AdaptorPacket rspPkg, Channel ch, REQ_MSG req, Exception ex) throws Exception, Pausable {
        if (reqPkg == null || reqPkg.getBody() == null)
            return new StringBuilder("EMPTY REQUEST " + ch.remoteAddress());
        String strReq = req != null ? U.proto2string(req) : "-";

        String remoteIp = ch != null && ch.remoteAddress() != null ? NicUtil.addr2ip((InetSocketAddress) ch.remoteAddress()) : "";
        if (StringUtils.isBlank(remoteIp)) {
            remoteIp = reqPkg.getRouterAddr() != null ? reqPkg.getRouterAddr().toString() : "-";
        }


        return new StringBuilder(200 + strReq.length())
                .append("c:").append(reqPkg.getIoCmd()).append(',')
                .append("s:").append(reqPkg.getIoSeq() != null ? reqPkg.getIoSeq() : "-").append(',')
                .append("e:").append(rspPkg != null ? rspPkg.getRetCode() : -1).append(',')
                .append("t:").append(String.valueOf(System.currentTimeMillis() - reqPkg.getCreateTime())).append("ms,")
                .append("m:").append(ex != null ? ex.getMessage() : rspPkg != null && rspPkg.getErrorMsg() != null ? rspPkg.getErrorMsg() : null).append(',')
                .append("ud:").append(reqPkg.getReqUid()).append(',')
                .append("r:").append(remoteIp).append(',')
                .append("n:").append(reqPkg.getCallerName()).append(',')
                .append("ip:").append(reqPkg.getClientIp() != null ? reqPkg.getClientIp() : "-").append(',')
                //.append("atip:").append(msg.hasAuthIp() ? msg.getAuthIp() : "-").append(',')
                .append("p:").append(strReq).append(';');

    }
}