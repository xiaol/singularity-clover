package com.singularity.clover.activity.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.singularity.clover.R;

public class ListItemOBJView extends ImageView {
	private TextPaint mTextPaint;
	private Paint mPaint;
    private String mText;
    private int mAscent;
    private Rect text_bounds = new Rect();
    private float processBarHeight,processBarWidth,mProcessRate;
    
    final static int DEFAULT_TEXT_SIZE = 12;
    float mProcessStep = 20;
    int mProcessStepCount;
    float mProcessBarHeight = 10;
    float mProcessStepHeight = 5;
    
    Context mContext;
    Bitmap mArrow;
	
	public ListItemOBJView(Context context, AttributeSet attrs) {
		super(context, attrs);
		float density = context.getResources().getDisplayMetrics().density;
		mProcessStep = density*mProcessStep;
		mProcessBarHeight = density*mProcessBarHeight;
		mProcessStepHeight = density*mProcessStepHeight;
		initView();
		
		mContext = context;
	}

	protected void setParams(int OBJCount, float processRate){
		processBarHeight = mProcessBarHeight;
		processBarWidth = mProcessStep*OBJCount;
		mProcessStepCount = OBJCount+1;
		if(mProcessStepCount > 10){
			mProcessStepCount = 10;}
		
		mProcessRate = processRate;
		mText = (int)(OBJCount*processRate)+"/"+OBJCount;
		
		mArrow = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.task_overview_arrow_icon);

	}
	
	private final void initView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));

        //setPadding(3, 3, 3, 3);
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
		mPaint.setDither(true);
		mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mTextPaint.getTextBounds(mText, 0, mText.length(), text_bounds);
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
            result = Math.max(text_bounds.width(),mArrow.getWidth()) + (int)processBarWidth
            		+ getPaddingLeft() + getPaddingRight();

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

        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = text_bounds.height()+(int)processBarHeight + mArrow.getHeight()
            	+ getPaddingTop() + getPaddingBottom();

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
        mPaint.setStrokeWidth(2.5f);
        float width = Math.max(text_bounds.width(),mArrow.getWidth());
        canvas.drawLine(getPaddingLeft()+width/2, getHeight()-getPaddingBottom(),
        		getWidth()-getPaddingRight() -getPaddingLeft()-width/2,
        		getHeight()-getPaddingBottom(), mPaint);
        mPaint.setStrokeWidth(2.5f);
        for(int i =0; i< mProcessStepCount;i++){
        	if(i == mProcessStepCount -1){
        		canvas.drawLine(
        			getWidth()-getPaddingRight() -getPaddingLeft()-width/2,
        			getHeight()-getPaddingBottom(),
        			getWidth()-getPaddingRight() -getPaddingLeft()-width/2,
        			getHeight()-getPaddingBottom()-mProcessStepHeight,mPaint);
        		continue;
        	}
        	canvas.drawLine(getPaddingLeft()+width/2 + i*mProcessStep,
        			getHeight()-getPaddingBottom(),
        			getPaddingLeft()+width/2 + i*mProcessStep,
        			getHeight()-getPaddingBottom()-mProcessStepHeight,mPaint);
        }
        float x = (getWidth() - getPaddingLeft() - getPaddingRight()
        		-width)*mProcessRate + width/2;
        //canvas.drawText(mText, x ,
        		//text_bounds.height(), mTextPaint);
        canvas.drawBitmap(mArrow, x- mArrow.getWidth()/2, text_bounds.height()+mProcessStepHeight/2, null);
    }
}
