package com.singularity.clover.util.drag;

import com.singularity.clover.util.drag.DragScrollView.GestureCallback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class DragVerticalScrollView extends ScrollView {
		
	private boolean bDrag = false;
	private GestureDetector mGesture = new GestureDetector(new OnGesture());
	private GestureCallback mCallback;
	
	public DragVerticalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setGestrueCallback(GestureCallback callback){
		mCallback = callback;
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(bDrag)
			return false;
		return super.onInterceptTouchEvent(ev);
	}

	public void setDragging(boolean bDrag){
		this.bDrag = bDrag;
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mGesture.onTouchEvent(ev);
		if(bDrag){
			return true;
		}else{
			return super.onTouchEvent(ev) || true;}
	}
	
	private class OnGesture extends SimpleOnGestureListener{

		@Override
		public void onLongPress(MotionEvent e) {
			if(mCallback != null && bDrag != true)
				mCallback.onLongPress(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(mCallback != null)
				mCallback.onSingleTapUp(e);
			return false;
		}
		
		
	}
}
