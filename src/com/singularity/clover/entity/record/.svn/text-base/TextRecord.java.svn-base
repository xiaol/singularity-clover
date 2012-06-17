package com.singularity.clover.entity.record;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

import com.singularity.clover.R;
import com.singularity.clover.Global;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.util.BitmapUtil;

public class TextRecord extends Record {
	public static String TAG = "text_record";
	
	private Bitmap mBitmap = null;
	
	static{
		TextRecord prototype = new TextRecord("Prototype");
		EntityPool.instance().register(TAG,prototype);
		RecordFactory.register(TAG, prototype);
	}
	
	public TextRecord(String prototype) {
		super(prototype);
	}
	
	public TextRecord(){
		super();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Persisable create() {
		return new TextRecord("Prototype");
	}

	@Override
	public String getTAG() {
		return TAG;
	}

	@Override
	public Bitmap convertoBitmap(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int thresholdW = (int) (displayMetrics.density*BitmapUtil.IMAGE_PREVIEW_SIZE);
		int thresholdH = (int) (displayMetrics.density*BitmapUtil.IMAGE_PREVIEW_SIZE);
		if(mBitmap == null)
			mBitmap = Bitmap.createBitmap(thresholdW, thresholdH, Config.ARGB_4444);
		Canvas c = new Canvas(mBitmap);
		c.drawColor(context.getResources().getColor(R.color.whiteboard_bg));
		
		int colorGreen = context.getResources().getColor(R.color.dark_green_2);
		Paint textPaint = new Paint();
		textPaint.setTextSize(12);
        textPaint.setColor(context.getResources().getColor(R.color.dark_red));
        textPaint.setDither(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setUnderlineText(true);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(1);
        linePaint.setAntiAlias(true);
        
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.BLACK);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);
        rectPaint.setStrokeCap(Paint.Cap.ROUND);
        rectPaint.setStrokeWidth(1);
        rectPaint.setAntiAlias(true);
        
        String text = "Empty";
        if(name != null && name != "")
        	text = name;
        
        float cellW = thresholdW/4f;
        float cellH = thresholdH/5f;
        
        LinkedList<TextRecord> stack = new LinkedList<TextRecord>();
        ArrayList<Integer> slideNum = new ArrayList<Integer>();
        
		TextRecord it = this;
		slideNum.add(1);
        c.drawText(text, thresholdW/2, cellH, textPaint);
        textPaint.setTextSize(10);
        
        Path path = new Path();
        do{
        	
			while(it.getRecordChildId() != Global.INVALIDATE_ID){
				stack.addFirst(it);
				it = (TextRecord) EntityPool.instance().forId(
						it.getRecordChildId(),Record.TAG);
				if(stack.size()+1>slideNum.size()){
					slideNum.add(1);
				}else{
					slideNum.set(stack.size(),slideNum.get(stack.size())+1);
				}
				
				if((stack.size() & 0x1) == 0){
					rectPaint.setColor(colorGreen);	
				}else{
					rectPaint.setColor(Color.BLUE);
				}
				cellW = thresholdW/(stack.size()*2 +2);
				if(stack.size() > 1){
					path.moveTo(slideNum.get(stack.size())*cellW, (stack.size()+1)*cellH );
					path.lineTo(slideNum.get(stack.size()-1)*(thresholdW/(stack.size()*2)),
							(stack.size())*cellH );
				}
				
				/*if(it.getName().length() > 2){
					c.drawTextOnPath(it.getName().substring(0, 2)+"..",
							path, 0, -2, textPaint);
				}else{
					c.drawTextOnPath(it.getName(), path, 0, -2, textPaint);
				}*/
				c.drawPath(path, linePaint);
				
				c.drawRect(slideNum.get(stack.size())*cellW-cellW*0.25f,
						(stack.size()+1)*cellH -0.25f*cellW,
						slideNum.get(stack.size())*cellW+cellW*0.25f, 
						(stack.size()+1)*cellH +0.25f*cellW, rectPaint);
				path.reset();
			}
			if(it.getRecordNextId() != Global.INVALIDATE_ID){
				slideNum.set(stack.size(),slideNum.get(stack.size())+1);
				it = (TextRecord) EntityPool.instance().forId(
						it.getRecordNextId(),Record.TAG);
		
				if(stack.size() >1){
					path.moveTo(slideNum.get(stack.size())*cellW,
							(stack.size()+1)*cellH);
					path.lineTo(slideNum.get(stack.size()-1)*(thresholdW/(stack.size()*2)) ,
							(stack.size())*cellH);
				}
				c.drawRect(slideNum.get(stack.size())*cellW-cellW*0.25f,
						(stack.size()+1)*cellH -0.25f*cellW,
						slideNum.get(stack.size())*cellW + cellW*0.25f, 
						(stack.size()+1)*cellH +0.25f*cellW, rectPaint);
				c.drawPath(path, linePaint);
				path.reset();

			}else{
				do{		
					it = stack.poll();
					if(it == null){
						BitmapUtil.decorateBitmap(mBitmap,c,displayMetrics.density);
						return mBitmap;
					}
					if(it.getRecordNextId() != Global.INVALIDATE_ID){
						slideNum.set(stack.size(),slideNum.get(stack.size())+1);
						it = (TextRecord) EntityPool.instance().forId(
								it.getRecordNextId(),Record.TAG);
						if((stack.size() & 0x1) == 0){
							rectPaint.setColor(colorGreen);
						}else{
							rectPaint.setColor(Color.BLUE);				
						}
						cellW = thresholdW/(stack.size()*2 +2);
						if(stack.size() >1){
							path.moveTo(slideNum.get(stack.size())*cellW,
									(stack.size()+1)*cellH);
							path.lineTo(slideNum.get(stack.size()-1)*(thresholdW/(stack.size()*2)) ,
										(stack.size())*cellH);
						}
						
						c.drawRect(slideNum.get(stack.size())*cellW-cellW*0.25f,
								(stack.size()+1)*cellH -0.25f*cellW,
								slideNum.get(stack.size())*cellW + cellW*0.25f, 
								(stack.size()+1)*cellH +0.25f*cellW, rectPaint);
		
						c.drawPath(path, linePaint);
						path.reset();
						break;
					}
				}while(it.getRecordNextId() == Global.INVALIDATE_ID);
			}
		}while(!stack.isEmpty());
        
        BitmapUtil.decorateBitmap(mBitmap,c,displayMetrics.density);
		return mBitmap;
	}

	@Override
	public void recycle() {
		if(mBitmap != null){
			mBitmap.recycle();}
		mBitmap = null;
	}

	@Override
	public void delete() {
		TextRecord it = this;
		if(it.getRecordChildId() != Global.INVALIDATE_ID){
			TextRecord record = (TextRecord) EntityPool.instance().forId(
					it.getRecordChildId(),Record.TAG);
			record.delete();
		}
		if(it.getRecordNextId() != Global.INVALIDATE_ID){
			TextRecord record = (TextRecord) EntityPool.instance().forId(
					it.getRecordNextId(),Record.TAG);
			record.delete();
		}
		super.delete();
	}

	@Override
	public Record recentRecord() {
		LinkedList<TextRecord> stack = new LinkedList<TextRecord>();
		TextRecord result = this;
		TextRecord it = this;
		do{
			while(it.getRecordChildId() != Global.INVALIDATE_ID){
				stack.addFirst(it);
				it = (TextRecord) EntityPool.instance().forId(
						it.getRecordChildId(),Record.TAG);
				if(result.createDate < it.createDate){
					result = it;
				}
			}
			if(it.getRecordNextId() != Global.INVALIDATE_ID){
				it = (TextRecord) EntityPool.instance().forId(
						it.getRecordNextId(),Record.TAG);
				if(result.createDate < it.createDate){
					result = it;
				}
			}else{
				do{		
					it = stack.poll();
					if(it == null)
						return result;
					if(it.getRecordNextId() != Global.INVALIDATE_ID){
						it = (TextRecord) EntityPool.instance().forId(
								it.getRecordNextId(),Record.TAG);
						if(result.createDate < it.createDate){
							result = it;
						}
						break;
					}
				}while(it.getRecordNextId() == Global.INVALIDATE_ID);
			}
		}while(!stack.isEmpty());
		
		return null;
	}

	
}
