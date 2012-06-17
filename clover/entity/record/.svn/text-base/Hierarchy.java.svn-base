package com.singularity.clover.entity.record;

import java.util.ArrayList;
import java.util.LinkedList;

import com.singularity.clover.Global;
import com.singularity.clover.entity.EntityPool;

public class Hierarchy{
	
	private ArrayList<Long> ids = null;

	public class Node {
		long recordId;

		public Node(long id){
			recordId = id;
		}
		
		public long getId(){
			return recordId;
		}
	}
	
	public LinkedList<Node> buildHierarchy(ArrayList<Long> ids){
		this.ids = ids;
		LinkedList<Node> result = new LinkedList<Hierarchy.Node>();
		while(!ids.isEmpty()){
			Node it = build(ids.get(0));
			boolean bHave = false;
			for(Node entry:result){
				if(entry.recordId == it.recordId){
					bHave = true;
					break;}
			}
			if(!bHave)
				result.add(it);
		}
		return result;
	}
	
	public Node build(long id){
		boolean bEnd = false;
		Record record = (Record) EntityPool.
			instance().forId(id, Record.TAG);
		do{
			ids.remove(id);
			if(record.recordParentId != Global.INVALIDATE_ID){			
				Record parent = (Record) EntityPool.instance().
					forId(record.recordParentId, Record.TAG);
				if(parent != null){
					parent.recordChildId = id;
					id = parent.id;
					record = parent;
				}else{
					parent = new TextRecord();
					parent.store();
					record.recordParentId = parent.getId();
					record.store();
					return new Node(record.recordParentId);
				}
			}else if(record.recordPrevId != Global.INVALIDATE_ID){
				record = (Record) EntityPool.instance().
					forId(record.recordPrevId, Record.TAG);
				if(record != null){
					record.recordNextId = id;
					id = record.id;
				}else{
					return new Node(id);
				}
			}else if(record.recordParentId == Global.INVALIDATE_ID &&
					record.recordPrevId == Global.INVALIDATE_ID){
				bEnd = true;
				return new Node(record.id);
				
			}else{
				return null;
			}
		}while(!bEnd);
		
		return null;
	}
	
	public void destroy(){
		
	}
}
