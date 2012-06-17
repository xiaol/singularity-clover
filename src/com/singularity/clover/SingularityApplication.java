package com.singularity.clover;

import android.app.Application;
import android.os.Handler;

import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.notification.NotifierBinder;

public class SingularityApplication extends Application {
	private static SingularityApplication self = null;
	private NotifierBinder mNoitifierBinder;
	private static boolean bLBSService = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		DBAdapter.initialize(this);
		
		mNoitifierBinder = new NotifierBinder();
		NotifierBinder.initialize(this);
		self = this;
		mNoitifierBinder.bindNotifer();
	}
	
	public static SingularityApplication instance(){
		return self;
	}
	
	public  NotifierBinder getNotifierBinder(){
		return mNoitifierBinder;
	}

	public boolean isLBSOn(){
		return bLBSService;
	}
	
	public void setLBSService(boolean bLBS){
		bLBSService = bLBS;
	}
	
}
