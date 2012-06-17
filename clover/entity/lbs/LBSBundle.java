package com.singularity.clover.entity.lbs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.util.Util;

public class LBSBundle implements Persisable,Serializable {
	
	private static final long serialVersionUID = 6949774277357798315L;

	public static final String TAG = "lbs_bundle";
	
	public static final int STATE_ENABLE = 1;
	public static final int STATE_DISABLE = 2;
	
	private long id = Global.INVALIDATE_ID;
	private String parentTag;

	private long parentId = Global.INVALIDATE_ID;
	private String name;
	private ArrayList<Coordinate> mCoordinateSet;
	private int mStatus = STATE_ENABLE;

	public  class Coordinate implements Serializable{
		private static final long serialVersionUID = -1688512850012723504L;
		Integer fixLatitude;
		Integer fixLongtitude;
		
		public int getFixLongtitude() {
			return fixLongtitude;
		}

		public void setFixLongtitude(int fixLongtitude) {
			this.fixLongtitude = fixLongtitude;
		}

		public int getFixLatitude() {
			return fixLatitude;
		}

		public void setFixLatitude(int fixLatitude) {
			this.fixLatitude = fixLatitude;
		}

		public Coordinate(int fixLatitude,int fixLongtitude){
			this.fixLatitude = fixLatitude;
			this.fixLongtitude = fixLongtitude;
		}
	}
	
	public static final String LBSBUNDLE_TABLE = 
		"CREATE TABLE IF NOT EXISTS "
	 	+ "lbs_bundle("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "parent_tag TEXT NOT NULL,"
		+ "parent_id INTEGER NOT NULL,"
		+ "name TEXT,"
		+ "create_date INTEGER,"
		+ "status INTEGER,"
		+ "coordinate BLOB,"
		+ "radius INTEGER,"
		+ "extra BLOB"
		+ ")";
	
	static{
		EntityPool.instance().register(TAG,new LBSBundle("Prototype"));
	}
	
	private LBSBundle(String prototpye){}
	
	/**
	 * @param longitude 不能为null
	 * @param latitude  不能为null
	 * @param parentId
	 * @param parentTag
	 */
	public LBSBundle(ArrayList<Coordinate> coordinateSet,long parentId, String parentTag){
		id = IdGenerator.nextId(TAG);
		EntityPool.instance().add(id, this, TAG);
		this.parentId = parentId;
		this.parentTag = parentTag;
		mCoordinateSet = coordinateSet;
	}
	
	public ArrayList<Coordinate> getCoordinateSet() {
		return mCoordinateSet;
	}

	public void setCoordinateSet(ArrayList<Coordinate> mCoordinateSet) {
		this.mCoordinateSet = mCoordinateSet;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getSchema() {
		return LBSBUNDLE_TABLE;
	}

	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("parent_tag",parentTag);
		content.put("parent_id",parentId);
		content.put("name",name);
		content.put("status",mStatus);
		try {
			if(mCoordinateSet.isEmpty()){
				content.putNull("coordinate");
			}else{
				content.put("coordinate",Util.serializeIdArray(mCoordinateSet));}
		} catch (IOException e) {
			e.printStackTrace();
		}
		storeHelper(content);	
	}

	@Override
	public void delete() {
		deleteInDB();
		EntityPool.instance().removeId(id, TAG);
	}
	
	public void deleteInDB(){
		DBAdapter.instance().deleteEntry(
				TAG, "_id=?",new String[]{Long.toString(id)});
		EntityPool.instance().removeId(id, TAG);
	}

	@Override
	public Persisable load(long id) {
		Cursor cur = DBAdapter.instance().retrieveById(TAG, id);
	    if(!cur.moveToFirst()){
	    	cur.close();
	    	return null;
	    }
		LBSBundle lbs = loadHelper(cur);
		cur.close();
		return lbs;
	}

	private LBSBundle loadHelper(Cursor cur){
		LBSBundle lbs = (LBSBundle) EntityPool.
			instance().getPrototype(TAG).create();
		lbs.id = cur.getLong(0);
		lbs.parentId = cur.getLong(2);
		lbs.parentTag = cur.getString(1);
		lbs.mStatus = cur.getInt(5);
		try {
			lbs.mCoordinateSet = (ArrayList<Coordinate>
					) Util.deserializeIdArray(cur,6);
			if(lbs.mCoordinateSet == null){
				lbs.mCoordinateSet = new ArrayList<Coordinate>();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		EntityPool.instance().add(lbs.id, lbs, TAG);
		return lbs;
	}
	
	@Override
	public Persisable create() {
		return new LBSBundle("Prototype");
	}

	@Override
	public ArrayList<Long> loadTable(String whereClause, String[] whereArgs) {
		Cursor cur = DBAdapter.instance().
			retrieveAll(TAG,whereClause,whereArgs);
	    if(!cur.moveToFirst()){
	    	cur.close();
	    	return null;
	    }
	    
	    ArrayList<Long> ids = new ArrayList<Long>();
	    do{
		    LBSBundle lbs = loadHelper(cur);   
		    ids.add(lbs.getId());
	    }while(cur.moveToNext());
	    cur.close();
	    return ids;
	}

	@Override
	public void clearTable() {
		// TODO Auto-generated method stub
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
	
	public int getStatus() {
		return mStatus;
	}

	public void setStatus(int mStatus) {
		this.mStatus = mStatus;
	}
	
		public String getParentTag() {
		return parentTag;
	}

	public void setParentTag(String parentTag) {
		this.parentTag = parentTag;
	}
	
	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
}
