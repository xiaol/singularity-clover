package com.singularity.clover.entity.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.text.format.Time;

import com.singularity.clover.Global;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.objective.AbstractObjective;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.util.Util;


public class Task extends AbstractTask {
	public static final String TAG = "task";
	
	public final static int TASK_NOT_PERIODIC = 0;
	private	OBJIds OBJs = new OBJIds();	
	private long max = Global.INITIAL_TASK_MAX;
	private long process = Global.INITIAL_TASK_VALUE;
	private long scenarioId = Global.INVALIDATE_ID;
	private long parentId = Global.INVALIDATE_ID;
	private int periodic = TASK_NOT_PERIODIC;

	public static final String TASK_TABLE = 
		"CREATE TABLE IF NOT EXISTS "
	 	+ "task("
		+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "parent_id INTEGER,"
		+ "scenario INTEGER,"
		+ "name TEXT,"
		+ "create_date INTEGER NOT NULL,"
		+ "start_date INTEGER,"
		+ "end_date INTEGER,"
		+ "estimate INTEGER,"
		+ "priority TEXT NOT NULL,"
		+ "status INTEGER NOT NULL,"
		+ "objectives BLOB,"
		+ "records BLOB,"
		+ "periodic INTEGER,"
		+ "notifiers BLOB"
		+ ")";
	
	static{
		EntityPool.instance().register(TAG,new Task("Prototype"));
	}

	public class OBJIds{
		private ArrayList<String> idsList = null;
		public OBJIds(){}	
		
		public void setIds(ArrayList<String> ids){
			idsList = ids;
			if(idsList == null)
				idsList = new ArrayList<String>();
		}
		
		public void addId(String tag,long id){
			if(idsList == null)
				idsList = new ArrayList<String>();
			if(!idsList.contains(tag +","+id))
				idsList.add(tag+","+id);
		}
		
		public boolean containId(String tag,long id){
			if(idsList == null)
				return false;
			return idsList.contains(tag+","+id);
		}
		
		public boolean isEmpty(){
			if(idsList == null)
				return true;
			return idsList.isEmpty();
		}
		
		public int getCount(){
			if(idsList == null){
				return 0;
			}else{
				return idsList.size();}
		}
		
	}
	
	public Task(){}
	
	private Task(String prototype) {super(prototype);}

	public Persisable load(long id){
		Cursor cur = DBAdapter.instance().retrieveById(TAG, id);
		AbstractTask task; 	
		if(!cur.moveToFirst())
			task = null;
		else
			task = loadHelper(cur);
		cur.close();
		return task;
	}
	
	@SuppressWarnings("unchecked")
	public AbstractTask loadHelper(Cursor cur){
		Task task = new Task("Prototype");
		task.id = cur.getLong(0);
		task.parentId = cur.getLong(1);
		task.scenarioId = cur.getLong(2);
		task.name = cur.getString(3);
		task.createDate = cur.getLong(4);
		task.startDate = cur.getLong(5);
		task.endDate = cur.getLong(6);
		task.estimate = cur.getInt(7);
		task.priority = Priority.valueOf(cur.getString(8));
		task.status = cur.getInt(9);
		
		try {
			task.OBJs.setIds((ArrayList<String>) Util.deserializeIdArray(cur,10));
			task.attachment.setIds((LinkedList<String>) Util.deserializeIdArray(cur, 11));
			task.notification.setIds((LinkedList<Long>) Util.deserializeIdArray(cur, 13));
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		task.periodic = cur.getInt(12);
		
		/*
		for(String entry:task.OBJs.idsList){
			String[] str = entry.split(",");
			EntityPool.instance().forId(Long.parseLong(str[1]), str[0]);
		}
		
		for(String entry:task.attachment.records){
			String[] str = entry.split(",");
			EntityPool.instance().forId(Long.parseLong(str[1]), str[0]);
		}*/
		
		EntityPool.instance().add(task.id, task, TAG);
		return task;
	}
	@Override
	public void store() {
		ContentValues content = new ContentValues();
		content.put("parent_id",parentId);
		content.put("scenario",scenarioId);
		content.put("name",name);
		content.put("create_date",createDate);
		content.put("start_date",startDate);
		content.put("end_date",endDate);
		content.put("estimate",estimate);
		content.put("priority",priority.name());
		content.put("status",status);
		try {
			if(OBJs.isEmpty()){
				content.putNull("objectives");
			}else{
				content.put("objectives",Util.serializeIdArray(OBJs.idsList));}
			if(attachment.isEmpty()){
				content.putNull("records");
			}else{
				content.put("records",Util.serializeIdArray(attachment.records));}
			
			if(notification.isEmpty()){
				content.putNull("notifiers");
			}else{
				content.put("notifiers",
						Util.serializeIdArray(notification.notifierIds));}
		} catch (IOException e) {
			e.printStackTrace();
		}
		content.put("periodic",periodic);
		
		storeHelper(TAG, content);
	}
	

	public AbstractObjective getOBJAt(int index) {
		String[] str = OBJs.idsList.get(index).split(",");
		return (AbstractObjective) EntityPool.instance()
					.forId(Long.parseLong(str[1]),str[0]);
	}

	public void removeOBJ(AbstractObjective obj) {
		OBJs.idsList.remove(obj.getTAG()+","+obj.getId());
		updateOBJs();
	}
	
	public int getObjCount(){
		return OBJs.getCount();
	}
	
	@Override
	public void updateRecords(){
		ContentValues content = new ContentValues();
		if(attachment.isEmpty()){
			content.putNull("records");
		}else{
			try {
				content.put("records",Util.serializeIdArray(attachment.records));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		DBAdapter.instance().updateEntry(getTAG(), id, content);	
	}
	
	public void updateOBJs(){
		ContentValues content = new ContentValues();
		if(OBJs.isEmpty()){
			content.putNull("objectives");
		}else{
			try {
				content.put("objectives",Util.serializeIdArray(OBJs.idsList));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		DBAdapter.instance().updateEntry(getTAG(), id, content);	
	}
	
	public AbstractObjective orderedOBJ(String tag) {
		AbstractObjective result = null;
		if (!OBJs.isEmpty()) {
			for (String entry : OBJs.idsList) {
				String[] str = entry.split(",");
				if (str[0].equals(tag) || tag == null) {
					AbstractObjective obj = (AbstractObjective) EntityPool
							.instance().forId(Long.parseLong(str[1]), str[0]);
					if(obj == null){
						continue;}
					
					if (result == null) {
						if (!obj.isDone()) {
							result = obj;
						}
					} else {
						if (result.getY() > obj.getY() && !obj.isDone()) {
							result = obj;
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * @param bDone 是否排序已经完成的目标，真为存在完成目标
	 * @return
	 */
	public TreeMap<Integer,String> sortOBJ(boolean bDone){
		TreeMap<Integer,String> result = new TreeMap<Integer, String>();
		if (!OBJs.isEmpty()) {
			for (String entry : OBJs.idsList) {
				String[] str = entry.split(",");
				AbstractObjective obj = (AbstractObjective) EntityPool
					.instance().forId(Long.parseLong(str[1]), str[0]);
				if(!obj.isDone() || bDone){
					result.put(obj.getY(),entry);
				}
			}
		}
		return result;
	}
	
	
	public void updateNotifiers(){
		ContentValues content = new ContentValues();
		if(notification.isEmpty()){
			content.putNull("notifiers");
		}else{
			try {
				content.put("notifiers",
						Util.serializeIdArray(notification.notifierIds));
				DBAdapter.instance().updateEntry(getTAG(), id, content);	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}

	public void processValidate(){
		max = Global.INITIAL_TASK_MAX;
		process = Global.INITIAL_TASK_VALUE;
		if(!OBJs.isEmpty()){
			for(String entry:OBJs.idsList){
				String[] str = entry.split(",");
				AbstractObjective obj = (AbstractObjective) EntityPool.instance()
									.forId(Long.parseLong(str[1]), str[0]);
				if(obj == null){
					continue;}
				
				obj.processValidate();	
			}
			
			if(max == process){
				status = Status.DONE.getStatus();
			}else{
				status = Status.WAITINGTODO.getStatus();	
			}
		}
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	protected void onEndDateChange() {
		Time now = new Time();
		Time end = new Time();
		end.set(endDate);
		now.setToNow();
		
		if( Time.compare(now, end) > 0 ){
				status = Status.EXPIRED.getStatus();
		}else{
			if(status == Status.EXPIRED.getStatus()){
				status = Status.WAITINGTODO.getStatus();
			}
		}		
	}

	@Override
	protected void onStartDateChange() {
		Time now = new Time();
		Time start = new Time();
		start.set(startDate);
		now.setToNow();
		
		if( Time.compare(now, start) < 0 ){
			status = Status.ONGOING.getStatus();
		}else{
			if(status == Status.WAITINGTODO.getStatus()){
				status = Status.EXPIRED.getStatus();
			}
		}
	}
	
	@Override
	public String getTAG() {
		return TAG;
	}
	
	public void delete(){
		if(!OBJs.isEmpty()){
			for(String entry:OBJs.idsList){
				String[] str = entry.split(",");
				AbstractObjective OBJ = (AbstractObjective) EntityPool.instance(
						).forId(Long.parseLong(str[1]), str[0]);
				OBJ.deleteInDB();
			}
			OBJs.idsList.clear();
		}
		
		if(!notification.isEmpty()){
			notification.clear();
		}
		super.delete();
	}
	
	public void deleteRecords(){
		for(int i = 0;i<attachment.getRecordsCount();i++){
			Record record = attachment.getRecordAt(i);
			if(record == null){
				continue;
			}else{
				record.delete();
			}	
		}
	}
	
	public void detachRecords(){
		for(int i = 0;i<attachment.getRecordsCount();i++){
			Record record = attachment.getRecordAt(i);
			attachment.detach(record.getTAG(), record.getId());
			record.store();
		}
	}
	
	public OBJIds getObjs(){
		return OBJs;
	}
	
	public void setProcess(long max,long process){
		this.max += max;
		this.process += process;
	}

	@Override
	public String getSchema() {
		return TASK_TABLE;
	}

	@Override
	public Persisable create() {
		return new Task("Prototype");
	}
	
	public Plan getParent(){
		return (Plan) EntityPool.instance().forId(parentId,"plan");
	}
	
	public void setParent(long id){
		parentId = id;
	}

	@Override
	public ArrayList<Long> loadTable(String whereClause, String[] whereArgs) {
		Cursor cur = DBAdapter.instance().retrieveAll(TAG,whereClause,whereArgs);
		ArrayList<Long> ids = null;
	    if(!cur.moveToFirst()){
	    }else{
		    ids = new ArrayList<Long>();
		    do{
			    AbstractTask task = loadHelper(cur);   
			    ids.add(task.getId());
		    }while(cur.moveToNext());
	    }
	    cur.close();
	    return ids;	}
	
	public long getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(long scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	public int getPeriodic() {
		return periodic;
	}

	public void setPeriodic(int periodic) {
		this.periodic = periodic;
	}

	@Override
	public float getProcess() {
		return process/(float)max;
	}

	@Override
	public void updateStatus() {
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
			if(periodic != TASK_NOT_PERIODIC){
				it.set(startDate);
				int offset = Time.getJulianDay(it.toMillis(false),it.gmtoff) -
					Time.getJulianDay(System.currentTimeMillis(),it.gmtoff);
				mDay = periodic + offset%periodic;
				mDay = mDay <0?-mDay:mDay;
				mDay = mDay == periodic?0:mDay;
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
			if(periodic != TASK_NOT_PERIODIC){
				it.set(startDate);
				int offset = Time.getJulianDay(it.toMillis(false),it.gmtoff) -
					Time.getJulianDay(System.currentTimeMillis(),it.gmtoff);
				mDay = periodic + offset%periodic;
				mDay = mDay <0?-mDay:mDay;
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
	
	static public  ArrayList<Long> currentTasks(){
		Time it = new Time();
		it.setToNow();
		it.set(0, 0, 0, it.monthDay, it.month, it.year);
		long start = it.toMillis(false);
		it.set(59, 59, 23, it.monthDay, it.month, it.year);
		long end = it.toMillis(false);
		String where = " ((start_date BETWEEN " + start + " AND " + end + ")"
				+ " OR (end_date BETWEEN " + start + " AND " + end + ")"
				+ " OR (start_date < " + start + " AND end_date > " + end
				+ ")) ";
		String sql = "SELECT task._id,task.periodic,task.start_date FROM task WHERE "+ where;
		Cursor cur = DBAdapter.instance().execQuery(sql, null);
		it.setToNow();
		
		ArrayList<Long> taskIds = new ArrayList<Long>();
		int count = 0;
		if(cur.moveToFirst()){
			do{
				if(!taskIds.contains(cur.getLong(0))){
					if(cur.getInt(1) != Task.TASK_NOT_PERIODIC){
						long offset = Time.getJulianDay(cur.getLong(2),it.gmtoff) -
							Time.getJulianDay(System.currentTimeMillis(),it.gmtoff);
						long m = cur.getInt(1) + offset%cur.getInt(1);
						if(m == cur.getInt(1)){
							taskIds.add(cur.getLong(0));
						}
					}else{
						taskIds.add(cur.getLong(0));
					}
				}
				count++;
			}while(cur.moveToNext());
		}
		cur.close();
		return taskIds;
	}
}
