package com.tencent.dolphin.svrcore.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U {
	static final Logger log = LoggerFactory.getLogger(U.class);
	/** -Djungle.svrcore.debug=false|true */
	public static final boolean SVRCORE_DEBUG_ENABLED = "true".equals(System.getProperty("dolphin.svrcore.debug"));
	public static final boolean SVRCORE_PFM_ENABLED = "true".equals(System.getProperty("dolphin.pfm"));
	
	public static byte[] EMPTY_BYTES = new byte[0];
    
    public static boolean isProtobufMessage(Object msg){
    	return Message.class.isInstance(msg);
    }
    
    public static String int2ip(int v){
		 StringBuilder sb = new StringBuilder(15);
		 return sb.append(((int)(v>>24))&0xFF).append('.')
				 .append(((int)(v>>16))&0xFF).append('.')
				 .append(((int)(v>>8))&0xFF).append('.')
				 .append(v&0xFF).toString();
	 }
    
	 
	 /** 判断i是否数字 */
	 public static int gint(Object i, int defValue){
		 return (i instanceof Number) ? ((Number)i).intValue() : defValue;
	 }
	 /** 判断l是否数字 */
	 public static long glong(Object l, long defValue){
		 return (l instanceof Number) ? ((Number)l).longValue() : defValue;
	 }
	 
	 /** 转换为int */
	 public static int pint(String i, int defValue){
		 try {
			return Integer.parseInt(i);
		} catch (Exception e) {
			return defValue;
		}
	 }
	 /** 转换为long */
	 public static long plong(String l, long defValue){
		 try {
			return Long.parseLong(l);
		} catch (Exception e) {
			return defValue;
		}
	 }
    
	 public static String toJsonString(Object o, boolean propagate){
		try {
			return o==null ? "<NULL>" : JSON_MAPPER.writeValueAsString(o);
		} catch (Exception e) {
			if (propagate)
				throw new RuntimeException(e);
			else
				log.error("", e);
		}
		return "";
	}
	
	public static <T> T parseJson(String json, Class<T> type, boolean propagate){
		try {
			return JSON_MAPPER.readValue(json, type);
		} catch (Exception e) {
			if (propagate)
				throw new RuntimeException(e);
			else
				log.error("", e);
		}
		return null;
	}
	 
    static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static String proto2string(Message msg){
    	try {
			return msg==null ? "<NULL>" : toJsonString(proto2map(msg), false);
		} catch (Exception e) {
			log.error("", e);
			return "";
		}
    }

public static Map<String, Object> proto2map(Message msg){
	if (msg == null)
		return null;
	Map<String, Object> r = new HashMap<String, Object>();
	for (Map.Entry<FieldDescriptor, Object> e : msg.getAllFields().entrySet()){
		FieldDescriptor f = e.getKey();
		if (Type.MESSAGE.equals(f.getType())){
			if (f.isRepeated()){
				List<Message> ls = (List)e.getValue();
				List<Map<String, Object>> rls = new ArrayList<Map<String,Object>>(ls.size());
				for (Message subMsg : ls){
					rls.add(proto2map(subMsg));
				}
				r.put(f.getName(), rls);
			}
			else{
				r.put(f.getName(), proto2map((Message)e.getValue()));
			}
		}
		else if (Type.ENUM.equals(f.getType())){
			if (f.isRepeated()){
				List<EnumValueDescriptor> ls = (List)e.getValue();
				List<String> rls = new ArrayList<String>(ls.size());
				for (EnumValueDescriptor v : ls){
					rls.add(v.getName() + '(' + v.getNumber() + ')');
				}
				r.put(f.getName(), rls);
			}
			else{
				EnumValueDescriptor v = (EnumValueDescriptor)e.getValue();
				r.put(f.getName(), v.getName() + '(' + v.getNumber() + ')');
			}
		}
		else if (Type.BYTES.equals(f.getType())){
			if (f.isRepeated()){
				List<ByteString> ls = (List)e.getValue();
				List<String> rls = new ArrayList<String>(ls.size());
				for (ByteString v : ls){
					rls.add(new String(Base64.encodeBase64(v.toByteArray())));
				}
				r.put(f.getName(), rls);
			}
			else{
				ByteString v = (ByteString)e.getValue();
				r.put(f.getName(), new String(Base64.encodeBase64(v.toByteArray())));
			}
		}
		else{
			r.put(f.getName(), rawtype(f, e.getValue(), true) );
		}
	}
	return r;
}


static Object rawtype(FieldDescriptor f, Object v , boolean flag){
	Object ret = null;
	if(flag && f.isRepeated()){
		List<Object> list = (List<Object>)v;
		ArrayList<Object> newList = new ArrayList<Object>(list.size());
		for (Object obj : list) {
			newList.add(rawtype(f, obj,false));
		}
		return newList;
	}
	if (Type.INT32.equals(f.getType()) || Type.SINT32.equals(f.getType()) ){
		try {
			ret = ((Number)v).intValue();
		} catch (Exception e) {
			ret = Integer.parseInt(v.toString());
		}
		return ret;
	}
	else if (Type.UINT32.equals(f.getType())){
		try {
			ret = (long)(((Number)v).intValue() & 0xFFFFFFFFL);
		} catch (Exception e) {
			ret = Long.parseLong(v.toString());
		}
		return ret;
	}
		
	else if (Type.UINT64.equals(f.getType()) ||
			Type.INT64.equals(f.getType())||
			Type.SINT64.equals(f.getType())
			){
		try {
			ret = ((Number)v).longValue();
		} catch (Exception e) {
			ret = Long.parseLong(v.toString());
		}
		return ret;
	}
	else if (Type.STRING.equals(f.getType()))
		return v.toString();
	else if (Type.BOOL.equals(f.getType())){
		try {
			ret = (Boolean)v;
		} catch (Exception e) {
			ret = Boolean.parseBoolean(v.toString());
		}
		return ret;
	}
	else if (Type.BYTES.equals(f.getType()))
		return ByteString.copyFrom(Base64.decodeBase64(v.toString()));
	else{
		return v;
		//logger.error(f.getName()+" is "+f.getType()+", but data is "+v.getClass());
		//throw new IllegalArgumentException(f.getName()+" is "+f.getType()+", but data is "+v.getClass());
	}
}
}
