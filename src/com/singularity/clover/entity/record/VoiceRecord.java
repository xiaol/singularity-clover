package com.singularity.clover.entity.record;

import android.content.Context;
import android.graphics.Bitmap;

import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;

public class VoiceRecord extends Record {
	public static String TAG = "voice_record";
	
	static{
		VoiceRecord prototype = new VoiceRecord("Prototype");
		EntityPool.instance().register(TAG,prototype);
		RecordFactory.register(TAG, prototype);
	}
	
	public VoiceRecord(String string) {
		super("Prototype");
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Persisable create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTAG() {
		return TAG;
	}

	@Override
	public Bitmap convertoBitmap(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recycle() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Record recentRecord() {
		// TODO Auto-generated method stub
		return null;
	}

}
