package com.singularity.clover;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.singularity.clover.activity.entity.TaskOverViewActivity;
import com.singularity.clover.activity.entity.TaskViewActivity;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.objective.AbstractObjective;
import com.singularity.clover.entity.objective.CheckableObj;
import com.singularity.clover.entity.objective.DurableObj;
import com.singularity.clover.entity.objective.NumericObj;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.entity.wrapper.Scenario;
import com.singularity.clover.notification.NotifierReceiver;


/**
 * @author  Administrator
 */
public class SingularityWidgetProvider extends AppWidgetProvider {
	public static final String ACTION_WIDGET_PREV = "com.singularity.PREV";
	public static final String ACTION_WIDGET_NEXT = "com.singularity.NEXT";
	public static final String ACTION_WIDGET_CTRL1 = "com.singularity.CTRL1";
	public static final String ACTION_WIDGET_CTRL2 = "com.singularity.CTRL2";
	public static final String ACTION_WIDGET_CTRL3 = "com.singularity.CTRL3";
	public static final String ACTION_WIDGET_BUTTON_ENABLE = "com.singularity.BUTTON_ENABLE";
	public static final String ACTION_WIDGET_BUTTON_DISABLE = "com.singularity.BUTTON_DISABLE";
	public static final String ACTION_WIDGET_MANUALUPDATE ="com.singularity.MANUALUPDATE";
	public static final String ACTION_WIDGET_INVALID ="com.singularity.INVALID";
	public static final String ACTION_WIDGET_TIMER = "com.singularity.TIMER";
	public static final String ACTION_WIDGET_ALARM ="com.singularity.alarm.WIDGET";
	public static final String ACTION_WIDGET_TIMER_RESET ="com.singularity.TIMER.RESET";
	public static final String URI_SCHEME = "singularitywidget";
	public static final String TASK_POS="com.singularity.TASK_POS";
	public static final String TASK_ID="com.singularity.TASK_ID";
	private static int taskPos = 0;
	private static int mTimerStatus = 0;
	private static int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static int mAppWidgetIds[];
	private static PendingIntent mPending;
	/**
	 */
	private static ArrayList<Long> mCurrentTaskIds;// = Task.currentTasks();
	private static boolean bInit = false;
	private int[] mOBJPanelArrays = {R.id.widget_obj_ctrl1,R.id.widget_obj_content1,
									 R.id.widget_obj_ctrl2,R.id.widget_obj_content2,
									 R.id.widget_obj_ctrl3,R.id.widget_obj_content3};
	
	private String[] mCTRLActions = {ACTION_WIDGET_CTRL1,ACTION_WIDGET_CTRL2,ACTION_WIDGET_CTRL3};
	private int[] mTimerResIds = {R.drawable.widget_timer_off,R.drawable.widget_timer_quarter,
								  R.drawable.widget_timer_half,R.drawable.widget_timer_three_quarters,
								  R.drawable.widget_timer_one};
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		//Log.v("onUpdate","begin");
		final int N = appWidgetIds.length;
		mAppWidgetIds = appWidgetIds;
		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		mCurrentTaskIds = Task.currentTasks();
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			
			views.setOnClickPendingIntent(R.id.widget_prev,
					makePendingAction(context, appWidgetId,ACTION_WIDGET_PREV));
			views.setOnClickPendingIntent(R.id.widget_next,
					makePendingAction(context, appWidgetId, ACTION_WIDGET_NEXT));
			views.setOnClickPendingIntent(R.id.widget_root,
					makePendingAction(context, appWidgetId, ACTION_WIDGET_BUTTON_ENABLE));
			views.setOnClickPendingIntent(R.id.widget_timer,
					makePendingAction(context, appWidgetId, ACTION_WIDGET_TIMER));
			
			
			if(mCurrentTaskIds.size() > 0 && taskPos < mCurrentTaskIds.size()){
				Task task = getTaskAtPos();
				updateTextView(context,task, views);
				updateOBJPanel(views, context,task, appWidgetId);
				updatePrevAndNextBtns(views, context, appWidgetId);
				updateHomeBtn(context,task.getId(),views);
			}else{
				homeBtnToLaunch(context, views);
				updateTextView(context,null, views);
			}

			// Tell the AppWidgetManager to perform an update on the current App
			// Widget
			mWidgetId = appWidgetId;
			if(!bInit){
				//setUpdatePeriodic(context, appWidgetId, 10*1000);
				bInit = true;
			}
			appWidgetManager.updateAppWidget(appWidgetId, views);
			
		}
	}
	
	private void onManualUpdate(Context context){
		if( mAppWidgetIds != null){
			onUpdate(context,
					AppWidgetManager.getInstance(context), mAppWidgetIds);
		}
	}

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (action.equals(ACTION_WIDGET_PREV)) {
			taskPos--;
			if(taskPos < 0){
				taskPos = 0;
			}
			Task task = getTaskAtPos();
			if(task != null){
				RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
				updateTextView(context,task, views);
				
				int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
				if(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
					updateOBJPanel(views, context,task, appWidgetId);
					updatePrevAndNextBtns(views, context, appWidgetId);
					updateHomeBtn(context,task.getId(), views);
					AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
				}
			}
			
		}else if(action.equals(ACTION_WIDGET_NEXT)){
			taskPos++;
			int count = mCurrentTaskIds.size();
			if(taskPos >= count && count > 0 ){
				taskPos = count - 1;
			}
			Task task = getTaskAtPos();
			if( task != null ){
				RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
				updateTextView(context,task, views);
				
				int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
				if(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
					updateOBJPanel(views, context, task,appWidgetId);
					updatePrevAndNextBtns(views, context, appWidgetId);
					updateHomeBtn(context,task.getId(), views);
					AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
				}
			}
		}else if(action.equals(mCTRLActions[0]) || 
				action.equals(mCTRLActions[1]) || action.equals(mCTRLActions[2])){
			int index = 0;
			if(action.equals(mCTRLActions[0])){
				index = 0;
			}else if(action.equals(mCTRLActions[1])){
				index = 1;
			}else if(action.equals(mCTRLActions[2])){
				index = 2;
			}
			
			Task task = getTaskAtPos();
			if(task != null){
				TreeMap<Integer, String> OBJs = task.sortOBJ(false);
				for(int i = 0;i <index;i++){
					OBJs.remove(OBJs.firstKey());}
				
				String[] str = OBJs.get(OBJs.firstKey()).split(",");
				AbstractObjective obj = (AbstractObjective) EntityPool
					.instance().forId(Long.parseLong(str[1]), str[0]);
				if(obj.getTAG().equals(CheckableObj.TAG)){
					CheckableObj check = (CheckableObj) obj;
					check.check();
				}else if(obj.getTAG().equals(NumericObj.TAG)){
					NumericObj numeric = (NumericObj) obj;
					numeric.setValue(numeric.getValue()+1);
				}else if(obj.getTAG().equals(DurableObj.TAG)){
					DurableObj durable = (DurableObj) obj;
					durable.setRunning(!durable.isRunning());
					if(durable.isRunning()){
						long delay = durable.howLongExpired();
						if(delay > 0){
							durable.setAlarm(SingularityApplication.instance(),
									System.currentTimeMillis()+delay);
						}
					}else{
						durable.cancelAlarm();
					}
				}
				obj.store();
				RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.widget_layout);
				int appWidgetId = intent.getIntExtra(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {	
					updateOBJPanel(views, context, task,appWidgetId);
					AppWidgetManager.getInstance(context).updateAppWidget(
							appWidgetId, views);
				}
			}
		}else if(action.equals(ACTION_WIDGET_BUTTON_ENABLE)){
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			int appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			Task task = getTaskAtPos();
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				if(task != null){
					views.setViewVisibility(R.id.widget_next, View.VISIBLE);
					views.setViewVisibility(R.id.widget_prev, View.VISIBLE);
					views.setViewVisibility(R.id.widget_home, View.VISIBLE);
					updateTextView(context,task,views);
					updateOBJPanel(views, context, task,appWidgetId);
					updateHomeBtn(context,task.getId(), views);
				}else{
					views.setViewVisibility(R.id.widget_next, View.INVISIBLE);
					views.setViewVisibility(R.id.widget_prev, View.INVISIBLE);
					views.setViewVisibility(R.id.widget_home, View.VISIBLE);
					homeBtnToLaunch(context, views);
					updateTextView(context,task,views);
				}
				updatePrevAndNextBtns(views, context, appWidgetId);
				
				AppWidgetManager.getInstance(context).updateAppWidget(
						appWidgetId, views);
				makeUpdateAction(context, appWidgetId, ACTION_WIDGET_BUTTON_DISABLE, 3000);
				mWidgetId = appWidgetId;
			}
		}else if(action.equals(ACTION_WIDGET_BUTTON_DISABLE)){
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				views.setViewVisibility(R.id.widget_next, View.INVISIBLE);
				views.setViewVisibility(R.id.widget_prev, View.INVISIBLE);
				views.setViewVisibility(R.id.widget_home, View.INVISIBLE);
				
				AppWidgetManager.getInstance(context).updateAppWidget(
						/*Integer.valueOf(intent.getData().getFragment())*/appWidgetIds[0], views);
			
		}else if(action.equals(ACTION_WIDGET_MANUALUPDATE)){
			onManualUpdate(context);
		}else if(action.equals(ACTION_WIDGET_TIMER)){
			mTimerStatus++;
			mTimerStatus %= 5;
			if(mPending != null){
				cancelAlarm();}
			if(mTimerStatus != 0){
				setAlarm(context, System.currentTimeMillis()+30*mTimerStatus*60*1000);
				int hour = 30*mTimerStatus/60;
				int minuites = 30*mTimerStatus%60;
				String content = context.getText(R.string.alarm_toast_prefix).toString();
				if(hour == 0){
					content += minuites + context.getText(R.string.mininute_short).toString();
				}else{
					if(hour == 1){
						content += "1" + context.getText(R.string.hour_pular).toString();
					}else{
						content += hour + context.getText(R.string.hour_short).toString();
					}
					if(minuites == 0){
					}else{
						content += minuites + context.getText(R.string.mininute_short).toString();
					}
				}
				content += context.getText(R.string.alarm_toast_suffix).toString();
				Toast.makeText(context,content , Toast.LENGTH_SHORT).show();
			}else{
				CharSequence content = context.getText(R.string.alarm_toast_cancel);
				Toast.makeText(context,content , Toast.LENGTH_SHORT).show();
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			views.setImageViewResource(R.id.widget_timer,mTimerResIds[mTimerStatus]);
			int appWidgetId = intent.getIntExtra(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Task task = getTaskAtPos();
				if(task != null)
					updateOBJPanel(views, context, task,appWidgetId);
				AppWidgetManager.getInstance(context).updateAppWidget(
						appWidgetId, views);
				mWidgetId = appWidgetId;
			}
		}else if(action.equals(ACTION_WIDGET_TIMER_RESET)){
			mTimerStatus = 0;
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			views.setImageViewResource(R.id.widget_timer,mTimerResIds[mTimerStatus]);
			int appWidgetId = mWidgetId;
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Task task = getTaskAtPos();
				if(task != null)
					updateOBJPanel(views, context, task,appWidgetId);
				AppWidgetManager.getInstance(context).updateAppWidget(
						appWidgetId, views);
			}
		}
		if(!action.equals(ACTION_WIDGET_BUTTON_DISABLE) 
				&& !(action.equals(ACTION_WIDGET_BUTTON_ENABLE) || action.equals(ACTION_WIDGET_TIMER))
				&& !(action.equals(mCTRLActions[0]) || action.equals(mCTRLActions[1]) || action.equals(mCTRLActions[2]))
				&& ( action.equals(ACTION_WIDGET_PREV)
				|| action.equals(ACTION_WIDGET_NEXT) ) ){
			int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			Intent active = new Intent();
			active.setAction(ACTION_WIDGET_BUTTON_ENABLE);
			active.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			context.sendBroadcast(active);
		}
		super.onReceive(context, intent);
	}
	
	public void onEnabled(Context context){
		//Log.v("onEnable","begin");
		super.onEnabled(context);
		onManualUpdate(context);
	}
	
	private PendingIntent makePendingAction(Context context,int appWidgetId,String action){
		Intent active = new Intent();
		active.setAction(action);
		active.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		return(PendingIntent.getBroadcast(context, 0, active, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void makeUpdateAction(Context context,int appWidgetId, String action,long interval){	
		Intent active = new Intent();	
		active.setAction(action);
		active.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{ appWidgetId});
		
//		Uri data = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#"),
//        String.valueOf(appWidgetId));
//		active.setData(data);
		
		PendingIntent result = PendingIntent.getBroadcast(context, 0,
				active, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		long time = SystemClock.elapsedRealtime()+interval;
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, time, result);
	}
	
	private void setUpdatePeriodic(Context context,int appWidgetId,long interval){
		Intent widgetUpdate = new Intent();
        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });

        //widgetUpdate.setData(Uri.withAppendedPath(
        	//Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)));
        PendingIntent newPending = PendingIntent.getBroadcast(context, 
        		0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (interval >= 0) {
            alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, 
            		SystemClock.elapsedRealtime(), interval, newPending);
        } else {
            alarms.cancel(newPending);
        }
	}
	
   @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            //setUpdatePeriodic(context, appWidgetId, -1);

        }
        bInit = false;
        super.onDeleted(context, appWidgetIds);
    }
   
   private void updateOBJPanel(RemoteViews views,Context context,Task task,int appWidgetId){	
		task.processValidate();
		TreeMap<Integer, String> OBJs = task.sortOBJ(false);
		int count = 0;
		for(Entry<Integer, String> entry:OBJs.entrySet()){
			if(count > 2){
				break;}
			
			String[] str = entry.getValue().split(",");
			AbstractObjective obj = (AbstractObjective) EntityPool
				.instance().forId(Long.parseLong(str[1]), str[0]);
			String content ="";
			if(str[0].equals(CheckableObj.TAG)){
				CheckableObj check = (CheckableObj) obj;
				views.setOnClickPendingIntent(mOBJPanelArrays[count*2],
					makePendingAction(context, appWidgetId, mCTRLActions[count]));
				views.setImageViewResource(mOBJPanelArrays[count*2],
					R.drawable.widget_checkbox);
				content = check.getName();
			}else if(str[0].equals(DurableObj.TAG)){
				DurableObj durable = (DurableObj) obj;
				views.setOnClickPendingIntent(mOBJPanelArrays[count*2],
					makePendingAction(context, appWidgetId, mCTRLActions[count]));
				if(durable.isRunning()){
					views.setImageViewResource(mOBJPanelArrays[count*2],
						R.drawable.widget_pause);
				}else{
					views.setImageViewResource(mOBJPanelArrays[count*2],
						R.drawable.widget_play);
					}
				long minutes = durable.howLongExpired()/(60*1000);
				long hour = minutes / 60;
				if(hour != 0){
					content = Long.toString(hour) + context.getText(R.string.hour_short)+
						minutes%(60)+context.getText(R.string.mininute_short)
						+ context.getText(R.string.remains);
				}else{
					content = Long.toString(minutes%(60))+
						context.getText(R.string.mininute_short)+ 
						context.getText(R.string.remains);
				}
			}else if(str[0].equals(NumericObj.TAG)){
				NumericObj numeric = (NumericObj) obj;
				views.setOnClickPendingIntent(mOBJPanelArrays[count*2],
					makePendingAction(context, appWidgetId, mCTRLActions[count]));
				views.setImageViewResource(mOBJPanelArrays[count*2],
					R.drawable.widget_plus);
				if(numeric.getMax() != 0){
					content = Integer.toString(numeric.getValue()) + "/"+
						Integer.toString(numeric.getMax())+numeric.getUnit();
				}else{
					content = Integer.toString(numeric.getValue()) +numeric.getUnit();
				}
			}
			views.setTextViewText(mOBJPanelArrays[count*2+1], content);
			views.setViewVisibility(mOBJPanelArrays[count*2], View.VISIBLE);
			views.setViewVisibility(mOBJPanelArrays[count*2+1], View.VISIBLE);
			count++;
		}
		count--;
		if(count < 2){
			for(int i = count+1;i <=2;i++){
				views.setViewVisibility(mOBJPanelArrays[i*2], View.INVISIBLE);
				views.setViewVisibility(mOBJPanelArrays[i*2+1], View.INVISIBLE);
			}
		}
	}
   
   private void updatePrevAndNextBtns(RemoteViews views,Context context,int appWidgetId){
	   if(taskPos == 0 && mCurrentTaskIds.size() == 1){
		   views.setViewVisibility(R.id.widget_next, View.INVISIBLE);
		   views.setViewVisibility(R.id.widget_prev, View.INVISIBLE);
	   }else if(taskPos == 0 ){
		   views.setViewVisibility(R.id.widget_prev, View.INVISIBLE);
	   }else if(taskPos == mCurrentTaskIds.size()-1){
		   views.setViewVisibility(R.id.widget_next, View.INVISIBLE);
	   }else{
		   views.setViewVisibility(R.id.widget_next, View.VISIBLE);
		   views.setViewVisibility(R.id.widget_prev, View.VISIBLE);
	   }
   }
   
   private Task getTaskAtPos(){
	   //if(mCurrentTaskIds == null){
		   mCurrentTaskIds = Task.currentTasks();//}
	   
	   if(taskPos >= 0 && taskPos < mCurrentTaskIds.size()){
		   return (Task) EntityPool.instance().forId(mCurrentTaskIds.get(taskPos),Task.TAG);
	   }else{
		   return null;
	   }
   }
   
   private void updateHomeBtn(Context context,long taskId,RemoteViews views){
		Intent intent = new Intent(context, TaskViewActivity.class);
		intent.setAction(TaskViewActivity.TASK_EDIT);
		intent.putExtra(TaskViewActivity.IN_TASK_ID, taskId);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
			intent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_home, pendingIntent); 
   }
   
   private void homeBtnToLaunch(Context context,RemoteViews views){
	   Intent intent = new Intent(context, TaskOverViewActivity.class);
	   PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
			intent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_home, pendingIntent); 
   }
   
   private void updateTextView(Context context, Task task,RemoteViews views){
	   if(task != null){
		   Scenario scenario = (Scenario) EntityPool.instance().forId(
				   task.getScenarioId(), Scenario.TAG);
		   String title = scenario.getName() + "-"+task.name;
		   views.setTextViewText(R.id.widget_title,title);	   
	   }else{
		   views.setTextViewText(R.id.widget_title,"");
	   }
	   String str =  DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_WEEKDAY
					   | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
	   views.setTextViewText(R.id.widget_text_date, str);
   }
   
   public void setAlarm(Context context,long time){
		if(mPending == null){
			Intent intent = new Intent(context, NotifierReceiver.class);
			intent.setAction(ACTION_WIDGET_ALARM);
			mPending = PendingIntent.getBroadcast(context, 0, intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, time, mPending);
		}
	}

	public void cancelAlarm(){
		SingularityApplication.instance().getNotifierBinder().cancelAlarm(mPending);
		mPending = null;
	}
   
}
