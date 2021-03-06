package com.singularity.clover.activity.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.singularity.clover.R;
import com.singularity.clover.Global;
import com.singularity.clover.util.BitmapUtil;

public class WhiteboardView extends SurfaceView 
								implements SurfaceHolder.Callback{
	protected RenderThread _thread;
	private DrawingPath currentDrawingPath;
	private Paint currentPaint,textPaint;
	private Bitmap _bitmap,_bitmapOrg;
	private boolean bMove = false;
	private boolean bPreview = false;
	private boolean bText = false;
	private Context _context;
	private Rect rectCanvas,rectScreen;
	private float coordinateX,coordinateY;
	private float textX,textY;
	private String textToPaint = null;
	private GestureDetector detector = new GestureDetector(_context
												,new GestureListener());
	private Rect mDrawingRect = new Rect();
	private boolean bInitDrawingRect = true;
	private float mBasePressure = 0.15f;
	private float mCrosserWidth = 5;
	
	public WhiteboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		_thread = new RenderThread(getHolder());
		_context = context;
		currentPaint = new Paint();
		textPaint = new Paint();
		configPaint(Color.BLACK,
				(int) (5*getResources().getDisplayMetrics().density));
		mCrosserWidth = 5*getResources().getDisplayMetrics().density;

		Display display = ((WindowManager) context.
				getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        coordinateX = display.getWidth(); 
        coordinateY = display.getHeight();
        rectCanvas = new Rect(0, 0, 
        	display.getWidth()*Global.WHITEBOARD_SIZE_FACTOR,
        	display.getHeight()*Global.WHITEBOARD_SIZE_FACTOR);
        rectScreen = new Rect((int)coordinateX, (int)coordinateY,
        		(int)coordinateX + display.getWidth(),(int)coordinateY +display.getHeight());
        mDrawingRect = new Rect((int)coordinateX, (int)coordinateY,
        		(int)coordinateX + display.getWidth(),(int)coordinateY +display.getHeight());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if(_bitmap == null){
			_bitmap = BitmapUtil.createOriginalBitmap(_context);
			_thread._canvas = new Canvas(_bitmap);
			_thread._canvas.drawColor(
					_context.getResources().getColor(R.color.whiteboard_bg));
			
			float widthPaint = currentPaint.getStrokeWidth();
			currentPaint.setStrokeWidth(widthPaint/2);
			_thread._canvas.drawLine(_bitmap.getWidth()/2-mCrosserWidth,
					_bitmap.getHeight()/2, _bitmap.getWidth()/2+mCrosserWidth,
					_bitmap.getHeight()/2, currentPaint);
			_thread._canvas.drawLine(_bitmap.getWidth()/2,
					_bitmap.getHeight()/2-mCrosserWidth, _bitmap.getWidth()/2,
					_bitmap.getHeight()/2+mCrosserWidth, currentPaint);
			currentPaint.setStrokeWidth(widthPaint);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(_bitmap == null){
			_bitmap = BitmapUtil.createOriginalBitmap(_context);
			_thread._canvas = new Canvas(_bitmap);
			_thread._canvas.drawColor(
					_context.getResources().getColor(R.color.whiteboard_bg));
			
			float width = currentPaint.getStrokeWidth();
			currentPaint.setStrokeWidth(width/2);
			_thread._canvas.drawLine(_bitmap.getWidth()/2-mCrosserWidth,
					_bitmap.getHeight()/2, _bitmap.getWidth()/2+mCrosserWidth,
					_bitmap.getHeight()/2, currentPaint);
			_thread._canvas.drawLine(_bitmap.getWidth()/2,
					_bitmap.getHeight()/2-mCrosserWidth, _bitmap.getWidth()/2,
					_bitmap.getHeight()/2+mCrosserWidth, currentPaint);
			currentPaint.setStrokeWidth(width);
			
		}else{
			
		}
		if(!_thread.isRunning()){
			_thread.setRunning(true);
			_thread.start();
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		_thread.setRunning(false);
		while (retry) {
			try {
				_thread.join();
				retry = false;
				_thread._canvas = null;
				if(_bitmap != null){
					_bitmap.recycle();
					_bitmap = null;
				}	
				System.gc();
			} catch (InterruptedException e) {
				
			}
		}
	}

	public void configPaint(int color,int penSize){
        currentPaint.setColor(color);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(penSize);
        currentPaint.setAntiAlias(true);
        
        textPaint.setTextSize(25);
        textPaint.setColor(Color.BLACK);
        textPaint.setDither(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setUnderlineText(true);
	}
	
	public void setMoving(boolean bMove){
		this.bMove = bMove;
	}
	
	public void setPreview(boolean bPreview){
		this.bPreview = bPreview;
		if(bPreview && _bitmap != null){
			_bitmapOrg = _bitmap;
			_bitmap = BitmapUtil.previewBitmp(_bitmap, _context);
		}else{
			_thread._canvas = null;
			_bitmap.recycle();
			_bitmap = _bitmapOrg;		
		}
		_thread._canvas = new Canvas(_bitmap);
	}
	
	public void setInputText(boolean bText){
		this.bText = bText;	
	}
	
	public void setConfirm(boolean bConfirm,String text){
		View v = (View) getParent();
		if(bConfirm){
			synchronized (text) {
				textToPaint = text;
				Rect rect = new Rect();
				textPaint.getTextBounds(textToPaint, 0, textToPaint.length(), rect);
				rect.offsetTo((int)(textX + coordinateX - rect.width()/2.0),
								(int)(textY + coordinateY - rect.height()/2.0));
				mDrawingRect.union(rect);
			}
		}else{
			bText = false;
			textToPaint = null;
		}
		v.findViewById(R.id.whiteboard_edit_text_layout).
					setVisibility(View.GONE);
		//_thread.setRunning(true);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!bMove && !bPreview && !bText) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				currentDrawingPath = new DrawingPath();
				Paint paint = new Paint(currentPaint);
				//mBasePressure = event.getPressure();
				paint.setStrokeWidth(
						currentPaint.getStrokeWidth());
				//Log.d("Pressure",Float.toString(event.getPressure()));
				currentDrawingPath._paint = paint;
				currentDrawingPath._path = new Path();
				currentDrawingPath._path.moveTo(event.getX()+coordinateX,
												event.getY()+coordinateY);
				if(bInitDrawingRect){
					mDrawingRect.set((int)(event.getX()+coordinateX),
							(int)(event.getY()+coordinateY),(int)(event.getX()+coordinateX),
							(int)(event.getY()+coordinateY));
					bInitDrawingRect = false;
				}
				mDrawingRect.union((int)(event.getX()+coordinateX),
					(int)(event.getY()+coordinateY));
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				//Log.d("Pressure",Float.toString(event.getPressure()));
				
				currentDrawingPath._paint.setStrokeWidth(
						currentPaint.getStrokeWidth()*(event.getPressure()/mBasePressure));

				synchronized (currentDrawingPath._path) {
					currentDrawingPath._path.lineTo(event.getX()+coordinateX, 
												event.getY()+coordinateY);
				}
				currentDrawingPath.lastX =event.getX()+coordinateX;
				currentDrawingPath.lastY = event.getY()+coordinateY;
				
				mDrawingRect.union((int)(event.getX()+coordinateX),
					(int)(event.getY()+coordinateY));
				_thread._renderPreviewPaths.add(currentDrawingPath);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				
				currentDrawingPath._paint.setStrokeWidth(
						currentPaint.getStrokeWidth()*(event.getPressure()/mBasePressure));
				synchronized (currentDrawingPath._path) {
					currentDrawingPath._path.lineTo(event.getX()+coordinateX, 
													event.getY()+coordinateY);
				}
				_thread._renderPreviewPaths.add(currentDrawingPath);
				//_thread._renderPaths.add(currentDrawingPath);
				mDrawingRect.union((int)(event.getX()+coordinateX),
					(int)(event.getY()+coordinateY));
			}
		
			/*Log.d("Drawing Rect x,y,w,h",mDrawingRect.left +","
					+mDrawingRect.top+","+mDrawingRect.width()+","+mDrawingRect.height());*/
			
		} else if(bMove){
			detector.onTouchEvent(event);
		} else if(bText){
			detector.onTouchEvent(event);
			mDrawingRect.union((int)(event.getX()+coordinateX),
					(int)(event.getY()+coordinateY));
		}
		return true;
	}
	
	private class GestureListener extends SimpleOnGestureListener{
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e, float disX,
				float disY) {
			Rect temp = new Rect(rectScreen);
			temp.offset((int) disX, (int) disY);
			if (rectCanvas.contains(temp)) {
				coordinateX += disX;coordinateY += disY;
				rectScreen = temp;
			}
			return false;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(bText){
				//_thread.setRunning(false);
				textX = e.getX();textY = e.getY();
				View v = (View) getParent();
				v.findViewById(R.id.whiteboard_edit_text_layout).
					setVisibility(View.VISIBLE);
			}	
			return super.onSingleTapUp(e);
		}			
	}
	
	/*这个函数希望不要被重复调用*/
	public Bitmap getBitmap(){
		try{
			int width = _bitmap.getWidth();
			int height = _bitmap.getHeight();
			if(mDrawingRect.left < 0){
				mDrawingRect.left = 0;}
			
			if(mDrawingRect.top < 0){
				mDrawingRect.top = 0;}
			
			if(width < mDrawingRect.left + mDrawingRect.width()){
				width = width - mDrawingRect.left;
			}else{
				width = mDrawingRect.width();
			}
			if(height < mDrawingRect.top + mDrawingRect.width()){
				height = height - mDrawingRect.top;
			}else{
				height = mDrawingRect.height();
			}
			
		    Bitmap bitmapToSave = Bitmap.createBitmap(_bitmap, mDrawingRect.left,
					mDrawingRect.top,width, height);
		    _thread.setRunning(false);
		    synchronized (_bitmap) {
			    _bitmap.recycle();
			    _bitmap = null;
		    }
			return bitmapToSave;
		}catch(OutOfMemoryError e){
			System.gc();
			return _bitmap;
		}
	    
	}
	
	public boolean setBitmap(String strUri){
		if(_bitmap != null){
			_bitmap.recycle();
		}
		
		_bitmap = BitmapUtil.getBitmap(_context, strUri,mDrawingRect);
		if(_bitmap == null){
			return false;
		}
		if(_bitmap.isMutable()){
			_thread._canvas = new Canvas(_bitmap);
		}else{
			_thread._canvas = new Canvas();
			//_thread._canvas.setBitmap(_bitmap.copy(Bitmap.Config.RGB_565, true));
			Display display = ((WindowManager) _context.getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay();
		    int desWidth = display.getWidth()*Global.WHITEBOARD_SIZE_FACTOR; 
		    int desHeight = display.getHeight()*Global.WHITEBOARD_SIZE_FACTOR;
		    int left = (desWidth-_bitmap.getWidth())/2;
			int top = (desHeight-_bitmap.getHeight())/2;
			_thread._canvas.drawBitmap(_bitmap, (desWidth-_bitmap.getWidth())/2,
					(desHeight-_bitmap.getHeight())/2, null);
			mDrawingRect.set(left, top, 
					left +_bitmap.getWidth(), top + _bitmap.getHeight());
			Toast.makeText(_context, _context.getText(
					R.string.can_not_edit), Toast.LENGTH_LONG).show();	
		}
		bInitDrawingRect = false;
		return true;
	}
	
	
	public void recycle(){
		_context = null;
	}
	
	private class DrawingPath{
		public Path _path;
		public Paint _paint;
		public float lastX,lastY;
	}
	
	protected class RenderThread extends Thread{
		private SurfaceHolder _surfaceHolder;
		private List<DrawingPath> _renderPaths;
		private List<DrawingPath> _renderPreviewPaths;
		private boolean _run = false;
		private Canvas _canvas;
		
		public boolean isRunning(){
			return _run;
		}
		
		public RenderThread(SurfaceHolder surfaceHolder){
			_surfaceHolder = surfaceHolder;
			_renderPaths = Collections.
				synchronizedList(new ArrayList<DrawingPath>());
			_renderPreviewPaths = Collections.
				synchronizedList(new ArrayList<DrawingPath>());
		}
		
		public void setRunning(boolean bRun){
			_run = bRun;
		}
		
		@Override
		public void run() {
			Canvas canvas = null;
			while (_run) {
				try {
				canvas = _surfaceHolder.lockCanvas(null);
				
				synchronized (_renderPaths) {
					Iterator<DrawingPath> it = _renderPaths.iterator();
					while (it.hasNext()) {
						final DrawingPath path = (DrawingPath) it.next();
						_canvas.drawPath(path._path, path._paint);
					}
					_renderPaths.clear();
					
				  }
				synchronized (_renderPreviewPaths) {
					Iterator<DrawingPath> it2 = _renderPreviewPaths.iterator();
					while (it2.hasNext()) {
						final DrawingPath path = (DrawingPath) it2.next();
						synchronized (path._path) {
							_canvas.drawPath(path._path, path._paint);
							path._path.reset();
							path._path.moveTo(path.lastX, path.lastY);
						}	
					}
					_renderPreviewPaths.clear();
				}
				
				if (textToPaint != null) {
					synchronized (textToPaint) {
						_canvas.drawText(textToPaint, textX + coordinateX,
								textY + coordinateY, textPaint);
						textToPaint = null;
					}
				}
				
				if(_bitmap != null){
					synchronized (_bitmap) {
						if (bPreview) {
							canvas.drawBitmap(_bitmap, 0, 0, null);
						}else {
							if(!_bitmap.isRecycled()){
								canvas.drawBitmap(_bitmap,
									-coordinateX,-coordinateY, null);
							}
						}
					}
				}
				
				} finally {
					_surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}

	}

	
}
