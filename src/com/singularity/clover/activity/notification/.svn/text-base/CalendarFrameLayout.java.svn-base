package com.singularity.clover.activity.notification;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class CalendarFrameLayout extends RelativeLayout {
	private GestureCallback mCallback = null;
	private GestureDetector mGestureDetector = 
					new GestureDetector(new OnGesture());
	private GestureDetector mLongPressListener = 
					new GestureDetector(new OnLongPress());
	private Paint currentPaint = new Paint();
	private Paint textPaint = new Paint();
	private boolean bDrag = false;
	private boolean bDrawWatch = false;
	
	public void setGestrueCallback(GestureCallback callback){
		mCallback = callback;
	}
	
	public interface GestureCallback{
		void onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY);
		void onLongPress(MotionEvent e);
		boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY);
		boolean onSingleTapConfirmed(MotionEvent e);
	}
	
	public CalendarFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(mGestureDetector.onTouchEvent(ev))
			return true;
		if(bDrag) 
			return false;
		return super.onInterceptTouchEvent(ev);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mLongPressListener.onTouchEvent(event);
		return true;
	}
	
	public void setDragging(boolean bDrag){
		this.bDrag = bDrag;
	}

	public void setDrawingWatch(boolean bDraw){
		bDrawWatch = bDraw;
	}
	
	private class OnGesture extends SimpleOnGestureListener{

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//Log.d("Fling in calendar x,y", velocityX +","+velocityY);
			if(mCallback != null && !bDrag)
				mCallback.onFling(e1, e2, velocityX, velocityY);
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if(mCallback !=null && !bDrag){
				return mCallback.onScroll(e1, e2, distanceX, distanceY);
			}else{
				return false;
			}
		}

		
	
	}
	
	private class OnLongPress extends SimpleOnGestureListener{
		
		@Override
		public void onLongPress(MotionEvent e) {
			if(mCallback != null && !bDrag)
				mCallback.onLongPress(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if(mCallback != null && !bDrag){
				return mCallback.onSingleTapConfirmed(e);
			}
			return false;
		}
		
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(!bDrawWatch)
			return;
		
		currentPaint.setDither(true);
        currentPaint.setColor(Color.GRAY);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(2);
        

		textPaint.setTextSize(12);
        textPaint.setColor(Color.RED);
        textPaint.setDither(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setUnderlineText(true);
        
		/*canvas.drawRect(CalendarActivity.INDICATOR_ZONE/2, 
				CalendarActivity.INDICATOR_ZONE/2, 
				getWidth()-CalendarActivity.INDICATOR_ZONE/2, 
				getHeight()-CalendarActivity.INDICATOR_ZONE/2, currentPaint);*/
		
		int hRadius = getHeight()/2;
		int wRadius = getWidth()/2;
		int offset = CalendarActivity.INDICATOR_ZONE/2;
		
		int timeLength = hRadius*4 + wRadius*4 - 8*offset;
		float seed = 24*6;
		float step = timeLength/seed;
		float[] points = new float[(int)((seed)*2)];
		float x,y;
		x = wRadius;y=offset;
		
		currentPaint.setColor(Color.RED);
		canvas.drawPoint(x,y, currentPaint);
		canvas.drawText("0",x,y, textPaint);
		textPaint.setColor(Color.BLACK);
		
		int state = 0;
		for(int i = 0; i< seed;i++){
			switch (state) {
			case 0:
				x = x -step;
				if(x< offset){
					y += offset - x;
					state = 1;
					x = offset;}
				points[i*2] = x;
				points[i*2+1] = y;
				break;
			case 1:
				y = y + step;
				if(y > 2*hRadius -offset ){
					x += y - 2*hRadius +offset;
					state = 2;
					y = 2*hRadius - offset;}
				points[i*2] = x;
				points[i*2+1] = y;
				break;
			case 2:
				x = x +step;
				if(x > 2*wRadius - offset){
					y -= x - 2*wRadius +offset;
					state =3;
					x = 2*wRadius - offset;
				}
				points[i*2] = x;
				points[i*2+1] = y;
				break;
			case 3:
				y  = y - step;
				if( y < offset){
					x -= offset - y;
					state = 4;
					y = offset;
				}
				points[i*2] = x;
				points[i*2+1] = y;
				break;
			case 4:
				x = x - step;
				if(x < wRadius){
					continue;
				}
				points[i*2] = x;
				points[i*2+1] = y;
				break;
			default:
				break;
			}
			
			if((i+1)%6 == 0 && (i+1)/6 != 24){
				String text = Integer.toString((i+1)/6);
				canvas.drawText(text,
						points[i*2], points[i*2+1], textPaint);
			}
		}		
		
		currentPaint.setColor(Color.BLACK);
		canvas.drawPoints(points, currentPaint);
	}
	
	
}
