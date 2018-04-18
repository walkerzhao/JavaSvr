package com.tencent.jungle.svrcore.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tencent.jungle.svrcore.packet.IoPacket;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MonitorBlackUidUtil {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected List<Long> black_uids;
	
	@Inject
	public MonitorBlackUidUtil(Configuration configs){
		black_uids = null;
		List<String> black_uidstrs = configs.getList("monitor.black.uids", null);
		if(black_uidstrs != null){
			black_uids = new ArrayList<Long>(black_uidstrs.size());
			for(String temp:black_uidstrs){
				black_uids.add(Long.parseLong(temp));
			}
		}
	}

	public List<Long> getBlack_uids() {
		return black_uids;
	}

	public void setBlack_uids(List<Long> black_uids) {
		this.black_uids = black_uids;
	}
	
	public void addBlackUid(long uid){
		if(uid == 0)
			return;
		if(black_uids == null){
			black_uids = new ArrayList<Long>();
			black_uids.add(uid);
		}else{
			if(!black_uids.contains(uid)){
				black_uids.add(uid);
			}
		}
	}
	
	public boolean isInBlack(IoPacket req){
		
		if(black_uids == null)
			return false;
		try{
			long uid = req.getReqUid();
			if(uid == 0)
				return false;
			if(black_uids.contains(uid)){
				logger.error("intercept black req uid:" + uid);
				return true;
			}
		}catch(Exception e){
			logger.error("check req uid is in monitor black uids exception:", e);
		}
		return false;
	}
	
}
