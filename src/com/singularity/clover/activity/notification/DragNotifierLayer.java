package com.singularity.clover.activity.notification;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.singularity.clover.entity.Draggable;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.util.drag.DragEntityHolder;
import com.singularity.clover.util.drag.DragLayer;
import com.singularity.clover.util.drag.DragSource;
import com.singularity.clover.util.drag.DragView;

public class DragNotifierLayer extends DragLayer {

	public DragNotifierLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		View v = (View) dragInfo;

		int w = v.getWidth();
		int h = v.getHeight();
		int left = x -xOffset;
		int top = y -yOffset;
		DragLayer.LayoutParams lp = new DragLayer.LayoutParams(w, h, left, top);
		this.updateViewLayout(v, lp);
		DragEntityHolder holder = (DragEntityHolder) v.getTag();
		if(holder != null){
			Draggable draggable = (Draggable) EntityPool.
				instance().forId(holder.getId(), holder.getTAG());
			draggable.updatePosition(x, y);}
	}
}
