package com.singularity.clover.util.drag;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class DragViewGroup extends LinearLayout {
	private GestureDetector mGesture = 
		new GestureDetector(new GestureListener());
	boolean bLongPress = true;
	
	public DragViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {	
		mGesture.onTouchEvent(ev);
		return super.onInterceptTouchEvent(ev);
	}

	
	private class GestureListener extends SimpleOnGestureListener{

		@Override
		public void onLongPress(MotionEvent e) {
			if(bLongPress){
				performLongClick();	
			}else{
				bLongPress = true;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		bLongPress = false;
		return true;
	}	
	
}
