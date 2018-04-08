package com.tencent.jungle.svrcore.utils;

import java.util.concurrent.TimeoutException;

/**
 * 通常超时异常在框架抛出，不需要打出无效的堆栈
 */
@SuppressWarnings("serial")
public class ClientTimeoutException extends TimeoutException{
    
    public ClientTimeoutException(String msg) {
        super(msg);
    }
    
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
