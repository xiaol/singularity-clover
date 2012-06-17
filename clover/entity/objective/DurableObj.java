package com.singularity.clover.entity.objective;

import java.text.NumberFormat;
import java.util.NoSuchElementException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.Time;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.notification.NotifierReceiver;

public class DurableObj extends AbstractObjective {
	public static final String TAG = "durable_obj";
	public static final String ACTION_ALARM_GOAL = "com.singularity.alarm.GOAL";
	
	public static final int MINUTE_UNIT = 1;
	public static final int HOUR_UINT = 60;
	
	private long sTick = Global.INVALIDATE_DATE;
	private long eTick = Global.INVALIDATE_DATE;
	private long maxCount = Global.INITIAL_DURATION;
	private long elapsedCount = 0;

	private int unit = MINUTE_UNIT;
	private boolean bRunning = false;
	private PendingIntent mPending = null;

	public static String DURABLEOBJ_TABLE =
		"CREATE TABLE IF NOT EXISTS "
		+ "durable_obj("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "owener_id INTEGER NOT NULL,"
		+ "name TEXT,"
		+ "start_tick INTEGER NOT NULL,"
		+ "end_tick INTEGER NOT NULL,"
		+ "elapse_count INTEGER NOT NULL,"
		+ "max_count INTEGER NOT NULL,"
		+ "unit INTEGER NOT NULL,"
		+ "is_running INTEGER NOT NULL,"
		+ "x INTEGER,"
		+ "y INTEGER"
		+ ")";
	
	static{
		DurableObj prototype = new DurableObj("Prototype");
		EntityPool.instance().register(TAG,prototype);
		OBJFactory.register(TAG, prototype);
	}
	
	public DurableObj(Task parent) {
		super(parent);
	}

	private DurableObj(String prototype) {super(prototype);}

	public Persisable load(long dbId){	
		DurableObj durable = new DurableObj("Prototype");
		Cursor cursor = DBAdapter.instance().retrieveById(TAG,dbId);
		if(cursor.moveToFirst()){
			durable.id = dbId;
			durable.owener_id = cursor.getLong(1);
			durable.name = cursor.getString(2);
			durable.sTick = cursor.getLong(3);
			durable.eTick = cursor.getLong(4);
			durable.elapsedCount = cursor.getLong(5);
			durable.maxCount = cursor.getLong(6);
			durable.unit = cursor.getInt(7);
			durable.bRunning = cursor.getInt(8)>0?true:false;
			durable.x = cursor.getInt(9);
			durable.y = cursor.getInt(10);
		}else{
			//throw new NoSuchElementException("No match row found");
			cursor.close();
			return null;
		}	
		EntityPool.instance().add(durable.id,durable,TAG);
		cursor.close();
		return durable;
	}
	
	@Override
	public long getId() {
		return id;
	}

	public void set(int maxCount,int unit){
		this.maxCount = maxCount;
		this.unit = unit;
	}
	
	public void setRunning(boolean bRunning){
		this.bRunning = bRunning;
		Time now = new Time();
		now.setToNow();
		if(bRunning){
			sTick = now.toMillis(false);
		}else{
			eTick = now.toMillis(false);
			elapsedCount = elapsedCount + (eTick - sTick);
			if(elapsedCount > maxCount*unit*60*1000){
				if(maxCount != 0){
					bDone = true;
				}else{
					bDone = false;
				}
			}
		}
	}
	
	public void reset(){
		bRunning = false;
		elapsedCount = 0;
		bDone = false;
	}
	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("name",name);
		content.put("start_tick",sTick);
		content.put("end_tick",eTick);
		content.put("owener_id",owener_id);
		content.put("elapse_count",elapsedCount);
		content.put("max_count",maxCount);
		content.put("unit",unit);
		content.put("is_running",bRunning?1:0);
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
		if(elapsedCount > maxCount*unit*60*1000){
			getParent().setProcess(100, 100);
			bDone = true;
		}else{
			if(bRunning){
				Time now = new Time();
				now.setToNow();
				elapsedCount = elapsedCount + (now.toMillis(false) - sTick);
				sTick = now.toMillis(false);
				if(elapsedCount < maxCount*unit*60*1000){
					getParent().setProcess(100, (long) (elapsedCount/(float)(maxCount*unit*60*1000)*100));
					bDone = false;
				}else{
					if(maxCount == 0){
						getParent().setProcess(100, 0);
						bDone = false;
					}else{
						getParent().setProcess(100, 100);
						bDone = true;
					}
				}
			}
		}
	}

	@Override
	public String getSchema() {
		return DURABLEOBJ_TABLE;
	}

	@Override
	public Persisable create() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRunning(){
		return bRunning;
	}
	
	public long getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(long maxCount) {
		this.maxCount = maxCount;
	}

	
	public float getElaspeCount() {
		return (int)(elapsedCount/(float)(unit*60*1000)*100)/100f;
	}
	
	public String getUnitString(){
		if(unit == MINUTE_UNIT){
			return SingularityApplication.instance().getText(R.string.mininute_short).toString();
		}else if(unit == HOUR_UINT){
			return SingularityApplication.instance().getText(R.string.hour_short).toString();
		}
		return " ";
	}
	
	public int getUnit(){
		return unit;
	}
	
	public long howLongExpired(){
		processValidate();
		long result;
		result = (maxCount*unit*60*1000 - elapsedCount);
		return result;
	}
	
	public void setAlarm(Context context,long time){
		if(mPending == null){
			Intent intent = new Intent(context, NotifierReceiver.class);
			intent.setAction(ACTION_ALARM_GOAL);
	
			intent.putExtra(NotifierReceiver.IN_NOTIFIY_TASK_ID, 
					getParent().getId());
			mPending = PendingIntent.getBroadcast(context, 0, intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, time, mPending);
		}
	}

	public void cancelAlarm(){
		SingularityApplication.instance().getNotifierBinder().cancelAlarm(mPending);
		mPending = null;
	}
}
