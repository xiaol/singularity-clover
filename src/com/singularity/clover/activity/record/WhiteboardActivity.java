package com.singularity.clover.activity.record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.record.PictureRecord;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.util.BitmapToFile;
import com.singularity.clover.util.BitmapUtil;

import dalvik.system.VMRuntime;

public class WhiteboardActivity extends Activity implements Handler.Callback{
	public static final String WHITEBOARD_NEW = "com.singularity.activity.record.New";
	public static final String WHITEBOARD_EDIT = "com.singularity.activity.record.Edit";
	public static final String WHITEBOARD_ATTACH_DONE = "attach_done";
	
	public final static String WHITEBOARD_URI ="whiteboard_uri";
	public final static String RECORD_ID = "record_id";
	
	public final static float TARGET_HEAP_UTILIZATION = 0.5f;
	public final static int CWJ_HEAP_SIZE = 12* 1024* 1024 ; 
	
	 //挂接过程中可以可以直接创建新任务
	public static final String OUT_ATTACHTO_TASK = "attachto_task";
	public static final String OUT_ATTACHTO_PLAN = "attachto_plan";
	
	private WhiteboardView _surfaceView = null;
	private CheckBox _move,_preview;
	private RadioGroup toolsMenu = null;
	public final static String PREFIX_FILE_NAME = "whiteboard";
	private long saveId;
	private boolean bShow = true;
	private boolean bEdit = false;
	private View btnSave;
	private EditText textInput;
	
	private OnCheckChanged radioLisener = new OnCheckChanged();
	private DisplayMetrics mDisplayMetrics;
	
	public WhiteboardActivity() {
		super();
		this.saveId = IdGenerator.nextId(Record.TAG);
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
		VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
		
		setContentView(R.layout.whiteboard_layout);
		_surfaceView = (WhiteboardView) findViewById(R.id.whiteboard_canvas);
		_move = (CheckBox) findViewById(R.id.whiteboard_checkbox_move);
		_preview = (CheckBox) findViewById(R.id.whiteboard_checkbox_preview);
		toolsMenu = (RadioGroup) findViewById(R.id.whiteboard_tools_menu);
		toolsMenu.setOnCheckedChangeListener(radioLisener);
		btnSave = findViewById(R.id.whiteboard_button_save);
		textInput = (EditText) findViewById(R.id.whiteboard_edit_text);
		
		mDisplayMetrics = getResources().getDisplayMetrics();
	
		String action = getIntent().getAction();
		if(action.equals(WHITEBOARD_NEW)){
			bEdit = false;
		}else if(action.equals(WHITEBOARD_EDIT)){
			String strUri = getIntent().getStringExtra(WHITEBOARD_URI);
			saveId = getIntent().getLongExtra(RECORD_ID, Global.INVALIDATE_ID);
			if(!_surfaceView.setBitmap(strUri)){
				_surfaceView._thread.destroy();
				_surfaceView.getHolder().removeCallback(_surfaceView);
				finish();}
			bEdit = true;
		}
	}
	
	public void onSave(View v){
		boolean bSave = true;
		String state = Environment.getExternalStorageState();		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		}else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			bSave = false;
		}else{
			bSave = false;}
		
		if(!bSave){
			Activity currentActivity = this;
			final AlertDialog alertDialog = new AlertDialog.
										Builder(currentActivity).create();
		
			String title = getResources().getString(R.string.dialog_sdcard_title);
			String message = getResources().getString(R.string.dialog_sdcard_message);
			String ok = getResources().getString(R.string.ok_button);
			alertDialog.setTitle(title);
			alertDialog
					.setMessage(message);
		
			alertDialog.setButton(ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alertDialog.show();
			return;
		}
			
		Bitmap bitmap = _surfaceView.getBitmap();
		if(bEdit){
			PictureRecord pic = (PictureRecord) EntityPool.instance().
				forId(saveId, Record.TAG);
			pic.setBitmap(BitmapUtil.getScaledBitmap(this,bitmap));}
		
		
		File file = new File(Global.APP_FILE_PATH);
		if (!file.exists()) {
			if (!file.mkdirs()) {
			}
		}
		File file2 = new File(file,PREFIX_FILE_NAME + saveId +".png");
		FileOutputStream out;
		try {
			out = new FileOutputStream(file2);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			Uri uri = Uri.fromFile(file2);
			out.flush();out.close();
			if(!bEdit){
				PictureRecord pic = new PictureRecord("name", 0,uri.toString() );
				pic.store();
				pic.setBitmap(BitmapUtil.getScaledBitmap(this,bitmap));
				Intent intent = new Intent();
				intent.putExtra(RecordOverViewActivity.RESULT_RECORD_ID, pic.getId());
				setResult(RESULT_OK, intent);
			}else{
				setResult(RESULT_OK);
			}
			bitmap.recycle();
			bitmap = null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setResult(RESULT_CANCELED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setResult(RESULT_CANCELED);
		}
		
        /*Handler saveHandler = new Handler(this);  bitmap内存溢出
        Bitmap[] bitmaps = new Bitmap[]{bitmap};
        BitmapToFile aysnc = new BitmapToFile(saveHandler, PREFIX_FILE_NAME, saveId);
        aysnc.addHandler(RecordOverViewActivity._handler);
        aysnc.execute(bitmaps);*/
        finish();
	}

	public void onMove(View v){
		if(_preview.isChecked()){
			_preview.toggle();
			_surfaceView.setPreview(false);
			btnSave.setVisibility(View.VISIBLE);
		}
		_surfaceView.setMoving(_move.isChecked());
	}
	
	public void onPreview(View v){	
		if(_move.isChecked()){
			_move.toggle();
			_surfaceView.setMoving(false);
		}
		_surfaceView.setPreview(_preview.isChecked());
		if(_preview.isChecked()){
			btnSave.setVisibility(View.GONE);
		}else{
			btnSave.setVisibility(View.VISIBLE);
		}
	}

	public void onDelete(View v){
		Record record = (Record)EntityPool.instance().
							forId(saveId,PictureRecord.TAG);
		record.delete();
		syncronizeOverView(RecordOverViewActivity.MSG_DELETE, saveId);
		finish();
	}
	
	public void onToolsHandle(View v){
		switch(toolsMenu.getVisibility()){
		case View.VISIBLE:
			toolsMenu.setVisibility(View.GONE);
			break;
		case View.GONE:
			toolsMenu.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	public void onEditConfirm(View v){
		_surfaceView.setConfirm(true,textInput.getText().toString());
	}
	
	public void onEditCancel(View v){
		_surfaceView.setConfirm(false,null);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		bShow = false;
	}
	
	@Override
	protected void onRestart() {
		bShow = true;
		super.onRestart();
	}

	@Override
	protected void onResume() {
		bShow = true;
		super.onResume();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		_surfaceView.recycle();
		_surfaceView = null;
		System.gc();
	}

	@Override
	public void onBackPressed() {
		
		final WhiteboardActivity currentActivity = this;
		final AlertDialog alertDialog = new AlertDialog.
										Builder(currentActivity).create();
		
		String title = getResources().getString(R.string.dialog_save_title);
		String message = getResources().getString(R.string.dialog_save_message);
		String yes = getResources().getString(R.string.yes_button);
		String no = getResources().getString(R.string.no_button);
		String cancel = getResources().getString(R.string.cancel_button);;
			
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				currentActivity.onSave(null);
				return;
			}
		});
		alertDialog.setButton3(no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(bEdit){
					setResult(RESULT_OK);
				}else{
					setResult(RESULT_CANCELED);
				}
				currentActivity.onSuperBackPressed();		
				return;
			}
		});
		
		alertDialog.setButton2(cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alertDialog.show();
	}

	public void onSuperBackPressed(){
		super.onBackPressed();
		finish();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch(toolsMenu.getVisibility()){
		case View.VISIBLE:
			toolsMenu.setVisibility(View.GONE);
			break;
		case View.GONE:
			toolsMenu.setVisibility(View.VISIBLE);
			break;
		}
		return false;
	}


	@Override
	public boolean handleMessage(Message msg) {
		Activity currentActivity = this;
		final AlertDialog alertDialog = new AlertDialog.
										Builder(currentActivity).create();
		String ok = getResources().getString(R.string.ok_button);
		if (msg.what == BitmapToFile.MSG_SAVE_DONE) {
			String title = getResources().getString(R.string.dialog_saved_title);
			String message = getResources().getString(R.string.dialog_saved_message);
			alertDialog.setTitle(title);
			alertDialog.setMessage(message);
				
		} else if(msg.what == BitmapToFile.MSG_SAVE_FAILED){
			String title = getResources().getString(R.string.dialog_sdcard_title);
			String message = getResources().getString(R.string.dialog_sdcard_message);
			
			alertDialog.setTitle(title);
			alertDialog
					.setMessage(message);
		}
		alertDialog.setButton(ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		
		if(bShow)
			alertDialog.show();
		return true;
		
	}
	
	
	private void syncronizeOverView(int code,long id){
		Message updateMsg = RecordOverViewActivity._handler.obtainMessage();
		updateMsg.what = code;
		Bundle data = new Bundle();
		data.putLong("id", id);
		updateMsg.setData(data);
		RecordOverViewActivity._handler.sendMessage(updateMsg);
	}
	
	public class OnCheckChanged implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			_surfaceView.setInputText(false);
			switch(arg1){
			case R.id.whiteboard_check_text:
				_surfaceView.setInputText(true);
				break;
			case R.id.whiteboard_check_pen_black:
				_surfaceView.configPaint(Color.BLACK,(int) (5*mDisplayMetrics.density));
				break;
			case R.id.whiteboard_check_pen_red:
				_surfaceView.configPaint(Color.RED,(int) (5*mDisplayMetrics.density));
				break;
			case R.id.whiteboard_check_pen_green:
				_surfaceView.configPaint(getResources().getColor(R.color.dark_green_2),
						(int) (5*mDisplayMetrics.density));
				break;
			case R.id.whiteboard_check_eraser:
				_surfaceView.configPaint(
						getResources().getColor(R.color.whiteboard_bg),
						(int) (15*mDisplayMetrics.density));
				break;
			}		
		}		
	}
}
