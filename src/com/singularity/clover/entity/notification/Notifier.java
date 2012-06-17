package com.singularity.clover.entity.notification;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.database.Cursor;

import com.singularity.clover.Global;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.activity.notification.CalendarActivity;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.Draggable;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.task.AbstractTask;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.notification.NotifierBinder.NotifierType;

public class Notifier implements Persisable,Draggable{
	public static String TAG = "notifier";
	
	private static CalendarActivity mActivity = null;

	private long id = Global.INVALIDATE_ID;
	private long owenerId = Global.INVALIDATE_ID;
	private int type = NotifierType.SYSTEM_NOTIFICATION.getType();
	private long createDate = Global.INVALIDATE_DATE;
	private long triggerDate = Global.INVALIDATE_DATE;
	private PendingIntent mPendingIntent = null;

	private String NOTIFIER_TABLE =
		"CREATE TABLE IF NOT EXISTS "
	 	+ "notifier("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "owener_id INTEGER,"
		+ "type INTEGER,"
		+ "create_date INTEGER NOT NULL,"
		+ "trigger_date INTEGER NOT NULL"
		+ ")";

	static{
		EntityPool.instance().register(TAG,new Notifier("Prototype"));
	}
	
	public Notifier(long triggerDate,long owenerId) {
		id = IdGenerator.nextId(TAG);
		EntityPool.instance().add(id, this, TAG);
		this.triggerDate = triggerDate;
		this.owenerId = owenerId;
	}

	private Notifier(String prototype){}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getSchema() {
		return NOTIFIER_TABLE;
	}

	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("create_date",createDate);
		content.put("type",type);
		content.put("trigger_date",triggerDate);
		content.put("owener_id",owenerId);
		storeHelper(content);	
	}

	@Override
	public void delete() {
		AbstractTask task = (AbstractTask) EntityPool.instance().forId(owenerId, Task.TAG);
		task.notification.remove(id);
		task.store();
		deleteInDB();
	}
	
	public void deleteInDB(){
		DBAdapter.instance().deleteEntry(
				TAG, "_id=?",new String[]{Long.toString(id)});
		EntityPool.instance().removeId(id, TAG);
		SingularityApplication.instance().getNotifierBinder().cancelAlarm(mPendingIntent);
	}

	@Override
	public Persisable load(long id) {
		Cursor cur = DBAdapter.instance().retrieveById(TAG, id);
	    if(!cur.moveToFirst()){
	    	cur.close();
	    	return null;
	    }
		Notifier notifier = loadHelper(cur);
		cur.close();
		return notifier;
	}

	private Notifier loadHelper(Cursor cur){
		String type = cur.getString(2);
		Notifier notifier = (Notifier) EntityPool.
			instance().getPrototype(TAG).create();
		notifier.id = cur.getLong(0);
		notifier.owenerId = cur.getLong(1);
		notifier.type = cur.getInt(2);
		notifier.createDate = cur.getLong(3);
		notifier.triggerDate = cur.getLong(4);
		
		EntityPool.instance().add(notifier.id, notifier, TAG);
		return notifier;
	}
		
	@Override
	public Persisable create() {
		return new Notifier("Prototype");
	}

	@Override
	public ArrayList<Long> loadTable(String whereClause,String[] whereArgs) {
		Cursor cur = DBAdapter.instance().
			retrieveAll(TAG,whereClause,whereArgs);
	    if(!cur.moveToFirst()){
	    	cur.close();
	    	return null;
	    }
	    
	    ArrayList<Long> ids = new ArrayList<Long>();
	    do{
		    Notifier notifier = loadHelper(cur);   
		    ids.add(notifier.getId());
	    }while(cur.moveToNext());
	    cur.close();
	    return ids;
	}

	@Override
	public void clearTable() {
		// TODO Auto-generated method stub

	}

	public String getTAG(){
		return TAG;
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
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getTriggerDate() {
		return triggerDate;
	}

	public void setTriggerDate(long triggerDate) {
		this.triggerDate = triggerDate;
	}

	@Override
	public void updatePosition(int x, int y) {
		if(mActivity != null){
			triggerDate = mActivity.caculateToTime(new int[]{x,y});
			store();}
	}
	
	public static void removeActivity() {
		mActivity = null;
	}

	public static void setActivity(CalendarActivity mActivity) {
		Notifier.mActivity = mActivity;
	}
	
	public long getOwenerId(){
		return owenerId;
	}

	public void attachPendingIntent(PendingIntent pendingIntent) {
		mPendingIntent = pendingIntent;
	}
}
