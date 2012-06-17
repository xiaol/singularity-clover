package com.singularity.clover.activity.notification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.singularity.clover.R;
import com.singularity.clover.Global;
import com.singularity.clover.activity.entity.TaskOverViewActivity;
import com.singularity.clover.activity.entity.TaskViewActivity;
import com.singularity.clover.activity.notification.CalendarActivity.ImageAdapter.ViewHolder;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.notification.Notifier;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.util.drag.DragController;
import com.singularity.clover.util.drag.DragDeleteZone;
import com.singularity.clover.util.drag.DragEntityHolder;
import com.singularity.clover.util.drag.DragLayer;
import com.singularity.clover.util.drag.DragSource;
import com.singularity.clover.util.drag.MyAbsoluteLayout;
import com.singularity.clover.util.drag.DragController.DragListener;

public class CalendarActivity extends Activity 
										implements DragListener{
	public final static String CALENDAR_WIZARD_NEW = "singularity.task.wizard.new";
	public final static String CALENDAR_EDIT = "singularity.calendar.edit";
	public final static String CALENDAR_OVERVIEW = "singularity.calendar.overview";
	public final static String IN_TASK_ID = "com.singularity.activity.notification.task.id";
	public final static String IN_SCENARIO_ID = "com.singularity.activity.notification.scenario.id";
	
	protected static int INDICATOR_ZONE;
	
	private ArrayList<Integer> dates;
	private OnImageViewClick onDateClick = new OnImageViewClick();
	protected int mYear,mMonth;
	protected int mMonthDay;
	protected Time today;
	protected int mTaskYear,mTaskMonth,mTaskDay;
	protected int mTaskEndYear,mTaskEndMonth,mTaskEndDay;
	protected int mTaskPeriodic;
	
	protected DragNotifierCtrl mDragController;
	protected DragLayer mDragLayer;
	protected GridView mCalendar;
	protected TextView mDate;
	protected View mWeek;
	protected CalendarFrameLayout mFrame;
	protected DragDeleteZone mDeleteZ;
	private GestureCallback mGestureCallback;
	private CalendarActivityHelper mHelper;
	protected ArrayList<View> notifierIndicators = new ArrayList<View>();
	protected ArrayList<Long> dateNotifierIds = new ArrayList<Long>();
	protected ArrayList<Integer> notifierDates = new ArrayList<Integer>();
	
	protected int mState = STATE_MARK_IDLE;
	protected final static int STATE_MARK_IDLE = 0;
	protected final static int STATE_MARK_DATE = 1;
	protected final static int STATE_MARK_DEADLINE = 2;
	protected final static int STATE_MARK_PERIODIC = 3;
	protected final static int STATE_MARK_NOTIFICAION = 4;
	protected final static int STATE_OVERVIEW = 5;
	protected boolean bNew = false;
	
	protected Task mTask = null;
	
	protected int[] mCoordinate = new int[2];
	protected DisplayMetrics mDisplayMetrics;
	
	protected int mItemHeight,mItemWidth,mItemStartX,mItemStartY;
	protected CalendarCanvas mCalendarCanvas;
	protected int mFirstDayWeek;
	protected int mLastDate;
	
	protected Time mTimeCache = new Time();
	
	public class ImageAdapter extends BaseAdapter {
		private Context _context;
			
		public ImageAdapter(Context _context) {
			super();
			this._context = _context;
		}

		@Override
		public int getCount() {
			return dates.size();
		}

		@Override
		public Object getItem(int pos) {
			return dates.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			TextView textView;
			ViewHolder holder;
			if (convertView == null) { 
				textView = new TextView(_context);
				textView.setGravity(Gravity.BOTTOM|Gravity.RIGHT);	
				textView.setPadding(0, 0, (int)(5*mDisplayMetrics.density),
						(int)(5*mDisplayMetrics.density));
				textView.setTextColor(Color.BLACK);
				textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				textView.setLayoutParams(new GridView.LayoutParams(
						LayoutParams.FILL_PARENT,
						(int) mHelper.getCalendarItemHeight()));
				textView.setOnClickListener(onDateClick);
				holder = new ViewHolder();
			} else {
				textView = (TextView) convertView;
				holder = (ViewHolder) textView.getTag();
				textView.getLayoutParams().height = 
					(int) mHelper.getCalendarItemHeight();
				textView.setCompoundDrawablesWithIntrinsicBounds(
									null, null, null, null);
				}
			
			holder.pos = position;
			textView.setTag(holder);
			if(dates.get(position)!= -1){
				textView.setText(Integer.toString(dates.get(position)));
				textView.setBackgroundResource(R.drawable.calendar_item);
				
				if( mTask != null){
					if(dates.get(position) == mTaskDay 
							&& mYear == mTaskYear && mMonth == mTaskMonth){
						textView.setBackgroundResource(R.drawable.calendar_item_green);
					}
					if(dates.get(position) == mTaskEndDay 
							&& mYear == mTaskEndYear && mMonth == mTaskEndMonth){
						textView.setBackgroundResource(R.drawable.calendar_item_red);
					}
					if(mTask.getPeriodic() != Task.TASK_NOT_PERIODIC){
						mTimeCache.set(dates.get(position), mMonth, mYear);
						Time it = mTimeCache;
						long offset = Time.getJulianDay(it.toMillis(false),mTimeCache.gmtoff) -
							Time.getJulianDay(mTask.getStartDate(),mTimeCache.gmtoff);
						
						long endOffset = Time.getJulianDay(it.toMillis(false),mTimeCache.gmtoff)
							- Time.getJulianDay(mTask.getEndDate(),mTimeCache.gmtoff);
						if(mTask.getEndDate() == Global.INVALIDATE_DATE){
							endOffset = -1;}
						
						if(offset > 0 && offset%mTask.getPeriodic() == 0 && endOffset <= 0){
							textView.setBackgroundResource(R.drawable.calendar_item_yellow);
							Drawable top = getResources().getDrawable(R.drawable.periodic_icon);
							textView.setCompoundDrawablesWithIntrinsicBounds(
									null, top, null, null);
						}
					}		
					
					for(int entry:notifierDates){
						if(entry == dates.get(position)){
							/*textView.setBackgroundResource(
									R.drawable.calendar_item_notification);*/
							Drawable top = getResources().getDrawable(R.drawable.alarm_icon);
							textView.setCompoundDrawablesWithIntrinsicBounds(
									null, top, null, null);
						}
					}
					if(mState == STATE_MARK_NOTIFICAION){
						if(mMonthDay == dates.get(position)){
							textView.setBackgroundResource(
									R.drawable.calendar_item_purple);
						}		
					}
				}
				
				if(dates.get(position) == today.monthDay 
						&& today.year == mYear && today.month == mMonth){
					textView.setBackgroundResource(R.drawable.calendar_item_orange);
				}
			}else{
				textView.setText("");
				textView.setBackgroundResource(R.drawable.calendar_item_gray_no_light);}	
			
			return textView;
		}
	
		public class ViewHolder{
			int pos;
			ArrayList<Long> notifiers;
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_layout);
		mDisplayMetrics = getResources().getDisplayMetrics();
		INDICATOR_ZONE = (int) (32*mDisplayMetrics.density);
		
		/*Rect rect = new Rect();
		Window win = getWindow();
		win.getDecorView().getWindowVisibleDisplayFrame(rect);
		mStatusBarHeight = rect.top;
		int contentViewTop =
		win.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		mTitleBarHeight = contentViewTop - mStatusBarHeight; */
		mHelper = new CalendarActivityHelper(this);
		parse(getIntent());
		initData();
		
		mCalendar = (GridView) findViewById(R.id.calendar_gridview);
	    ImageAdapter adapter = new ImageAdapter(this);
	    mCalendar.setAdapter(adapter);
	    
	    mDeleteZ = (DragDeleteZone) findViewById(
	    		R.id.calendar_drag_delete_zone);
	     
	    mDragController = new DragNotifierCtrl(this);
	    mDragLayer = (DragLayer) findViewById(R.id.calendar_drag_layer);
	    mDragLayer.setDragController(mDragController);
	    mDragController.addDropTarget (mDragLayer);
	    mDragController.addDropTarget(mDeleteZ);
	    mDragController.setDragListener(this);
	        
	    mFrame = (CalendarFrameLayout) findViewById(R.id.calendar_frame_layout);
	    mGestureCallback = new GestureCallback();
	    mFrame.setGestrueCallback(mGestureCallback);
	    
	    mDate = (TextView) findViewById(R.id.calendar_date_text);
	    mDate.setText(mYear+"/"+(mMonth+1));
	    
	    mWeek = findViewById(R.id.calendar_week_layout);
	}

	protected void initData(){
		dates = new ArrayList<Integer>();
		for(int i = 0;i < 42;i++)
			dates.add(-1);
		
		today = new Time();
		today.setToNow();
		mYear = today.year;
		mMonth = today.month;
		mMonthDay = Global.INVALIDATE_MONTH_DAY;
		//today.set(today.monthDay,today.month,today.year);
		onDateChange(mYear, mMonth);
			
		if(mTask == null){
			mTaskYear = mTaskMonth =mTaskDay = -1;
		    mTaskEndYear =mTaskEndMonth=mTaskEndDay=-1;
		}else{
			Time it = new Time();
			it.set(mTask.getStartDate());
			mTaskYear = it.year;
			mTaskMonth = it.month;
			mTaskDay = it.monthDay;
			
			it.set(mTask.getEndDate());
			mTaskEndYear = it.year;
			mTaskEndMonth = it.month;
			mTaskEndDay = it.monthDay;
			
			notifierDates.clear();
			mTask.notification.getDatesByMonth(
					notifierDates, mMonth, mYear);
		}
	}
	
	protected void parse(Intent intent){
		String action;
		if(intent == null){
			action = CALENDAR_OVERVIEW;
		}else{
			action = intent.getAction();
		}
		
		if(action == null){
			action = CALENDAR_OVERVIEW;
		}
		
	    if(action.equals(CALENDAR_WIZARD_NEW)){
	    	long id = intent.getLongExtra(IN_SCENARIO_ID, Global.INVALIDATE_ID);
	    	mTask = new Task();
	    	mHelper.wizardMode(mTask);
	    	mTask.setScenarioId(id);
	    	mTask.store();
	    	bNew = true;
	    	mState = STATE_MARK_IDLE;
	    }else if(action.equals(CALENDAR_EDIT)){
	    	long id = intent.getLongExtra(IN_TASK_ID, Global.INVALIDATE_ID);
	    	if(id != Global.INVALIDATE_ID){
	    		mTask = (Task) EntityPool.instance().forId(id, Task.TAG);
		    	mHelper.wizardMode(mTask);
		    	bNew = false;	
	    	}else{
	    		finish();
	    	}
	    	mState = STATE_MARK_IDLE;	    
	    }else if(action.equals(CALENDAR_OVERVIEW)){
	    	mHelper.overviewMode();
	    	mTask = null;
	    	mState = STATE_OVERVIEW;
	    	bNew = false;
	    }else{
	    }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		parse(intent);
		initData();
		notifyCalendarInvalidated();
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*parse(getIntent());
		initData();*/
		mDate.setText(mYear+"/"+(mMonth+1));
	}

	@Override
	public void onBackPressed() {
		if(mState == STATE_OVERVIEW ){
			Intent intent = new Intent(
					CalendarActivity.this, TaskOverViewActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			setIntent(null);
			return;
		}else{
			if(bNew){
				switch (mState) {
				case CalendarActivity.STATE_MARK_NOTIFICAION:
					mHelper.onMarkNotificationDone();
					break;
				case CalendarActivity.STATE_MARK_PERIODIC:
					mHelper.onMarkPeriodicDone();
					mTask.store();
					break;
				case CalendarActivity.STATE_MARK_DEADLINE:
					mHelper.onMarkDeadlineDone();
					mTask.store();
					break;
				case CalendarActivity.STATE_MARK_DATE:
					mHelper.onMarkTaskDateDone();
					mTask.store();
					break;
				case CalendarActivity.STATE_MARK_IDLE:
					mTask.delete();
					Intent intent = new Intent(
							CalendarActivity.this, TaskOverViewActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
					setIntent(null);
					break;
				default:
					break;
				}
			}else{
				/*Intent intent = new Intent(
						CalendarActivity.this, TaskViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.setAction(TaskViewActivity.TASK_EDIT);
				intent.putExtra(TaskViewActivity.IN_TASK_ID, mTask.getId());
				startActivity(intent);*/
				
				switch (mState) {
				case CalendarActivity.STATE_MARK_NOTIFICAION:
					mHelper.onMarkNotificationDone();
					break;
				case CalendarActivity.STATE_MARK_PERIODIC:
					mHelper.onMarkPeriodicDone();
					mTask.store();
					break;
				case CalendarActivity.STATE_MARK_DEADLINE:
					mHelper.onMarkDeadlineDone();
					mTask.store();
					break;
				case CalendarActivity.STATE_MARK_DATE:
					mHelper.onMarkTaskDateDone();
					mTask.store();
					break;
				case CalendarActivity.STATE_MARK_IDLE:
					super.onBackPressed();
					break;
				default:
					break;
				}
				
			}
			mHelper.mConfirm.setVisibility(View.GONE);
			setIntent(null);
			return;
		}
	}

	private class OnImageViewClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			if(dates.get(holder.pos) == -1){
				return;}
			switch (mState) {
			case STATE_MARK_DATE:
				Time it = caculateDate(holder.pos);
				mTask.setStartDate(it.toMillis(false));
				mTaskDay = it.monthDay;
				mTaskMonth = it.month;
				mTaskYear = it.year;
				notifyCalendarInvalidated();
				break;
				
			case STATE_MARK_DEADLINE:
				Time it2 = caculateDate(holder.pos);
				mTask.setEndDate(it2.toMillis(false));
				mTaskEndDay = it2.monthDay;
				mTaskEndMonth = it2.month;
				mTaskEndYear = it2.year;
				notifyCalendarInvalidated();
				break;
				
			case STATE_MARK_PERIODIC:
				Time it3 = caculateDate(holder.pos);
				int offset = Time.getJulianDay(it3.toMillis(false),it3.gmtoff) -
						Time.getJulianDay(mTask.getStartDate(),it3.gmtoff);
						
				if(offset > 0) {
					String prefix = getResources().getString(
							R.string.toast_calendar_periodic_prefix);
					String suffix = getResources().getString(
							R.string.toast_calendar_periodic_suffix);
					mTask.setPeriodic(offset);
					Toast toast = Toast.makeText(CalendarActivity.this, 
							prefix +offset+suffix, Toast.LENGTH_SHORT);
					toast.show();
					
					mHelper.mPeriodic.setText(prefix + offset+suffix);
					notifyCalendarInvalidated();
				}else{
					String hint = getResources().getString(
							R.string.toast_calendar_periodic_hint);
					Toast toast = Toast.makeText(CalendarActivity.this, 
							hint, Toast.LENGTH_SHORT);
					toast.show();
				}
				break;
			case STATE_MARK_NOTIFICAION:
				mTimeCache.set(dates.get(holder.pos), mMonth, mYear);
				mTimeCache.normalize(false);
				int todayJulianDay = Time.getJulianDay(today.toMillis(false),today.gmtoff);
				int timeJulianDay = Time.getJulianDay(mTimeCache.toMillis(false), mTimeCache.gmtoff);
				if( timeJulianDay < todayJulianDay){
					String date;
					date = DateUtils.formatDateTime(
							null, today.toMillis(false), DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
					String prefix = getResources().getString(
							R.string.toast_calendar_periodic_hint);
					Toast toast = Toast.makeText(CalendarActivity.this, 
							prefix + date, Toast.LENGTH_SHORT);
					toast.show();
					break;}	
				
				mMonthDay = dates.get(holder.pos);
				dateNotifierIds.clear();
				mTask.notification.getIdsByDate(
						dateNotifierIds, mTimeCache.toMillis(false));
				
				Iterator<View> views = notifierIndicators.iterator();
				
				boolean bUsed = true;
				for(long id:dateNotifierIds){
					Notifier notifier = (Notifier) EntityPool.
						instance().forId(id, Notifier.TAG);
					timeTocoordinate(mCoordinate, notifier.getTriggerDate());
					if(views.hasNext() && bUsed){
						View view = views.next();
						EntityHolder holder2 = (EntityHolder) view.getTag();
						holder2.id = id;
						view.setVisibility(View.VISIBLE);
						MyAbsoluteLayout.LayoutParams lp = 
							(MyAbsoluteLayout.LayoutParams) view.getLayoutParams();
						lp.x = mCoordinate[0] - 32/2;
						lp.y = mCoordinate[1] - 32/2;
						view.requestLayout();
					}else{
						initNotifyIndicator(mCoordinate[0], mCoordinate[1],notifier);
						bUsed = false;
					}
				}
				
				while(views.hasNext() && bUsed){
					View view = views.next();
					view.setVisibility(View.GONE);
				}
				notifyCalendarInvalidated();
				break;
			case STATE_OVERVIEW:
				mTimeCache.set(dates.get(holder.pos),mMonth,mYear);
				boolean bTask = false;
				if(mCalendarCanvas.ids != null){
					for(long entry:mCalendarCanvas.ids){
						Task task = (Task) EntityPool.instance().forId(entry,Task.TAG);
						long date = mTimeCache.toMillis(false);
						if((task.getStartDate() <= date && task.getEndDate() >=date)
							|| (task.getStartDate() == date && task.getEndDate() == Global.INVALIDATE_DATE)){
							bTask = true;
							break;
						}
					}
				}
				if(bTask){
					Intent intent = new Intent(
							CalendarActivity.this,TaskOverViewActivity.class);
					intent.setAction(TaskOverViewActivity.TASK_OVERVIEWBY_CALENDER);
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					intent.putExtra(TaskOverViewActivity.IN_CALENDAR_DATE,
							mTimeCache.toMillis(false));
					startActivity(intent);
				}else{
					String hint = getResources(
							).getString(R.string.calendar_overview_hint);
					Toast toast = Toast.makeText(CalendarActivity.this, 
							hint, Toast.LENGTH_SHORT);
					toast.show();
				}
				
				break;
			default:
				break;
			}
		}	
	}
	
	private void onDateChange(int year,int month){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH,(month-1)<0?11:(month-1));
		//int lastMonthDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.MONTH,month);
		cal.set(Calendar.DAY_OF_MONTH,1);
		mFirstDayWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
		mFirstDayWeek = mFirstDayWeek == 0?7:mFirstDayWeek;
		mLastDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		int offset = mFirstDayWeek + mLastDate;
		int date = 1;
		for(int i = 0 ;i< 42;i++){
			if(i < offset && i >= mFirstDayWeek){
				dates.set(i, date);
				date++;
			}else{
				dates.set(i,-1);}
		}
	}
	
	private Time caculateDate(int pos){
		mMonthDay = dates.get(pos);
		mTimeCache.set(mMonthDay, mMonth, mYear);
		return mTimeCache;
	}
	
	
	private class OnDateIndicatorLongClick implements View.OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			if (!v.isInTouchMode()) 
				return false;			
			Object dragInfo = v;
			mDragController.startDrag (v, mDragLayer, 
					dragInfo, DragController.DRAG_ACTION_MOVE);
			return true;
		}
		
	}
	
	private class GestureCallback implements CalendarFrameLayout.GestureCallback{

		@Override
		public void onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(velocityX < 0 && 
					abs(e1.getX()-e2.getX()) > abs(e2.getY()-e2.getY())){
				mMonth++;
				if(mMonth > 11){
					mYear++;
					mMonth = 0;
				}
			}else{
				mMonth--;
				if(mMonth < 0){
					mYear--;
					mMonth = 11;
				}
			}
			onCalenderScrollHelper();
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if(mState == STATE_MARK_NOTIFICAION && !mDragController.isDragging()){
				if(mMonthDay == Global.INVALIDATE_MONTH_DAY){
					String hint = getResources().getString(
							R.string.toast_calendar_mark_day_please);
					Toast toast = Toast.makeText(CalendarActivity.this, 
							hint, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				mCoordinate[0] = (int) e.getX();
				mCoordinate[1] = (int) e.getY();
				transform(mCoordinate);
				long time = caculateToTime(mCoordinate);
				Notifier notifier = new Notifier(time,mTask.getId());
				notifier.store();
				mTask.notification.add(notifier.getId());
				mTask.store();
				initNotifyIndicator(mCoordinate[0], mCoordinate[1],notifier);
			
			}	
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if(abs(distanceX) > abs(distanceY) ){
				if(distanceX > 0){
					mMonth++;
					if(mMonth > 11){
						mYear++;
						mMonth = 0;
					}
				}else{
					mMonth--;
					if(mMonth < 0){
						mYear--;
						mMonth = 11;
					}
				}
			}else{
				return false;
			}
			onCalenderScrollHelper();
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			String hint = getResources().getString(
					R.string.toast_calendar_notification_single_tap);
			Toast toast = Toast.makeText(CalendarActivity.this, 
				hint, Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}	
	}

	private void onCalenderScrollHelper(){
		onDateChange(mYear, mMonth);
			mDate.setText(mYear+"/"+(mMonth+1));
			mMonthDay = Global.INVALIDATE_MONTH_DAY;
			
			if(mState != STATE_OVERVIEW){
				notifierDates.clear();
				mTask.notification.getDatesByMonth(
						notifierDates, mMonth, mYear);
				if(mState == STATE_MARK_NOTIFICAION){
					for(View entry:notifierIndicators){
						entry.setVisibility(View.GONE);
					}
				}
			}else if(mState == STATE_OVERVIEW){
				View view = mCalendar.getChildAt(0);
				mItemStartX = view.getLeft();
				mItemStartY = view.getTop();
				mItemHeight = view.getHeight();
				mItemWidth = view.getWidth();
			
				mCalendarCanvas.setParams(mItemStartX, 
						mItemStartY, mItemWidth,mItemHeight,
						mFirstDayWeek,mLastDate,
						mMonth,mYear);
				mCalendarCanvas.setTaskIds(mHelper.queryByMonth());
				mCalendarCanvas.invalidate();
			}
			
			BaseAdapter adapter = (BaseAdapter)mCalendar.getAdapter();
			adapter.notifyDataSetChanged();
	}
	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		mDeleteZ.setVisibility(View.VISIBLE);
		mFrame.setDragging(true);
	}

	@Override
	public void onDragEnd() {
		mDeleteZ.setVisibility(View.GONE);
		mFrame.setDragging(false);
	}
	
	private void transform(int[] coordinate){
		float ratio = 0;
		float x,y;
		float wRadius = (mFrame.getWidth() - INDICATOR_ZONE)/2.0f;
		float hRadius = (mFrame.getHeight() - INDICATOR_ZONE)/2.0f;
		
		x = coordinate[0] - INDICATOR_ZONE/2 - wRadius;
		y = coordinate[1] - INDICATOR_ZONE/2 - hRadius;
		
		if(abs(x) > abs(y)* wRadius/hRadius){
			ratio = abs(x)/wRadius;
			y = y/ratio;
			int symbol= x >=0?1:-1;
			x = symbol*wRadius;
		}else{
			ratio = abs(y)/hRadius;
			x = x/ratio;
			int symbol= y >=0?1:-1;
			y = symbol*hRadius;
		}
		
		coordinate[0] = (int) (x + INDICATOR_ZONE/2 + wRadius);
		coordinate[1] = (int) (y + INDICATOR_ZONE/2 + hRadius);
	}
	
	public long caculateToTime(int[] coordinate){
		float totalLength = 2*mFrame.getWidth() + 
			2*mFrame.getHeight()- 4*INDICATOR_ZONE;
		float seed = 24*60/5;
		float wRadius = (mFrame.getWidth() - INDICATOR_ZONE)/2.0f;
		float hRadius = (mFrame.getHeight() - INDICATOR_ZONE)/2.0f;
		float ratio = hRadius/wRadius;
		
		float x,y;
		x = coordinate[0] - INDICATOR_ZONE/2 - wRadius;
		y = coordinate[1] - INDICATOR_ZONE/2 - hRadius;
		
		float timeLength = 0;
		if(x < 0 && y <0 ){
			if( -x*ratio < -y){
				timeLength = -x;
			}else{
				timeLength = wRadius + hRadius + y;
			}
		}else if(x < 0 && y > 0){
			if( -x*ratio >y){
				timeLength = wRadius + hRadius +y;
			}else{
				timeLength = wRadius + 2*hRadius +wRadius+x;
			}
		}else if(x>0 && y>0){
			if(y > x*ratio){
				timeLength = 2*wRadius + 2*hRadius + x;
			}else{
				timeLength = 3*wRadius + 2*hRadius + hRadius - y;
			}
		}else if(x>0 && y<0){
			if(x*ratio > -y){
				timeLength = 3*wRadius + 3*hRadius -y;
			}else{
				timeLength = 3*wRadius + 4*hRadius + wRadius -x;
			}
		}
		int hour = (int) (timeLength/totalLength*24);
		int minute5 = (int) (timeLength/totalLength*seed + 0.5f) - hour*12;
		int minute = 5*minute5;
		
		Time notifierTime = new Time();
		notifierTime.set(0, minute, hour, mMonthDay, mMonth, mYear);
		
		Toast toast = Toast.makeText(this, 
				hour+":"+minute, Toast.LENGTH_SHORT);
		toast.show();
		return notifierTime.toMillis(false);
	}
	
	public void timeTocoordinate(int[] coordinate,long time){
		Time notifierTime = new Time();
		notifierTime.set(time);
		
		float totalLength = 2*mFrame.getWidth() + 
			2*mFrame.getHeight()- 4*INDICATOR_ZONE;
		float seed5 = 24*60;
		float wRadius = (mFrame.getWidth() - INDICATOR_ZONE)/2.0f;
		float hRadius = (mFrame.getHeight() - INDICATOR_ZONE)/2.0f;
		
		float timeLength = (notifierTime.hour*60 + 
				notifierTime.minute)*(totalLength/seed5);
		
		float x,y;
		if(timeLength < wRadius ){
			x = -timeLength; 
			y = 0;
		}else if(timeLength < wRadius + hRadius){
			x = -wRadius;
			y = -hRadius + (timeLength - wRadius);
		}else if(timeLength < wRadius + 2*hRadius){
			x = -wRadius;
			y = timeLength - wRadius - hRadius;
		}else if(timeLength < 2*wRadius + 2*hRadius){
			x = -wRadius + timeLength - wRadius - 2*hRadius;
			y = hRadius;
		}else if(timeLength <3*wRadius + 2*hRadius){
			x = timeLength - 2*wRadius - 2*hRadius;
			y = hRadius;
		}else if(timeLength<3*wRadius + 3*hRadius){
			x = wRadius;
			y = hRadius - (timeLength - (3*wRadius + 2*hRadius));
		}else if(timeLength<3*wRadius + 4*hRadius){
			x = wRadius;
			y = -timeLength + (3*wRadius + 3*hRadius);
		}else if(timeLength<4*wRadius + 4*hRadius){
			x = wRadius - timeLength + (3*wRadius + 4*hRadius);
			y = -hRadius;
		}else{
			x = 0;
			y =0;
		}
		
		coordinate[0] = (int) (x + INDICATOR_ZONE/2 + wRadius);
		coordinate[1] = (int) (y + INDICATOR_ZONE/2 + hRadius);
	}
	
	private float abs(float a){
		return a>0?a:-a;
	}
	
	private View initNotifyIndicator(int x,int y,Notifier notifier){
		ImageView indicatorView = new ImageView(this);
		int r = (int) (16*mDisplayMetrics.density);
		
	    indicatorView.setLayoutParams(
	    		new MyAbsoluteLayout.LayoutParams(2*r,2*r,x-r,y-r));
	    indicatorView.setBackgroundResource(R.drawable.calendar_indicator);
	    
	    indicatorView.setOnLongClickListener(new OnDateIndicatorLongClick());
	    mDragLayer.addView(indicatorView);
	    EntityHolder holder = new EntityHolder(
						notifier.getId(), notifier.getTAG());
		indicatorView.setTag(holder);
	    notifierIndicators.add(indicatorView);
	    
	    return indicatorView;
	}
	
	protected class EntityHolder implements DragEntityHolder{
		private long id;
		private String tag;
		
		protected EntityHolder(long id,String tag){
			this.id = id;
			this.tag = tag;
		}
		@Override
		public String getTAG() {
			return tag;
		}

		@Override
		public long getId() {
			return id;
		}
	}
	
	protected void notifyCalendarInvalidated(){
		BaseAdapter adapter = (BaseAdapter)mCalendar.getAdapter();
		adapter.notifyDataSetInvalidated();
	}
	
	protected void notifyCalendarChanged(){
		BaseAdapter adapter = (BaseAdapter)mCalendar.getAdapter();
		adapter.notifyDataSetChanged();
	}
}
