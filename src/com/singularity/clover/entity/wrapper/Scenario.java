package com.singularity.clover.entity.wrapper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.task.AbstractTask;
import com.singularity.clover.entity.task.Task;

public class Scenario extends AbstractWrapper {
	public final static String TAG = "scenario";
	
	private ArrayList<Long> ids = new ArrayList<Long>();

	private String SCENARIO_TABLE =
		"CREATE TABLE IF NOT EXISTS "
	 	+ "scenario("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "name TEXT NOT NULL,"
		+ "icon_res_id INTEGER NOT NULL"
		+ ")";

	static{
		EntityPool.instance().register(TAG,new Scenario("Prototype"));
	}
	
	public Scenario(String name,int resId) {
		id = IdGenerator.nextId(TAG);
		EntityPool.instance().add(id, this, TAG);
		this.name = name;
		this.iconResId = resId;
	}
	
	private Scenario(String prototype) {}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getSchema() {
		return SCENARIO_TABLE;
	}

	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("name",name);
		content.put("icon_res_id",iconResId);
		storeHelper(content);	
	}

	@Override
	public void delete() {
		String[] whereArgs = new String[] { Long.toString(id) };
		DBAdapter.instance().deleteEntry(Scenario.TAG, "_id=?", whereArgs);
		EntityPool.instance().removeId(id, TAG);
	}

	@Override
	public Persisable load(long id) {
		Cursor cur = DBAdapter.instance().retrieveById(TAG, id);
	    if(!cur.moveToFirst()){
	    	cur.close();
	    	return null;
	    }
	
		Scenario scenario = loadHelper(cur);
		cur.close();
		return scenario;
	}
	
	private Scenario loadHelper(Cursor cur){
		Scenario scenario = (Scenario) EntityPool.instance().getPrototype(TAG).create();
		scenario.id = cur.getLong(0);
		scenario.name = cur.getString(1);
		scenario.iconResId = cur.getInt(2);

		EntityPool.instance().add(scenario.id, scenario, TAG);
		return scenario;
	}
	
	@Override
	public Persisable create() {
		return new Scenario("Prototype");
	}

	@Override
	public ArrayList<Long> loadTable(String whereClause,String[] whereArgs) {
		Cursor cur = DBAdapter.instance().retrieveAll(TAG,whereClause,whereArgs);
		ArrayList<Long> ids = null;
	    if(!cur.moveToFirst()){
	    }else{
		    ids = new ArrayList<Long>();
		    do{
			    Scenario scenario = loadHelper(cur);   
			    ids.add(scenario.getId());
		    }while(cur.moveToNext());    
	    }
	    cur.close();
	    return ids;
	}

	@Override
	public void clearTable() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sortBy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void filterBy() {
		Cursor cur = DBAdapter.instance().execQuery("SELECT * FROM" +Task.TAG+"WHERE "
				+TAG+"=?",new String[]{Long.toString(id)});
		Task prototype = (Task) EntityPool.instance().getPrototype(Task.TAG);
		ids.clear();
			
		if(!cur.moveToFirst()){
	    	cur.close();return;
	    }
		
	    do{
		    AbstractTask task =  prototype.loadHelper(cur);
		    ids.add(task.getId());
	    }while(cur.moveToNext());
	    cur.close();	
	}

	@Override
	protected String getTAG() {
		return TAG;
	}
	
	public void setParams(String name,int resId){
		this.name = name;
		this.iconResId = resId;
	}

	public int getResId(){
		return iconResId;
	}
}
