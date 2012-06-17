package com.singularity.clover.entity.objective;

import java.util.NoSuchElementException;
import android.content.ContentValues;
import android.database.Cursor;

import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.task.Task;

public class CheckableObj extends AbstractObjective {
	
	public static final String TAG = "checkable_obj";
	private boolean bCheck = false;
	
	public static String CHECKABLEOBJ_TABLE =
		"CREATE TABLE IF NOT EXISTS "
		+ "checkable_obj("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "owener_id INTEGER NOT NULL,"
		+ "name TEXT,"
		+ "check_number INTEGER NOT NULL,"
		+ "x INTEGER,"
		+ "y INTEGER"
		+ ")";
	
	static{
		CheckableObj prototype = new CheckableObj("Prototype");
		EntityPool.instance().register(TAG,prototype);
		OBJFactory.register(TAG, prototype);
	}
	
	public CheckableObj(Task parent) {
		super(parent);
	}

	private CheckableObj(String prototype) {super(prototype);}

	public Persisable load(long dbId){
		CheckableObj check = new CheckableObj("Prototype");	
		Cursor cursor = DBAdapter.instance().retrieveById(TAG,dbId);
		if( cursor.moveToFirst()){
			check.id = dbId;
			check.owener_id = cursor.getLong(1);
			check.name = cursor.getString(2);
			check.bCheck = cursor.getInt(3) > 0?true:false;
			check.bDone = check.bCheck;
			check.x = cursor.getInt(4);
			check.y = cursor.getInt(5);
		}else{
			//throw new NoSuchElementException("No match row found");
			cursor.close();
			return null;
		}
		EntityPool.instance().add(check.id,check,TAG);
		cursor.close();
		return check;
	}
	
	
	public void check(){
		bCheck = true;
		bDone = true;
		//getParent().processValidate();
	}
	
	public void unCheck(){
		bCheck = false;
		bDone = false;
		//getParent().processValidate();
	}
	
	public boolean getCheckState(){
		return bCheck;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("name",name);
		content.put("check_number",bCheck);
		content.put("owener_id",owener_id);
		content.put("x",x);
		content.put("y",y);

		storeHelper(content);
	}

	@Override
	public String getTAG() {
		return TAG;
	}

	@Override
	public void processValidate() {
		if(bCheck){
			getParent().setProcess(100, 100);
		}else{
			getParent().setProcess(100, 0);
		}
		
	}

	@Override
	public String getSchema() {
		return CHECKABLEOBJ_TABLE;
	}

	@Override
	public Persisable create() {
		// TODO Auto-generated method stub
		return null;
	}

}
