package com.singularity.clover.util;

import java.io.File;
import java.io.IOException;

import com.singularity.clover.Global;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class MediaUtil {
	MediaRecorder _recorder;
	MediaPlayer _player;
	
	String path = null;
	private static final String TAG = "media_util";
	private Context _context;

	public MediaUtil(Context context,String path){
		_context = context;
		this.path = normalizePath(path);
	}
	
	private class ErrorListener implements OnErrorListener{

		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			// TODO Auto-generated method stub
		}
		
	}
	private String normalizePath(String path) {
		/*if (!path.startsWith("/"))
			path = "/" + path;*/
		if (!path.contains("."))
			path += ".3gp";
	    return Global.APP_FILE_PATH + path;
	}
	
	public void startRecording() throws IOException {
		String state = android.os.Environment.getExternalStorageState();
	    if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  
	        throw new IOException("SD Card is not mounted.  It is " + state + ".");
		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) 
			throw new IOException("Path to file could not be created.");	
		_recorder = new MediaRecorder();
		_recorder.setOnErrorListener(new ErrorListener());
		_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		_recorder.setOutputFile(path);
		_recorder.prepare();
		_recorder.start();
	}

	public void stopRecording() {
		_recorder.stop();
		_recorder.release();
		processaudiofile();
	}

	protected void processaudiofile() {
		ContentValues values = new ContentValues(3);
		long current = System.currentTimeMillis();
		values.put(MediaStore.Audio.Media.TITLE, "audio" + new File(path).getName());
		values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
		values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
		values.put(MediaStore.Audio.Media.DATA, path);
		ContentResolver contentResolver = _context.getContentResolver();

		Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Uri newUri = contentResolver.insert(base, values);

		_context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
	}

	public void onActivityPause() {
		if (_recorder != null) {
			_recorder.release();
			_recorder = null;
		}
		if (_player != null) {
			_player.release();
			_player = null;
		}
	}

	public void startPlaying() {
		_player = new MediaPlayer();
		try {
			_player.setDataSource(path);
			_player.prepare();
			_player.start();
		} catch (IOException e) {
			//Log.e(TAG, "prepare() failed");
		}
	}

	public void stopPlaying() {
		_player.release();
		_player = null;
	}

}
