package com.tencent.jungle.svrcore.utils;

//import com.tencent.jungle.jni.Oilib;
import org.apache.commons.lang.SystemUtils;

public class MonitorUtils {
	public static void monitor(int monId){
		// 此处不会导致Oilib初始化
		if (!SystemUtils.IS_OS_WINDOWS){
//			Oilib.attrInc(monId, 1);
		}
	}
	
	/**
	 * 多次上报
	 * @param monId
	 * @param num
	 */
	public static void monitor(int monId, int num){
		// 此处不会导致Oilib初始化
		if (!SystemUtils.IS_OS_WINDOWS){
//			Oilib.attrInc(monId, num);
		}
	}
}