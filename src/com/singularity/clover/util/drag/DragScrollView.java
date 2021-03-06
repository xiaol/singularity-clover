package com.singularity.clover.util.drag;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class DragScrollView extends HorizontalScrollView {
	private boolean bDrag = false;
	private GestureDetector mGesture = new GestureDetector(new OnGesture());
	private GestureCallback mCallback;
	
	public DragScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setGestrueCallback(GestureCallback callback){
		mCallback = callback;
	}
	
	public interface GestureCallback{
		void onLongPress(MotionEvent e);
		void onSingleTapUp(MotionEvent e);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(bDrag){
			return false;}
		boolean result = super.onInterceptTouchEvent(ev);
		return result;
	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mGesture.onTouchEvent(ev);
		if(bDrag){
			return true;
		}else{
			return super.onTouchEvent(ev) || true;}
	}

	public void setDragging(boolean bDrag){
		this.bDrag = bDrag;
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
