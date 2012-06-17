package com.singularity.clover.entity.wrapper;


import android.content.ContentValues;
import android.database.Cursor;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.Persisable;

public abstract class AbstractWrapper implements Persisable {
	
	protected long id = Global.INVALIDATE_ID;
	protected String name = null;
	protected int iconResId = Global.INVALIDATE_RESID;
	
	protected void storeHelper(ContentValues content){
		Cursor cur = DBAdapter.instance().retrieveById(getTAG(), id);
		if(!cur.moveToFirst()){
			id = DBAdapter.instance().insert(getTAG(), content);
		} else {
			DBAdapter.instance().updateEntry(getTAG(), id, content);
		}
		cur.close();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	protected abstract String getTAG();
	protected abstract void sortBy();
	protected abstract void filterBy();
}
