package com.tencent.dolphin.svrcore.comm;

import com.tencent.dolphin.svrcore.client.PacketLikeLookupItem;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelAttachment {
	public static final AttributeKey<ChannelAttachment> ATTR_KEY_CH_ATTACHMENT = AttributeKey.valueOf("attach");

	/**把lookups按channel分离，减少在高并发rpc时竞争*/
	public final ConcurrentHashMap<Object, PacketLikeLookupItem> lookups;
	public final AtomicInteger written = new AtomicInteger(0);
	
	public ChannelAttachment() {
		this(new ConcurrentHashMap<Object, PacketLikeLookupItem>(1024, 0.75F, 128));
	}
	
	public ChannelAttachment(ConcurrentHashMap<Object, PacketLikeLookupItem> lookups) {
		this.lookups = lookups;
	}
	
	public static ChannelAttachment get(Channel ch) {
		return ch!=null ? ch.attr(ATTR_KEY_CH_ATTACHMENT).get() : null;
	}
}
