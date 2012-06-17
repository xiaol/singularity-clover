package com.singularity.clover.entity.objective;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.Draggable;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.task.Task;

public abstract class AbstractObjective implements Persisable,Draggable{

	public String name = null;
	protected long id = Global.INVALIDATE_ID;
	protected long owener_id = Global.INVALIDATE_ID;
	protected boolean bDone = false;
	private Task parent = null;
	protected int x,y;
	
	public AbstractObjective(Task parent){
		id = IdGenerator.nextId(getTAG());
		owener_id = parent.getId();
		this.parent = parent;
		EntityPool.instance().add(id, this, getTAG());
	}
	
	public AbstractObjective(String prototype) {}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract void processValidate();
	
	public boolean isDone(){
		return bDone;
	}
	
	public abstract String getTAG();
	
	public void clearTable(){
		DBAdapter.instance().deleteEntry(getTAG(), "1", null);
	}
	
	protected void storeHelper(ContentValues content){
		Cursor cur = DBAdapter.instance().retrieveById(getTAG(), id);
		if(!cur.moveToFirst()){
			id = DBAdapter.instance().insert(getTAG(), content);
		} else {
			DBAdapter.instance().updateEntry(getTAG(), id, content);
		}
		cur.close();
		getParent().getObjs().addId(getTAG(),id);
	}
	
	public void delete(){
		getParent().removeOBJ(this);
		String[] whereArgs = new String[] { Long.toString(id) };
		DBAdapter.instance().deleteEntry(getTAG(), "_id=?", whereArgs);
		EntityPool.instance().removeId(id, getTAG());
	}
	
	public void deleteInDB(){
		String[] whereArgs = new String[] { Long.toString(id) };
		DBAdapter.instance().deleteEntry(getTAG(), "_id=?", whereArgs);
		EntityPool.instance().removeId(id, getTAG());
	}
	
	public Task getParent(){
		parent = (Task) EntityPool.instance().forId(owener_id,Task.TAG);
		return parent;
	}
	
	public ArrayList<Long> loadTable(String whereClause,String[] whereArgs){
		return null;
	}
	
	@Override
	public void updatePosition(int x, int y) {
		ContentValues content = new ContentValues();
		this.x = x;
		this.y = y;
		content.put("x",x);
		content.put("y",y);
		DBAdapter.instance().updateEntry(getTAG(), id, content);	
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
}
