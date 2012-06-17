package com.singularity.clover.entity.record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.Draggable;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.task.AbstractTask;
import com.singularity.clover.util.Util;

public abstract class Record implements Persisable,Draggable{
	public static String TAG = "record";
	
	protected HashMap<String, ArrayList<Long>> owenerMap = 
		new HashMap<String, ArrayList<Long>>();
	protected String content = null;
	protected long id = Global.INVALIDATE_ID;
	protected long createDate = Global.INVALIDATE_DATE;
	public String name = null;
	protected long recordParentId = Global.INVALIDATE_ID;
	protected long recordChildId = Global.INVALIDATE_ID;
	protected long recordNextId = Global.INVALIDATE_ID;
	protected long recordPrevId = Global.INVALIDATE_ID;
	protected int x=0,y=0;

	/* 采用广义表结构，见类Hierarchy
	 * record_prev_id与record_parent_id只会存在其一，并唯一
	 * 
	 * 如果为父节点(record_parent_id != INVALDATE_ID),
	 * 则在集合中通过id == record_parent_id寻找所有子节点*/
	private String RECORD_TABLE =
		"CREATE TABLE IF NOT EXISTS "
	 	+ "record("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "name TEXT,"
		+ "type TEXT,"
		+ "create_date INTEGER NOT NULL,"
		+ "content TEXT,"
		+ "parents BLOB,"               
		+ "record_parent_id INTEGER,"
		+ "record_prev_id INTEGER,"
		+ "x INTEGER,"
		+ "y INTEGER"
		+ ")";
	
	
	static{
		EntityPool.instance().register(TAG,new TextRecord("Prototype"));
	}
	
	/**
	 * entityPool中添加是以Record的TAG添加的，因为表结构在Record中
	 */
	public Record(){
		id = IdGenerator.nextId(TAG);
		EntityPool.instance().add(id, this, TAG);
		createDate = System.currentTimeMillis();
	}
	
	public Record(String prototype) {}
	
	
	public abstract String getTAG();
	
	public Set<Entry<String, ArrayList<Long>>> getParent(){
		return owenerMap.entrySet();
	}
	
	public void setParent(String tag,long parentId){
		if(owenerMap == null){
			owenerMap = new HashMap<String, ArrayList<Long>>();}
		
		if(owenerMap.containsKey(tag)){
			owenerMap.get(tag).add(parentId);
		}else{
			owenerMap.put(tag,new ArrayList<Long>());
			owenerMap.get(tag).add(parentId);
		}
	}
	
	public void removeParent(String parentTag,long parentId){
		if(owenerMap == null){
			return;}
		if(owenerMap.containsKey(parentTag)){
			owenerMap.get(parentTag).remove(parentId);
		}
	}
	
	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("name",name);
		content.put("create_date",createDate);
		content.put("type", getTAG());
		content.put("content",this.content);
		content.put("record_parent_id",recordParentId);
		content.put("record_prev_id",recordPrevId);
		content.put("x",x);
		content.put("y",y);
		
		try {
			boolean bEmpty = true;
			for(Entry<String, ArrayList<Long>> entry:owenerMap.entrySet()){
				bEmpty = bEmpty && entry.getValue().isEmpty();
			}
			if(bEmpty){
				content.putNull("parents");
			}else{
				content.put("parents",Util.serializeIdArray(owenerMap));}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		storeHelper(content);		
	}
	
	@Override
	public Persisable load(long id) {
		Cursor cur = DBAdapter.instance().execQuery("SELECT * FROM " + TAG + 
				" WHERE _id=?",new String[] { Long.toString(id) });
	    if(!cur.moveToFirst()){
	    	cur.close();
	    	return null;
	    }
	
		Record record = loadHelper(cur);
		cur.close();
		return record;
	}
		
	protected void storeHelper(ContentValues content){
		Cursor cur = DBAdapter.instance().retrieveById(TAG, id);
		if(!cur.moveToFirst()){
			id = DBAdapter.instance().insert(TAG, content);
		} else {
			DBAdapter.instance().updateEntry(TAG, id, content);
		}
		cur.close();
	}
	
	public void setContent(String content){
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
	public final String getSchema(){
		return RECORD_TABLE;
	}
	
	public final ArrayList<Long> loadTable(String whereClause,String[] whereArgs){
		Cursor cur = DBAdapter.instance().
			retrieveAll(TAG,whereClause,whereArgs);
		ArrayList<Long> ids = new ArrayList<Long>();
	    if(cur.moveToFirst()){
	    	do{
	    		Record record = loadHelper(cur);   
	    		ids.add(record.getId());
	    	}while(cur.moveToNext());
	    }        
	    cur.close();
	    return ids;
	}

	@SuppressWarnings("unchecked")
	private Record loadHelper(Cursor cur){
		String tag = cur.getString(2);
		Record record = (Record) EntityPool.instance().getPrototype(tag).create();
		record.id = cur.getLong(0);
		record.name = cur.getString(1);
		record.createDate = cur.getLong(3);
		record.content = cur.getString(4);
		record.recordParentId = cur.getLong(6);
		record.recordPrevId=cur.getLong(7);
		record.x = cur.getInt(8);
		record.y = cur.getInt(9);
		
		
		try {
			
			HashMap<String, ArrayList<Long>> it = (HashMap<String, 
					ArrayList<Long>>) Util.deserializeIdArray(cur,5);
			if(it != null){
				record.owenerMap = it;}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		EntityPool.instance().add(record.id, record, TAG);
		return record;
	}
	
	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		createDate = System.currentTimeMillis();
	}
	
	
	@Override
	public void delete() {
		for(Entry<String, ArrayList<Long>> entry:owenerMap.entrySet()){
			for(long id:entry.getValue()){
				AbstractTask ow = (AbstractTask) EntityPool.instance().
					forId(id, entry.getKey());
				ow.attachment.detach(getTAG(), this.id);
				ow.updateRecords();
			}
		}
		DBAdapter.instance().deleteEntry(
				TAG, "_id=?",new String[]{Long.toString(id)});
		EntityPool.instance().removeId(id, TAG);
	}
	
	@Override
	public final void clearTable(){
		DBAdapter.instance().deleteEntry(TAG, "1", null);
	}
		
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void updatePosition(int x, int y) {
		ContentValues content = new ContentValues();
		this.x = x;
		this.y = y;
		content.put("x",x);
		content.put("y",y);
		DBAdapter.instance().updateEntry(TAG, id, content);	
	}

	public long getRecordParentId() {
		return recordParentId;
	}

	public void setRecordParentId(long recordParentId) {
		this.recordParentId = recordParentId;
	}

	public long getRecordPrevId() {
		return recordPrevId;
	}

	public void setRecordPrevId(long recordPrevId) {
		this.recordPrevId = recordPrevId;
	}
	
		public long getRecordChildId() {
		return recordChildId;
	}

	public void setRecordChildId(long recordChildId) {
		this.recordChildId = recordChildId;
	}

	public long getRecordNextId() {
		return recordNextId;
	}

	public void setRecordNextId(long recordNextId) {
		this.recordNextId = recordNextId;
	}
	
	public abstract Bitmap convertoBitmap(Context context);
	public abstract void recycle();
	
	public abstract Record recentRecord();
}
