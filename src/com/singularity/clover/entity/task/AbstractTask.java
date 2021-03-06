package com.singularity.clover.entity.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.Time;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.notification.Notifier;
import com.singularity.clover.entity.record.Record;


public abstract class AbstractTask implements Persisable{
	public String name = null;
	protected long id = Global.INVALIDATE_ID;
	protected long createDate = Global.INVALIDATE_DATE;
	protected int estimate = Global.INVALIDATE_ESTIMATE;
	protected long startDate = Global.INVALIDATE_DATE;
	protected long endDate = Global.INVALIDATE_DATE;
	
	protected Priority priority = Priority.INVALIDATE;
	protected int status = Status.INVALIDATE.getStatus();
	public Attachment attachment = new Attachment();
	public Notification notification = new Notification();
	
	public int mDay;
	
	public AbstractTask(){
		id = IdGenerator.nextId(getTAG());
		EntityPool.instance().add(id, this, getTAG());
	}
	
	public AbstractTask(String prototype){}

	public enum Priority {
		HIGH(0), MEDIUM(2), LOW(4), NONE(8),INVALIDATE(256);
		int iPriority;
		private Priority(int priority) {
			iPriority = priority;
		}
		public int getPriority(){
			return iPriority;
		}
	}
	
	public enum FilterType{
		STATUS_ONGOING,STATUS_WAITINGTODO,STATUS_DOING,
		STATUS_DONE,STATUS_EXPIRED,PRIORITY,ESTIMATE;
	}
	
	public enum Status {
		ONGOING(4), WAITINGTODO(2), DOING(0), DONE(8), EXPIRED(16),INVALIDATE(256);
		int iStatus;
		private Status(int status){
			iStatus = status;
		}
		public int getStatus(){
			return iStatus;
		}
	}
	
	public int getStatus(){
		return status;
	}
	
	public void updateStatus(){
		Time it = new Time();
		it.setToNow();	
		int today = Time.getJulianDay(it.toMillis(false), it.gmtoff);
		
		if( startDate != Global.INVALIDATE_DATE && endDate != Global.INVALIDATE_DATE){ 
			int end = Time.getJulianDay(endDate, it.gmtoff);
			int start = Time.getJulianDay(startDate, it.gmtoff);
			if( today > end ){
				status = Status.EXPIRED.getStatus();
			}else if( today <= end && today >= start){
				mDay = end - today;
				status = Status.WAITINGTODO.getStatus();
			}else if( today < start ){
				mDay = start - today;
				status = Status.ONGOING.getStatus();
			}
		}else if( startDate != Global.INVALIDATE_DATE){
			int start = Time.getJulianDay(startDate, it.gmtoff);
			if( today < start ){
				mDay = start - today;
				status = Status.ONGOING.getStatus();
			}else if(today == start ){
				status = Status.WAITINGTODO.getStatus();
			}else{
				status = Status.EXPIRED.getStatus();
			}
		}else if( endDate != Global.INVALIDATE_DATE ){
			int end = Time.getJulianDay(endDate, it.gmtoff);
			if( today > end ){
				status = Status.EXPIRED.getStatus();
			}else if( today <= end){
				mDay = end - today;
				status = Status.WAITINGTODO.getStatus();
			}
		}else{
			status = Status.INVALIDATE.getStatus();
		}
		
		
	}
	
	public void setStatus(int s){
		status = s;
	}
	
	public abstract void processValidate();

	public abstract float getProcess();
	
	public long getStartDate(){
		return startDate;
	}
	
	public long getEndDate(){
		return endDate;
	}
	
	public void setEndDate(long end){
		endDate = end;
		onEndDateChange();	
	}
	
	public void setStartDate(long start){
		startDate = start;
		onStartDateChange();
	}
	
	public abstract String getTAG();
	
	public void clearTable(){
		DBAdapter.instance().deleteEntry(getTAG(), "1", null);
	}
	
	protected void storeHelper(String tag,ContentValues content){
		Cursor cur = DBAdapter.instance().retrieveById(getTAG(), id);
		if(!cur.moveToFirst()){
			id = DBAdapter.instance().insert(getTAG(), content);
		} else {
			DBAdapter.instance().updateEntry(getTAG(), id, content);
		}
		cur.close();
	}
	
	public void delete(){
		String[] whereArgs = new String[] { Long.toString(id) };
		DBAdapter.instance().deleteEntry(getTAG(), "_id=?", whereArgs);
		EntityPool.instance().removeId(id, getTAG());
	}
		
	public class Attachment{
		protected LinkedList<String> records = null;
		
		private Attachment(){}
		
		public void detach(String tag,long recordId){
			if(records == null)
				records = new LinkedList<String>();
			records.remove(tag+","+recordId);
			Record record = (Record) EntityPool.instance().
				forId(recordId, Record.TAG);
			if(record != null){
				record.removeParent(getTAG(), id);
			}
		}
		
		public void detach(int index){
			records.remove(index);
		}
		
		public void attach(String tag,long recordId){
			if(records == null)
				records = new LinkedList<String>();
			if(!records.contains(tag+","+recordId))
				records.add(tag+","+recordId);
			Record record = (Record) EntityPool.instance().
				forId(recordId, Record.TAG);
			record.setParent(getTAG(), id);
		}
		
		public int getRecordsCount(){
			if(records == null){
				return 0;
			}else{
				return records.size();}
		}
		
		public Record getRecordAt(int index){
			String[] str = records.get(index).split(",");
			return (Record) EntityPool.instance()
					.forId(Long.parseLong(str[1]),Record.TAG);
		}
		
		public void setIds(LinkedList<String> ids){
			records = ids;
			if(records == null)
				records = new LinkedList<String>();
		}
		
		public boolean isEmpty(){
			if(records == null)
				return true;
			return records.isEmpty();
		}
		
		public String recentText(){
			Record result = null;
			for(int i=0;i<records.size();i++){
				Record record = getRecordAt(i);
				if(record == null){
					continue;}
				
				if(record.recentRecord() != null){
					if(result == null){
						result = record.recentRecord();
					}else{
						if(result.getCreateDate() < record.recentRecord().getCreateDate()){
							result = record.recentRecord();
						}
					}
				}
			}
			if(result != null){
				return result.getName();
			}else{
				return null;
			}
		}
	}
	
	public class Notification{
		protected LinkedList<Long> notifierIds = null;
		
		protected Notification(){
			notifierIds = new LinkedList<Long>();
		}
		
		public void add(long notifierId){
			notifierIds.add(notifierId);
		}
		
		public void remove(long notifierId){
			notifierIds.remove(notifierId);
		}
		
		public boolean isEmpty(){
			if(notifierIds == null)
				return true;
			return notifierIds.isEmpty();
		}
		
		public void setIds(LinkedList<Long> ids){
			notifierIds = ids;
			if(notifierIds == null)
				notifierIds = new LinkedList<Long>();
		}
		
		public ArrayList<Long> getIdsByDate(ArrayList<Long> ids,long date){
			for(long id:notifierIds){
				Notifier notifier = (Notifier) EntityPool.
					instance().forId(id, Notifier.TAG);
				long trigger = notifier.getTriggerDate();
				Time a = new Time();
				a.set(trigger);
				Time b = new Time();
				b.set(date);
				if( Time.getJulianDay(trigger, a.gmtoff) == Time.getJulianDay(date, b.gmtoff)){
					ids.add(id);
				}
			}
			return ids;
		}
		
		public void getDatesByMonth(ArrayList<Integer> dates,int month,int year){
			Time it = new Time();
			for(long id:notifierIds){
				Notifier notifier = (Notifier) EntityPool.
					instance().forId(id, Notifier.TAG);
				if(notifier == null){
					continue;}
				
				long trigger = notifier.getTriggerDate();
				it.set(trigger);
				if(it.month == month && it.year == year){
					dates.add(it.monthDay);
				}
			}
		}
		
		public long getNextNotifier(){
			if(notifierIds == null){
				return Global.INVALIDATE_DATE;}
			
			if(notifierIds.isEmpty()){
				return Global.INVALIDATE_DATE;}
			
			long wand = Global.INVALIDATE_DATE;
			boolean bInit = true;
			for(long entry:notifierIds){
				Notifier notifier = (Notifier) EntityPool.
					instance().forId(entry, Notifier.TAG);
				long now = System.currentTimeMillis();
				if(notifier == null){
					continue;}
				
				long trigger = notifier.getTriggerDate();
				if(trigger < now){	
					continue;
				}else if(bInit){
					wand = trigger;
					bInit =false;
					continue;
				}		
				if(trigger < wand){
					wand = trigger;
				}
			}
			return wand;
		}
		/*删除所有子提醒，并不维护父子关系*/
		public void clear(){
			for(long entry:notifierIds){
				Notifier notifier = (Notifier) EntityPool.instance().
					forId(entry, Notifier.TAG);
				notifier.deleteInDB();
			}
		}
	}
	
	protected ArrayList<Long> filterBy(FilterType type){
		return null;
	}
	
	
	protected abstract void onEndDateChange();
	protected abstract void onStartDateChange();

	public abstract void updateRecords();

}
