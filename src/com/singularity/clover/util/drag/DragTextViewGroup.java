package com.singularity.clover.util.drag;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class DragTextViewGroup extends LinearLayout {
	private GestureDetector mGesture = 
		new GestureDetector(new GestureListener());
	boolean bLongPress = false;
	
	public DragTextViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {	
		return super.onInterceptTouchEvent(ev);
	}

	
	private class GestureListener extends SimpleOnGestureListener{

		@Override
		public void onLongPress(MotionEvent e) {
			performLongClick();
			
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			performClick();
			return true;
		}		
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGesture.onTouchEvent(event);
		return true;
	}	
	
}
