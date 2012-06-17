package com.singularity.clover.activity.lbs;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.lbs.LBSBundle;
import com.singularity.clover.entity.lbs.LBSBundle.Coordinate;
import com.singularity.clover.entity.wrapper.Scenario;

public class MapCanvasOverlay extends Overlay implements
		OnGestureListener {
	private GestureDetector gestureDetector;
	private OnGestureListener onGestureListener;
	protected DrawingPath currentDrawingPath;
	protected ArrayList<DrawingPath> mDrawingPaths = new ArrayList<DrawingPath>();
	private Polygon currentPolygon;
	private Paint currentPaint = new Paint();
	private float mBasePressure = 0.15f;
	private ArrayList<Float> arrayX = new ArrayList<Float>();
	private ArrayList<Float> arrayY = new ArrayList<Float>();
	protected ArrayList<Coordinate> mCoordinateSet = new ArrayList<Coordinate>();
	private boolean bActive = false;
	protected ArrayList<Long> currentLocs;
	protected long parentId;
	protected String parentTag;

	public MapCanvasOverlay() {
		gestureDetector = new GestureDetector(this);
		configPaint(Color.GRAY, 5);
	}

	public MapCanvasOverlay(OnGestureListener onGestureListener,
			ArrayList<Long> currentLocs,long parentId,String parentTag) {
		this();
		init(currentLocs,parentId,parentTag);
		setOnGestureListener(onGestureListener);
	}
	
	protected void init(ArrayList<Long> currentLocs,
			long parentId,String parentTag){
		this.currentLocs = currentLocs;
		this.parentId = parentId;
		this.parentTag = parentTag;
	
	}
	
	protected void setCurrentLocs(ArrayList<Long> currentLocs){
		this.currentLocs = currentLocs;
	}
	
	protected void recaculate(MapView mapView){
		mDrawingPaths.clear();
		Projection proj = mapView.getProjection();
		if(currentLocs != null){
			for(long entry:currentLocs){
				LBSBundle lbs = (LBSBundle) EntityPool.instance(
						).forId(entry, LBSBundle.TAG);
				if(lbs == null){
					continue;}
				ArrayList<Coordinate> coordinateSet = lbs.getCoordinateSet();
				DrawingPath drawPath = new DrawingPath();
				drawPath._path = new Path();
				drawPath._paint = new Paint(currentPaint);
				
				Scenario scenario = (Scenario) EntityPool.instance(
						).forId(lbs.getParentId(), lbs.getParentTag());
				drawPath._paint.setColor(scenario.getResId());
				drawPath._paint.setAlpha(128);
				if(coordinateSet == null){
					continue;}
				
				for(Coordinate coordinate:coordinateSet){
					GeoPoint geoPt= new GeoPoint(coordinate.getFixLatitude(),
							coordinate.getFixLongtitude());
					
					Point pt = proj.toPixels(geoPt, null);
					if(coordinateSet.indexOf(coordinate) == 0){
						drawPath._path.moveTo(pt.x, pt.y);
					}else{
						drawPath._path.lineTo(pt.x, pt.y);}
				}
				mDrawingPaths.add(drawPath);
			}
		}
	}
	protected void setActive(boolean bActive){
		this.bActive = bActive;
	}

	private class DrawingPath{
		public Path _path;
		public Paint _paint;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;}
		
		if(bActive){
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				currentDrawingPath = new DrawingPath();
				Paint paint = new Paint(currentPaint);
				Scenario scenario = (Scenario) EntityPool.instance(
						).forId(parentId, parentTag);
				paint.setColor(scenario.getResId());
				paint.setAlpha(128);
				//mBasePressure = event.getPressure();
				paint.setStrokeWidth(
						currentPaint.getStrokeWidth());
				//Log.d("Pressure",Float.toString(event.getPressure()));
				currentDrawingPath._paint = paint;
				currentDrawingPath._path = new Path();
				currentDrawingPath._path.moveTo(event.getX(),
												event.getY());
				
				arrayX.clear();
				arrayY.clear();
				arrayX.add(event.getX());
				arrayY.add(event.getY());
				
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				//Log.d("Pressure",Float.toString(event.getPressure()));
				
				currentDrawingPath._paint.setStrokeWidth(
						currentPaint.getStrokeWidth()*(event.getPressure()/mBasePressure));

				
				currentDrawingPath._path.lineTo(event.getX(), event.getY());
						
				arrayX.add(event.getX());
				arrayY.add(event.getY());
				
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				
				currentDrawingPath._paint.setStrokeWidth(
						currentPaint.getStrokeWidth()*(event.getPressure()/mBasePressure));
				
				currentDrawingPath._path.lineTo(event.getX(), event.getY());
				
				arrayX.add(event.getX());arrayY.add(event.getY());
				arrayX.trimToSize();arrayY.trimToSize();
				Float[] x = new Float[arrayX.size()];
				Float[] y = new Float[arrayX.size()];
				currentPolygon = new Polygon((Float[])arrayX.toArray(x),
						(Float[])arrayY.toArray(y), arrayX.size());	
			}
		}else{
			currentDrawingPath = null;
		}
		return bActive;
	}
	
	public void configPaint(int color,int penSize){
        currentPaint.setColor(color);
        currentPaint.setStyle(Paint.Style.FILL);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(penSize);
        currentPaint.setAntiAlias(true);
        currentPaint.setAlpha(128);
	}

	public void markDone(MapView mapView){
		Projection proj = mapView.getProjection();
		mCoordinateSet.clear();
		LBSBundle lbs = null;
		if(currentLocs == null){
			lbs = new LBSBundle(null, parentId, parentTag);
			currentLocs = new ArrayList<Long>();
			currentLocs.add(lbs.getId());
		}else{
			if(currentLocs.isEmpty()){
				lbs = new LBSBundle(null, parentId, parentTag);
				currentLocs.add(lbs.getId());
			}else{
				lbs = (LBSBundle) EntityPool.instance().forId(
						currentLocs.get(0), LBSBundle.TAG);}
		}
		for(int i = 0;i< arrayX.size();i++){
			GeoPoint pt = proj.fromPixels(arrayX.get(i).intValue(), 
					arrayY.get(i).intValue());
			Coordinate coor = lbs.new Coordinate(
					pt.getLatitudeE6(), pt.getLongitudeE6());
			mCoordinateSet.add(coor);
		}
		lbs.setCoordinateSet(mCoordinateSet);
		lbs.store();
	}
	
	public void markClear(){
		mCoordinateSet.clear();
		LBSBundle lbs = null;
		if(currentLocs != null){
			if(!currentLocs.isEmpty()){
				lbs = (LBSBundle) EntityPool.instance().forId(
						currentLocs.get(0), LBSBundle.TAG);
				lbs.setCoordinateSet(mCoordinateSet);
				lbs.store();
			}
		}	
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		if (onGestureListener != null) {
			return onGestureListener.onDown(e);
		}
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (onGestureListener != null) {
			return onGestureListener.onFling(e1, e2, velocityX, velocityY);
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if (onGestureListener != null) {
			onGestureListener.onLongPress(e);
		}
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (onGestureListener != null) {
			onGestureListener.onScroll(e1, e2, distanceX, distanceY);
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		if (onGestureListener != null) {
			onGestureListener.onShowPress(e);
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (onGestureListener != null) {
			onGestureListener.onSingleTapUp(e);
		}
		return false;
	}

	public boolean isLongpressEnabled() {
		return gestureDetector.isLongpressEnabled();
	}

	public void setIsLongpressEnabled(boolean isLongpressEnabled) {
		gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
	}

	public OnGestureListener getOnGestureListener() {
		return onGestureListener;
	}

	public void setOnGestureListener(OnGestureListener onGestureListener) {
		this.onGestureListener = onGestureListener;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		if(currentDrawingPath != null){
			canvas.drawPath(currentDrawingPath._path,
					currentDrawingPath._paint);
		}
		recaculate(mapView);
		for(DrawingPath path:mDrawingPaths){
			canvas.drawPath(path._path,path._paint);
		}
	}

}
