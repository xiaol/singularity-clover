package com.singularity.clover.activity.entity;

import android.view.View;
import android.widget.ImageView;

import com.singularity.clover.activity.entity.TaskViewActivityHelper.ViewHolder;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.EntityViewModel;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.util.drag.DragLayer;
import com.singularity.clover.util.drag.MyAbsoluteLayout;
import com.singularity.clover.util.drag.MyAbsoluteLayout.LayoutParams;

public class RecordViewModel implements EntityViewModel{

	private final TaskViewActivityHelper taskViewActivityHelper;

	RecordViewModel(TaskViewActivityHelper taskViewActivityHelper) {
		this.taskViewActivityHelper = taskViewActivityHelper;
	}

	public final static int MODEL_PICTURE = 0;
	
	@Override
	public View initView() {
		ImageView imageView = new ImageView(
				this.taskViewActivityHelper.mActivity);
		imageView.setScaleType(ImageView.ScaleType.CENTER);
		return imageView;
	}

	@Override
	public View changeModel(int model, View srcView) {
		srcView.setOnLongClickListener(
				this.taskViewActivityHelper.onDraggableLongClick);
		return srcView;
	}

	@Override
	public View entityToView(Persisable e, View layout) {
		ImageView view = (ImageView) layout;
		Record record = (Record) e;
		view.setImageBitmap(record.convertoBitmap(
				this.taskViewActivityHelper.mActivity));
		view.invalidate();
		return layout;
	}

	@Override
	public Persisable viewToEntity(View layout, Persisable e) {
		return e;
	}
	
	public View addToLayout(DragLayer layout,int x,int y,long id){
		Record record = (Record) EntityPool.instance().forId(id, Record.TAG);
		this.taskViewActivityHelper.mActivity.getTask().
			attachment.attach(record.getTAG(),record.getId());
		this.taskViewActivityHelper.mActivity.getTask().store();
		
		//record.updatePosition(x, y);
		record.setX(x);record.setY(y);
		record.store();
		View v = initView();
		ViewHolder holder = this.taskViewActivityHelper.new ViewHolder(
				record.getId(),Record.TAG);
		v.setTag(holder);
		entityToView(record, v);
		changeModel(MODEL_PICTURE,v);
		
		MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,x,y);
		layout.addView(v, lp);
		return v;
	}

	@Override
	public void updateView(View v) {}
	
}