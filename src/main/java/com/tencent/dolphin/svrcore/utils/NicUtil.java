package com.tencent.dolphin.svrcore.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NicUtil {

	public static final Logger log = LoggerFactory.getLogger(NicUtil.class);

	/**
	 * 解析网卡IP
	 * @param nic 网卡标识，如eth1
	 * @return
	 */
	public static String resolveNicAddr(String nic) {
		try {
			NetworkInterface ni = NetworkInterface.getByName(nic);
			Enumeration<InetAddress> addrs = ni.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress i = addrs.nextElement();
				if (i instanceof Inet4Address) {
					return i.getHostAddress();
				}
			}
			addrs = ni.getInetAddresses();
			return addrs.nextElement().getHostAddress();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String addr2ip(InetSocketAddress addr){
		return addr.getAddress().getHostAddress();
	}

	/**
	 * 点分十进制字符串型IP转换成长整型IP.
	 * 注意大小端问题
	 * @param ip
	 * @return 长整型IP
	 */
	public static long string2Long(String ip) {
		if (ip != null && !ip.isEmpty()) {

			long address = 0L;

			try {
				String[] addr = ip.split("\\.");
				address = Integer.parseInt(addr[0]) & 0xFF;
				address |= ((Integer.parseInt(addr[1]) << 8) & 0xFF00);
				address |= ((Integer.parseInt(addr[2]) << 16) & 0xFF0000);
				address |= ((Integer.parseInt(addr[3]) << 24) & 0xFF000000);
			} catch (Exception e) {
				log.error("ip StringToLong Error:: ip={}", ip);
				return 0L;
			}

			return address;
		} else {
			return 0L;
		}
	}
}
