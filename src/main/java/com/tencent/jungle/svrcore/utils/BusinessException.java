package com.tencent.jungle.svrcore.utils;


public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	final int ec;
	public BusinessException(int ec, String message, Throwable cause) {
		super(message, cause);
		this.ec = ec;
	}
	
	public BusinessException(int ec, String message){
		super(message);
		this.ec = ec;
	}
	
	@Override
	public String toString() {
		return ec + ", " + getMessage();
	}
	
	public int getCode() {
		return ec;
	}
}
