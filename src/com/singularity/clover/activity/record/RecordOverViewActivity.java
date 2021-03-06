package com.singularity.clover.activity.record;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.singularity.clover.Global;
import com.singularity.clover.R;
import com.singularity.clover.activity.entity.TaskOverViewActivity;
import com.singularity.clover.activity.entity.TaskViewActivity;
import com.singularity.clover.activity.record.RecordOverViewActivity.ImageAdapter.ViewHolder;
import com.singularity.clover.database.IdGenerator;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.record.Hierarchy;
import com.singularity.clover.entity.record.Hierarchy.Node;
import com.singularity.clover.entity.record.PictureRecord;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.record.TextRecord;
import com.singularity.clover.util.BitmapToFile;
import com.singularity.clover.util.BitmapUtil;
import com.singularity.clover.view.ActionItem;
import com.singularity.clover.view.QuickAction;

import dalvik.system.VMRuntime;

public class RecordOverViewActivity extends Activity implements Handler.Callback{
	
	public final static String RECORD_OVERVIEW = "singularity.record.overview";
	public final static String RECORD_PICK = "singularity.record.pick";
	
	public final static int OUT_NEW_WHITEBOARD = 0;
	public final static int OUT_EDIT_WHITEBOARD = 1;
	public final static int OUT_EIDT_TEXT = 6;
	public final static int OUT_NEW_PHOTO = 2;
	public final static int OUT_NEW_TEXT = 3;
	public final static int OUT_NEW_VOICE = 4;
	public final static int OUT_PRIMARY_VIEW = 5;
	public final static int OUT_TAKE_PHOTO = 7;
	public final static int OUT_PICK_PHOTO = 8;
		
	public static final String RESULT_RECORD_ID = "overview.result.record.id";
	
	
	public final static int MSG_DELETE = 255;
	
	protected int mState = STATE_IDLE;
	protected final static int STATE_IDLE = 0;
	protected final static int STATE_PICK = 1;
	
	public static Handler _handler;
	private ArrayList<Long> ids;
	private LinkedList<Node> nodes;
	private ImageAdapter _adapter;
	private Hierarchy mHierarchyCtrl;
	
	protected QuickAction mQuickAction;
	private OnImageViewClick onImageViewClick = new OnImageViewClick();
	private OnImageLongClick mOnImageLongClick = new OnImageLongClick();
	private OnDeleteItemClick mOnDeleteItemClick = new OnDeleteItemClick();
	private Uri mUri;
	
	public class ImageAdapter extends BaseAdapter {
		private Context _context;
			
		public ImageAdapter(Context _context) {
			super();
			this._context = _context;
		}

		@Override
		public int getCount() {
			return nodes.size();
		}

		@Override
		public Object getItem(int pos) {
			return nodes.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			ImageView imageView;
			ViewHolder holder;
			if (convertView == null) { 
				imageView = new ImageView(_context);
				DisplayMetrics displayMetrics = _context.getResources().getDisplayMetrics();
				int threshold = (int) (displayMetrics.density*BitmapUtil.IMAGE_PREVIEW_SIZE);
				imageView.setLayoutParams(new GridView.LayoutParams(
						LayoutParams.FILL_PARENT,threshold));
				imageView.setScaleType(ImageView.ScaleType.CENTER);
				imageView.setOnClickListener(onImageViewClick);
				imageView.setOnLongClickListener(mOnImageLongClick);
				holder = new ViewHolder();
			} else {
				imageView = (ImageView) convertView;
				holder = (ViewHolder) imageView.getTag();
			}
			Record record = null;	
			
			holder.id = nodes.get(position).getId();
			record = (Record)EntityPool.instance().
						forId(holder.id,Record.TAG);
			holder.content = record.getContent();
			holder.tag = record.getTAG();
			imageView.setTag(holder);
			imageView.setImageBitmap(record.convertoBitmap(_context));
			return imageView;
		}
		
		public class ViewHolder{
			long id;
			String tag;
			String content;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VMRuntime.getRuntime().setTargetHeapUtilization(WhiteboardActivity.TARGET_HEAP_UTILIZATION);
		VMRuntime.getRuntime().setMinimumHeapSize(WhiteboardActivity.CWJ_HEAP_SIZE);
	    setContentView(R.layout.recordoverview_layout);
	    _handler = new Handler(this);
	    /*ids是被清空的集合*/
	    ids = EntityPool.instance().
	    	getPrototype(Record.TAG).loadTable(null,null);
	    mHierarchyCtrl = new Hierarchy();
	    nodes = mHierarchyCtrl.buildHierarchy(ids);
	    
	    Iterator<Node> it = nodes.iterator(); 
	    while(it.hasNext()){
	    	Record record = (Record) EntityPool.instance()
	    		.forId(it.next().getId(), Record.TAG);
	    	boolean bEmpty = true;
	    	for(Entry<String, ArrayList<Long>> entry:record.getParent()){
	    		bEmpty = bEmpty && entry.getValue().isEmpty();
	    	}
	    	if(!bEmpty){
	    		it.remove();}
	    }
	    GridView gridview = (GridView) findViewById(R.id.record_gridview);
	    _adapter = new ImageAdapter(this);
	    gridview.setAdapter(_adapter);
	    
	    String action = getIntent().getAction();
	    if(action.equals(RECORD_OVERVIEW)){
	    	
	    }else if(action.equals(RECORD_PICK)){
	    	mState = STATE_PICK;
	    }
	}
	
	
	@Override
	public void onBackPressed() {
		switch(mState){
		case STATE_IDLE:
			Intent intent = new Intent(this,TaskOverViewActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case STATE_PICK:
			super.onBackPressed();
			break;
		default:
				break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		String action = getIntent().getAction();
	    if(action.equals(RECORD_OVERVIEW)){
	    	
	    }else if(action.equals(RECORD_PICK)){
	    	mState = STATE_PICK;
	    }
	}


	public void onNewWhiteboard(View v){
		Intent intent = new Intent(this,WhiteboardActivity.class);
		intent.setAction(WhiteboardActivity.WHITEBOARD_NEW);
		startActivityForResult(intent, OUT_NEW_WHITEBOARD);
	}
	
	public void onNewNote(View v){
		Intent intent = new Intent(this,NoteActivity.class);
		intent.setAction(NoteActivity.NOTE_NEW);
		startActivityForResult(intent, OUT_NEW_TEXT);
	}
	
	public void onTakePhoto(View v){
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
		File file = new File(Global.APP_FILE_PATH);
		if (!file.exists()) {
			if (!file.mkdirs()) {
			}
		}
		long suffix = IdGenerator.nextId(Record.TAG);
		File photo = new File(file,WhiteboardActivity.PREFIX_FILE_NAME+suffix+".png");
		mUri = Uri.fromFile(photo);
		/*Camera camera = Camera.open();
		Camera.Parameters param = camera.getParameters();
		List<Camera.Size> supportSizes = param.getSupportedPictureSizes();
		DisplayMetrics dm = getResources().getDisplayMetrics();
		param.setPictureSize(dm.widthPixels, dm.heightPixels);*/
		BitmapUtil.takePhoto(this,OUT_TAKE_PHOTO,mUri);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		for(Node node:nodes){
			Record record= (Record) EntityPool.
				instance().forId(node.getId(), Record.TAG);
			if(record != null){
				record.recycle();}
			}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OUT_NEW_WHITEBOARD:
			if(resultCode == RESULT_OK){
				if(data != null){
					long id = data.getLongExtra(RESULT_RECORD_ID, Global.INVALIDATE_ID);
					if(id != Global.INVALIDATE_ID){
						nodes.add(mHierarchyCtrl.new Node(id));
						_adapter.notifyDataSetChanged();
					}
				}else{
					Toast.makeText(this, getText(R.string.record_error), Toast.LENGTH_LONG).show();
				}
			}
			break;
		case OUT_EDIT_WHITEBOARD:
			if(resultCode == RESULT_OK){
			}else if(resultCode == RESULT_CANCELED){
				Toast toast = Toast.makeText(this, 
						getText(R.string.memory_low), Toast.LENGTH_SHORT);
				toast.show();
			}
			break;
		case OUT_NEW_TEXT:
			if(resultCode == RESULT_OK){
				long id = data.getLongExtra(RESULT_RECORD_ID, Global.INVALIDATE_ID);
				if(id != Global.INVALIDATE_ID){
					nodes.add(mHierarchyCtrl.new Node(id));
					_adapter.notifyDataSetChanged();
				}
			}
			break;
		case OUT_EIDT_TEXT:
			if(resultCode == RESULT_OK){
				_adapter.notifyDataSetChanged();
			}else{
			}
			break;
		case OUT_TAKE_PHOTO:
			if(resultCode == RESULT_OK){
				//Time now = new Time();
				//now.setToNow();
				if(mUri == null){
					File file = new File(Global.APP_FILE_PATH);
					if (!file.exists()) {
						if (!file.mkdirs()) {
						}
					}
					long suffix = IdGenerator.nextId(Record.TAG);
					File photo = new File(file,WhiteboardActivity.PREFIX_FILE_NAME+suffix+".png");
					mUri = Uri.fromFile(photo);
				}
				PictureRecord pic = new PictureRecord(
						"name", 0, mUri.toString());
				pic.store();
				/*Bitmap resized = BitmapUtil.resizePhoto(this, mUri);
				if(resized != null){
					File file = new File(mUri.getPath());
					FileOutputStream out;
					try {
						out = new FileOutputStream(file);
						resized.compress(Bitmap.CompressFormat.PNG, 100, out);;
						out.flush();out.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Bitmap small = BitmapUtil.getScaledBitmap(this, resized);
					pic.setBitmap(small);
				}*/
				nodes.add(mHierarchyCtrl.new Node(pic.getId()));
				_adapter.notifyDataSetChanged();
			}else{
				
			}
			break;
		default:
			break;
		}
		
		
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what){
		case BitmapToFile.MSG_SAVE_DONE:
			long id = msg.getData().getLong("id");
			for(Node entry:nodes){
				if(entry.getId() == id){
					_adapter.notifyDataSetChanged();
					return true;}
			}
			nodes.add(mHierarchyCtrl.new Node(msg.getData().getLong("id")));
			_adapter.notifyDataSetChanged();
			break;
		case MSG_DELETE:
			long id2 = msg.getData().getLong("id");
			for(Node node:nodes){
				if(node.getId() == id2){
					nodes.remove(node);break;}
			}
			_adapter.notifyDataSetChanged();
			break;
		}
		
		return true;
	}
	
	private class OnImageViewClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			if(mState == STATE_PICK){
				Intent intent = new Intent();
				intent.putExtra(TaskViewActivity.RESULT_RECORD_ID, holder.id);
				setResult(RESULT_OK, intent);
				for(Node node:nodes){
					Record record= (Record) EntityPool.
						instance().forId(node.getId(), Record.TAG);
						record.recycle();
					} /*另一边的OnActivityResult先于这边的onDestroy调用*/
				finish();
				return;}
			if(holder.tag.equals(PictureRecord.TAG)){
				Intent intent = new Intent(RecordOverViewActivity.this,
						WhiteboardActivity.class);
				intent.setAction(WhiteboardActivity.WHITEBOARD_EDIT);
				intent.putExtra(WhiteboardActivity.WHITEBOARD_URI, holder.content);
				intent.putExtra(WhiteboardActivity.RECORD_ID, holder.id);
				startActivityForResult(intent, OUT_EDIT_WHITEBOARD);
			}else if(holder.tag.equals(TextRecord.TAG)){
				Intent intent = new Intent(RecordOverViewActivity.this,
						NoteActivity.class);
				intent.setAction(NoteActivity.NOTE_EDIT);
				intent.putExtra(NoteActivity.NOTE_EDIT_ID, holder.id);
				startActivityForResult(intent, OUT_EIDT_TEXT);
			}
		}	
	}
	
	private class OnImageLongClick implements View.OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			mQuickAction = new QuickAction(v);
			ActionItem deleteItem = new ActionItem();
			Drawable dr = getResources().getDrawable(
					R.drawable.action_item_delete);
			deleteItem.setIcon(dr);
			deleteItem.setOnClickListener(mOnDeleteItemClick);
			mQuickAction.addActionItem(deleteItem);
			mQuickAction.show();
			return true;
		}	
	}
	
	private class OnDeleteItemClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			View deleteView = mQuickAction.getAnchor();
			ViewHolder holder = (ViewHolder) deleteView.getTag();
			Record record = (Record)EntityPool.instance().
							forId(holder.id,Record.TAG);
			record.delete();
			
			for(Node node:nodes){
				if(node.getId() == holder.id){
					nodes.remove(node);break;}
			}
			_adapter.notifyDataSetChanged();
			mQuickAction.dismiss();
		}
		
	}
}
