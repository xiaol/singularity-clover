package com.singularity.clover.activity.entity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

import com.singularity.clover.R;
import com.singularity.clover.activity.entity.TaskViewActivityHelper.ViewHolder;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.EntityViewModel;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.objective.CheckableObj;

/**
 * @author xiaol
 * checkable对应视图模型
 */
public class CheckableViewModel implements EntityViewModel{
	
	private final TaskViewActivityHelper taskViewActivityHelper;
	private OnCheckedChange onCheckedChanged = null;
	
	CheckableViewModel(TaskViewActivityHelper taskViewActivityHelper) {
		this.taskViewActivityHelper = taskViewActivityHelper;
		onCheckedChanged = new OnCheckedChange();
	}

	public final static int MODEL_EDIT = 0;
	public final static int MODEL_DISPLAY = 1;
	
	@Override
	public View initView() {
		View layout = this.taskViewActivityHelper.mLinflater.inflate(
				R.layout.task_view_checkable_layout,null);
		layout.setTag(TaskViewActivityHelper.ITEM_CHECKABLE);
		return layout;
	}

	@Override
	public View changeModel(final int model,View layout) {
		View remove = layout.findViewById(
				R.id.task_view_checkable_remove);
		View edit = layout.findViewById(
				R.id.task_view_checkable_item);
		CheckBox check = (CheckBox) layout.findViewById(
				R.id.task_view_checkable_check);
		switch (model) {
		case MODEL_EDIT:
			remove.setVisibility(View.VISIBLE);
			layout.setOnLongClickListener(null);
			edit.setOnFocusChangeListener(null);
			check.setOnCheckedChangeListener(null);
			break;
		case MODEL_DISPLAY:
			remove.setVisibility(View.GONE);
			layout.setOnLongClickListener(
					this.taskViewActivityHelper.onDraggableLongClick);
			edit.setOnFocusChangeListener(
					this.taskViewActivityHelper.onEditTextFocusChanged);
			check.setOnClickListener(onCheckedChanged);
			break;
		}
		return layout;
	}

	@Override
	public View entityToView(Persisable e,View layout) {
		CheckableObj checkable = (CheckableObj) e;
		CheckBox check = (CheckBox) layout.
			findViewById(R.id.task_view_checkable_check);
		EditText edit = (EditText) layout.
			findViewById(R.id.task_view_checkable_item);
					
		check.setChecked(checkable.getCheckState());
		edit.setText(checkable.name);
		edit.setTag(this.taskViewActivityHelper.new EditHolder("name"));	
		return layout;
	}

	@Override
	public Persisable viewToEntity(View v,Persisable e) {
		CheckableObj checkable = (CheckableObj) e;
		CheckBox check = (CheckBox) v.findViewById(
				R.id.task_view_checkable_check);
		EditText edit = (EditText) v.findViewById(
				R.id.task_view_checkable_item);
		
		if(check.isChecked()){
			checkable.check();
		}else{
			checkable.unCheck();}
		checkable.setName(edit.getText().toString());
		
		checkable.store();
		edit.setTag(this.taskViewActivityHelper.new EditHolder("name"));
		
		return checkable;
	}	

	protected class OnCheckedChange implements OnClickListener{
	
		@Override
		public void onClick(View v) {
			CheckBox check = (CheckBox) v;
			View view = (View) v.getParent();
			ViewHolder holder = (ViewHolder) view.getTag();
			CheckableObj entity = (CheckableObj) EntityPool.instance().forId(
					holder.id, holder.tag);
			if((check.isChecked())){
				entity.check();
			}else{
				entity.unCheck();}
			entity.store();
		}
	}

	@Override
	public void updateView(View v) {}
}