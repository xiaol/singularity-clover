package com.singularity.clover.activity.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.singularity.clover.util.drag.DragController;
import com.singularity.clover.util.drag.DragSource;
import com.singularity.clover.util.drag.DragView;
import com.singularity.clover.util.drag.DropTarget;

public class DragNotifierCtrl extends DragController {
	private int INDICATOR_ZONE = 20;
	private CalendarActivity mActivity;
	public int[] screenXY = new int[2];
	public int mOffsetX,mOffsetY;
	
	
	public DragNotifierCtrl(CalendarActivity activity) {
		super(activity);
		mActivity = activity;
		INDICATOR_ZONE = CalendarActivity.INDICATOR_ZONE;
	}


    
	public void startDrag(Bitmap b, int screenX, int screenY,
            int textureLeft, int textureTop, int textureWidth, int textureHeight,
            DragSource source, Object dragInfo, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        if (mListener != null) {
            mListener.onDragStart(source, dragInfo, dragAction);
        }
        
        View v = (View) dragInfo;
        
        mMotionDownX = v.getWidth()/2 + screenX;
        mMotionDownY = v.getHeight()/2 +screenY;

        int registrationX = v.getWidth()/2;
        int registrationY = v.getHeight()/2;

        mTouchOffsetX = v.getWidth()/2;
        mTouchOffsetY = v.getHeight()/2;

        mDragging = true;
        if(mScrollView != null)
        	mScrollView.setDragging(true);
        mDragSource = source;
        mDragInfo = dragInfo;

        mVibrator.vibrate(VIBRATE_DURATION);
        DragView dragView = mDragView = new DragView(mContext, b, registrationX, registrationY,
                textureLeft, textureTop, textureWidth, textureHeight);
        dragView.show(mWindowToken, (int)mMotionDownX, (int)mMotionDownY);
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        screenXY[0] = (int)ev.getRawX();
        screenXY[1] = (int)ev.getRawY();
        mOffsetX = (int) (ev.getRawX() - ev.getX());
        mOffsetY = (int) (ev.getRawY() - ev.getY());
        if(!isInDeleteZone(screenXY[0], screenXY[1])){
        	transform();
        }else{}
        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
        	mDragView.move(screenXY[0],screenXY[1]);
        	
            final int[] coordinates = mCoordinatesTemp;
            DropTarget dropTarget = findDropTarget(screenXY[0], screenXY[1], coordinates);
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
                    dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                    }
                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                }
            } else {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                }
            }
            mLastDropTarget = dropTarget;
            
            break;
        case MotionEvent.ACTION_UP:
            if (mDragging) { 
            	
                drop(screenXY[0], screenXY[1]);
            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            cancelDrag();
        }

        return true;
	}
	
	private void transform(){
		float ratio = 0;
		float x,y;
		float wRadius = (mDisplayMetrics.widthPixels - mOffsetX - INDICATOR_ZONE)/2.0f;
		float hRadius = (mDisplayMetrics.heightPixels - mOffsetY - INDICATOR_ZONE)/2.0f;
		
		x = screenXY[0] - mOffsetX - INDICATOR_ZONE/2 - wRadius;
		y = screenXY[1] - mOffsetY - INDICATOR_ZONE/2 - hRadius;
		
		if(abs(x) > abs(y)* wRadius/hRadius){
			ratio = abs(x)/wRadius;
			y = y/ratio;
			int symbol= x >=0?1:-1;
			x = symbol*wRadius;
		}else{
			ratio = abs(y)/hRadius;
			x = x/ratio;
			int symbol= y >=0?1:-1;
			y = symbol*hRadius;
		}
		
		screenXY[0] = (int) (x + mOffsetX + INDICATOR_ZONE/2 + wRadius);
		screenXY[1] = (int) (y + mOffsetY + INDICATOR_ZONE/2 + hRadius);
	}
	
	private float abs(float a){
		return a>0?a:-a;
	}
	
	private boolean isInDeleteZone(int x,int y){
		Rect r = new Rect();
		int[] location = new int[2];
		mActivity.mDeleteZ.getHitRect(r);
		mActivity.mDeleteZ.getLocationOnScreen(location);
		r.offset(location[0]-mActivity.mDeleteZ.getLeft(),
				location[1]-mActivity.mDeleteZ.getTop());
		if(r.contains(x,y)){
			return true;
		}else{
			return false;
		}
	}
}
