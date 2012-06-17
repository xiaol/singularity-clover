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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.lbs.LBSBundle;
import com.singularity.clover.entity.lbs.LBSBundle.Coordinate;

public class LocationActivity extends MapActivity {

	public static final String IN_PARENT_ID = "com.singularity.clover.activity.LocationActivity.parentId";
	public static final String IN_PARENT_TAG = "com.singularity.clover.activity.LocationActivity.parentTag";
	
	public static final String LOCATION_VIEW = "com.singularity.clover.activity.location.view";
	public static final String LOCATION_OVERVIEW ="com.singularity.clover.activity.location.overview";
	public static final int MAX_SEARCH_RESULT = 5;
	
	MapView mMapView;
	List<Overlay> mapOverlays;
	Drawable drawableRedMark;
	Drawable drawableGreenMark;
	Drawable drawalbeLocationMark;
	MyItemizedOverlay itemizedOverlay;
	MyItemizedOverlay itemizedLocationOverlay;
	View mActive;
	private ImageButton mMapMode;
	private ImageButton mMapLocation;
	private ImageButton mSearch;
	private Location currentLocation;
	private OverlayItem mPositionItem;
	private boolean bActive = false;
	private MapCanvasOverlay mMapCanvas;
	protected MapController mMapCtrl;
	
	protected long parentId;
	protected String parentTag;
	protected ArrayList<Long> currentLocs;
	
	protected int STATE_IDLE = 0;
	protected int STATE_VIEW = 1;
	protected int STATE_OVERVIEW = 2;
	protected int mState = STATE_IDLE;
	
	// Minimum & maximum latitude so we can span it
	// The latitude is clamped between -90 degrees and +90 degrees inclusive
	// thus we ensure that we go beyond that number
 
    private int minLatitude = (int)(+91 * 1E6);
    private int maxLatitude = (int)(-91 * 1E6);
 
    // Minimum & maximum longitude so we can span it
    // The longitude is clamped between -180 degrees and +180 degrees inclusive
    // thus we ensure that we go beyond that number
    private int minLongitude  = (int)(+181 * 1E6);;
    private int maxLongitude  = (int)(-181 * 1E6);
	private AlertDialog mMarkDialog;;
      
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view_layout);
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setBuiltInZoomControls(true);
        mActive = findViewById(R.id.mapview_active_mark); 
        mMapMode = (ImageButton) findViewById(R.id.mapview_layer);
        mMapMode.setBackgroundResource(android.R.drawable.ic_menu_mapmode);
        mMapLocation = (ImageButton) findViewById(R.id.mapview_location);
        mMapLocation.setBackgroundResource(android.R.drawable.ic_menu_mylocation);
        mSearch = (ImageButton) findViewById(R.id.mapview_search);
        mSearch.setBackgroundResource(android.R.drawable.ic_menu_search);
        
        Intent intent = getIntent();
        mMapCtrl = mMapView.getController();
        if(intent != null){
        	handleIntent(intent);}	
		mapOverlays = mMapView.getOverlays();

		mapOverlays.clear();
		mMapCanvas = new MapCanvasOverlay(new MyOnGestureListener(),currentLocs,
				parentId,parentTag); 
		mapOverlays.add(mMapCanvas);

		drawableRedMark = getResources().getDrawable(R.drawable.marker);
		drawalbeLocationMark = getResources().getDrawable(R.drawable.location_mark);
    }
	
	@Override
	public void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	    mMapCanvas.init(currentLocs, parentId, parentTag);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void handleIntent(Intent intent) {
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, MySuggestionProvider.AUTHORITY,
					MySuggestionProvider.MODE);
			suggestions.saveRecentQuery(query, null);
			doMySearch(query, this);
	    }else{
	    	String action = intent.getAction();
	    	if(action.equals(LOCATION_VIEW)){
	    		parentId = intent.getLongExtra(IN_PARENT_ID, Global.INVALIDATE_ID);
	    		parentTag = intent.getStringExtra(IN_PARENT_TAG);
	    		if(parentId != Global.INVALIDATE_ID){
	    			String whereClause = "WHERE parent_id = ? AND parent_tag = ?";
	    			String[] whereArgs = new String[]{
	    					Long.toString(parentId),parentTag};
	    			currentLocs = EntityPool.instance().getPrototype(
	    					LBSBundle.TAG).loadTable(whereClause, whereArgs);
	    			mState = STATE_VIEW;
	    			if(currentLocs == null){
	    				Location loc = getLastLocation(Criteria.ACCURACY_COARSE);
	    				if(loc != null){
		    				double lat = loc.getLatitude();
						    double lng = loc.getLongitude();
						    GeoPoint geoPt = new GeoPoint((int)(lat*1E6),
						    		(int)(lng*1E6));
							mMapCtrl.animateTo(geoPt);
							mMapCtrl.setZoom(16);
	    				}else{
	    					mMapCtrl.setZoom(2);
	    				}
	    			}else{
		    			resetOverview();
		    			for(Long entry:currentLocs){
		    				LBSBundle lbs = (LBSBundle) EntityPool.instance(
		    						).forId(entry, LBSBundle.TAG);
		    				for(Coordinate coor:lbs.getCoordinateSet()){
		    					preOverview(coor.getFixLatitude(),
		    							coor.getFixLongtitude());
		    				}
		    			}
		    			processOverview();
	    			}
	    			mActive.setVisibility(View.VISIBLE);
	    		}else{
	    			finish();}
	    	}else if(action.equals(LOCATION_OVERVIEW)){
	    		currentLocs = EntityPool.instance().getPrototype(
	    					LBSBundle.TAG).loadTable(null, null);
	    		mState = STATE_OVERVIEW;
	    		if(currentLocs == null){
	    			Location loc = getLastLocation(Criteria.ACCURACY_COARSE);
	    			if(loc != null){
		    			double lat = loc.getLatitude();
						double lng = loc.getLongitude();
						GeoPoint geoPt = new GeoPoint((int)(lat*1E6),
						    (int)(lng*1E6));
						mMapCtrl.animateTo(geoPt);
						mMapCtrl.setZoom(16);
	    			}else{
	    				mMapCtrl.setZoom(2);
	    			}
	    		}else{
		    		resetOverview();
		    		for(Long entry:currentLocs){
		    			LBSBundle lbs = (LBSBundle) EntityPool.instance(
		    					).forId(entry, LBSBundle.TAG);
		    			for(Coordinate coor:lbs.getCoordinateSet()){
		    				preOverview(coor.getFixLatitude(),
		    							coor.getFixLongtitude());
		    			}
		    		}
		    		processOverview();
	    		}
	    		mActive.setVisibility(View.GONE);
	    	}
	    }
	}
	
	public void onBtnActive(View view){
		bActive = !bActive;
		mMapCanvas.setActive(bActive);
		if(!bActive){
			if(mMarkDialog == null){
				setupDialog();
				mMarkDialog.show();
			}else{
				mMarkDialog.show();
			}
		}
	}
	
	public void onBtnSearch(View view){
		onSearchRequested();
	}
	
	public void onBtnMapMode(View view){
		mMapView.setSatellite(!mMapView.isSatellite());
	}
	
	public void onBtnLocation(View view){
		Location loc = getLastLocation(Criteria.ACCURACY_FINE);
		if(loc != null){
			double lat = loc.getLatitude();
		    double lng = loc.getLongitude();
		    GeoPoint geoPt = new GeoPoint((int)(lat*1E6),(int)(lng*1E6));
			mMapCtrl.setCenter(geoPt);
			mMapCtrl.setZoom(16);
			
			mapOverlays.remove(itemizedLocationOverlay);
			itemizedLocationOverlay = new MyItemizedOverlay(
					drawalbeLocationMark,mMapView);
			OverlayItem overlayitem = new OverlayItem(
					geoPt,"", "");
			itemizedLocationOverlay.addOverlay(overlayitem);
			itemizedLocationOverlay.setDistance(loc.getAccuracy());
			mapOverlays.add(itemizedLocationOverlay);
		}else{
			notifyToast(getText(R.string.locate_failed));
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	public void doMySearch(final String query, final Context context) {
		notifyToast(getText(R.string.searching));
		Thread thread = new Thread() {
			@Override
			public void run() {
				Geocoder geocoder = new Geocoder(context, Locale.getDefault());
				List<Address> addresses = null;
				try {
					addresses = geocoder.getFromLocationName(query,
							MAX_SEARCH_RESULT);

					if (addresses == null){
						notifyToast(getText(R.string.searching_empty));
						return;
					}	else{
						if(addresses.isEmpty()){
							notifyToast(getText(R.string.searching_empty));
							return;}
					}
			
					mapOverlays.remove(itemizedOverlay);
					itemizedOverlay = new MyItemizedOverlay(drawableRedMark,
							mMapView);
					resetOverview();
					for (Address address : addresses) {
						Double latitude = address.getLatitude() * 1E6;
						Double longitude = address.getLongitude() * 1E6;
						GeoPoint geoPt = new GeoPoint(latitude.intValue(),
								longitude.intValue());
						OverlayItem overlayitem = new OverlayItem(geoPt,
								address.getLocality(), address.getFeatureName());

						itemizedOverlay.addOverlay(overlayitem);
						preOverview(latitude.intValue(), longitude.intValue());
					}
					mapOverlays.add(itemizedOverlay);
					processOverview();
					
				} catch (IOException e) {
					Log.e("MapView", "Impossible to connect to Geocoder", e);
					notifyToast(getText(R.string.searching_failed));
				} finally {
					
				}
			}
		};
		thread.start();
	}

	private void resetOverview(){
		minLatitude = (int)(+91 * 1E6);
	    maxLatitude = (int)(-91 * 1E6);
	    minLongitude  = (int)(+181 * 1E6);;
	    maxLongitude  = (int)(-181 * 1E6);;
	}
	
	private void preOverview(int latitude, int longitude) {

		// Sometimes the longitude or latitude gathering
		// did not work so skipping the point
		// doubt anybody would be at 0 0
		if (latitude != 0 && longitude != 0) {

			// Sets the minimum and maximum latitude so we can span and zoom
			minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
			maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;

			// Sets the minimum and maximum latitude so we can span and zoom
			minLongitude = (minLongitude > longitude) ? longitude
					: minLongitude;
			maxLongitude = (maxLongitude < longitude) ? longitude
					: maxLongitude;
		}

	}

	private void processOverview() {
		// Zoom to span from the list of points
		int latitudeSpan = maxLatitude - minLatitude;
		int longitudeSpan = maxLongitude - minLongitude;
		if(latitudeSpan == 0 && longitudeSpan == 0){
			mMapCtrl.setZoom(18);
		}else{
			mMapCtrl.zoomToSpan((maxLatitude - minLatitude),
					(maxLongitude - minLongitude));
		}

		// Animate to the center cluster of points
		mMapCtrl.animateTo(new GeoPoint((maxLatitude + minLatitude) / 2,
				(maxLongitude + minLongitude) / 2));
	}
	
	private Location getLastLocation(int accuracy){
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
		criteria.setAccuracy(accuracy);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		
		String provider = locationManager.getBestProvider(criteria, true);
		if(provider == null){
			return null;}
		
		Location location = locationManager.getLastKnownLocation(provider);
		return location;
	}

	private void setupDialog(){
		AlertDialog.Builder builder;
		
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(LAYOUT_INFLATER_SERVICE);
		View root = (ViewGroup) inflater.inflate(
				R.layout.mapview_mark_dialog_layout,null);
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(root);
		builder.setTitle(getText(R.string.dialog_mark_title));
		mMarkDialog = builder.create();
	}
	
	public void onBtnReplace(View view){
		mMapCanvas.markDone(mMapView);
		mMapView.invalidate();
		mMarkDialog.dismiss();
	}
	
	public void onBtnClear(View view){
		mMapCanvas.markClear();
		mMarkDialog.dismiss();
	}
	
	public void onBtnCancel(View view){
		mMarkDialog.dismiss();
	}
	
	private void notifyToast(CharSequence text){
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public class MyOnGestureListener implements OnGestureListener{

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		
	}
}
