package com.tencent.dolphin.svrcore.client;

import java.util.concurrent.Future;

/**
 * 超时管理
 * @see DefaultTimeoutManager
 * @see InaccurateTimeoutManager
 */
public interface TimeoutManager {
	Future<?> watch(Runnable task, long timeout);
}
