package com.tencent.dolphin.svrcore.ps;

import com.google.inject.Injector;
import com.tencent.dolphin.api.APIException;
import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.comm.ServerConfigs;
import com.tencent.dolphin.svrcore.utils.BusinessException;
import com.tencent.dolphin.svrcore.utils.MonitorBlackUidUtil;
import com.tencent.dolphin.svrcore.utils.MonitorUtils;
import com.tencent.dolphin.svrcore.utils.U;
import com.tencent.dolphin.svrcore.ws.ServerWriteFutureListener;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调用流程：{@link #beforeService(IoPacket, Channel)} - {@link #service(IoPacket, Channel, T_ATT)} -(异常)- {@link #onException(IoPacket, Channel, Exception, T_ATT)} - {@link #onFormatFlowLog(IoPacket, IoPacket, Channel, T_ATT, Exception)}
 * @see ServerConfigs
 */
public abstract class BaseProcessor<T_REQ extends IoPacket, T_RSP extends IoPacket, T_ATT> 
		implements Processor<T_REQ, T_RSP>{
	//protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final Logger logger =  LoggerFactory.getLogger(getClass());
	
	/*static final Logger flow = LoggerFactory.getLogger("ServerFlow");
	static final Logger writeFlow = LoggerFactory.getLogger("ServerWriteFlow");*/
	static final Logger kflow = LoggerFactory.getLogger("ServerFlow");
	static final Logger kwriteFlow = LoggerFactory.getLogger("ServerWriteFlow");

	protected final ServerConfigs serverConfigs;
	protected final Configuration configs;
	protected final MonitorBlackUidUtil blackUidUtil;



	protected int processTimeOutValue;
	protected int processTimeOutMonitor ;

	
	public void setServiceCmd(Object oCmd){
		String cmd = "";
		if (oCmd instanceof String) {
			cmd = (String) oCmd;
			int lastDot = cmd.lastIndexOf('.') + 1/*忽略前导.*/;
			if (lastDot >= 1 && lastDot < cmd.length())
				cmd = cmd.substring(lastDot);
		} else {
			cmd = String.valueOf(oCmd);
		}

	}

	public void setServiceCmd(Object oCmd, int req, int succ, int fail, int timeoutV,int timeoutM) {

		String cmd = "";
		if (oCmd instanceof String) {
			cmd = (String) oCmd;
			int lastDot = cmd.lastIndexOf('.') + 1/*忽略前导.*/;
			if (lastDot >= 1 && lastDot < cmd.length())
				cmd = cmd.substring(lastDot);
		} else {
			cmd = String.valueOf(oCmd);
		}

	}
	
	protected void MonitorReq(){}
	protected void MonitorSucc(){}
	protected void MonitorFail(){}
	protected void MonitorTime200(){}
	
	protected boolean isWriteProcessor(){
		return false;
	}
	
	
	public BaseProcessor(Injector injector){
		
		this.configs = injector.getInstance(Configuration.class);
		this.blackUidUtil = injector.getInstance(MonitorBlackUidUtil.class);

		
		this.serverConfigs = injector.getInstance(ServerConfigs.class);
    }
	
	final String toErrorInfo(T_REQ req, Channel ioChannel){
		return "error in cmd=" + (req==null?"<NULL>":req.getIoCmd()) + ", remote=" + (ioChannel==null?"<NULL>":ioChannel.remoteAddress());
	}
	
	/** 默认打本地日志
	 * @throws */
	protected void doFlowLog(T_REQ reqPkg, T_RSP rspPkg, Channel ioChannel, T_ATT attach, Exception ex, StringBuilder log) {
		logger.info("log flow log");
	}
	
	@Override
	public T_RSP process(T_REQ req, Channel ioChannel) throws Exception {
		logger.info("process begin");
		 
		T_ATT attach = null;
		T_RSP rsp = null;
		Exception caughtException = null;
		try{
			attach = beforeService(req, ioChannel);
			logger.info("begin service.");
			rsp = service(req, ioChannel, attach);
		}
		catch (Exception ex){
			caughtException = ex;
			try{
				rsp = onException(req, ioChannel, caughtException, attach);
			}
			catch (Exception ex2){
				MonitorUtils.monitor(678455); //Auto-gen monitor: 异常处理抛异常
				logger.error(toErrorInfo(req, ioChannel), ex2);
			}
		}
		try{
			if(kflow.isInfoEnabled() || kwriteFlow.isInfoEnabled()){
				StringBuilder logString = onFormatFlowLog(req, rsp, ioChannel, attach, caughtException);
				doFlowLog(req, rsp, ioChannel, attach, caughtException, logString);
			}
			if(caughtException != null || rsp.getRetCode() !=0){
				if(!blackUidUtil.isInBlack(req)){

				}
			}
			else{

			}
		}
		catch (Exception ex3){
			MonitorUtils.monitor(678456); //Auto-gen monitor: 流水记录异常
			logger.error(toErrorInfo(req, ioChannel), ex3);
		}finally{
			long costTime = System.currentTimeMillis() - req.getCreateTime();
			if(costTime > 200){
				logger.info("cost is > 200ms and report it.");
			}

			if (processTimeOutMonitor != 0 && costTime > processTimeOutValue) {//可以自定义每一个命令字的超时时间，而不是统一全部200
				MonitorUtils.monitor(processTimeOutMonitor);
			}
			//智能监控上报
			try{
//				smartReporter.send(String.valueOf(req.getIoCmd()), ((Number)rsp.getRetCode()).intValue(), costTime, rsp.getErrorMsg());
			}catch(Exception e){
				logger.error("smartReporter error",e);
			}
		}
		return rsp;
	}
	
	/**
	 * 业务处理流程
	 * @param pkg 网络请求包
	 * @param ioChannel
	 * @param attach {@link #beforeService(IoPacket, Channel)} 返回的附件对象
	 * @return 响应网络包。不需要回包则返回NULL
	 * @throws Exception
	 */
	protected abstract T_RSP service(T_REQ pkg, Channel ioChannel, T_ATT attach) throws Exception;

	/** 异常处理。默认返回空包体 
	 * @throws  */
	protected T_RSP onException(T_REQ req, Channel ioChannel,
			Exception ex, T_ATT attach)  {
		logger.error(ioChannel.remoteAddress()+" processing "+req.getIoCmd()+" error", ex);
		int ec = 100003;
		if (ex instanceof BusinessException)
			ec = ((BusinessException)ex).getCode();
		else if (ex instanceof APIException)
			ec = ((APIException)ex).getErrorCode();
		else if (ex.getCause() instanceof APIException)
			ec = ((APIException)ex.getCause()).getErrorCode();
		return (T_RSP)req.newResponsePacket(req, ec, ex.getMessage(), U.EMPTY_BYTES);
	}
	
    /**
     * 异步回包模式下的util方法
     */
    public void asyncResponse(Channel ch, IoPacket rsp) {
        if (ch == null || !ch.isActive() || rsp == null) {
            throw new IllegalStateException("channel is null, or closed; or rsp is null " + ch + ", " + rsp);
        }
        ch.writeAndFlush(rsp).addListener(ServerWriteFutureListener.INSTANCE);
    }
	
	/**	格式化流水记录 */
	protected abstract StringBuilder onFormatFlowLog(T_REQ reqPkg, T_RSP rspPkg, Channel ioChannel, T_ATT attach, Exception ex) 
			throws Exception ;
	
 
	
	
	/**
	 * 请求预处理。一般用作协议包体解包，返回与cmd相关的一个请求对象。
	 * 如QApp协议中把QAppRequest中的二进制buffer解析为对应的proto对象，并传入{@link #service(IoPacket, Channel, T_ATT)}等后续方法中使用。
	 * @param pkg
	 * @param ioChannel
	 * @return 一个与请求包相关的对象，框架使用者可自行定义
	 * @throws Exception
	 */
	protected abstract T_ATT beforeService(T_REQ pkg, Channel ioChannel) throws Exception;
}
