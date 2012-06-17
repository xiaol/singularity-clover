package com.singularity.clover.entity.objective;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


public class OBJFactory{
	static HashMap<String, AbstractObjective> prototypeMap
								= new HashMap<String, AbstractObjective>();

	public static void register(String tag, AbstractObjective prototype) {
		prototypeMap.put(tag, prototype);
	}

	public static Set<Entry<String, AbstractObjective>> getAllOBJPrototype() {
		return prototypeMap.entrySet();
	}
	
	public static AbstractObjective getPrototype(String tag){
		return prototypeMap.get(tag);
	}
}