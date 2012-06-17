package com.singularity.clover.database;

import java.util.HashMap;

import com.singularity.clover.Global;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.record.RecordFactory;

public class IdGenerator {
	static HashMap<String,Long> groupIdBase = new HashMap<String, Long>();
	static HashMap<String,Boolean> bGroup = new HashMap<String, Boolean>();
	
	public static long nextId(String tag){
		if(bGroup.get(tag) == null || !bGroup.get(tag)){
			if(RecordFactory.isRecord(tag)){
				tag = Record.TAG;}
			
			long seq = DBAdapter.
				instance().lastInsertId(tag);
			if(seq == Global.INVALIDATE_ID)
				seq = 1;
			else 
				seq += 1;
			return seq;
		}else{
			long id = groupIdBase.get(tag) + 1;
			groupIdBase.put(tag,id);
			return id;
		}
	}
	
	public static long GroupIdStart(String tag){
		long id = nextId(tag);
		groupIdBase.put(tag,id);
		bGroup.put(tag,true);
		return id;
	}
	
	public static void GroupIdEnd(String tag){
		bGroup.put(tag,false);
	}
}
