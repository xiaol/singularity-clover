package com.singularity.clover.util.drag;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.singularity.clover.activity.entity.TaskViewActivityHelper.ViewHolder;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;

public class DragDeleteZone extends ImageView 
	implements DropTarget, DragController.DragListener{

	public DragDeleteZone(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		
	}

	@Override
	public void onDragEnd() {
		
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		View view = (View) dragInfo;
		ViewGroup group = (ViewGroup) source;
		DragEntityHolder holder = (DragEntityHolder) view.getTag();
		Persisable entity = EntityPool.instance().forId(holder.getId(), holder.getTAG());
		entity.delete();
		group.removeView(view);
		view = null;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		return null;
	}

	
}
