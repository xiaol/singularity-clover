package com.singularity.clover.entity;


import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;

import com.singularity.clover.Global;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.record.RecordFactory;

public class EntityPool {
	
	private static HashMap<String,HashMap<Long, Persisable>> 
			entityPool = new HashMap<String, HashMap<Long,Persisable>>();
	
	private static HashMap<String,Persisable> 
			entityFactory = new HashMap<String, Persisable>();
	
	private static EntityPool self = null;
	private EntityPool(){}
		
	public static EntityPool instance(){
		if(self == null)
			self = new EntityPool();
		return self;
	}
	
	
	public void register(String tag,Persisable protoType){
		entityPool.put(tag,new HashMap<Long, Persisable>());
		entityFactory.put(tag,protoType);
	}
		
	public  Persisable getPrototype(String tag){
		return entityFactory.get(tag);
	}
	
	public Set<Entry<String, Persisable>> getAllPrototype(){
		return entityFactory.entrySet();
	}
	
	public void add(long id, Persisable entity,String tag){
		if(RecordFactory.isRecord(tag)){
				tag = Record.TAG;}
		
		if(entityPool.containsKey(tag)){
			if(id == Global.INVALIDATE_ID){
				throw new NoSuchElementException();
			}
			HashMap<Long, Persisable> entityMap = entityPool.get(tag);
			entityMap.put(id,entity);
		}else{
			throw new NoSuchElementException();
		}		
	}
	
	public Persisable forId(long id,String tag){
		if(RecordFactory.isRecord(tag)){
				tag = Record.TAG;}
		
		if(entityPool.containsKey(tag)){
			HashMap<Long, Persisable> entityMap = entityPool.get(tag);
			if(entityMap.containsKey(id) && id != Global.INVALIDATE_ID){
				return entityMap.get(id);
			}else if(id == Global.INVALIDATE_ID){
				return null;
			}else{	
				Persisable entity = entityFactory.get(tag).load(id);
				if(entity == null){
					return null;
				}
				entityMap.put(entity.getId(),entity);
				return entity;
			}
		}else{
			throw new NoSuchElementException();
		}
	}

	public HashMap<Long, Persisable> forTag(String tag){
		if(RecordFactory.isRecord(tag)){
				tag = Record.TAG;}
		
		if(entityPool.containsKey(tag)){
			HashMap<Long, Persisable> entityMap = entityPool.get(tag);
			return entityMap;
		}else{
			return null;
		}
	}
	
	public void removeId(long id,String tag){
		if(RecordFactory.isRecord(tag)){
				tag = Record.TAG;}
		
		if(entityPool.containsKey(tag)){
			HashMap<Long, Persisable> entityMap = entityPool.get(tag);
			if(entityMap.containsKey(id) && id != Global.INVALIDATE_ID){
				entityMap.remove(id);
			}
		}
	}
	
	public void clear(){
		for (Map.Entry<String, Persisable> 
			classMap:entityFactory.entrySet()){
			classMap.getValue().clearTable();
		}
	}
	
	public void delete(Persisable entity){
		if(entity.getId() != Global.INVALIDATE_ID)
			entity.delete();
		else{
			throw new InvalidParameterException();
		}
	}
	
	public void delete(long id,String tag){
		forId(id,tag).delete();
	}
	
}
