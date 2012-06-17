package com.singularity.clover.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;

import com.singularity.clover.Global;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.record.PictureRecord;
import com.singularity.clover.entity.record.Record;

public class BitmapToFile extends AsyncTask<Bitmap,Void,Boolean> {
	public static final int MSG_SAVE_DONE = 0;
	public static final int MSG_SAVE_FAILED = 1;

	private ArrayList<Handler> handlers = new ArrayList<Handler>();
	private String[] uris;
	private String prefixFileName;
	private long base;

	public BitmapToFile(Handler handler,String prefix,long base) {
		handlers.add(handler);
		this.prefixFileName = prefix;
		this.base = base;
	}

	public void addHandler(Handler handler){
		handlers.add(handler);
	}
	@Override
    protected void onPostExecute(Boolean bool) {
      super.onPostExecute(bool);
      for(Handler entry:handlers){
    	if(entry == null)
    		continue;
		if (bool) {
			Message msg = entry.obtainMessage();
			Time now = new Time();
			now.setToNow();
			long date = now.toMillis(false);
			PictureRecord pic;
			pic = (PictureRecord) EntityPool.instance().forId(base, Record.TAG);
			if (pic == null) {
				pic = new PictureRecord("name", date, uris[0]);
				pic.store();}
				
			Bundle data = new Bundle();
			data.putLong("id", pic.getId());
			msg.setData(data);
			msg.what = MSG_SAVE_DONE;
			entry.sendMessage(msg);
		} else {
			entry.sendEmptyMessage(MSG_SAVE_FAILED);}
      }
    }

	@Override
	protected Boolean doInBackground(Bitmap... params) {
		try {
			String state = Environment.getExternalStorageState();		
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				return false;
			} else {
				return false;
			}
			File file = new File(Global.APP_FILE_PATH);
			if (!file.exists()) {
				if (!file.mkdirs()) {
				}
			}
			int count = params.length;
			ArrayList<String> strs = new ArrayList<String>();
			for (int i = 0; i < count; i++) {
				long suffix = i + base;
				File file2 = new File(file,prefixFileName + suffix +".png");
				final FileOutputStream out = new FileOutputStream(file2);
				params[i].compress(Bitmap.CompressFormat.PNG, 100, out);
				Uri uri = Uri.fromFile(file2);
				strs.add(uri.toString());
				out.flush();out.close();
				params[i].recycle();
				params[i] = null;
			}
			uris = (String[]) strs.toArray(new String[strs.size()]);
			params = null;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}