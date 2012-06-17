/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.singularity.clover.activity.lbs;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MyItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();
	private Context c;
	private final float INVALIDATE_DISTANCE = 0.0f;
	private float mDistance = INVALIDATE_DISTANCE;
	
	public MyItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		c = mapView.getContext();
	}

	public void addOverlay(OverlayItem overlay) {
	    m_overlays.add(overlay);
	    populate();
	}
	
	public void setDistance(float distance){
		mDistance = distance;
	}

	@Override
	protected OverlayItem createItem(int i) {
		if(m_overlays.isEmpty()){
			return null;}
		
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}
	
	public void clear(){
		m_overlays.clear();
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		Toast.makeText(c, "onBalloonTap for overlay index " + index,
				Toast.LENGTH_LONG).show();
		return true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(mDistance != INVALIDATE_DISTANCE ){
			drawArea(canvas, mapView);}
		
		super.draw(canvas, mapView, false);
		
	}
	
	@SuppressWarnings("unused")
	private void drawArea(Canvas canvas,MapView mapView){
		Paint currentPaint = new Paint();
		currentPaint.setColor(Color.GRAY);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(2);
        
        currentPaint.setAntiAlias(true);
		for(OverlayItem entry:m_overlays){
			// Convert geo coordinates to screen pixels
			Point screenPoint = new Point();
			Projection projecton = mapView.getProjection();
			projecton.toPixels(entry.getPoint(), screenPoint);
			float radius = projecton.metersToEquatorPixels(mDistance);
			canvas.drawCircle(screenPoint.x, screenPoint.y, radius, currentPaint);
		}
	}
	
}
