package com.singularity.clover.entity.record;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.util.BitmapUtil;

public class PictureRecord extends Record {
	public static String TAG = "picture_record";
	private Bitmap _bitmap = null;
	
	static{
		PictureRecord prototype = new PictureRecord("Prototype");
		EntityPool.instance().register(TAG,prototype);
		RecordFactory.register(TAG, prototype);
	}
	
	private PictureRecord(String string) {
		super("Prototype");
	}
	
	public PictureRecord(String name,long date,String uri){
		super();
		this.name = name;
		this.createDate = date;
		this.content = uri;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public void delete() {
		super.delete();
		Uri uri = Uri.parse(content);
		String path = uri.getPath();
		File file = new File(path);
		file.delete();
	}


	@Override
	public Persisable create() {
		return new PictureRecord("Prototype");
	}

	@Override
	public String getTAG() {
		return TAG;
	}

	@Override
	public Bitmap convertoBitmap(Context context) {
		if(content == null)
			return null;
		if(_bitmap != null)
			return _bitmap;
		try {
			_bitmap = BitmapUtil.getScaledBitmap(context, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _bitmap;
	}
	
	public void setBitmap(Bitmap bitmap){
		//_bitmap.recycle();
		_bitmap = bitmap;
	}

	@Override
	public void recycle() {
		if(_bitmap != null){
			//_bitmap.recycle();
			_bitmap = null;
		}
		
	}

	@Override
	public Record recentRecord() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
