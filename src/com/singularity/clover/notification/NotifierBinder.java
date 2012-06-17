package com.singularity.clover.notification;

import java.util.ArrayList;
import java.util.TreeMap;

import com.singularity.clover.Global;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.notification.Notifier;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotifierBinder {
	
	public static final String ACTION_ALARM_RING = "com.singularity.alarm.RING";
	public static final String ACTION_REFRESH_ALL = "com.singularity.alarm.REFRESH_ALL";
	private static Context _context;
	private PendingIntent pendingIntent = null;
	private TreeMap<Long, Long> notifierMaps = null;
	
	public enum NotifierType {
		ALARM_RING(0), ALARM_VIBRATE(2), 
		ALARM_NOSOUND(4), SYSTEM_NOTIFICATION(8);

		int code;
		private NotifierType(int code) {
			this.code = code;
		}
		
		public int getType() {
			return code;
		}
	}
	
	public static void initialize(Context context){
		_context = context;
	}

	public long bindNotifer() {
		long nextRing = getNextNotifer();
		if (nextRing < System.currentTimeMillis()) {	// another day
			return 0;
		}
		Intent intent = new Intent(_context, NotifierReceiver.class);
		intent.setAction(ACTION_ALARM_RING);
		Notifier notifier = (Notifier) EntityPool.instance().
			forId(notifierMaps.get(nextRing),Notifier.TAG);
		intent.putExtra(NotifierReceiver.IN_NOTIFIY_TASK_ID, 
				notifier.getOwenerId());
		pendingIntent = PendingIntent.getBroadcast(_context, 0, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) _context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextRing, pendingIntent);
		notifier.attachPendingIntent(pendingIntent);
		return nextRing;
	}
	
	public void cancelAlarm(PendingIntent pendingIntent) {
		if (pendingIntent != null) {
			AlarmManager alarmManager = (AlarmManager) _context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}
	}
	
	public long getNextNotifer(){
		if(notifierMaps == null){
			notifierMaps = new TreeMap<Long, Long>();
		}else{
			notifierMaps.clear();}

		String whereClause = "WHERE trigger_date > ? ORDER BY trigger_date ASC";
		String[] whereArgs = new String[]{
				Long.toString(System.currentTimeMillis())};
		ArrayList<Long> ids = EntityPool.instance().
			getPrototype(Notifier.TAG).loadTable(whereClause,whereArgs);
		if(ids == null){
			return Global.INVALIDATE_DATE;}
		
		for(long entry:ids){
			Notifier notifier = (Notifier) EntityPool.
				instance().forId(entry, Notifier.TAG);
			notifierMaps.put(notifier.getTriggerDate(),entry);
		}
		
		if(notifierMaps.isEmpty()){
			return Global.INVALIDATE_DATE;
		}else{
			return notifierMaps.firstKey();}
	}
	
	public void findType(int code){
		if((code & NotifierType.ALARM_RING.getType()) != 0){
			
		}else if((code & NotifierType.ALARM_VIBRATE.getType()) != 0){
			
		}else if((code & NotifierType.ALARM_NOSOUND.getType()) != 0){
			
		}else if((code & NotifierType.SYSTEM_NOTIFICATION.getType()) != 0){
			
		}
	}

}
