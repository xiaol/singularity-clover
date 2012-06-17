package com.singularity.clover.activity.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.SingularityPreference;
import com.singularity.clover.activity.entity.ColorPickerDialog.OnColorChangedListener;
import com.singularity.clover.activity.entity.TaskOverViewActivity.ListAdapter.ViewHolder;
import com.singularity.clover.activity.lbs.LocationActivity;
import com.singularity.clover.activity.lbs.LocationService;
import com.singularity.clover.activity.notification.CalendarActivity;
import com.singularity.clover.activity.record.RecordOverViewActivity;
import com.singularity.clover.database.DBAdapter;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.lbs.LBSBundle;
import com.singularity.clover.entity.objective.AbstractObjective;
import com.singularity.clover.entity.record.Hierarchy;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.task.AbstractTask;
import com.singularity.clover.entity.task.Task;
import com.singularity.clover.entity.wrapper.Scenario;
import com.singularity.clover.view.VerticalLabelView;

public class TaskOverViewActivity extends ListActivity
									implements OnColorChangedListener{
	public static final String TASK_OVERVIEWBY_SCENARIO = "scenario_overview";
	public static final String TASK_OVERVIEWBY_REVIEW = "review_overview";
	public static final String TASK_OVERVIEWBY_PLAN = "review_overview";
	public static final String TASK_OVERVIEWFOR_RECORD_ATTACH = "overview_record";
	public static final String TASK_OVERVIEWBY_CALENDER = "calender_overview";
	public static final String TASK_OVERVIEWBY_STATUS = "status_overview";
	public static final String TASK_OVERVIEW_ADD_TASK_DONE = "add_task";
	
	
	public static final String IN_ADD_TASK_ID = "add_task_id";
	public static final String IN_CALENDAR_DATE = "calendar_date";
	public static final String IN_SCENAIRO_ID = "com.singularity.clover.activity.entity.taskoverview.scenario.id";
	
	public static final int OUT_CALENDAR_OVERVIEW = 468;
	public static final int OUT_ADD_TASK = 0;
	public static final int OUT_EDIT_TASK = 1;
	public static final int CONTEXTMENU_DELETEITEM = 0;
	public static final int CONTEXTMENU_EDITITEM = 1;
	public static final int CONTEXTMENU_ADDITEM = 2;
	public static final int CONTEXTMENU_LOCATION = 3;
	
	private ArrayList<Long> taskIds = null;
	private OnClickTask onClick = new OnClickTask();
	private OnLongClickTask onLongClick = new OnLongClickTask();
	private TaskOverViewActivityHelper.OnCheckedChanged onChecklist= null;
	private TaskOverViewActivityHelper.OnNumericPlus onNumericPlus = null;
	private TaskOverViewActivityHelper.OnDurableCtrl onDurableCtrl = null;
	protected long mCurrentScenario = Global.INVALIDATE_ID;
	protected View mFocusScenario = null;
	private FloatView mFloatView;
	protected CheckBox mCheckLBS;
	protected View mMap;
	
	protected ListView mScenairoList;
	private AlertDialog mScenarioDialog;
	private EditText mScenarioDialogEdit;
	private ImageButton mScenarioDialogPicker;
	protected View mMindCollects;
	protected Button mBtnCalendar;
	private View mBtnDelete;
	private View mBtnAdd;
	private View mMenu;
	protected View mAbout;
	protected View mTutorialCompass;
	protected View mTutorialScanrio;
	protected ViewFlipper mFlipper;
	protected TextView mProverb;
	protected View mBookMark;
	protected TextView mReview;
	protected ReviewView mReviewCanvas;
	
	protected View focusView = null;
	protected long focusId;
	private AlertDialog mDeleteTaskDialog;
	private AlertDialog mDeleteScenarioDialog;
	private int mDeleteScenarioPosition;
	private int mPickedColor;
	private boolean bEditScenario = false;
	private long mEditScenarioId;
	
	private TaskOverViewActivityHelper mHelper;
	
	protected String whereClauseAddition="";
	protected long mCalendarDate = Global.INVALIDATE_DATE;
	protected CursorAdapter mScenarioListAdapter;
	protected DisplayMetrics mDisplayMetrics;
	protected int[] colors = new int[4];
	protected Random mDice;
	protected String[] mProverbs;
	
	public class ListAdapter extends BaseAdapter {
			
		public ListAdapter(Context _context) {
			super();
		}

		@Override
		public int getCount() {
			return taskIds.size();
		}

		@Override
		public Object getItem(int pos) {
			return taskIds.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return 0;
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			
			ViewHolder holder;
			if (convertView == null) { 
				Context mContext = TaskOverViewActivity.this;
				LayoutInflater inflater = (LayoutInflater) mContext.
					getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView =  inflater.inflate(
						R.layout.taskoverview_list_item_layout,null);
				convertView.setOnClickListener(onClick);
				convertView.setOnLongClickListener(onLongClick);
				holder = new ViewHolder();
				holder.viewOBJ = (ListItemOBJView) convertView.findViewById(
						R.id.taskoverview_list_item_objectives);
				holder.textName = (TextView) convertView.findViewById(
						R.id.taskoverview_list_item_task_name);
				holder.rec = (ImageView) convertView.findViewById(
						R.id.taskoverview_list_item_records);
				holder.pageCount = (TextView) convertView.findViewById(
						R.id.taskoverview_list_item_page_count);
				holder.imageRecord = (ImageView) convertView.findViewById(
						R.id.taskoverview_list_item_records);
				holder.imageNotification = (ImageView) convertView.findViewById(
						R.id.taskoverview_list_item_notification);
				holder.imageDate = (ImageView) convertView.findViewById(
						R.id.taskoverview_list_item_image_date);
				holder.textDate = (TextView) convertView.findViewById(
						R.id.taskoverview_list_item_text_date);
				holder.imagePeriodic = (ImageView) convertView.findViewById(
						R.id.taskoverview_list_item_periodic);
				holder.recordBrief = (TextView) convertView.findViewById(
						R.id.taskoverview_list_item_task_record_brief);
				holder.objContent = (TextView) convertView.findViewById(
						R.id.taskoverview_list_item_task_obj_content);
				holder.checklist = (CheckBox) convertView.findViewById(
						R.id.taskoverview_list_item_task_obj_checklist);
				holder.numericPlus = (ImageButton) convertView.findViewById(
						R.id.taskoverview_list_item_task_obj_numeric_plus);
				holder.durableCtrl = (ImageButton) convertView.findViewById(
						R.id.taskoverview_list_item_task_obj_durable_ctrl);	
				
				holder.checklist.setOnClickListener(onChecklist);
				holder.numericPlus.setOnClickListener(onNumericPlus);
				holder.durableCtrl.setOnClickListener(onDurableCtrl);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.taskId = taskIds.get(position);
			Task it = (Task) EntityPool.instance().forId(holder.taskId, Task.TAG);
			
			//convertView.setBackgroundColor(colors[position%4]);
			holder.viewOBJ.setVisibility(it.getObjCount()==0?View.GONE:View.VISIBLE);
			
			if(it.getObjCount() != 0){
				it.processValidate();	
				holder.viewOBJ.setParams(it.getObjCount(),it.getProcess());
				AbstractObjective obj = it.orderedOBJ(null);
				if(obj != null){
					mHelper.swiftOBJ(obj, holder);
				}else{
					mHelper.disableOBJPanel(holder);
				}
			}else{
				mHelper.disableOBJPanel(holder);
			}
			
			String title;
			if(it.name == null ){
				title = getText(R.string.empty_task_title).toString();
			}else{
				if(it.name.equals("")){
					title = getText(R.string.empty_task_title).toString();
				}else{
					title = it.name;
				}
			}
			it.updateStatus();
			if (it.getStatus() == AbstractTask.Status.EXPIRED.getStatus()) {
				SpannableStringBuilder builder = new SpannableStringBuilder();
				int base = title.length();
				builder.append(title);
				builder.setSpan(new ForegroundColorSpan(Color.GRAY), 0,
						base, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(new StrikethroughSpan(), 0, base,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.textName.setText(builder);
			}else{
				holder.textName.setText(title);
			}
			
			if(it.attachment.isEmpty()){
				holder.rec.setVisibility(View.GONE);
				holder.recordBrief.setVisibility(View.INVISIBLE);
			}else{
				holder.rec.setVisibility(View.VISIBLE);
				if(it.attachment.recentText() != null){
					holder.recordBrief.setVisibility(View.VISIBLE);
					holder.recordBrief.setText(it.attachment.recentText());
				}else{
					holder.recordBrief.setVisibility(View.INVISIBLE);
				}
			}
			holder.rec.setVisibility(it.attachment.isEmpty()?View.GONE:View.VISIBLE);
			long alarmDate = it.notification.getNextNotifier();
			holder.imageNotification.setVisibility(
					alarmDate == Global.INVALIDATE_DATE?View.INVISIBLE:View.VISIBLE);
			holder.imagePeriodic.setVisibility(
					it.getPeriodic() == Task.TASK_NOT_PERIODIC?View.INVISIBLE:View.VISIBLE);
			
			
			if(it.getStatus() == AbstractTask.Status.ONGOING.getStatus()){
				holder.imageDate.setVisibility(View.VISIBLE);
				holder.textDate.setVisibility(View.VISIBLE);
				holder.imageDate.setImageResource(R.drawable.task_overview_incoming_icon);
				holder.textDate.setTextColor(getResources().getColor(R.color.task_date_green));
				
			}else if(it.getStatus() == AbstractTask.Status.WAITINGTODO.getStatus()){
				holder.imageDate.setVisibility(View.VISIBLE);
				holder.textDate.setVisibility(View.VISIBLE);
				holder.imageDate.setImageResource(R.drawable.task_overview_deadline_icon);
				holder.textDate.setTextColor(getResources().getColor(R.color.task_date_red));
			}else{
				holder.imageDate.setVisibility(View.INVISIBLE);
				holder.textDate.setVisibility(View.INVISIBLE);
			}
			switch (it.mDay) {
			case 0:
				holder.textDate.setText(getText(R.string.task_status_today));
				break;
			case 1:
				holder.textDate.setText(getText(R.string.task_status_tomorrow));
				break;
			default:
				holder.textDate.setText(Integer.toString(it.mDay)
						+ getText(R.string.task_status_suffix));
				break;
			}
			holder.pageCount.setText(Integer.toString(position));
			
			convertView.setTag(holder);
			return convertView;
		}
		
		public class ViewHolder{
			long taskId;
			ListItemOBJView viewOBJ;
			TextView textName;
			ImageView rec;
			TextView pageCount;
			ImageView imageRecord;
			ImageView imageNotification;
			ImageView imagePeriodic;
			ImageView imageDate;
			TextView textDate;
			TextView recordBrief;
			TextView objContent;
			CheckBox checklist;
			ImageButton numericPlus;
			ImageButton durableCtrl;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taskoverview_layout);
		mBtnDelete = findViewById(R.id.task_overview_delete);
		mBtnAdd = findViewById(R.id.task_overview_add);
		mMenu = findViewById(R.id.task_overview_menu);
		mMenu.setVisibility(View.GONE);
		mFlipper = (ViewFlipper) findViewById(
				R.id.task_overview_viewflipper);
		mHelper = new TaskOverViewActivityHelper(this);
		onChecklist = mHelper.new OnCheckedChanged();
		onNumericPlus = mHelper.new OnNumericPlus();
		onDurableCtrl = mHelper.new OnDurableCtrl();
		mMindCollects = findViewById(
				R.id.task_overview_surface_mind_fragment);
		mBtnCalendar = (Button) findViewById(
				R.id.task_overview_surface_calendar);
		mProverb = (TextView) findViewById(
				R.id.task_overview_surface_proverb);
		mReview = (TextView) findViewById(
				R.id.task_overview_surface_review);
		mReviewCanvas = (ReviewView) findViewById(
				R.id.task_overview_surface_review_canvas);
		//mBookMark = findViewById(
				//R.id.task_overview_backto_surface);
		mAbout = findViewById(
				R.id.task_overview_surface_about);
		mCheckLBS = (CheckBox) findViewById(
				R.id.task_overview_surface_compass);
		mMap = findViewById(
				R.id.task_overview_surface_location);
		mDisplayMetrics = getResources().getDisplayMetrics();
		mTutorialCompass = findViewById(
				R.id.task_overview_surface_tutorial_compass);
		mTutorialScanrio = findViewById(
				R.id.task_overview_surface_tutorial_long_press_scenario);
		Resources res = getResources();
		mProverbs = res.getStringArray(R.array.proverb_array);
			
		Time today = new Time();
		today.setToNow();
		mDice = new Random(today.toMillis(false));
		
		String week = DateUtils.formatDateTime(
					this, today.toMillis(false), DateUtils.FORMAT_SHOW_WEEKDAY);
		String monthDay = Integer.toString(today.monthDay);
		String year = Integer.toString(today.year);
		String month = Integer.toString(today.month +1);
		/*SpannableStringBuilder builder = new SpannableStringBuilder();
		
		builder.append(week);
		builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
				0, week.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.append("\n");
		
		builder.append(monthDay);
		builder.append("\n");*/
		mBtnCalendar.setText(Html.fromHtml("<small>" + week + "</small>" +  "<br />" + 
            "<strong><big>" + monthDay + "</big></strong>" + "<br />" + 
            "<small>" + year + "</small>" + "<small>" +" "+ month + "</small>"));

		ArrayList<Long> ids = EntityPool.instance().
	    	getPrototype(Record.TAG).loadTable(null,null);
	    Hierarchy hierarchyCtrl = new Hierarchy();
	    hierarchyCtrl.buildHierarchy(ids);
		
	    EntityPool.instance(
	    		).getPrototype(LBSBundle.TAG).loadTable(null, null);
	    HashMap<Long, Persisable> bundles = EntityPool.instance()
						.forTag(LBSBundle.TAG);
	    if(!bundles.isEmpty()){
			mCheckLBS.setVisibility(View.VISIBLE);
			mMap.setVisibility(View.VISIBLE);
		}else{
			mCheckLBS.setVisibility(View.GONE);
			mMap.setVisibility(View.GONE);
		}
	    
		setupList(getIntent());
		setListAdapter(new ListAdapter(this));
		getListView().setOnHierarchyChangeListener(
				mHelper.new OnTaskListHierarchyChanged());
		
		rollProverb();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.gc();
		if(mFloatView != null){
			mFloatView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		if(mFlipper.getDisplayedChild() == 1 
				&& whereClauseAddition == ""){
			flipToSurface();
			return;
		}else if(whereClauseAddition != ""){
			Intent intent = new Intent(
					TaskOverViewActivity.this, CalendarActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			Handler handler = new Handler();
			handler.postDelayed(new flipPageRunnable(), 500);
		}else{
			moveTaskToBack(true);
		}
		
	}

	public class flipPageRunnable implements Runnable{

		@Override
		public void run() {
			whereClauseAddition = "";
			setupScenarioList();
			flipToSurface();	
		}
		
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch (mFlipper.getDisplayedChild()) {
		case 0:
			if(mAbout.getVisibility() != View.VISIBLE){
				mAbout.setVisibility(View.VISIBLE);
				if(mCheckLBS.getVisibility() == View.VISIBLE)
					mTutorialCompass.setVisibility(View.VISIBLE);
				mTutorialScanrio.setVisibility(View.VISIBLE);
			}else{
				mAbout.setVisibility(View.GONE);
				mTutorialCompass.setVisibility(View.GONE);
				mTutorialScanrio.setVisibility(View.GONE);
			}
			break;
		case 1:
			if(mMenu.getVisibility() != View.VISIBLE){
				mMenu.setVisibility(View.VISIBLE);
			}else{
				mMenu.setVisibility(View.GONE);}
			break;
		default:
			break;
		}
		return false;
	}

	private class MyCursorAdapter extends CursorAdapter{

		public MyCursorAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			VerticalLabelView text = (VerticalLabelView) view.findViewById(
					R.id.taskoverview_scenario_list_label);
			long currentId = cursor.getLong(0);
			if(currentId == mCurrentScenario){
				//mFocusScenario = view;
			}else{
			}
			view.setTag(currentId);
			text.setText(cursor.getString(1));
			text.setBackgroundColor(cursor.getInt(2));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View layout = mHelper.initScenairoLayout(context, cursor);
			return layout;
		}
		
	}
	
	private void setupScenarioList(){
		if(mScenairoList == null){
			mScenairoList = (ListView)findViewById(
					R.id.task_overview_scenario_list);
			mScenairoList.setOnItemClickListener(new OnClickScanario());
			//mScenairoList.setOnItemLongClickListener(new onLongClickScanrio());
			mScenairoList.setOnCreateContextMenuListener(new OnScenarioContextMenu());
			mScenairoList.setOnHierarchyChangeListener(
					mHelper.new OnScenarioHierarchyChanged());
		}
		Cursor cur = DBAdapter.instance().retrieveAll(Scenario.TAG, null, null);
		if(mScenarioListAdapter == null){
			mScenarioListAdapter = new MyCursorAdapter(TaskOverViewActivity.this, cur);
			mScenairoList.setAdapter(mScenarioListAdapter);
		}else{
			mScenarioListAdapter.changeCursor(cur);
		}
		
	}
	
	private void setupScenarioListByFilter(String sql,String[] whereArgs){
		if(mScenairoList == null){
			mScenairoList = (ListView)findViewById(
					R.id.task_overview_scenario_list);
			mScenairoList.setOnItemClickListener(new OnClickScanario());
			//mScenairoList.setOnItemLongClickListener(new onLongClickScanrio());
			mScenairoList.setOnCreateContextMenuListener(new OnScenarioContextMenu());
			mScenairoList.setOnHierarchyChangeListener(
					mHelper.new OnScenarioHierarchyChanged());
		}
		Cursor cur = DBAdapter.instance().execQuery(sql, whereArgs);
		if(mScenarioListAdapter == null){
			mScenarioListAdapter = new MyCursorAdapter(TaskOverViewActivity.this, cur);
			mScenairoList.setAdapter(mScenarioListAdapter);
		}else{
			mScenarioListAdapter.changeCursor(cur);
		}
	}
	
	private void setupList(Intent intent){
		String whereClause = null;
		String[] whereArgs = null;
		parse(intent,whereClause,whereArgs);
		
		/*taskIds = EntityPool.instance().
			getPrototype(Task.TAG).loadTable(" WHERE scenario = ?",whereArgs);*/
		if(taskIds == null){
			taskIds = new ArrayList<Long>();
		}else{}
	}
	
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String whereClause = null;
		String[] whereArgs = null;
		setIntent(intent);
		parse(intent, whereClause, whereArgs);
		rollProverb();
	}



	private void parse(Intent intent,String whereClause,String[] whereArgs){
		String action;
		if(intent == null){
			action = Intent.ACTION_MAIN;
		}else{
			action = intent.getAction();
		}
		if(action == null){
			action = Intent.ACTION_MAIN;}
		
		if(action.equals(TASK_OVERVIEWBY_SCENARIO)){
			long scenarioId = intent.getLongExtra(
					IN_SCENAIRO_ID, Global.INVALIDATE_ID);
			if(scenarioId == Global.INVALIDATE_ID){
			}else{
				setUpTaskListByScenairo(scenarioId);
			}
			setupScenarioList();
			flipToPage();
		}else if(action.equals(TASK_OVERVIEWBY_CALENDER)){
			long date = intent.getLongExtra(
					IN_CALENDAR_DATE, Global.INVALIDATE_DATE);
			if(date == Global.INVALIDATE_DATE){	
				setupScenarioList();
			}else{
				Time it = new Time();
				it.set(date);
				it.set(0,0,0,it.monthDay,it.month,it.year);
				long start = it.toMillis(false);
				it.set(59,59,23,it.monthDay,it.month,it.year);
				long end = it.toMillis(false);
				String where = " ((start_date BETWEEN "+start+" AND "+end+")" +
					" OR (end_date BETWEEN "+start+" AND "+end+")" +
					" OR (start_date < "+start+" AND end_date > "+end+")) ";
				whereClauseAddition = " AND "+where;
				mCalendarDate = date;
				flipToOverviewSurface();
				String sql = "SELECT scenario.* FROM (SELECT DISTINCT(scenario) AS scena from task WHERE "
					+where+")"+",scenario WHERE scena = scenario._id";
				setupScenarioListByFilter(sql, null);
			}
			
		}else if(action.equals(Intent.ACTION_MAIN)){
			setupScenarioList();
			SingularityApplication.instance().getNotifierBinder().bindNotifer();
		}else if(action.equals(TASK_OVERVIEW_ADD_TASK_DONE)){
			long in = intent.getLongExtra(IN_ADD_TASK_ID, Global.INVALIDATE_ID);
			if(in != Global.INVALIDATE_ID){
				if(taskIds == null){	
				}else{
					if(!taskIds.contains(in)){
						taskIds.add(in);
					}
				}		
			}
			setupScenarioList();
		}
	}	
	
	public void onAdd(View v){
		Intent intent = new Intent(this,CalendarActivity.class);
		intent.setAction(CalendarActivity.CALENDAR_WIZARD_NEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra(CalendarActivity.IN_SCENARIO_ID, mCurrentScenario);
		startActivityForResult(intent, OUT_ADD_TASK);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mCurrentScenario != Global.INVALIDATE_ID){
			if(mFloatView != null){
				mFloatView.setVisibility(View.VISIBLE);
			}
		}
		ListAdapter adapter = (ListAdapter) getListAdapter();
		adapter.notifyDataSetChanged();
		rollProverb();
		mCheckLBS.setChecked(SingularityApplication.instance().isLBSOn());
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mScenarioListAdapter.getCursor().close();
		System.gc();
	}

	private class OnClickTask implements OnClickListener{

		public void onClick(View v) {
			if(focusView != null){
				clearFocusView();
				return;}
				
			ViewHolder holder = (ViewHolder) v.getTag();
			Intent intent = new Intent(
					TaskOverViewActivity.this, TaskViewActivity.class);
			intent.setAction(TaskViewActivity.TASK_EDIT);
			//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra(TaskViewActivity.IN_TASK_ID,holder.taskId);
			startActivityForResult(intent, OUT_EDIT_TASK);
		}
		
	}
	
	private class OnLongClickTask implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			focusView(v);
			return true;
		}	
	}
	
	public void onDelete(View v){
		
		ViewHolder holder = (ViewHolder) focusView.getTag();
		AbstractTask task = (AbstractTask) EntityPool.instance().
			forId(holder.taskId, Task.TAG);
		if(mDeleteTaskDialog == null)
			setupDeleteTaskDialog();
		if(!task.attachment.isEmpty()){
			mDeleteTaskDialog.show();
		}else{
			task.delete();
			ListAdapter adapter = (ListAdapter) getListAdapter();
			taskIds.remove(holder.taskId);
			adapter.notifyDataSetChanged();
			mBtnDelete.setVisibility(View.GONE);
			clearFocusView();
		}
	}
	
	private class OnClickScanario implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(mFocusScenario != null){
				if(mFocusScenario == arg1){
					View ele = mFocusScenario.findViewById(R.id.taskoverview_scenario_list_ele);
					ele.setVisibility(View.VISIBLE);
					flipToSurface();
					return;
				}
				View ele = mFocusScenario.findViewById(R.id.taskoverview_scenario_list_ele);
				ele.setVisibility(View.VISIBLE);
			}
			mFocusScenario = arg1;
			long id = (Long) arg1.getTag();
			mCurrentScenario = id;
			String whereClause = " WHERE scenario = ?" + whereClauseAddition;
			String[] whereArgs = new String[]{Long.toString(id)};
			ArrayList<Long> temp = taskIds;
			taskIds = EntityPool.instance().
				getPrototype(Task.TAG).loadTable(whereClause,whereArgs);
			if(taskIds == null){
				temp.clear();
				taskIds = temp;}
			BaseAdapter contents = (BaseAdapter) getListAdapter();
			contents.notifyDataSetChanged();
			
			/*AbsListView.LayoutParams lp = (AbsListView.LayoutParams) arg1.getLayoutParams();
			lp.width = arg0.getWidth()*2;
			
			arg1.requestLayout();*/
			
			
			/*Bitmap bitmap = Util.getViewBitmap(arg1);
			if(mFloatView != null){
				mFloatView.remove();
			}
			mFloatView = new FloatView(TaskOverViewActivity.this, bitmap, 0, 0,
                0, 0, bitmap.getWidth(), bitmap.getHeight());
			int[] loc = new int[2];
	        arg1.getLocationOnScreen(loc);
			int x = loc[0];
			int y = loc[1];
			mFloatView.show(arg1.getWindowToken(),
					(int) (x - 10*mDisplayMetrics.density), y);*/
			View ele = arg1.findViewById(R.id.taskoverview_scenario_list_ele);
			ele.setVisibility(View.GONE);
			mMenu.setVisibility(View.VISIBLE);
			flipToPage();
			clearFocusView();
		}
		
	}
	
	private class OnScenarioContextMenu implements OnCreateContextMenuListener{

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.setHeaderTitle(
					getString(R.string.scenario_context_menu_title));
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			CursorAdapter adapter = (CursorAdapter)mScenairoList.getAdapter();
			
			Cursor cur = (Cursor) adapter.getItem(info.position);
			long id = cur.getLong(0);
			Scenario scenario = (Scenario) EntityPool.instance().
				forId(id, Scenario.TAG);
			
			String suffix = "'" + scenario.getName() + "'";
			if(id > 3){
				menu.add(0, CONTEXTMENU_DELETEITEM,0, 
					getString(R.string.scenario_context_menu_delete)+suffix);
			}
			
			menu.add(0, CONTEXTMENU_EDITITEM,1, 
					getString(R.string.scenario_context_menu_edit)+suffix);
			menu.add(0, CONTEXTMENU_ADDITEM,2,
					getString(R.string.scenario_context_menu_add));	
			menu.add(0, CONTEXTMENU_LOCATION,3,
					getString(R.string.scenario_context_menu_location));
			if(mFloatView != null){
				mFloatView.removeImmediate();}
		}	
		
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		/*2.x defect this is not called when back pressed*/
		if(mFloatView != null)
			mFloatView.retach();
		super.onContextMenuClosed(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = 
			(AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case CONTEXTMENU_DELETEITEM:
			if(mDeleteScenarioDialog == null){
				setupDeleteScanrioDialog();}
			mDeleteScenarioPosition = menuInfo.position;
			mDeleteScenarioDialog.show();

			break;
		case CONTEXTMENU_EDITITEM:
			CursorAdapter adapter2 = 
				(CursorAdapter)mScenairoList.getAdapter();
			
			Cursor cur2 = (Cursor) adapter2.getItem(menuInfo.position);
			if(mScenarioDialog == null)
				setupScanrioDialog();
			mScenarioDialogEdit.setText(cur2.getString(1));
			ColorDrawable drawable = new ColorDrawable(cur2.getInt(2));
			mScenarioDialogPicker.setImageDrawable(drawable);
			mPickedColor = cur2.getInt(2);
			mScenarioDialog.show();
			bEditScenario = true;
			mEditScenarioId = cur2.getLong(0);
			break;
		case CONTEXTMENU_ADDITEM:
			if(mScenarioDialog == null)
				setupScanrioDialog();
			bEditScenario = false;
			ColorDrawable drawable2 = new ColorDrawable(Color.GRAY);
			mScenarioDialogPicker.setImageDrawable(drawable2);
			mPickedColor = Color.GRAY;
			mScenarioDialog.show();
			break;
		case CONTEXTMENU_LOCATION:
			CursorAdapter adapter3 = 
				(CursorAdapter)mScenairoList.getAdapter();
			
			Cursor cur3 = (Cursor) adapter3.getItem(menuInfo.position);
			Intent intent = new Intent(this, LocationActivity.class);
			intent.setAction(LocationActivity.LOCATION_VIEW);
			intent.putExtra(LocationActivity.IN_PARENT_TAG, Scenario.TAG);
			intent.putExtra(LocationActivity.IN_PARENT_ID, cur3.getLong(0));
			startActivity(intent);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void colorChanged(int color) {
		mPickedColor = color;
		ColorDrawable drawable = new ColorDrawable(color);
		mScenarioDialogPicker.setImageDrawable(drawable);
	}
	
	private void setupScanrioDialog(){
		AlertDialog.Builder builder;
		
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(LAYOUT_INFLATER_SERVICE);
		View root = (ViewGroup) inflater.inflate(
				R.layout.taskoverview_scenario_dialog_layout,null);
		mScenarioDialogEdit = (EditText) root.findViewById(
				R.id.task_overview_scenario_dialog_edit);
		mScenarioDialogPicker = (ImageButton) root.findViewById(
				R.id.task_overview_scenario_dialog_color_picker);
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(root);
		mScenarioDialog = builder.create();
	}
	
	private void setupDeleteTaskDialog(){
		AlertDialog.Builder builder;
		
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(LAYOUT_INFLATER_SERVICE);
		View root = (ViewGroup) inflater.inflate(
				R.layout.taskoverview_delete_task_dialog_layout,null);
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(root);
		mDeleteTaskDialog = builder.create();
	}
	
	private void setupDeleteScanrioDialog(){
		AlertDialog.Builder builder;
		
		Context mContext = this;
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(LAYOUT_INFLATER_SERVICE);
		View root = (ViewGroup) inflater.inflate(
				R.layout.taskoverview_delete_scenario_dialog_layout,null);
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(root);
		mDeleteScenarioDialog = builder.create();
	}
	
	protected void focusView(View v){
		if(focusView != null){
			ViewHolder holder = (ViewHolder) focusView.getTag();
			holder.textName.setBackgroundColor(Color.TRANSPARENT);
		}
		focusView = v;
		ViewHolder holder = (ViewHolder) focusView.getTag();
		holder.textName.setBackgroundResource(R.drawable.text_background);
		mBtnDelete.setVisibility(View.VISIBLE);
		//mBookMark.setVisibility(View.INVISIBLE);
		mBtnAdd.setVisibility(View.INVISIBLE);
		focusId = holder.taskId;
	}
	
	protected void clearFocusView(){
		if(focusView != null){
			ViewHolder holder = (ViewHolder) focusView.getTag();
			holder.textName.setBackgroundColor(Color.TRANSPARENT);
			focusView = null;
			mBtnDelete.setVisibility(View.GONE);
			//mBookMark.setVisibility(View.VISIBLE);
			mBtnAdd.setVisibility(View.VISIBLE);
		}
	}

	public void onBtnDeleteRecord(View v){
		ViewHolder holder = (ViewHolder) focusView.getTag();
		Task task = (Task) EntityPool.instance().forId(holder.taskId, Task.TAG);
		task.deleteRecords();
		task.delete();
		ListAdapter adapter = (ListAdapter) getListAdapter();
		taskIds.remove(holder.taskId);
		adapter.notifyDataSetChanged();
		mDeleteTaskDialog.dismiss();
		mBtnDelete.setVisibility(View.GONE);
		clearFocusView();
	}
	
	public void onBtnDeleteTaskOnly(View v){
		ViewHolder holder = (ViewHolder) focusView.getTag();
		Task task = (Task) EntityPool.instance().forId(holder.taskId, Task.TAG);
		task.detachRecords();
		task.delete();
		ListAdapter adapter = (ListAdapter) getListAdapter();
		taskIds.remove(holder.taskId);
		adapter.notifyDataSetChanged();
		mDeleteTaskDialog.dismiss();
		mBtnDelete.setVisibility(View.GONE);
		clearFocusView();
	}
	
	public void onBtnDeleteTaskCancel(View v){
		mDeleteTaskDialog.dismiss();
	}
	
	public void onBtnDeleteScenario(View v){
		CursorAdapter adapter = (CursorAdapter) mScenairoList.getAdapter();

		Cursor cur = (Cursor) adapter.getItem(mDeleteScenarioPosition);
		long id = cur.getLong(0);
		String[] whereArgs = new String[] { Long.toString(id) };
		DBAdapter.instance().deleteEntry(Scenario.TAG, "_id=?", whereArgs);
		DBAdapter.instance().deleteEntry(Task.TAG, "scenario =?", whereArgs);
		if (mCurrentScenario == id) {
			taskIds.clear();
			flipToSurface();
		}else{}
		//Cursor newCur = DBAdapter.instance().retrieveAll(Scenario.TAG, null,
			//null);
		//adapter.changeCursor(newCur);
		adapter.getCursor().requery();
		adapter.notifyDataSetChanged();
		
		mDeleteScenarioDialog.dismiss();
		mCurrentScenario = Global.INVALIDATE_ID;
		mMenu.setVisibility(View.GONE);
		deleteLBSBundle(id, Scenario.TAG);
	}
	
	private void deleteLBSBundle(long parentId,String parentTag){
		String whereClause = "WHERE parent_id = ? AND parent_tag = ?";
	    String[] whereArgs = new String[]{Long.toString(parentId),parentTag};
	    ArrayList<Long> ids = EntityPool.instance().getPrototype(
	    			LBSBundle.TAG).loadTable(whereClause, whereArgs);
	    if(ids != null){
	    	for(Long id:ids){
		    	LBSBundle lbs = (LBSBundle) EntityPool.instance(
		    			).forId(id, LBSBundle.TAG);
		    	lbs.delete();
		    }
	    }
	}
	
	public void onBtnDeleteScenarioCancel(View v){
		mDeleteScenarioDialog.dismiss();
	}
	
	public void onBtnScenarioSave(View v){
		String name = mScenarioDialogEdit.getText().toString();
		Scenario scenario = null;
		if(name.equals("")){
			name = getText(R.string.scenario_tilte_default).toString();
		}
		if(bEditScenario){
			scenario = (Scenario) EntityPool.instance().
				forId(mEditScenarioId, Scenario.TAG);
			scenario.setParams(name, mPickedColor);
		}else{
			scenario = new Scenario(name, mPickedColor);}
		
		scenario.store();
		CursorAdapter adapter = (CursorAdapter) mScenairoList.getAdapter();
		//Cursor newCur = DBAdapter.instance().retrieveAll(Scenario.TAG, null,
				//null);
		//adapter.changeCursor(newCur);
		adapter.getCursor().requery();
		adapter.notifyDataSetChanged();
		mScenarioDialog.dismiss();
	}
	
	public void onBtnScenarioCancel(View v){
		mScenarioDialog.dismiss();
	}
	
	public void onBtnColorPicker(View v){
		ColorPickerDialog picker = new ColorPickerDialog(this, this, Color.BLUE);
		picker.show();
	}
	
	public void onBacktoSurface(View v){
		flipToSurface();
		whereClauseAddition="";
		setupScenarioList();
	}
	
	public void onBtnCalendar(View v){
		Intent intent = new Intent(TaskOverViewActivity.this,
				CalendarActivity.class);
		intent.setAction(CalendarActivity.CALENDAR_OVERVIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivityForResult(intent,OUT_CALENDAR_OVERVIEW);
	}
	
	public void onBtnMindFragment(View v){
		Intent intent = new Intent(TaskOverViewActivity.this,
						RecordOverViewActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.setAction(RecordOverViewActivity.RECORD_OVERVIEW);
		startActivity(intent);
		finish();
	}
	
	
	protected void flipToSurface(){
		mFlipper.setDisplayedChild(0);
		mMindCollects.setVisibility(View.VISIBLE);
		mBtnCalendar.setVisibility(View.VISIBLE);
		mReview.setVisibility(View.VISIBLE);
		mReviewCanvas.setVisibility(View.VISIBLE);
		HashMap<Long, Persisable> bundles = EntityPool.instance()
						.forTag(LBSBundle.TAG);
		if(!bundles.isEmpty()){
			mCheckLBS.setVisibility(View.VISIBLE);
			mMap.setVisibility(View.VISIBLE);
		}else{
			mCheckLBS.setVisibility(View.GONE);
			mMap.setVisibility(View.GONE);
		}
		if(mFocusScenario != null){
			View ele = mFocusScenario.findViewById(
					R.id.taskoverview_scenario_list_ele);
			ele.setVisibility(View.VISIBLE);
			mCurrentScenario = Global.INVALIDATE_ID;
			mFocusScenario = null;
		}
		if(mFloatView != null){
			mFloatView.remove();
			mFloatView = null;
		}
		rollProverb();
	}
	
	protected void flipToPage(){
		mFlipper.setDisplayedChild(1);
	}
	
	protected void flipToOverviewSurface(){
		mFlipper.setDisplayedChild(0);
		mMindCollects.setVisibility(View.GONE);
		mBtnCalendar.setVisibility(View.GONE);
		mReview.setVisibility(View.GONE);
		mReviewCanvas.setVisibility(View.GONE);
		mCheckLBS.setVisibility(View.GONE);
		mMap.setVisibility(View.GONE);
		
		if(mFocusScenario != null){
			View ele = mFocusScenario.findViewById(
					R.id.taskoverview_scenario_list_ele);
			ele.setVisibility(View.VISIBLE);
			mFocusScenario = null;
		}
		if(mFloatView != null){
			mFloatView.remove();
			mFloatView = null;
		}
		rollProverb();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case OUT_ADD_TASK:		
			break;
		default:
			break;
		}
	}
	
	protected void rollProverb(){
		mProverb.setText("\t"+mProverbs[mDice.nextInt(mProverbs.length)]);
		/*+Html.fromHtml("<strong>"+
				mProverbs[mDice.nextInt(mProverbs.length)]+"</strong>"));*/
		mHelper.setupReview();
	}
	
	public void onAbout(View v){
		Intent intent = new Intent(this,SingularityPreference.class);
		startActivity(intent);
	}

	public void onBtnCompass(View v){
		CheckBox check = (CheckBox) v;
		if(check.isChecked()){
			startLBSService();
		}else{
			stopLBSService();
		}
	}
	
	protected void startLBSService(){
		startService(new Intent(this,
					LocationService.class));
		SingularityApplication.instance().setLBSService(true);
	}
	
	protected void stopLBSService(){
		stopService(new Intent(this,
					LocationService.class));
		SingularityApplication.instance().setLBSService(false);
	}
	
	public void onBtnMap(View v){
		Intent intent = new Intent(this, LocationActivity.class);
		intent.setAction(LocationActivity.LOCATION_OVERVIEW);
		startActivity(intent);
	}
	
	protected void setUpTaskListByScenairo(long id){
		//mCurrentScenario = id;
		String whereClause = " WHERE scenario = ?";
		String[] whereArgs = new String[] { Long.toString(id) };
		ArrayList<Long> temp = taskIds;
		taskIds = EntityPool.instance().getPrototype(Task.TAG)
				.loadTable(whereClause, whereArgs);
		if (taskIds == null) {
			if(temp != null){
				temp.clear();
				taskIds = temp;
			}else{
				taskIds = new ArrayList<Long>();
			}
		}
	}
}
