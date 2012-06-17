package com.singularity.clover.entity.record;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class RecordFactory {
	static HashMap<String, Record> prototypeMap
								= new HashMap<String, Record>();

	public static void register(String tag, Record prototype) {
		prototypeMap.put(tag, prototype);
	}

	public static Set<Entry<String, Record>> getAllPrototype() {
		return prototypeMap.entrySet();
	}
	
	public static Record getPrototype(String tag){
		return prototypeMap.get(tag);
	}
	
	public static boolean isRecord(String tag){
		return prototypeMap.containsKey(tag);
	}
}
