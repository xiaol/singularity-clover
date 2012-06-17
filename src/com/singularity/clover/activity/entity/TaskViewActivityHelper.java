package com.singularity.clover.activity.entity;

import java.util.ArrayList;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.singularity.clover.R;
import com.singularity.clover.Global;
import com.singularity.clover.activity.entity.TaskViewActivity.OnDragViewLongClick;
import com.singularity.clover.activity.notification.CalendarActivity;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.objective.AbstractObjective;
import com.singularity.clover.entity.objective.CheckableObj;
import com.singularity.clover.entity.objective.DurableObj;
import com.singularity.clover.entity.objective.NumericObj;
import com.singularity.clover.entity.objective.OBJFactory;
import com.singularity.clover.entity.record.PictureRecord;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.record.TextRecord;
import com.singularity.clover.entity.record.VoiceRecord;
import com.singularity.clover.entity.task.AbstractTask;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.util.drag.DragEntityHolder;
import com.singularity.clover.util.drag.DragLayer;
import com.singularity.clover.util.drag.MyAbsoluteLayout;
import com.singularity.clover.util.drag.MyAbsoluteLayout.LayoutParams;

/**
 * @author XiaoL
 * 采用装配模式，实体并不关心自己的视图模型是如何，在装载时才根据种类建立视图
 */
public class TaskViewActivityHelper {
	protected TaskViewActivity mActivity;
	protected LayoutInflater mLinflater;
	
	public CheckableViewModel mCheckableModel = new CheckableViewModel(this);
	public NumericViewModel mNumericModel = new NumericViewModel(this);
	public DurableViewModel mDurableModel = new DurableViewModel(this);
	public TextRecordViewModel mTextModel = new TextRecordViewModel(this);
	protected RecordViewModel mRecordModel = new RecordViewModel(this);
	protected OnBasicInfoClick mBasicInfoClickListener = new OnBasicInfoClick();

	public OnDragViewLongClick onDraggableLongClick = null;
	public OnEidtTextFocusChanged onEditTextFocusChanged = null;

	
	public final static int ITEM_CHECKABLE = 0;
	public final static int ITEM_NUMERIC = 1;
	public final static int ITEM_DURABLE = 2;
	
	private final static int BASIC_INFO_X = 5;
	private final static int BASIC_INFO_Y = 5;
	private View mBasicInfoLayout;	

	public TaskViewActivityHelper(TaskViewActivity mActivity) {
		this.mActivity = mActivity;
		Context context = mActivity;
		mLinflater = (LayoutInflater) context.
			getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		onDraggableLongClick = mActivity.new OnDragViewLongClick();	
		onEditTextFocusChanged = new OnEidtTextFocusChanged();
	}
	
	public Task initTask(long scenarioId){
		Task task = new Task();
		task.setScenarioId(scenarioId);
		task.store();
		return task;
	}
	
	public void setupTask(Task task,DragLayer layout){
		setupBasicInfo(task, layout);
		setupOBJ(task, layout);
		setupRecords(task,layout);
	}
	
	public void setupBasicInfo(Task task,DragLayer layout){
		Context context = mActivity;
		if(mBasicInfoLayout == null){
			LayoutInflater inflater = (LayoutInflater) context.
					getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			mBasicInfoLayout = inflater.inflate(
					R.layout.task_view_basic_info_layout,null);
			MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,
					BASIC_INFO_X,BASIC_INFO_Y);
			layout.addView(mBasicInfoLayout,lp);
		}else{}
		
		EditText name = (EditText) mBasicInfoLayout.findViewById(
				R.id.task_view_basic_info_name);
		name.setOnFocusChangeListener(onEditTextFocusChanged);
		name.setText(task.name);
		name.setTag(new EditHolder("name"));
		ViewHolder holder = new ViewHolder(task.getId(), Task.TAG);
		mBasicInfoLayout.findViewById(R.id.task_view_basic_info_name_layout).setTag(holder);
		
		TextView info = (TextView) mBasicInfoLayout.findViewById(
				R.id.task_view_basic_info_other);
		String detail = null;
		
		boolean bDate = false,bDeadline = bDate,bAlarm = bDate,bPeriodic = bDate;
		String date;
		if(task.getStartDate() != Global.INVALIDATE_DATE){
			date = DateUtils.formatDateTime(
					null, task.getStartDate(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
			bDate = true;
		}else{
			date =" - ";
		}
		
		String deadline;
		if(task.getEndDate() != Global.INVALIDATE_DATE){
			deadline = DateUtils.formatDateTime(
					null, task.getEndDate(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
			bDeadline = true;
		}else{
			deadline=" - ";
		}
		
		/*SpannableStringBuilder builder = new SpannableStringBuilder();
		String title = "Date: ";
		builder.append(title);
		builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
				0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.append(date+" to "+deadline);
		builder.append("\n");
		
		long nextNotifier = task.notification.getNextNotifier();
		if(nextNotifier != Global.INVALIDATE_DATE){
			String next = DateUtils.formatDateTime(
					mActivity, nextNotifier, DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME);
			builder.append("Next alarm: "+next + "\n");
		}
		info.setText(builder);*/
	
		String strDate = mActivity.getResources(
				).getString(R.string.task_view_basic_info_date);
		String strTo = mActivity.getResources(
				).getString(R.string.task_view_basic_info_date_to);
		
		String content;
		if(bDeadline){
			content = "<b><small>"+strDate +"</small></b>" + "<strong><big><font color=\"green\">"+date
				+"</font></big></strong>"+"<small>"+strTo+"</small>"+
				"<strong><big><font color=\"red\">"+deadline+"</font></big></strong>" + "<br />"; 
		}else{
			content = "<b>"+strDate +"</b>"+"<strong><big><font color=\"red\">"+date
				+"</font></big></strong>"+ "<br />";}
		
		long nextNotifier = task.notification.getNextNotifier();
		if(nextNotifier != Global.INVALIDATE_DATE){
			String next = DateUtils.formatDateTime(
					mActivity, nextNotifier, DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME);

			String prefix = mActivity.getResources(
				).getString(R.string.task_view_basic_info_alarm);
			content = content+"<b><small>"+prefix+"</small></b>"+
			"<strong><big><font color=\"#AA05D3\">"+next+"</font></big></strong>"+"<br />";
			bAlarm = true;
		}
		
		if(task.getPeriodic() != Task.TASK_NOT_PERIODIC){
			Time it = new Time();
			it.set(task.getStartDate());
			long offset = Time.getJulianDay(it.toMillis(false),it.gmtoff) -
				Time.getJulianDay(System.currentTimeMillis(),it.gmtoff);
			long m = task.getPeriodic() + offset%task.getPeriodic();
			
			String strPeriodic = mActivity.getResources(
				).getString(R.string.task_view_basic_info_periodic);
			String suffix = mActivity.getResources(
				).getString(R.string.toast_calendar_periodic_suffix);
			if(m != task.getPeriodic()){
				content = content + "<b><small>"+strPeriodic +"</small></b>" + 
					"<strong><big><font color=\"black\">"+m + suffix+
					"</font></big></strong>" + "<br />";
			}else{
				content = content + "<b><small>"+strPeriodic +"</small></b>" + 
					"<strong><big><font color=\"black\">"+
					mActivity.getText(R.string.task_view_basic_info_today)
					+"</font></big></strong>" + "<br />";
			}
			bPeriodic = true;
		}
		if(!bDate && !bDeadline && !bAlarm && !bPeriodic){
			info.setText(Html.fromHtml("<big>"+mActivity.getResources(
					).getString(R.string.task_view_basic_info_hint)+"</big>"));
		}else{
			info.setText(Html.fromHtml(content));
		}
		info.setOnClickListener(mBasicInfoClickListener);
	}
	
	private void setupOBJ(Task task,DragLayer layout){
		int count = task.getObjCount();
		for(int i = 0; i< count;i++){
			View v = null;
			AbstractObjective OBJ = task.getOBJAt(i);
			if(OBJ == null){  //bad news
				continue;}
			
			if(OBJ.getTAG().equals(CheckableObj.TAG)){
				View caLayout = mCheckableModel.initView();
				mCheckableModel.changeModel(CheckableViewModel.MODEL_DISPLAY, caLayout);
				v = mCheckableModel.entityToView(OBJ, caLayout);
			}else if(OBJ.getTAG().equals(NumericObj.TAG)){
				View ncLayout = mNumericModel.initView();
				mNumericModel.changeModel(NumericViewModel.MODEL_DISPLAY, ncLayout);
				v = mNumericModel.entityToView(OBJ, ncLayout);
			}else if(OBJ.getTAG().equals(DurableObj.TAG)){
				View daLayout = mDurableModel.initView();
				mDurableModel.changeModel(DurableViewModel.MODEL_DISPLAY, daLayout);
				v = mDurableModel.entityToView(OBJ, daLayout);
			}else{
				//Log.d("SetupOBJS", "Task has a invalid OBJ");
			}
			
			ViewHolder holder = new ViewHolder(OBJ.getId(), OBJ.getTAG());
			v.setTag(holder);
			MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.
				LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT,OBJ.getX(),OBJ.getY());
			layout.addView(v, lp);
		}
	}
	
	public void setupRecords(AbstractTask task,DragLayer layout){
		int count = task.attachment.getRecordsCount();
		ArrayList<Integer> lostIdIndexs = new ArrayList<Integer>();
		for(int i = 0; i< count;i++){
			View v = null;
			Record record = task.attachment.getRecordAt(i);
			if(record == null){
				lostIdIndexs.add(i);
				continue;
				}
			if(record.getTAG().equals(TextRecord.TAG)){
				View tLayout = mTextModel.initView();
				mTextModel.changeModel(TextRecordViewModel.MODEL_DISPLAY, tLayout);
				v = mTextModel.entityToView(record, tLayout);
			}else if(record.getTAG().equals(PictureRecord.TAG)){
				View pLayout = mRecordModel.initView();
				mRecordModel.changeModel(RecordViewModel.MODEL_PICTURE,pLayout);
				v = mRecordModel.entityToView(record, pLayout);
			}else if(record.getTAG().equals(VoiceRecord.TAG)){
				
			}
			ViewHolder holder = new ViewHolder(record.getId(),Record.TAG);
			v.setTag(holder);
			
			MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.
				LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT,record.getX(),record.getY());
			layout.addView(v, lp);
			v.setOnClickListener(mActivity.mRecordClickListner);
		}
		for(int entry:lostIdIndexs){
			task.attachment.detach(entry);}
		
		if(!lostIdIndexs.isEmpty()){
			task.store();}
	}
	
	public ViewGroup initGroup(){
		Context context = mActivity;
		LayoutInflater inflater = (LayoutInflater) context.
				getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(
				R.layout.task_view_item_group_layout,null);
		return (ViewGroup) layout;
	}
	
	public void prepareOBJGroup(){
		for(Entry<String, AbstractObjective> 
			entry:OBJFactory.getAllOBJPrototype()){
			IdGenerator.GroupIdStart(entry.getKey());
		}
	}
	
	public void endOBJGroup(){
		for(Entry<String, AbstractObjective> 
			entry:OBJFactory.getAllOBJPrototype()){
			IdGenerator.GroupIdEnd(entry.getKey());
		}
	}
	
	public void groupStoreHelper(DragLayer layer,
			ViewGroup group,int offsetX,int offsetY){
		AbstractObjective OBJ = null;
		/*a edit text nested in group*/
		while (group.getChildCount() > 1) {
			View v = group.getChildAt(1);
			switch ((Integer) v.getTag()) {
			case ITEM_CHECKABLE:
				OBJ = new CheckableObj(mActivity.getTask());
				mCheckableModel.changeModel(CheckableViewModel.MODEL_DISPLAY, v);
				mCheckableModel.viewToEntity(v, OBJ);
				break;
			case ITEM_NUMERIC:
				OBJ = new NumericObj(mActivity.getTask());
				mNumericModel.changeModel(NumericViewModel.MODEL_DISPLAY, v);
				mNumericModel.viewToEntity(v, OBJ);
				break;
			case ITEM_DURABLE:
				OBJ = new DurableObj(mActivity.getTask());
				mDurableModel.changeModel(DurableViewModel.MODEL_DISPLAY, v);
				mDurableModel.viewToEntity(v, OBJ);
				break;
			}
			if (OBJ != null) {
				int[] location = new int[2]; 
				mActivity.mLayout.getLocationOnScreen(location);
				int x = v.getLeft() + offsetX - location[0];
				int y = v.getTop()+ offsetY - location[1];
				OBJ.updatePosition(x,y);
				MyAbsoluteLayout.LayoutParams lp = 
					new MyAbsoluteLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,x,y);
				group.removeView(v);
				
				layer.addView(v,lp);
				v.requestLayout();
				
				ViewHolder holder = new ViewHolder(OBJ.getId(),OBJ.getTAG());
				v.setTag(holder);
				OBJ = null;
			} else {
				//Log.d("TaskViewActivityHelper", "OBJGroup have invalid child");
			}
		}
		mActivity.getTask().store();	
	}
	
	public interface Field {
		String getField();
	}
	
	public class ViewHolder implements Field,DragEntityHolder{
		public long id;
		public String tag;
		public String field = null;
		
		public ViewHolder(long id,String tag){
			this.id = id;
			this.tag = tag;
		}
		
		public void setField(String name){
			field = name;}

		@Override
		public String getField() {
			return field;}

		@Override
		public String getTAG() {
			return tag;}

		@Override
		public long getId() {
			return id;}
	}
	
	public class EditHolder implements Field{
		public String field = null;
		
		public EditHolder(String name){
			field = name;}

		@Override
		public String getField() {
			return field;}

	}
	protected class OnEidtTextFocusChanged implements OnFocusChangeListener{

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) return;
			
			ViewHolder holder = null;
			EditText edit = (EditText) v;
			
			Field editField = (Field) v.getTag();
			v = (View) v.getParent();
			if(v == null){
				holder = (ViewHolder) editField;
			}else{
				holder = (ViewHolder) v.getTag();
				if(holder == null ){
					holder = (ViewHolder) editField;
				}
			}
			
			Persisable entity = EntityPool.instance().forId(
					holder.id, holder.tag);
			
			try {
				entity.getClass().getField(editField.getField()).
					set(entity, edit.getText().toString());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();}
			
			entity.store();
		}
	}
	
	protected class OnBasicInfoClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mActivity,CalendarActivity.class);
			intent.setAction(CalendarActivity.CALENDAR_EDIT);
			intent.putExtra(CalendarActivity.IN_TASK_ID, mActivity.mTask.getId());
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			mActivity.startActivity(intent);
			
		}
		
	}
}
