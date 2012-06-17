package com.singularity.clover.entity.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.util.Util;

public class Plan extends AbstractTask {
	public static final String TAG = "plan";
	private LinkedList<AbstractTask> children = null;
	private ArrayList<Long> childrenIds = null;
	
	public static final String PLAN_TABLE = 
		"CREATE TABLE IF NOT EXISTS "
	 	+ "plan("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "parent_id,"
		+ "name TEXT,"
		+ "create_date INTEGER NOT NULL,"
		+ "start_date INTEGER,"
		+ "end_date INTEGER,"
		+ "priority TEXT NOT NULL,"
		+ "status INTEGER NOT NULL,"
		+ "children BLOB," 
		+ "records BLOB"
		+ ")";
	
	static{
		EntityPool.instance().register(TAG,new Plan("Prototype"));
	}
	
	public Plan(){
		children = new LinkedList<AbstractTask>();
		childrenIds = new ArrayList<Long>();
	}
	
	public Plan(String prototype) {super(prototype);}

	public Persisable load(long id){
		Cursor cur = DBAdapter.instance().retrieveById(TAG, id);
		Plan plan;
		if(!cur.moveToFirst())
			plan = null;
		else
			plan = loadHelper(cur);	
		cur.close();
		return plan;
	}
	
	@SuppressWarnings("unchecked")
	public Plan loadHelper(Cursor cur){
		Plan plan = new Plan("Prototype");
		plan.id = id;
		plan.name = cur.getString(2);
		plan.createDate = cur.getLong(3);
		plan.startDate = cur.getLong(4);
		plan.endDate = cur.getLong(5);
		plan.priority = Priority.valueOf(cur.getString(6));
		plan.status = cur.getInt(7);

		try {
			plan.childrenIds = (ArrayList<Long>) Util.
				deserializeIdArray(cur, 8);
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		EntityPool.instance().add(plan.id, plan, TAG);
		return plan;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("name",name);
		content.put("create_date",createDate);
		content.put("start_date",startDate);
		content.put("end_date",endDate);
		content.put("priority",priority.name());
		content.put("status",status);
		
		try {
			content.put("children",Util.serializeIdArray(childrenIds));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		storeHelper(TAG, content);
	}

	public int size(){
		return children.size();
	}
	
	@Override
	public String getTAG() {
		return TAG;
	}

	public void add(AbstractTask child) throws IOException{
		if(child.getId() == Global.INVALIDATE_ID)
			child.store();
		children.add(child);
		childrenIds.add(child.getId());
	}
	
	public void remove(AbstractTask child, Boolean bDelete){
		children.remove(child);
		if( bDelete ){
			child.delete();
		}
	}

	public AbstractTask getChildAt(int index) {
		return children.get(index);
	}

	public int getChildCount(){
		return children.size();
	}
	
	public void changeIndex(int src, int des) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSchema() {
		return PLAN_TABLE;
	}

	@Override
	public Persisable create() {
		return new Plan("Prototype");
	}
	
	@Override
	protected void onEndDateChange() {}
	protected void onStartDateChange() {}
	public void processValidate() {}

	@Override
	public ArrayList<Long> loadTable(String whereClause, String[] whereArgs) {
		Cursor cur = DBAdapter.instance().
			retrieveAll(TAG,whereClause,whereArgs);
		ArrayList<Long> ids = null;
	    if(!cur.moveToFirst()){
	    }else{
		    ids = new ArrayList<Long>();
		    do{
			    Plan plan = loadHelper(cur);   
			    ids.add(plan.getId());
		    }while(cur.moveToNext());
	    }
	    cur.close();
	    return ids;
	}

	public void updateRecords() {
	}

	@Override
	public float getProcess() {
		return 0f;
	}
	
}
