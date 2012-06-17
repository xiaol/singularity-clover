package com.singularity.clover.activity.record;

import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.singularity.clover.R;
import com.singularity.clover.activity.record.NoteActivity.Note;


public class NoteActivityHelper {
	public static final int ID_TEXTVIEW = 0;
	public static final int ID_CHECK_EXPEND = 1;
	public static final int ID_BUTTON_ADDSUBNOTE = 2;
	public static final int ID_EDITTEXT = 3;
	public static final int ID_EDIT_CONFIRM = 4;
	public static final int ID_EDIT_CANCEL = 5;
	
	private NoteActivity activity;
	
	private OnNoteClick onNoteClick;
	private OnAddNote onAddNote;
	private OnCheckExpend onCheckExpend;
	private OnNoteLongClick onNoteLongClick;
	private OnBtnConfirm onBtnConfirm;
	
	protected int mColorEnabled,mColorDisabled;
	
	public NoteActivityHelper(NoteActivity activity){
		this.activity = activity;
		onNoteClick = new OnNoteClick();
		onAddNote = new OnAddNote();
		onCheckExpend = new OnCheckExpend();
		onNoteLongClick = new OnNoteLongClick();
		onBtnConfirm = new OnBtnConfirm();
		
		mColorEnabled = activity.getResources().getColor(R.color.sky_dark_blue);
		mColorDisabled = activity.getResources().getColor(R.color.note_gray);
		}
	
	public View initView(Note note){
		FrameLayout view = new FrameLayout(activity);
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
			LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lParams.weight = note.weight;
		lParams.setMargins(3, 3, 3, 3);
		view.setBackgroundColor(note.bVisible?mColorEnabled:mColorDisabled);
		view.setTag(note);
		
		FrameLayout.LayoutParams fParams0 = new FrameLayout.LayoutParams(
			LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.CENTER);
		EditText editText = new EditText(activity);
		editText.setLayoutParams(fParams0);
		editText.setId(ID_EDITTEXT);
		editText.setVisibility(View.GONE);
		editText.setText(note.rec.getName());
		editText.setGravity(Gravity.TOP);
		
		Button btnConfirm = new Button(activity);
		FrameLayout.LayoutParams fParams4 = new FrameLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.BOTTOM|Gravity.RIGHT);
		btnConfirm.setId(ID_EDIT_CONFIRM);
		btnConfirm.setLayoutParams(fParams4);
		btnConfirm.setVisibility(View.GONE);
		btnConfirm.setOnClickListener(onBtnConfirm);
		btnConfirm.setText("Confirm");
		
		FrameLayout.LayoutParams fParams1 = new FrameLayout.LayoutParams(
			LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.CENTER);
		TextView textView = new TextView(activity);
		//textView.setText("Note("+note.x + ","+note.y+")");
		textView.setGravity(Gravity.CENTER);
		textView.setLayoutParams(fParams1);
		textView.setId(ID_TEXTVIEW);
		textView.setText(note.rec.getName());
		textView.setEllipsize(TruncateAt.MARQUEE);
		textView.setMaxLines(4);
		textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		textView.setHint(activity.getResources().getString(R.string.note_hint));
		
		FrameLayout.LayoutParams fParams2 = new FrameLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.BOTTOM|Gravity.LEFT);
		CheckBox checkExpend = new CheckBox(activity);
		checkExpend.setId(ID_CHECK_EXPEND);
		checkExpend.setLayoutParams(fParams2);
		checkExpend.setOnClickListener(onCheckExpend);
		if(note.notes.isEmpty()){
			checkExpend.setEnabled(false);
		}else{
			checkExpend.setEnabled(true);}
		checkExpend.setButtonDrawable(R.drawable.note_check_expend_bg);
		
		FrameLayout.LayoutParams fParams3 = new FrameLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.TOP|Gravity.RIGHT);
		ImageButton btnAdd = new ImageButton(activity);
		btnAdd.setId(ID_BUTTON_ADDSUBNOTE);
		btnAdd.setLayoutParams(fParams3);
		btnAdd.setOnClickListener(onAddNote);
		btnAdd.setBackgroundResource(R.drawable.note_button_add_bg);
		
		view.addView(editText);
		view.addView(btnConfirm);
		view.addView(textView);
		view.addView(checkExpend);
		view.addView(btnAdd);
		view.setLayoutParams(lParams);		
		view.setOnClickListener(onNoteClick);
		view.setOnLongClickListener(onNoteLongClick);
		return view;
	}
	
	public class OnNoteClick implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Note note = (Note) v.getTag();
			if (note.bVisible) {
			} else {
				activity.setVisible(note);
				activity.layoutNotes(note, true);
				activity.horizontalLayouts.get(note.y).requestLayout();
				activity.horizontalLayouts.get(note.y).invalidate();
			}
		}
	}

	public class OnNoteLongClick implements View.OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			Note note = (Note) v.getTag();
			if (!note.bVisible) {
				return false;
			} else {
				v.findViewById(NoteActivityHelper.
						ID_EDITTEXT).setVisibility(View.VISIBLE);
				v.findViewById(NoteActivityHelper.
						ID_EDIT_CONFIRM).setVisibility(View.VISIBLE);
				v.findViewById(NoteActivityHelper.
						ID_BUTTON_ADDSUBNOTE).setVisibility(View.GONE);
				ViewGroup parent = (ViewGroup) v.getParent();
				LinearLayout.LayoutParams lp = 
					(android.widget.LinearLayout.LayoutParams) parent.getLayoutParams();
				lp.weight = 2f;
				LinearLayout.LayoutParams lp2 = 
					(android.widget.LinearLayout.LayoutParams) v.getLayoutParams();
				lp2.weight = 0.3f;
				parent.getParent().requestLayout();
				return true;
			}
		}	
	}
	
	public class OnAddNote implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Note note = (Note) ((FrameLayout)v.getParent()).getTag();
			if (note.bVisible)
				activity.add(note);
		}
	}
	
	public class OnCheckExpend implements OnClickListener{

		@Override
		public void onClick(View v) {
			CheckBox check = (CheckBox) v;
			Note note = (Note) ((FrameLayout)v.getParent()).getTag();
			activity.layoutNotes(note,check.isChecked());
		}
		
	}
	
	public class OnBtnConfirm implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			FrameLayout views = ((FrameLayout)v.getParent());
			Note note = (Note) views.getTag();
			EditText edit = (EditText) views.findViewById(ID_EDITTEXT);
			edit.setVisibility(View.GONE);
			views.findViewById(ID_EDIT_CONFIRM).setVisibility(View.GONE);
			views.findViewById(NoteActivityHelper.
					ID_BUTTON_ADDSUBNOTE).setVisibility(View.VISIBLE);
			TextView text = (TextView) views.findViewById(ID_TEXTVIEW);
			String content = edit.getText().toString();
			text.setText(content);
			note.rec.setName(content);
			note.store();
			ViewGroup parent = (ViewGroup) views.getParent();
			LinearLayout.LayoutParams lp = 
				(android.widget.LinearLayout.LayoutParams) parent.getLayoutParams();
			lp.weight = 1f;
			LinearLayout.LayoutParams lp2 = 
				(android.widget.LinearLayout.LayoutParams) views.getLayoutParams();
			lp2.weight = note.weight;
			parent.getParent().requestLayout();
		}	
	}
}
