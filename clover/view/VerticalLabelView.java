package com.singularity.clover.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.singularity.clover.R;

public class VerticalLabelView extends View {
    private TextPaint mTextPaint;
    private Paint mPaint;
    private String mText;
    private int mAscent;
    private Rect text_bounds = new Rect();
    private boolean bBackground = false;

    final static int DEFAULT_TEXT_SIZE = 15;
    protected DisplayMetrics mDisplayMetrics;
    
    public VerticalLabelView(Context context) {
        super(context);
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        initLabelView();
    }

    public VerticalLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        initLabelView();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalLabelView);

        CharSequence s = a.getString(R.styleable.VerticalLabelView_text);
        if (s != null) setText(s.toString());

        setTextColor(a.getColor(R.styleable.VerticalLabelView_textColor, 0xFF000000));

        int textSize = a.getDimensionPixelOffset(R.styleable.VerticalLabelView_textSize, 0);
        if (textSize > 0) setTextSize(textSize);

        a.recycle();
    }

    private final void initLabelView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE*mDisplayMetrics.density);
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextAlign(Align.CENTER);
        //mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }
    
    public void setBackgroundColor(int color){
    	bBackground = true;
    	if(mPaint == null){
    		mPaint = new Paint();
    	}
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
		mPaint.setDither(true);
		mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
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

        if ( specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) ((/*text_bounds.height() */14+ 
            		getPaddingLeft() + getPaddingRight())*mDisplayMetrics.density);

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
        if ( specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) (text_bounds.width() + getPaddingTop() 
            		+ getPaddingBottom()+10*mDisplayMetrics.density);

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

        float text_horizontally_centered_origin_x = getPaddingTop() + text_bounds.width()/2f;
        float text_horizontally_centered_origin_y = getPaddingLeft() /*- mAscent*/ ;
        if(bBackground){
        	RectF rect = new RectF(-2*mDisplayMetrics.density, 2*mDisplayMetrics.density,
        			getMeasuredWidth(), getMeasuredHeight() - 2*mDisplayMetrics.density);
        	canvas.drawRoundRect(rect, 3*mDisplayMetrics.density, 3*mDisplayMetrics.density, mPaint);
        }
        canvas.translate(text_horizontally_centered_origin_y, text_horizontally_centered_origin_x);
        canvas.rotate(90);
        canvas.drawText(mText, 0, 0, mTextPaint);
        
       
    }
}