package com.singularity.clover.notification;

import java.io.IOException;

import com.singularity.clover.Global;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.activity.entity.TaskOverViewActivity;
import com.singularity.clover.activity.entity.TaskViewActivity;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;


public class NotifierService extends Service {
	
	public static final String IN_NOTIFIY_TASK_ID = 
		"com.singularity.notification.NotifierService.task.id";
	private static Vibrator mVibrator = null;
	private static AudioManager mAudioManager = null;
	private static MediaPlayer mMediaPlayer;
	private static final long[] vibratePattern = {20000, 10000};
	public static final String START_NOTIFY = "com.singularity.notifier.start";
	public static final String STOP_NOTIFY = "com.singularity.notifier.stop";
	public static final String STOP_NOTIFY_AND_SWITCH = "com.singularity.notifier.switch";

	@Override
	public void onCreate() {
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mMediaPlayer = new MediaPlayer();
		Uri alert = RingtoneManager.getValidRingtoneUri(getApplicationContext());
		try {
			if(alert == null){
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
			mMediaPlayer.setDataSource(this, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction() == null) {
			return START_NOT_STICKY;
		}
		if (intent.getAction().equals(START_NOTIFY)) {
			mVibrator.vibrate(vibratePattern, 0);
			startRing();
		} else if (intent.getAction().equals(STOP_NOTIFY)) {
			mVibrator.cancel();
			stopRing();
		} else if (intent.getAction().equals(STOP_NOTIFY_AND_SWITCH)) {
			mVibrator.cancel();
			stopRing();
			Intent switchIntent = new Intent(this, TaskViewActivity.class);
			switchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			switchIntent.setAction(TaskViewActivity.TASK_SHOW_NOTIFICATION);
			long showId = intent.getLongExtra(
					IN_NOTIFIY_TASK_ID, Global.INVALIDATE_ID);
			switchIntent.putExtra(TaskViewActivity.IN_TASK_ID, showId);
			startActivity(switchIntent);
		}
		return START_NOT_STICKY;
	}
	
	private void startRing() {
		if (mAudioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
			mMediaPlayer.start();
		}else{
			mMediaPlayer.start();
		}
	}
	
	private void stopRing() {
		mMediaPlayer.pause();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
