package com.singularity.clover.activity.entity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.singularity.clover.R;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.wrapper.Scenario;

public class ReviewView extends ImageView {
	
	private Paint mPaint;
    float mScenarioRadius = 9;
    int mScenarioCount;
    float mStep = 30;
    float mWidth,mHeight;
    ArrayList<Long> mIds;
    
    Context mContext;
	
	public ReviewView(Context context, AttributeSet attrs) {
		super(context, attrs);
		float density = context.getResources().getDisplayMetrics().density;
		mScenarioRadius = density*mScenarioRadius;
		mStep = density*mStep;
		initView();
		mContext = context;
	}

	protected void setParams(ArrayList<Long> ids){
		mIds = ids;
		int count = ids.size();
		mHeight = mStep;
		mWidth = count*mStep;
		mScenarioCount = count;
		if(mScenarioCount > 5){
			mScenarioCount = 5;}

	}
	
	private final void initView() {

        //setPadding(3, 3, 3, 3);
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
		mPaint.setDither(true);
		mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result =  (int)mWidth+ getPaddingLeft() + getPaddingRight();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int)mHeight + getPaddingTop() + getPaddingBottom();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pos = mStep/2;
        for(int i= 0; i<mScenarioCount;i++){
        	long id = mIds.get(i);
        	Scenario sceanrio = (Scenario) EntityPool.instance().forId(id, Scenario.TAG);
        	mPaint.setColor(sceanrio.getResId());
        	RectF rect = new RectF((int)(pos-mScenarioRadius), (int)(mHeight/2 - mScenarioRadius*2/3),
        			(int)(pos+mScenarioRadius), (int)(mHeight/2+mScenarioRadius*2/3));
        	canvas.drawRoundRect(rect, 3, 3,mPaint);
        	pos += mStep;
        }
    }
}
