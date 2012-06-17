package com.singularity.clover.activity.entity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.activity.entity.TaskViewActivityHelper.ViewHolder;
import com.singularity.clover.activity.record.NoteActivity;
import com.singularity.clover.activity.record.RecordOverViewActivity;
import com.singularity.clover.activity.record.WhiteboardActivity;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.EntityViewModel;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.record.PictureRecord;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.record.TextRecord;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.util.drag.DragController;
import com.singularity.clover.util.drag.DragDeleteZone;
import com.singularity.clover.util.drag.DragLayer;
import com.singularity.clover.util.drag.DragScrollView;
import com.singularity.clover.util.drag.DragSource;
import com.singularity.clover.util.drag.DragVerticalScrollView;
import com.singularity.clover.util.drag.MyAbsoluteLayout.LayoutParams;

public class TaskViewActivity extends Activity 
								implements DragController.DragListener{
	
	public static final String TASK_NEW = "singularity.activity.entity.task.new";
	public static final String TASK_EDIT = "singularity.activity.entity.task.edit";
	public static final String TASK_SHOW_NOTIFICATION =
		"singularity.activity.entity.task.notification.show";
	public static final String IN_TASK_ID = 
		"com.singularity.activity.entity.TaskViewActivity.task.id";
	public static final String IN_SCENARIO_ID = 
		"com.singularity.activity.entity.TaskViewActivity.scenario.id";
	
	public static final String RESULT_RECORD_ID = 
		"com.singularity.activity.entity.TaskViewActivity.result.record.id";
	
	public static final int OUT_NOTE_NEW = 0;
	public static final int OUT_NOTE_EDIT = 1;
	public static final int OUT_RECORD_PICK = 2;
	public static final int OUT_RECORD_EDIT = 3;
	public static final int OUT_PICTURE_EDIT = 4;
	
	protected DragController mDragController;
	protected DragLayer mDragLayer;
	private DragDeleteZone mDeleteZ;
	private View mMenu;
	protected View mDropIndicator;
	protected View mLayout;
	
	private AlertDialog mOBJDialog = null;
	private ViewGroup mOBJLayout;
	private ViewGroup mOBJGroup;
	private float mTouchX,mTouchY;
	
	private DragScrollView mHorizontalScrollView;
	private DragVerticalScrollView mVerticalScrollView;
	
	private EntityViewModel mEditViewModel;
	private View mEditView;
	
	
	private TaskViewActivityHelper mHelper;
	protected Task mTask;
	
	private GestureCallback mGestureCallback;
	protected OnRecordClick mRecordClickListner = new OnRecordClick();
	protected OnNoteClick mNoteClickListner = new OnNoteClick();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_view_layout);
		mHelper = new TaskViewActivityHelper(this);
		parse(getIntent());	
		
		mLayout = findViewById(R.id.task_view_layout);
		mDragController = new DragController(this);
	    mDragLayer = (DragLayer) findViewById(R.id.task_view_drag_layer);
	    mDeleteZ = (DragDeleteZone) findViewById(R.id.task_view_drag_delete_zone);
	    mMenu = findViewById(R.id.task_view_menu);
	    mDropIndicator = findViewById(R.id.task_view_drop_indicator);
	    
	    mDragLayer.setDragController(mDragController);
	    mDragController.addDropTarget (mDragLayer);
	    mDragController.addDropTarget(mDeleteZ);
	    mDragController.setDragListener(mDeleteZ);
	    mDragController.setDragListener(this);
	    mHorizontalScrollView = (DragScrollView) findViewById(
	    		R.id.task_view_framelayout);
	    
	    mVerticalScrollView = (DragVerticalScrollView) findViewById(
				R.id.task_view_framelayout_vertical);
	    mGestureCallback = new GestureCallback();
	    mHorizontalScrollView.setGestrueCallback(mGestureCallback);
	    mVerticalScrollView.setGestrueCallback(mGestureCallback);
	    
		mHelper.setupTask(mTask,mDragLayer);
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		parse(intent);
		mHelper.setupBasicInfo(mTask,mDragLayer);
	}

	private void parse(Intent intent){
		String action = intent.getAction();
		if(action.equals(TASK_NEW)){
			long id = intent.getLongExtra(IN_TASK_ID, Global.INVALIDATE_ID);
			mTask = (Task) EntityPool.instance().forId(id, Task.TAG);
			if(mTask == null){
				Intent error = new Intent(
				TaskViewActivity.this,TaskOverViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(error);
				finish();}
		}else if(action.equals(TASK_EDIT)){
			long id = intent.getLongExtra(IN_TASK_ID, Global.INVALIDATE_ID);
			mTask = (Task) EntityPool.instance().forId(id, Task.TAG);
			if(mTask == null){
				Intent error = new Intent(
				TaskViewActivity.this,TaskOverViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(error);
				finish();}
		}else if(action.equals(TASK_SHOW_NOTIFICATION)){
			long id = intent.getLongExtra(IN_TASK_ID, Global.INVALIDATE_ID);
			mTask = (Task) EntityPool.instance().forId(id, Task.TAG);
			SingularityApplication.instance().getNotifierBinder().bindNotifer();
			if(mTask == null){
				Intent error = new Intent(
					TaskViewActivity.this,TaskOverViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(error);
				finish();
			}
		}
	}
	
	public class OnDragViewLongClick implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			if (!v.isInTouchMode()) 
				return false;			
			Object dragInfo = v;
			mDragController.startDrag (v, mDragLayer, 
				dragInfo, DragController.DRAG_ACTION_MOVE);
			return true;
		}	
	}
	
	private void setupOBJDialog(){
		AlertDialog.Builder builder;
		
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(LAYOUT_INFLATER_SERVICE);
		View root = (ViewGroup) inflater.inflate(
				R.layout.task_view_objective_layout,null);
		mOBJLayout = (ViewGroup) root.findViewById(
				R.id.task_view_objectives_layout);
		mOBJGroup = (ViewGroup) root.findViewById(
				R.id.task_view_objectives_group);
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(root);
		mOBJDialog = builder.create();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	
	@Override
	public void onBackPressed() {
		View view = getCurrentFocus();
		if(view != null){
			view.clearFocus();}
		
		if(mMenu.getVisibility() == View.VISIBLE){
			mMenu.setVisibility(View.GONE);
			mDropIndicator.setVisibility(View.GONE);
			return;
		}else{
			String action = getIntent().getAction();
			
			if(action.equals(TASK_EDIT)){
				super.onBackPressed();
			}else if(action.equals(TASK_NEW)){
				Intent intent = new Intent(
						TaskViewActivity.this,TaskOverViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.setAction(TaskOverViewActivity.TASK_OVERVIEW_ADD_TASK_DONE);
				intent.putExtra(TaskOverViewActivity.IN_ADD_TASK_ID, mTask.getId());
				startActivity(intent);
			}else if(action.equals(TASK_SHOW_NOTIFICATION)){
				Intent intent = new Intent(
						TaskViewActivity.this,TaskOverViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				//intent.setAction(TaskOverViewActivity.TASK_OVERVIEWBY_SCENARIO);
				//intent.putExtra(TaskOverViewActivity.IN_SCENAIRO_ID, mTask.getScenarioId());
				startActivity(intent);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void onAddRecord(View v){
		mMenu.setVisibility(View.GONE);
		mDropIndicator.setVisibility(View.GONE);
		Intent intent = new Intent(this, RecordOverViewActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.setAction(RecordOverViewActivity.RECORD_PICK);
		startActivityForResult(intent, OUT_RECORD_PICK);
	}
	
	public void onAddOBJ(View v){
		if(mOBJDialog == null)
			setupOBJDialog();
		mOBJDialog.show();
		mMenu.setVisibility(View.GONE);
		mDropIndicator.setVisibility(View.GONE);
	}
	
	public void onAddTextRecord(View v){
		newNote();
		mMenu.setVisibility(View.GONE);
		mDropIndicator.setVisibility(View.GONE);
	}
	
	public void onCheckable(View v){
		View layout = mHelper.mCheckableModel.initView();
		mHelper.mCheckableModel.changeModel(CheckableViewModel.MODEL_EDIT, layout);
		mOBJGroup.addView(layout);
	}
	
	public void onOBJRemove(View v){
		mOBJGroup.removeView((View) v.getParent());
	}
	
	public void onNumeric(View v){
		View layout = mHelper.mNumericModel.initView();
		mHelper.mNumericModel.changeModel(NumericViewModel.MODEL_EDIT, layout);
		mOBJGroup.addView(layout);
	}
	
	public void onDurable(View v){
		View layout = mHelper.mDurableModel.initView();
		mHelper.mDurableModel.changeModel(DurableViewModel.MODEL_EDIT, layout);
		mOBJGroup.addView(layout);
	}
	
	public void onOBJConfirm(View v){
		mOBJDialog.dismiss();
		mHelper.groupStoreHelper(mDragLayer,mOBJGroup,(int)mTouchX,(int)mTouchY);
	}
	
	public void onOBJCancel(View v){
		mOBJDialog.dismiss();
	}
	
	public Task getTask(){
		return mTask;
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		mDeleteZ.setVisibility(View.VISIBLE);
		mHorizontalScrollView.setDragging(true);
		mVerticalScrollView.setDragging(true);
		mMenu.setVisibility(View.GONE);
		mDropIndicator.setVisibility(View.GONE);
	}

	@Override
	public void onDragEnd() {
		mDeleteZ.setVisibility(View.GONE);	
		mHorizontalScrollView.setDragging(false);
		mVerticalScrollView.setDragging(false);
		
	}

	private void newNote(){
		Intent intent = new Intent(TaskViewActivity.this, NoteActivity.class);
		intent.setAction(NoteActivity.NOTE_NEW);
		startActivityForResult(intent, OUT_NOTE_NEW);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OUT_NOTE_NEW:
			if(resultCode == RESULT_OK){
				long id = data.getLongExtra(
						RESULT_RECORD_ID, Global.INVALIDATE_ID);
				if(id != Global.INVALIDATE_ID){
					View layout = mHelper.mTextModel.addToLayout(
							mDragLayer,(int)mTouchX, (int)mTouchY,id);
					layout.setOnClickListener(mNoteClickListner);}
			}else{}
			break;
		case OUT_RECORD_PICK:
			if(resultCode == RESULT_OK){
				long id = data.getLongExtra(
						RESULT_RECORD_ID, Global.INVALIDATE_ID);
				if(id != Global.INVALIDATE_ID){
					Record record = (Record) EntityPool.instance().forId(id, Record.TAG);
					if(record.getTAG() == TextRecord.TAG){
						View layout = mHelper.mTextModel.addToLayout(
							mDragLayer,(int)mTouchX, (int)mTouchY,id);
						layout.setOnClickListener(mNoteClickListner);
					}else if(record.getTAG() == PictureRecord.TAG){
						View layout = mHelper.mRecordModel.addToLayout(mDragLayer, 
								(int)mTouchX, (int)mTouchY, id);
						layout.setOnClickListener(mRecordClickListner);
					}
				}
			}else{}
			break;
		case OUT_NOTE_EDIT:
			if(resultCode == RESULT_OK){
				if(mEditView == null){ /*因为挂机添加判断，原因不明*/
					break;}
				
				ViewHolder holder = (ViewHolder) mEditView.getTag();
				Persisable e = EntityPool.instance().forId(holder.id, holder.tag);
				mEditViewModel.entityToView(e, mEditView);
			}else{}
			break;
		case OUT_PICTURE_EDIT:
			if(resultCode == RESULT_OK){
				if(mEditView == null){ /*因为挂机添加判断，原因不明*/
					break;}
				
				ViewHolder holder = (ViewHolder) mEditView.getTag();
				Persisable e = EntityPool.instance().forId(holder.id, holder.tag);
				mEditViewModel.entityToView(e, mEditView);
			}else{
				Toast toast = Toast.makeText(this, 
						getText(R.string.memory_low), Toast.LENGTH_SHORT);
				toast.show();
			}
			break;
		default:
			break;
			}
	}
	
	private class OnNoteClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			Intent intent = new Intent(TaskViewActivity.this, NoteActivity.class);
			intent.setAction(NoteActivity.NOTE_EDIT);
			intent.putExtra(NoteActivity.NOTE_EDIT_ID, holder.id);
			startActivityForResult(intent, OUT_NOTE_EDIT);
		}
		
	}
	
	private class OnRecordClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			Record record= (Record) EntityPool.instance().forId(holder.id, holder.tag);
			if(record.getTAG() == TextRecord.TAG){
				Intent intent = new Intent(TaskViewActivity.this, NoteActivity.class);
				intent.setAction(NoteActivity.NOTE_EDIT);
				intent.putExtra(NoteActivity.NOTE_EDIT_ID, holder.id);
				mEditViewModel = mHelper.mTextModel;
				mEditView = v;
				startActivityForResult(intent, OUT_NOTE_EDIT);
				
			}else if(record.getTAG() == PictureRecord.TAG){
				Intent intent = new Intent(TaskViewActivity.this,
						WhiteboardActivity.class);
				intent.setAction(WhiteboardActivity.WHITEBOARD_EDIT);
				intent.putExtra(WhiteboardActivity.WHITEBOARD_URI, record.getContent());
				intent.putExtra(WhiteboardActivity.RECORD_ID, holder.id);
				
				mEditViewModel = mHelper.mRecordModel;
				mEditView = v;
				startActivityForResult(intent, OUT_PICTURE_EDIT);		
			}
		}
		
	}
	
	private class GestureCallback implements DragScrollView.GestureCallback {

		@Override
		public void onLongPress(MotionEvent e) {
			mTouchX = e.getX(); 
			mTouchY = e.getY();
			mMenu.setVisibility(View.VISIBLE);
			LayoutParams lp = (LayoutParams) mDropIndicator.getLayoutParams();
			lp.x = (int) mTouchX;
			lp.y = (int) mTouchY;
			mDragLayer.updateViewLayout(mDropIndicator, lp);
			mDropIndicator.setVisibility(View.VISIBLE);
		}

		@Override
		public void onSingleTapUp(MotionEvent e) {
			
			View focus = TaskViewActivity.this.getCurrentFocus();
			if(focus != null && focus != mDragLayer){
				focus.clearFocus();
				InputMethodManager inputManager = (InputMethodManager) 
					TaskViewActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE); 
				inputManager.hideSoftInputFromWindow(
					focus.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
			}
			if(mMenu.getVisibility() == View.VISIBLE){
				mMenu.setVisibility(View.GONE);
				mDropIndicator.setVisibility(View.GONE);
			}else{
				Toast.makeText(TaskViewActivity.this, getString(R.string.click_task_view_hint), 
						Toast.LENGTH_SHORT).show();
			}
			
		}
		
		
	}
}
