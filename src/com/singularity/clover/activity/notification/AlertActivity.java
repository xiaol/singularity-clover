package com.singularity.clover.activity.notification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.SingularityWidgetProvider;
import com.singularity.clover.activity.entity.TaskViewActivity;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.notification.NotifierBinder;
import com.singularity.clover.notification.NotifierReceiver;
import com.singularity.clover.notification.NotifierService;

public class AlertActivity extends Activity {
	
	public static final String IN_TASK_ID ="com.singularity.AlertActivity.TASK.ID";
	private long mTaskId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_activity_layout);
		
		mTaskId = getIntent().getExtras().getLong(IN_TASK_ID, Global.INVALIDATE_ID);
		
		TextView title = (TextView) findViewById(R.id.alert_title);
		Button okBtn = (Button) findViewById(R.id.alert_button_ok);
		Button delayBtn = (Button) findViewById(R.id.alert_button_delay);
		
		if(mTaskId != Global.INVALIDATE_ID){
			Task task = (Task) EntityPool.instance().forId(mTaskId,Task.TAG);
			title.setText(task.name);
		}else{}
		
		okBtn.setOnClickListener(new OnClickListener() {
				
			@Override
			public void onClick(View v) {
				Context context = AlertActivity.this;
				Intent serviceIntent = new Intent(context, NotifierService.class);
				serviceIntent.setAction(NotifierService.STOP_NOTIFY);
				context.startService(serviceIntent);
				if(mTaskId == Global.INVALIDATE_ID){
					return;}
				
				Intent intent = new Intent(context, TaskViewActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(TaskViewActivity.TASK_SHOW_NOTIFICATION);;
				intent.putExtra(TaskViewActivity.IN_TASK_ID, mTaskId);
				context.startActivity(intent);
				
				return;
				
			}
		});
		
		delayBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Context context = AlertActivity.this;
				Intent serviceIntent = new Intent(AlertActivity.this, NotifierService.class);
				serviceIntent.setAction(NotifierService.STOP_NOTIFY);
				AlertActivity.this.startService(serviceIntent);
				Intent intent;
				if(mTaskId == Global.INVALIDATE_ID){
					intent = new Intent(context, NotifierReceiver.class);
					intent.setAction(NotifierBinder.ACTION_ALARM_RING);
					intent.putExtra(NotifierReceiver.IN_NOTIFIY_TASK_ID, 
							mTaskId);
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
	}
	
}
