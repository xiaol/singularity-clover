package com.singularity.clover.activity.record;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.singularity.clover.R;
import com.singularity.clover.Global;
import com.singularity.clover.activity.entity.TaskViewActivity;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.record.Record;
import com.singularity.clover.entity.record.TextRecord;

public class NoteActivity extends Activity {
	public final static String NOTE_NEW = "Singularity.note.new";
	public final static String NOTE_EDIT = "Singularity.note.edit";
	
	public final static String NOTE_EDIT_ID = "Note.edit.id";
	
	public ArrayList<LinearLayout> horizontalLayouts = 
									new ArrayList<LinearLayout>();
	private LinearLayout verticalLayout = null;
	private ArrayList<Note> visibleNotes = new ArrayList<Note>();
	private int mHeight = -1;
	private NoteActivityHelper mHelper;
	private Note mRoot;
	
	
	public class Note {
	    int x, y;
		float weight;
		TextRecord rec = null;
		ArrayList<Note> notes = new ArrayList<Note>();
		boolean bVisible = false;

		public Note() {
			x = y = 0;weight = 1f;
			rec = new TextRecord();
		}
		
		public Note(long id){
			rec = (TextRecord) EntityPool.instance().forId(id, Record.TAG);
			x = y = 0;weight = 1f;
		}

		public void updateView() {
			View view = horizontalLayouts.get(y).getChildAt(x);
			if (view != null) {
				LinearLayout.LayoutParams lParams = 
					(LinearLayout.LayoutParams) view.getLayoutParams();
				lParams.weight = weight;
				view.setBackgroundColor(bVisible?mHelper.mColorEnabled:mHelper.mColorDisabled);
				CheckBox checkExpend = (CheckBox) view.
					findViewById(NoteActivityHelper.ID_CHECK_EXPEND);
				checkExpend.setEnabled(!notes.isEmpty());
				checkExpend.setChecked(bVisible && !notes.isEmpty());
			}
		}
		
		public void store(){	
			rec.store();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_layout);
		mHelper = new NoteActivityHelper(this);
		verticalLayout = (LinearLayout) findViewById(
			R.id.note_vertical_layout);
		
		String action = getIntent().getAction();
		if(action.equals(NOTE_NEW)){
			Note note = new Note(); 
			View v = mHelper.initView(note);
			LinearLayout list = getList(0); list.addView(v);
			setVisible(note);
			note.store();
			mRoot = note;
			Intent intent = getIntent();
			intent.setAction(NOTE_EDIT);
			intent.putExtra(NOTE_EDIT_ID, note.rec.getId());
			setIntent(intent);
		}else if(action.equals(NOTE_EDIT)){
			long rootId = getIntent().getLongExtra(
					NOTE_EDIT_ID, Global.INVALIDATE_ID);
			Note root = build(rootId);
			View v = mHelper.initView(root);
			LinearLayout list = getList(0); list.addView(v);
			setVisible(root);
			if(rootId != Global.INVALIDATE_ID)
				layoutNotes(root,true);
			mRoot = root;
		}
	}
	

	@Override
	protected void onPause() {
		super.onPause();
	}


	@Override
	public void onBackPressed() {
		boolean bOK = true;
		if(mRoot.rec.name == null){
			if(mRoot.rec.getRecordChildId() == Global.INVALIDATE_ID
					&& mRoot.rec.getRecordNextId() == Global.INVALIDATE_ID){
				mRoot.rec.delete();
				bOK =false;
			}
		}else if(mRoot.rec.name.equals("")){
			if (mRoot.rec.getRecordChildId() == Global.INVALIDATE_ID
					&& mRoot.rec.getRecordNextId() == Global.INVALIDATE_ID) {
				mRoot.rec.delete();
				bOK = false;
			}	
		}
		if(bOK){
			Intent intent = new Intent();
			intent.putExtra(TaskViewActivity.RESULT_RECORD_ID, mRoot.rec.getId());
			intent.putExtra(RecordOverViewActivity.RESULT_RECORD_ID, mRoot.rec.getId());
			setResult(RESULT_OK, intent);
		}else{}
		finish();
		//super.onBackPressed();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHelper = null;
	}


	public void setVisible(Note note) {
		if (note.bVisible)
			return;
		if (note.y < visibleNotes.size()) {
			Note old = visibleNotes.get(note.y);
			old.bVisible = false;old.weight = 1f;
			old.updateView();
			visibleNotes.set(note.y, note);
		} else
			visibleNotes.add(note);
		note.bVisible = true;note.weight = 0.5f;
		note.updateView();
	}

	private LinearLayout initList() {
		LinearLayout hLayout;
		hLayout = new LinearLayout(this);
		hLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lParams = new LinearLayout.
			LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		hLayout.setLayoutParams(lParams);
		lParams.weight = 1f;
		return hLayout;
	}


	// 添加水平线性布局
	protected LinearLayout getList(int yPos) {
		LinearLayout hLayout;
		if (yPos <= mHeight) {
			hLayout = horizontalLayouts.get(yPos);
			hLayout.removeAllViews();
		} else if (yPos <= horizontalLayouts.size() - 1) {
			mHeight++;
			hLayout = horizontalLayouts.get(yPos);
			verticalLayout.addView(hLayout, yPos);
			hLayout.removeAllViews();
		} else {
			mHeight++;
			hLayout = initList();
			horizontalLayouts.add(hLayout);
			verticalLayout.addView(hLayout, yPos);	
		}
		return hLayout;
	}

	public void add(Note parent) {
		Note note = new Note();
		if (parent.y == mHeight) {
			note.x = 0;
			note.y = parent.y + 1; // 子节点Y轴坐标递增
			note.rec.setRecordParentId(parent.rec.getId());
			parent.rec.setRecordChildId(note.rec.getId());
		} else/* (parentNode.getLayoutPosY() < mListHeight) */{
			note.x = parent.notes.size();
			note.y = parent.y + 1;
			note.rec.setRecordPrevId(parent.notes.get(note.x-1).rec.getId());
			parent.notes.get(note.x-1).rec.setRecordNextId(note.rec.getId());
		}
		parent.notes.add(note);
		parent.updateView();
		note.store();
		layoutNotes(parent, true);
	}
	
	
	public Note build(long rootId){
		Note root = new Note(rootId);
		Note it = root;
		
		LinkedList<Note> stack = new LinkedList<Note>();
		do{
			while(it.rec.getRecordChildId() != Global.INVALIDATE_ID){
				stack.addFirst(it);
				Note note = new Note(it.rec.getRecordChildId());
				note.x = 0;note.y = it.y + 1;
				it = note;
			}
			if(stack.peek() == null)
				break;
			stack.peek().notes.add(it);
			if(it.rec.getRecordNextId() != Global.INVALIDATE_ID){
				Note note = new Note(it.rec.getRecordNextId());
				note.x = it.x + 1;note.y = it.y;
				it = note;
			}else{
				do{
					it = stack.poll();
					if(stack.peek() == null)
						return root;
					stack.peek().notes.add(it);
					if(it.rec.getRecordNextId() != Global.INVALIDATE_ID){
						Note note = new Note(it.rec.getRecordNextId());
						note.x = it.x + 1;note.y = it.y;
						it = note;
						break;
					}else{}
				}while(it.rec.getRecordNextId() == Global.INVALIDATE_ID);
			}
		}while(!stack.isEmpty());
		
		return root;
	}

	public boolean layoutNotes(Note parent,boolean bExpend) {
		int yPos = parent.y;
		boolean bInvalidate = false;
		// visibleNotes状态理论过程 总高度变短->增加高度->清除多余高度
		while (!parent.notes.isEmpty() && bExpend) {
			LinearLayout hLinearLayout = getList(yPos + 1);
			for (Note entry:parent.notes) {
				View view = mHelper.initView(entry);
				hLinearLayout.addView(view);}
				
			parent = parent.notes.get(0);
			setVisible(parent);
			bInvalidate = true;yPos++;
		}
		while (yPos < mHeight) {
			Note old = visibleNotes.get(mHeight);
			old.bVisible = false;old.weight = 1;
			old.updateView();
			
			visibleNotes.remove(mHeight);
			verticalLayout.removeViewAt(mHeight);
			mHeight--;}
		return bInvalidate;
	}

	
}
