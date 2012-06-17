package com.singularity.clover.activity.lbs;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.activity.entity.TaskOverViewActivity;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.lbs.LBSBundle;
import com.singularity.clover.entity.lbs.LBSBundle.Coordinate;
import com.singularity.clover.entity.wrapper.Scenario;

public class LocationService extends Service {

	private final DecimalFormat sevenSigDigits = new DecimalFormat("0.#######");
	private final DateFormat timestampFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private LocationManager lm;
	private LocationListener locationListener;

	private static long minTimeMillis = 60000;
	private static long minDistanceMeters = 15;
	private static float minAccuracyMeters = 30;
	private static float maxAccuracyMeters = 200;

	private int lastStatus = 0;
	private static boolean showingDebugToast = false;
	private long lastLBSId = Global.INVALIDATE_ID;
	protected static final int VIBRATE_DURATION = 30000;

	private static final String tag = "LocationService";

	/** Called when the activity is first created. */
	private void startService() {

		// ---use the LocationManager class to obtain GPS locations---
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener();

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMillis,
				minDistanceMeters, locationListener);
		locationListener.onLocationChanged(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER));
	}

	private void shutdownService() {
		lm.removeUpdates(locationListener);
	}

	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			if (loc != null) {
				HashMap<Long, Persisable> bundles = EntityPool.instance()
						.forTag(LBSBundle.TAG);
				if (bundles != null) {
					long lastTemp = Global.INVALIDATE_ID;
					for (Entry<Long, Persisable> entry : bundles.entrySet()) {
						LBSBundle lbs = (LBSBundle) entry.getValue();
						ArrayList<Coordinate> coordinateSet = lbs
								.getCoordinateSet();
						Scenario scenario = (Scenario) EntityPool.instance()
								.forId(lbs.getParentId(), lbs.getParentTag());

						if (coordinateSet == null
								|| lbs.getStatus() == LBSBundle.STATE_DISABLE
								|| scenario == null) {
							if (scenario == null) {
								lbs.deleteInDB();
							}
							continue;
						}
						coordinateSet.trimToSize();
						Float[] x = new Float[coordinateSet.size()];
						Float[] y = new Float[coordinateSet.size()];
						int i = 0;
						for (Coordinate coor : coordinateSet) {
							x[i] = (float) coor.getFixLongtitude();
							y[i] = (float) coor.getFixLatitude();
							i++;
						}
						Polygon polygon = new Polygon(x, y, i); // 循环结束时I已经+1
						double lat = loc.getLatitude();
						double lng = loc.getLongitude();
						if (polygon.contains((int) (lng * 1E6),
								(int) (lat * 1E6))) {
							if (showingDebugToast)
								Toast.makeText(getBaseContext(),
										"In LBSBundle Id: " + lbs.getId(),
										Toast.LENGTH_SHORT).show();
							if(lastLBSId != lbs.getId()){
								lastTemp = lbs.getId();
								showNotification(lbs, R.string.enter_location);
							}
						} else {
							boolean bIn = false;
							if(loc.getAccuracy() > minAccuracyMeters 
									&& loc.getAccuracy() < maxAccuracyMeters){
								int r = (int) (loc.getAccuracy()/30/3600*1E6);
								int x1 = (int) (lng*1E6);
								int y1 = (int) (lat*1E6);
								if(polygon.contains(x1,y1,r)){
									if (showingDebugToast)
										Toast.makeText(getBaseContext(),
												"In LBSBundle Id: " + lbs.getId(),
												Toast.LENGTH_SHORT).show();
									if(lastLBSId != lbs.getId()){
										lastTemp = lbs.getId();
										showNotification(lbs, R.string.enter_location);
									}
									bIn = true;
								}
							}
							if(!bIn){
								LBSBundle lastLBS = (LBSBundle) EntityPool
										.instance().forId(lastLBSId, LBSBundle.TAG);
								if (lastLBS != null ) {
									showNotification(lastLBS,
											R.string.leave_location);
									lastLBSId = Global.INVALIDATE_ID;
								}else{}
							}
						}
					}
					lastLBSId = lastTemp;
				}
				if (showingDebugToast)
					toastInfo(loc);

			}
		}

		public void onProviderDisabled(String provider) {
			if (showingDebugToast)
				Toast.makeText(getBaseContext(),
						"onProviderDisabled: " + provider, Toast.LENGTH_SHORT)
						.show();

		}

		public void onProviderEnabled(String provider) {
			if (showingDebugToast)
				Toast.makeText(getBaseContext(),
						"onProviderEnabled: " + provider, Toast.LENGTH_SHORT)
						.show();

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			String showStatus = null;
			if (status == LocationProvider.AVAILABLE)
				showStatus = "Available";
			if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
				showStatus = "Temporarily Unavailable";
			if (status == LocationProvider.OUT_OF_SERVICE)
				showStatus = "Out of Service";
			if (status != lastStatus && showingDebugToast) {
				Toast.makeText(getBaseContext(), "new status: " + showStatus,
						Toast.LENGTH_SHORT).show();
			}
			lastStatus = status;
		}

	}

	// Below is the service framework methods

	private NotificationManager mNM;

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		startService();

		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		shutdownService();

		// Cancel the persistent notification.
		mNM.cancel(R.string.local_service_started);

		// Tell the user we stopped.
		Toast.makeText(this,R.string.local_service_stopped,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.local_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.compass25,
				text, System.currentTimeMillis());
		notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent intent = new Intent(this, TaskOverViewActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.service_name),
				text, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.local_service_started, notification);
	}

	private void showNotification(LBSBundle lbs,int status){
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		Scenario scenario = (Scenario) EntityPool.instance(
				).forId(lbs.getParentId(), lbs.getParentTag());
		
		
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.compass25,
				scenario.getName(), System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent intent = new Intent(this, TaskOverViewActivity.class);
		intent.setAction(TaskOverViewActivity.TASK_OVERVIEWBY_SCENARIO);
		intent.putExtra(TaskOverViewActivity.IN_SCENAIRO_ID, scenario.getId());
		//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		CharSequence text = getText(status);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, scenario.getName(),
				text, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(	status, notification);
	}
	
	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public static void setMinTimeMillis(long _minTimeMillis) {
		minTimeMillis = _minTimeMillis;
	}

	public static long getMinTimeMillis() {
		return minTimeMillis;
	}

	public static void setMinDistanceMeters(long _minDistanceMeters) {
		minDistanceMeters = _minDistanceMeters;
	}

	public static long getMinDistanceMeters() {
		return minDistanceMeters;
	}

	public static float getMinAccuracyMeters() {
		return minAccuracyMeters;
	}

	public static void setMinAccuracyMeters(float minAccuracyMeters) {
		LocationService.minAccuracyMeters = minAccuracyMeters;
	}

	public static void setShowingDebugToast(boolean showingDebugToast) {
		LocationService.showingDebugToast = showingDebugToast;
	}

	public static boolean isShowingDebugToast() {
		return showingDebugToast;
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		LocationService getService() {
			return LocationService.this;
		}
	}
	
	private void toastInfo(Location loc){
		Toast.makeText(
			getBaseContext(),
			"Location at: \nLat: "
			+ sevenSigDigits.format(loc.getLatitude())
			+ " \nLon: "
			+ sevenSigDigits.format(loc.getLongitude())
			+ " \nAlt: "
			+ (loc.hasAltitude() ? loc.getAltitude() + "m" : "?")
			+ " \nAcc: "
			+ (loc.hasAccuracy() ? loc.getAccuracy() + "m" : "?")
			+ " \nSpeed"
			+ (loc.hasSpeed()?loc.getSpeed() + "m/s":"?")
			+ " \nBearing"
			+ (loc.hasBearing()?loc.getBearing() + "degrees":"?"),
			Toast.LENGTH_SHORT).show();
	}
}
