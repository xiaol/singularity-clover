package com.singularity.clover.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.SingularityWidgetProvider;
import com.singularity.clover.activity.entity.TaskViewActivity;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.objective.DurableObj;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.entity.wrapper.Scenario;

public class NotifierReceiver extends BroadcastReceiver {

	public static final String IN_NOTIFIY_TASK_ID = 
		"com.singularity.notification.NotifierReceiver.task.id";
	private static final long VIBRATION_DURATION = 60 * 1000;
	private static int notificationId = 1;
	private static final int CANCEL_VIBRATION = 2;
	private static final long[] vibratePattern = new long[60];

	static {
		for (int i = 0; i < 60; i++) {
			vibratePattern[i] = 5000;
		}
	}

	private static int nextNotificationId() {
		return notificationId++;
	}
	
	public class AutoCancelHandler extends Handler {
		private Context mContext;

		public AutoCancelHandler(Context context) {
			mContext = context;
		}

		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent(mContext, NotifierService.class);
			intent.setAction(NotifierService.STOP_NOTIFY);
			mContext.startService(intent);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(NotifierBinder.ACTION_ALARM_RING)) {
			long showId = intent.getLongExtra(IN_NOTIFIY_TASK_ID,
					Global.INVALIDATE_ID);
			Task task = (Task) EntityPool.instance().forId(showId, Task.TAG);
			
			NotificationManager notificationManager =
				(NotificationManager) SingularityApplication.instance().
				 getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

			int icon = R.drawable.ic_notification;
			CharSequence tickerText = context.getText(R.string.notify_title);
			long when = System.currentTimeMillis();

			// build notification
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			CharSequence contentTitle;
			CharSequence contentText;
			if(task != null){
				if(task.name != null){
					if(task.name.equals("")){
						contentTitle = context.getText(R.string.empty_task_title);
					}else{
						contentTitle = task.name;
					}
				}else{
					contentTitle = context.getText(R.string.empty_task_title);
				}
				Scenario scenario = (Scenario) EntityPool.instance().forId(
						task.getScenarioId(), Scenario.TAG);
				if(scenario != null){
					contentText = scenario.getName();
				}else{
					contentText = "";
				}
			}else{
				contentTitle = context.getText(R.string.empty_task_title);
				contentText = context.getText(R.string.notify_description);
			}
			
			alert(contentText, context, contentTitle, task.getId());
			Intent notificationIntent = new Intent(context,	NotifierService.class);
			notificationIntent.setAction(NotifierService.STOP_NOTIFY_AND_SWITCH);
			notificationIntent.putExtra(NotifierService.IN_NOTIFIY_TASK_ID, showId);
			PendingIntent contentIntent = PendingIntent.getService(context, 0,
					notificationIntent, PendingIntent.FLAG_ONE_SHOT);
			
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			notificationManager.notify(nextNotificationId(), notification);

			// start sound and vibration
			Intent serviceIntent = new Intent(context, NotifierService.class);
			serviceIntent.setAction(NotifierService.START_NOTIFY);
			context.startService(serviceIntent);
			
			SingularityApplication.instance().getNotifierBinder().bindNotifer();
			
			// stop sound and vibration after 1 min
			AutoCancelHandler handler = new AutoCancelHandler(context);
			handler.sendEmptyMessageDelayed(CANCEL_VIBRATION,
					VIBRATION_DURATION);
		} else if (action.equals(NotifierBinder.ACTION_REFRESH_ALL)) {
			//sychronize date
		}else if(action.equals(DurableObj.ACTION_ALARM_GOAL)){
			NotificationManager notificationManager =
				(NotificationManager) SingularityApplication.instance().
				 getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
			
			long showId = intent.getLongExtra(IN_NOTIFIY_TASK_ID,
					Global.INVALIDATE_ID);
			Task task = (Task) EntityPool.instance().forId(showId, Task.TAG);
			
			final int notifyId = R.drawable.ic_notification_goal;
	
			// Set the icon, scrolling text and timestamp
			final Notification notification = new Notification(R.drawable.ic_notification_goal,
					task.name, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			// The PendingIntent to launch our activity if the user selects this
			// notification
			Intent goalIntent = new Intent(context, TaskViewActivity.class);
			goalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			goalIntent.setAction(TaskViewActivity.TASK_SHOW_NOTIFICATION);;
			goalIntent.putExtra(TaskViewActivity.IN_TASK_ID, task.getId());
			//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					goalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	
			notification.setLatestEventInfo(context, task.name,
					context.getText(R.string.goal_alarm), contentIntent);
			notificationManager.notify(notifyId, notification);
			
			alert(context.getText(R.string.goal_alarm), context, task.name, task.getId());
		}else if(action.equals(SingularityWidgetProvider.ACTION_WIDGET_ALARM)){
		
			NotificationManager notificationManager =
				(NotificationManager) SingularityApplication.instance().
				 getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

			int icon = R.drawable.ic_notification;
			CharSequence tickerText = context.getText(R.string.notify_title);
			long when = System.currentTimeMillis();

			// build notification
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			CharSequence contentTitle = context.getText(R.string.notify_title);
			CharSequence contentText = context.getText(R.string.notify_description);

			
			Intent notificationIntent = new Intent(context,	NotifierService.class);
			notificationIntent.setAction(NotifierService.STOP_NOTIFY);
			PendingIntent contentIntent = PendingIntent.getService(context, 0,
					notificationIntent, PendingIntent.FLAG_ONE_SHOT);
			
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			notificationManager.notify(nextNotificationId(), notification);

			// start sound and vibration
			Intent serviceIntent = new Intent(context, NotifierService.class);
			serviceIntent.setAction(NotifierService.START_NOTIFY);
			context.startService(serviceIntent);
			
			SingularityApplication.instance().getNotifierBinder().bindNotifer();
			alert(contentText, context, contentTitle, Global.INVALIDATE_ID);
			Intent widgetIntent = new Intent();
			widgetIntent.setAction(SingularityWidgetProvider.ACTION_WIDGET_TIMER_RESET);
			context.sendBroadcast(widgetIntent);	
			
			// stop sound and vibration after 1 min
			AutoCancelHandler handler = new AutoCancelHandler(context);
			handler.sendEmptyMessageDelayed(CANCEL_VIBRATION,
					VIBRATION_DURATION);
		}
	}

	
	protected void alert(CharSequence contentText,final 
			Context context,CharSequence contentTitle,final long taskId){
		/*Intent intent = new Intent(context, AlertActivity.class);
		intent.putExtra(AlertActivity.IN_TASK_ID,taskId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);*/
		
		View layout = View.inflate(context, R.layout.alert_activity_layout, null);
		Button titleBtn = (Button) layout.findViewById(R.id.alert_title);
		titleBtn.setText(contentTitle);
		titleBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(context, NotifierService.class);
				serviceIntent.setAction(NotifierService.STOP_NOTIFY);
				context.startService(serviceIntent);
				WindowManager wm = (WindowManager) context.
						getSystemService(Context.WINDOW_SERVICE);
				wm.removeView((View) v.getParent());
				if(taskId == Global.INVALIDATE_ID){
					return;}
				
				Intent intent = new Intent(context, TaskViewActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(TaskViewActivity.TASK_SHOW_NOTIFICATION);;
				intent.putExtra(TaskViewActivity.IN_TASK_ID, taskId);
				context.startActivity(intent);
				
				return;
				
			}
		});
		
		layout.findViewById(R.id.alert_button_ok).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(context, NotifierService.class);
				serviceIntent.setAction(NotifierService.STOP_NOTIFY);
				context.startService(serviceIntent);
				WindowManager wm = (WindowManager) context.
						getSystemService(Context.WINDOW_SERVICE);
				wm.removeView((View) v.getParent());
			}
		});
		
		layout.findViewById(R.id.alert_button_delay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(context, NotifierService.class);
				serviceIntent.setAction(NotifierService.STOP_NOTIFY);
				context.startService(serviceIntent);
				
				WindowManager wm = (WindowManager) context.
						getSystemService(Context.WINDOW_SERVICE);
				wm.removeView((View) v.getParent());
				
				Intent intent;
				if(taskId != Global.INVALIDATE_ID){
					intent = new Intent(context, NotifierReceiver.class);
					intent.setAction(NotifierBinder.ACTION_ALARM_RING);
					intent.putExtra(NotifierReceiver.IN_NOTIFIY_TASK_ID, 
							taskId);
				}else{
					intent = new Intent(context, NotifierReceiver.class);
					intent.setAction(SingularityWidgetProvider.ACTION_WIDGET_ALARM);
				}
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
						PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager alarmManager = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP, 
						System.currentTimeMillis() + 60*1000*10, pendingIntent);
				return;
			}
		});
		
		/*ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;*/
		
		WindowManager.LayoutParams lp;
        int pixelFormat;

        pixelFormat = PixelFormat.TRANSLUCENT;

        lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/,
                pixelFormat);
//        lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.CENTER;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        wm.addView(layout, lp);
	}
	
	
}
