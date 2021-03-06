package com.singularity.clover.activity.notification;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.singularity.clover.Global;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.entity.wrapper.Scenario;

public class CalendarCanvas extends FrameLayout {
	protected int[] offsetYs;
	protected int[] offsetDays;
	protected int OFFSET_Y;
	protected int OFFSET_EDGE;
	protected ArrayList<Long> ids = null;
	protected int x,y,w,h,firstDayWeek,maxMonthDay,month,year;
	protected Path path;
	protected Paint mPaint1,mPaint2,mPaint;
	protected TextPaint mTextPaint;
	protected float arrowLength;
	protected float mRadius;
	private DisplayMetrics dm;
	
	public CalendarCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		dm = getResources().getDisplayMetrics();
		OFFSET_Y = (int) (12*dm.density);
		OFFSET_EDGE = (int) (12*dm.density);
		offsetYs = new int[31];
		offsetDays = new int[31];
		arrowLength = 10*dm.density;
		mRadius = 20*dm.density;
		path = new Path();
		
		mPaint1 = new Paint();
		mPaint2 = new Paint();
		
		mPaint1.setColor(Color.RED);
		mPaint1.setDither(true);
		mPaint1.setAntiAlias(true);
        mPaint1.setStyle(Paint.Style.STROKE);
        mPaint1.setStrokeJoin(Paint.Join.ROUND);
        mPaint1.setStrokeCap(Paint.Cap.ROUND);
        mPaint1.setStrokeWidth(2*dm.density);
        
        mPaint2.setColor(Color.RED);
        mPaint2.setDither(true);
		mPaint2.setAntiAlias(true);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeJoin(Paint.Join.ROUND);
        mPaint2.setStrokeCap(Paint.Cap.ROUND);
        mPaint2.setStrokeWidth(2*dm.density);
        float intervals[]={5.0f*dm.density,5.0f*dm.density};
        mPaint2.setPathEffect(new DashPathEffect(intervals, 10));
        
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(10*dm.density);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		initOffsets();
		if(ids == null)
			return;
		
		for(long id:ids){
			Task task = (Task) EntityPool.instance().forId(id, Task.TAG);
			if(task == null){
				continue;}
			
			long start = task.getStartDate();
			long end = task.getEndDate();
			Scenario scenario = (Scenario) EntityPool.
				instance().forId(task.getScenarioId(),Scenario.TAG);
			if(scenario == null){
				mPaint1.setColor(Color.GRAY);
				mPaint2.setColor(Color.GRAY);
				mTextPaint.setColor(Color.GRAY);
			}else{
				mPaint1.setColor(scenario.getResId());
				mPaint2.setColor(scenario.getResId());
				mTextPaint.setColor(Color.DKGRAY);
			}
			
			if(end == Global.INVALIDATE_DATE || start == end){
				Time tS = new Time();
				tS.set(start);
				int rowS = (tS.monthDay + firstDayWeek-1)/7 + 1;
				int columnS = (tS.monthDay + firstDayWeek)%7;
				columnS = columnS==0?7:columnS;
				
				RectF rect = new RectF();
				rect.left = (columnS - 1) * w + x + OFFSET_Y;
				rect.top = (rowS - 1) * h + y + OFFSET_Y;
				rect.bottom = (rowS) * h + y - OFFSET_Y;
				rect.right = (columnS) * w + x - OFFSET_Y;
				
				Path path = new Path();
				
				
				if(offsetDays[tS.monthDay-1] == 0){
					path.addArc(rect, 240, -180);
					mTextPaint.setTextAlign(Align.LEFT);
					canvas.drawTextOnPath(task.name == null?"":task.name, path, 0, -3, mTextPaint);
				}else{
					path.addArc(rect,60-(offsetDays[tS.monthDay-1]-1)*20*dm.density,-20*dm.density);
				}
				canvas.drawPath(path, mPaint1);
				offsetDays[tS.monthDay-1]++;
				continue;}
			
			Time tS = new Time();
			Time tE = new Time();
			tE.set(end);
			if(start == Global.INVALIDATE_DATE){
				tS.set(1,tE.month,tE.year);
			}else{
				tS.set(start);
			}
			
			
			if(tS.after(tE)){
				//mPaint = mPaint2;
				mPaint = mPaint1;
				
			}else{
				mPaint = mPaint1;
			}
			if(tS.month == tE.month){
				/* row 1-6 column 1-7*/
				int rowS = (tS.monthDay-1 + firstDayWeek)/7 + 1;
				int rowE = (tE.monthDay-1 + firstDayWeek)/7 + 1;
				int columnS = (tS.monthDay-1 + firstDayWeek)%7 + 1;
				int columnE = (tE.monthDay-1 + firstDayWeek)%7 + 1;
				
				drawHelper(rowS, rowE, columnS, columnE, tS, tE, canvas,task);
			}else if(tS.month == month){
				int rowS = (tS.monthDay-1 + firstDayWeek)/7 + 1;
				int rowE = 6;
				int columnS = (tS.monthDay-1 + firstDayWeek)%7 + 1;
				int columnE = 7;
				
				drawHelper(rowS, rowE, columnS, columnE, tS, tE, canvas,task);
			}else if(tE.month == month){
				int rowS = 1;
				int rowE = (tE.monthDay-1 + firstDayWeek)/7 + 1;
				int columnS = 1;
				int columnE = (tE.monthDay-1 + firstDayWeek)%7 + 1;

				drawHelper(rowS, rowE, columnS, columnE, tS, tE, canvas,task);
			}else{/* 整个时间段跨过了该月*/
				
			}
			
			path.reset();
		}
	}
	
	protected void initOffsets(){
		for(int i =0; i< 31;i++){
			offsetYs[i] = OFFSET_EDGE;
			offsetDays[i] = 0;
		}
	}
	
	protected void setTaskIds(ArrayList<Long> ids){
		this.ids = ids;
	}
	
	protected void setParams(int x,int y,int w,int h,
			int firstDayWeek,int maxMonthDay,int month,int year){
		this.x = x; this.y = y;
		this.w = w; this.h = h;
		this.firstDayWeek = firstDayWeek;
		this.maxMonthDay = maxMonthDay;
		this.month = month;
		this.year = year;
	}
	
	protected void drawHelper(int rowS,int rowE,int columnS,int columnE,
			Time tS, Time tE,Canvas canvas,Task task){
		Paint paint;
		float startX,startY,endX,endY;
		if(rowS == rowE){
			int offset = 0;
			int start,end;
			if(tS.monthDay>tE.monthDay){
				start = tE.monthDay;
				end = tS.monthDay;
			}else{
				start = tS.monthDay;
				end = tE.monthDay;}
			
			for(int i = start-1;i < end; i++){
				if(offsetYs[i] > offset){
					offset = offsetYs[i];
				}
			}
			endY = startY = (rowS - 1) * h + offset + y;
			startX = (columnS - 1) * w + w / 2f + x;
			path.moveTo(startX, startY);
			endX = (columnE - 1) * w + w / 2f + x;
			path.lineTo(endX, startY);

			offsetYs[start - 1] = OFFSET_Y + offset;
			if (offsetYs[start - 1] > h - OFFSET_EDGE) {
				offsetYs[start - 1] = h - OFFSET_EDGE;
			}
			for (int i = start; i < end; i++) {
				offsetYs[i] = offsetYs[start - 1];
			}
			paint = mPaint;
		} else {
			startY = (rowS - 1) * h + h / 2f + y;
			startX = (columnS - 1) * w + w / 2f + x;
			
			endX = (columnE - 1) * w + w / 2f + x;
			endY = (rowE - 1) * h + h / 2f + y;
			//path.quadTo(endX-(endX - startX)/2, endY,endX, endY);
			
			float k;
			if(startX != endX){
				k = (startY - endY) / (startX - endX);
			}else{
				k = 0;
			}
			float radius = 5*dm.density;
			float tempX,tempY;
			tempY = startY - (k==0?-1:1)*radius;
			if(Math.abs(k)>4){
				tempX = startX - (k>0?-k:k)*radius/4;
				tempY = startY - k*radius/4;
			}else{
				tempX = startX - (k>0?-k:k)*radius;
				tempY = startY - k*radius;
			}
			path.moveTo(tempX, tempY);	
			endY = endY - (k==0?1:-1)*radius;
			if(Math.abs(k)>4){
				endX = endX - (k>0?-k:k)*radius/4;
				endY = endY - (k>0?-k:k)*radius/4;
			}else{
				endX = endX - (k>0?-k:k)*radius;
				endY = endY - (k>0?-k:k)*radius;
			}
			path.lineTo(endX,endY);
			
			Matrix matrix = new Matrix();
			float arrowX;
			float arrowY;
			Path arrowL = new Path();
			if(startX != endX){
				k = (startY - endY) / (startX - endX);
				float length = arrowLength;
				if(Math.abs(k)>2) length /=k;
				arrowX = endX - ((startX - endX)<0?1:-1)*length;
				arrowY = k * arrowX + (startY - k * startX);
				/*arrowX = endX - ((startX - endX)<0?1:-1)*arrowLength;
				arrowY = endY;*/
				
			}else{
				arrowX = endX;
				if(endY > startY){
					arrowY = endY - arrowLength;
				}else{
					arrowY = endY + arrowLength;
				}
			}
			
			arrowL.moveTo(arrowX, arrowY);
			arrowL.lineTo(endX, endY);
			Path arrowR = new Path(arrowL);

			matrix.setRotate(-30, endX, endY);
			arrowL.transform(matrix);

			matrix.reset();
			matrix.setRotate(30, endX, endY);
			arrowR.transform(matrix);
			paint = mPaint;
	
			canvas.drawPath(arrowR, paint);
			canvas.drawPath(arrowL, paint);
		}
		//float textOffset = (startX - endX)*(startX - endX) + (startY - endY)*(startY-endY);
		canvas.drawPath(path, paint);
		mTextPaint.setTextAlign(Align.CENTER);
		canvas.drawTextOnPath(task.name == null?"":task.name, 
				path, 0, -3, mTextPaint);
	}
}
