package com.singularity.clover.activity.entity;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.text.Html;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.activity.entity.TaskOverViewActivity.ListAdapter.ViewHolder;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.objective.AbstractObjective;
import com.singularity.clover.entity.objective.CheckableObj;
import com.singularity.clover.entity.objective.DurableObj;
import com.singularity.clover.entity.objective.NumericObj;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.view.VerticalLabelView;

public class TaskOverViewActivityHelper {
	private TaskOverViewActivity mActivity;

	public TaskOverViewActivityHelper(TaskOverViewActivity mActivity) {
		this.mActivity = mActivity;
	}

	protected Drawable adjust(Drawable d, int to) {
		Bitmap src = ((BitmapDrawable) d).getBitmap();
		Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
		
		int count = bitmap.getWidth()*bitmap.getHeight();
		int[] pixels = new int[count];
		bitmap.getPixels( pixels, 0 , bitmap.getWidth(),
				0, 0 , bitmap.getWidth(), bitmap.getHeight());
		for (int i = 0;i<count;i++)
			if (match(pixels[i]))
				pixels[i] = Color.argb(Color.alpha(pixels[i]),
						Color.red(to), Color.green(to), Color.blue(to));
		
		bitmap.setPixels(pixels, 0, bitmap.getWidth(), 
				0, 0, bitmap.getWidth(), bitmap.getHeight());
		src = null;
		return new BitmapDrawable(bitmap);
	}
	
	protected Drawable adjustNinePath(Drawable d,int to){
		NinePatchDrawable nine = (NinePatchDrawable) d;
		return nine;
		/*Î´ÊµÏÖ*/
	}

	protected boolean match(int pixel) {
		return /*Color.alpha(pixel) == 255;*/ (Color.red(pixel) == 255 && Color.green(pixel) == 255
			&& Color.blue(pixel) == 255) || Color.alpha(pixel) >= 128;
	}

	protected class OnTaskListHierarchyChanged implements 
										OnHierarchyChangeListener{
			
			@Override
			public void onChildViewRemoved(View parent, View child) {
				ViewHolder childHolder = (ViewHolder) child.getTag();
				if(child == mActivity.focusView && childHolder.taskId != mActivity.focusId){
					childHolder.textName.setBackgroundColor(Color.TRANSPARENT);
				}
				
				if(childHolder.taskId == mActivity.focusId && mActivity.focusView != null){
					childHolder.textName.setBackgroundResource(R.drawable.text_background);
					mActivity.focusView = child;
				}	
			}
			
			@Override
			public void onChildViewAdded(View parent, View child) {
				ViewHolder childHolder = (ViewHolder) child.getTag();
				if(child == mActivity.focusView && childHolder.taskId != mActivity.focusId){
					childHolder.textName.setBackgroundColor(Color.TRANSPARENT);
				}
				
				if(childHolder.taskId == mActivity.focusId && mActivity.focusView != null){
					childHolder.textName.setBackgroundResource(R.drawable.text_background);
					mActivity.focusView = child;
				}				
				
			}
	}
	
	protected class OnScenarioHierarchyChanged implements
										OnHierarchyChangeListener{

		@Override
		public void onChildViewAdded(View arg0, View parent) {
			long id = (Long) parent.getTag();
			View child = parent.findViewById(R.id.taskoverview_scenario_list_ele);
			if(child.getVisibility() == View.GONE && id != mActivity.mCurrentScenario){
				child.setVisibility(View.VISIBLE);
			}
			
			if(id == mActivity.mCurrentScenario && 
					mActivity.mCurrentScenario != Global.INVALIDATE_ID){
				child.setVisibility(View.GONE);
				mActivity.mFocusScenario = parent;
			}
		}

		@Override
		public void onChildViewRemoved(View arg0, View parent) {
			long id = (Long) parent.getTag();
			View child = parent.findViewById(R.id.taskoverview_scenario_list_ele);
			if(child.getVisibility() == View.GONE && id != mActivity.mCurrentScenario){
				child.setVisibility(View.VISIBLE);
			}
			
			if(id == mActivity.mCurrentScenario && 
					mActivity.mCurrentScenario != Global.INVALIDATE_ID){
				child.setVisibility(View.GONE);
				mActivity.mFocusScenario = parent;
			}
		}
		
	}
	
	protected void setupReview(){
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
		String sql = "SELECT task._id,task.scenario,task.periodic,task.start_date FROM task WHERE "+ where;
		Cursor cur = DBAdapter.instance().execQuery(sql, null);
		it.setToNow();
		
		ArrayList<Long> scenarioIds = new ArrayList<Long>();
		int count = 0;
		if(cur.moveToFirst()){
			do{
				if(!scenarioIds.contains(cur.getLong(1))){
					if(cur.getInt(2) != Task.TASK_NOT_PERIODIC){
						long offset = Time.getJulianDay(it.toMillis(false),it.gmtoff) -
							Time.getJulianDay(cur.getLong(3),it.gmtoff);
						long m = cur.getInt(2) + offset%cur.getInt(2);
						if(m == cur.getInt(2)){
							scenarioIds.add(cur.getLong(1));
							count++;
						}
					}else{
						scenarioIds.add(cur.getLong(1));
						count++;
					}
				}	
			}while(cur.moveToNext());
		}
		cur.close();
		mActivity.mReview.setText(Html.fromHtml(mActivity.getText(
				R.string.review_text)+ "<strong><big>" +Integer.toString(count)+"</big></strong>"));
		mActivity.mReviewCanvas.setParams(scenarioIds);
	}
	
	protected class OnCheckedChanged implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			final CheckBox check = (CheckBox) v;
			View convertView = (View) v.getParent();
			final TaskOverViewActivity.ListAdapter.ViewHolder holder = 
				(TaskOverViewActivity.ListAdapter.ViewHolder) convertView.getTag();
				
			Task it = (Task) EntityPool.instance().forId(holder.taskId, Task.TAG);	
			if(check.isChecked()){
				if(it.getObjCount() != 0){
					CheckableObj obj = (CheckableObj) it.orderedOBJ(null);
					if(obj != null){
						obj.check();
						obj.store();
					}
					AbstractObjective nextOBJ = it.orderedOBJ(null);
					if(nextOBJ != null){
						Handler handler = new Handler();
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									check.setChecked(false);
								}
							}, 700);
						swiftOBJ(nextOBJ, holder);
					}else{
						
						Handler handler = new Handler();
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									check.setChecked(false);
									holder.objContent.setVisibility(View.GONE);
									holder.checklist.setVisibility(View.GONE);
								}
							}, 700);
					}
					it.processValidate();	
					holder.viewOBJ.setParams(it.getObjCount(),it.getProcess());
					holder.viewOBJ.invalidate();
				}else{
					disableOBJPanel(holder);
				}
				
				
			}else{
			}
		}
				
	}
	
	protected View initScenairoLayout(Context context, Cursor cursor){
		View layout = mActivity.getLayoutInflater().inflate(
				R.layout.taskoverview_scenario_list_item_layout, null);
		VerticalLabelView text = (VerticalLabelView) layout.findViewById(
				R.id.taskoverview_scenario_list_label);
		text.setTag(cursor.getLong(0));
		text.setText(cursor.getString(1));
		text.setPadding((int) (3 * mActivity.mDisplayMetrics.density),
				(int) (10 * mActivity.mDisplayMetrics.density), 0,
				(int) (10 * mActivity.mDisplayMetrics.density));
		/*text.setDrawingCacheEnabled(true);
		Drawable d = mActivity.getResources().getDrawable(
				R.drawable.scenario_tag_background);
		text.setBackgroundDrawable(adjust(d, cursor.getInt(2)));*/
		return layout;
	}
	
	protected void swiftOBJ(AbstractObjective obj,ViewHolder holder){
		if(obj.getTAG().equals(CheckableObj.TAG)){
			holder.objContent.setText(obj.getName());
			holder.objContent.setVisibility(View.VISIBLE);
			holder.checklist.setVisibility(View.VISIBLE);
			holder.numericPlus.setVisibility(View.GONE);
			holder.durableCtrl.setVisibility(View.GONE);
		}else if(obj.getTAG().equals(DurableObj.TAG)){
			DurableObj durable = (DurableObj) obj;
			long minutes = durable.howLongExpired()/(60*1000);
			long hour = minutes / 60;
			String timeSpan;
			if(hour != 0){
				timeSpan = Long.toString(hour) + mActivity.getText(R.string.hour_short)+
					minutes%(60)+mActivity.getText(R.string.mininute_short) + mActivity.getText(R.string.remains);
			}else{
				timeSpan = Long.toString(minutes%(60))+mActivity.getText(
						R.string.mininute_short)+ mActivity.getText(R.string.remains);
			}
			holder.objContent.setText(timeSpan);
			holder.objContent.setVisibility(View.VISIBLE);
			holder.durableCtrl.setVisibility(View.VISIBLE);
			holder.checklist.setVisibility(View.GONE);
			holder.numericPlus.setVisibility(View.GONE);
			if(durable.isRunning()){
				long delay = durable.howLongExpired();
				if(delay > 0){
					durable.setAlarm(SingularityApplication.instance(),
							System.currentTimeMillis()+delay);
				}
				holder.durableCtrl.setBackgroundResource(R.drawable.task_item_obj_button_pause_bg);
			}else{
				durable.cancelAlarm();
				holder.durableCtrl.setBackgroundResource(R.drawable.task_item_obj_button_play_bg);
			}
		}else if(obj.getTAG().equals(NumericObj.TAG)){
			NumericObj numeric = (NumericObj) obj;
			String content;
			if(numeric.getMax() != 0){
				content = Integer.toString(numeric.getValue()) + "/"+
					Integer.toString(numeric.getMax())+numeric.getUnit();
			}else{
				content = Integer.toString(numeric.getValue()) +numeric.getUnit();
			}
			holder.objContent.setText(content);
			holder.objContent.setVisibility(View.VISIBLE);
			holder.numericPlus.setVisibility(View.VISIBLE);
			holder.checklist.setVisibility(View.GONE);
			holder.durableCtrl.setVisibility(View.GONE);
		}
	}
	
	protected class OnNumericPlus implements OnClickListener{

		@Override
		public void onClick(View v) {
			final ImageButton plus = (ImageButton) v;
			View convertView = (View) v.getParent();
			final TaskOverViewActivity.ListAdapter.ViewHolder holder = 
				(TaskOverViewActivity.ListAdapter.ViewHolder) convertView.getTag();
			
			Task it = (Task) EntityPool.instance().forId(holder.taskId, Task.TAG);
			AbstractObjective obj = it.orderedOBJ(null);

			NumericObj numeric = (NumericObj) obj;
			numeric.setValue(numeric.getValue()+1);	
			numeric.store();
			String content;
			if(numeric.getMax() != 0){
				content = Integer.toString(numeric.getValue()) + "/"+
					Integer.toString(numeric.getMax())+numeric.getUnit();
			}else{
				content = Integer.toString(numeric.getValue()) +numeric.getUnit();
			}
			holder.objContent.setText(content);
			
			it.processValidate();	
			holder.viewOBJ.setParams(it.getObjCount(),it.getProcess());
			holder.viewOBJ.invalidate();
			
			if(numeric.isDone()){
				AbstractObjective nextOBJ = it.orderedOBJ(null);
				if(nextOBJ != null){
					swiftOBJ(nextOBJ, holder);
				}else{
					disableOBJPanel(holder);
				}
			};
			
		}
		
	}
	
	protected class OnDurableCtrl implements OnClickListener{

		@Override
		public void onClick(View v) {
			final ImageButton ctrl = (ImageButton) v;
			View convertView = (View) v.getParent();
			final TaskOverViewActivity.ListAdapter.ViewHolder holder = 
				(TaskOverViewActivity.ListAdapter.ViewHolder) convertView.getTag();
			
			Task it = (Task) EntityPool.instance().forId(holder.taskId, Task.TAG);
			AbstractObjective obj = it.orderedOBJ(null);
			DurableObj durable = (DurableObj) obj;	
			durable.setRunning(!durable.isRunning());
			if(durable.isRunning()){
				long delay = durable.howLongExpired();
				if(delay > 0){
					durable.setAlarm(SingularityApplication.instance(),
							System.currentTimeMillis()+delay);
				}
				ctrl.setBackgroundResource(R.drawable.task_item_obj_button_pause_bg);
			}else{
				durable.cancelAlarm();
				ctrl.setBackgroundResource(R.drawable.task_item_obj_button_play_bg);
			}
			durable.store();
			long minutes = durable.howLongExpired()/(60*1000);
			long hour = minutes / 60;
			String timeSpan;
			if(hour != 0){
				timeSpan = Long.toString(hour) + mActivity.getText(R.string.hour_short)+
					minutes%(60)+mActivity.getText(R.string.mininute_short) + mActivity.getText(R.string.remains);
			}else{
				timeSpan = Long.toString(minutes%(60))+mActivity.getText(
						R.string.mininute_short)+ mActivity.getText(R.string.remains);
			}
			holder.objContent.setText(timeSpan);
			
			it.processValidate();	
			holder.viewOBJ.setParams(it.getObjCount(),it.getProcess());
			holder.viewOBJ.invalidate();
			
			if(durable.isDone()){
				AbstractObjective nextOBJ = it.orderedOBJ(null);
				if(nextOBJ != null){
					swiftOBJ(obj, holder);
				}else{
					disableOBJPanel(holder);
				}
			}
		}
		
	}
	
	protected void disableOBJPanel(ViewHolder holder){
		holder.objContent.setVisibility(View.GONE);
		holder.numericPlus.setVisibility(View.GONE);
		holder.checklist.setVisibility(View.GONE);
		holder.durableCtrl.setVisibility(View.GONE);
	}
}
