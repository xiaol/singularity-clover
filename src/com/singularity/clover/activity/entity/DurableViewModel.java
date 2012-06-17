package com.singularity.clover.activity.entity;

import android.app.NotificationManager;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.singularity.clover.R;
import com.singularity.clover.SingularityApplication;
import com.singularity.clover.activity.entity.TaskViewActivityHelper.ViewHolder;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.EntityViewModel;
import com.singularity.clover.entity.Persisable;
import com.singularity.clover.entity.notification.Notifier;
import com.singularity.clover.entity.objective.DurableObj;
import com.singularity.clover.notification.NotifierReceiver;

/**
 * @author xiaol
 * Durable对应视图模型
 */
public class DurableViewModel implements EntityViewModel{

	private final TaskViewActivityHelper taskViewActivityHelper;
	private OnCtrlClick mCtrlClick;
	private OnResetClick mResetClick;
	protected NotificationManager mNM;

	DurableViewModel(TaskViewActivityHelper taskViewActivityHelper) {
		this.taskViewActivityHelper = taskViewActivityHelper;
		mCtrlClick = new OnCtrlClick();
		mResetClick = new OnResetClick();
	}

	public final static int MODEL_EDIT = 0;
	public final static int MODEL_DISPLAY = 1;
	
	public final static int QUARTER = 0;
	public final static int HARF = 1;
	public final static int HARF_AND_QUARTER =2;
	public final static int ONE =3;
	
	protected OnShortCutClick mShortCutClick = new OnShortCutClick();
	protected OnUnitClick mUnitClick = new OnUnitClick();
	
	@Override
	public View initView() {
		View layout = this.taskViewActivityHelper.mLinflater.inflate(
			R.layout.task_view_durable_layout,null);
		layout.setTag(TaskViewActivityHelper.ITEM_DURABLE);
		return layout;
	}

	@Override
	public View changeModel(int model, View layout) {
		View remove = layout.findViewById(R.id.task_view_durable_remove);
		View ctrl = layout.findViewById(R.id.task_view_durable_time_ctrl);
		EditText max = (EditText) layout.findViewById(
				R.id.task_view_durable_max);
		View unit = layout.findViewById(R.id.task_view_durable_unit);
		View shortcut = layout.findViewById(R.id.task_view_durable_shortcut);
		View process = layout.findViewById(R.id.task_view_durable_process);
		View reset = layout.findViewById(R.id.task_view_durable_reset);
		
		switch (model) {
		case MODEL_EDIT:
			ctrl.setVisibility(View.GONE);
			max.setVisibility(View.VISIBLE);
			unit.setVisibility(View.VISIBLE);
			shortcut.setVisibility(View.VISIBLE);
			remove.setVisibility(View.VISIBLE);
			process.setVisibility(View.GONE);
			layout.setOnLongClickListener(null);
			shortcut.setTag(QUARTER);
			unit.setTag(DurableObj.MINUTE_UNIT);
			shortcut.setOnClickListener(mShortCutClick);
			unit.setOnClickListener(mUnitClick);
			reset.setVisibility(View.GONE);
			break;
		case MODEL_DISPLAY:
			ctrl.setVisibility(View.VISIBLE);
			max.setVisibility(View.GONE);
			unit.setVisibility(View.GONE);
			shortcut.setVisibility(View.GONE);
			remove.setVisibility(View.GONE);
			process.setVisibility(View.VISIBLE);
			reset.setVisibility(View.VISIBLE);

			layout.setOnLongClickListener(
					this.taskViewActivityHelper.onDraggableLongClick);
			ctrl.setOnClickListener(mCtrlClick);
			reset.setOnClickListener(mResetClick);
			break;
		}
		return layout;
	}

	@Override
	public View entityToView(Persisable e, View layout) {
		DurableObj durable = (DurableObj) e;
		float elapse = durable.getElaspeCount();
		long max = durable.getMaxCount();
		TextView text = (TextView) layout.findViewById(
				R.id.task_view_durable_process_text);
		
		View indicator = layout.findViewById(
				R.id.task_view_durable_process_indicator);
		View processBar = layout.findViewById(
				R.id.task_view_durable_process_background);
		FrameLayout.LayoutParams barIp = (LayoutParams) processBar.getLayoutParams();
		FrameLayout.LayoutParams lp = (LayoutParams) indicator.getLayoutParams();
		
		if(max != 0){
			lp.setMargins((int) (elapse*barIp.width/max), 0, 0, 0);
			text.setText(elapse+"/"+max+" "+ durable.getUnitString());
		}else{
			text.setText(elapse+ durable.getUnitString());
		}
		
		ImageButton btn = (ImageButton) layout.findViewById(
				R.id.task_view_durable_time_ctrl);
		if(durable.isRunning()){
			btn.setBackgroundResource(
				R.drawable.task_view_durable_button_time_ctrl_pause_bg);
			long timeLength = durable.howLongExpired();
			notifyAlarm(durable, timeLength);
		}else{
			btn.setBackgroundResource(
				R.drawable.task_view_durable_button_time_ctrl_bg);}
		return layout;
	}

	@Override
	public Persisable viewToEntity(View layout, Persisable e) {
		DurableObj durable = (DurableObj) e;
		EditText max = (EditText) layout.findViewById(
				R.id.task_view_durable_max);
		ImageView unit = (ImageView) layout.findViewById(R.id.task_view_durable_unit);
		TextView processText = (TextView) layout.findViewById(
				R.id.task_view_durable_process_text);
		int unitState = (Integer) unit.getTag();
		String maxStr = max.getText().toString();
		if(maxStr.equals("")){
			durable.set(0,unitState);
		}else{
			durable.set(Integer.parseInt(max.getText().toString()),unitState);
		}
		processText.setText(maxStr + durable.getUnitString());
		
		durable.store();
		return durable;
	}
	
	protected class OnCtrlClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			ImageButton btn = (ImageButton) v;
			View view = (View) v.getParent();	
			ViewHolder holder = (ViewHolder) view.getTag();
			DurableObj durable = (DurableObj) EntityPool.instance().forId(
					holder.id, holder.tag);
			durable.setRunning(!durable.isRunning());
			if(durable.isRunning()){
				btn.setBackgroundResource(
						R.drawable.task_view_durable_button_time_ctrl_pause_bg);
				long timeLength = durable.howLongExpired();
				notifyAlarm(durable, timeLength);
			}else{
				btn.setBackgroundResource(
						R.drawable.task_view_durable_button_time_ctrl_bg);
				cancelAlarm(durable);
			}
			durable.store();
			
			View indicator = view.findViewById(
					R.id.task_view_durable_process_indicator);
			FrameLayout.LayoutParams lp = (LayoutParams) indicator.getLayoutParams();
			float elapse = durable.getElaspeCount();
			long max = durable.getMaxCount();
			if(max != 0)
				lp.setMargins((int) (elapse*view.getWidth()/max), 0, 0, 0);
			
			TextView text = (TextView) view.findViewById(
				R.id.task_view_durable_process_text);
			if(max == 0){
				text.setText(elapse + durable.getUnitString());
			}else{
				text.setText(elapse+"/"+max+" "+ durable.getUnitString());
			}
			
			view.requestLayout();
		}
		
	}
	
	protected class OnResetClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			View view = (View) v.getParent();	
			ImageButton btn = (ImageButton) view.findViewById(R.id.task_view_durable_time_ctrl);
			ViewHolder holder = (ViewHolder) view.getTag();
			DurableObj durable = (DurableObj) EntityPool.instance().forId(
					holder.id, holder.tag);
			
			
			if(durable.isRunning()){
				btn.setBackgroundResource(
						R.drawable.task_view_durable_button_time_ctrl_bg);
				cancelAlarm(durable);
			}else{}
			durable.reset();
			durable.store();
			
			View indicator = view.findViewById(
					R.id.task_view_durable_process_indicator);
			FrameLayout.LayoutParams lp = (LayoutParams) indicator.getLayoutParams();
			float elapse = durable.getElaspeCount();
			long max = durable.getMaxCount();
			if(max != 0)
				lp.setMargins((int) (elapse*view.getWidth()/max), 0, 0, 0);
			
			TextView text = (TextView) view.findViewById(
				R.id.task_view_durable_process_text);
			if(max == 0){
				text.setText(elapse + durable.getUnitString());
			}else{
				text.setText(elapse+"/"+max+" "+ durable.getUnitString());
			}
			
			view.requestLayout();
			
		}
		
	}

	protected class OnShortCutClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			int state = (Integer) v.getTag();
			ImageView image = (ImageView) v;
			View parent =  (View) v.getParent();
			
			EditText max = (EditText) parent.findViewById(
				R.id.task_view_durable_max);
			View unit = parent.findViewById(R.id.task_view_durable_unit);
			
			int unitState = (Integer) unit.getTag();
			switch (state) {
			case QUARTER:
				v.setTag(HARF);
				image.setBackgroundResource(
						R.drawable.task_view_durable_shortcut_harf);
				if(unitState == DurableObj.MINUTE_UNIT){
					max.setText("30");
				}else if(unitState == DurableObj.HOUR_UINT){
					max.setText("6");}
				
				break;
			case HARF:
				v.setTag(HARF_AND_QUARTER);
				image.setBackgroundResource(
						R.drawable.task_view_durable_shortcut_harf_and_quarter);
				if(unitState == DurableObj.MINUTE_UNIT){
					max.setText("45");
				}else if(unitState == DurableObj.HOUR_UINT){
					max.setText("9");}
				
				break;
			case HARF_AND_QUARTER:
				v.setTag(ONE);
				image.setBackgroundResource(
						R.drawable.task_view_durable_shortcut_one);
				if(unitState == DurableObj.MINUTE_UNIT){
					max.setText("60");
				}else if(unitState == DurableObj.HOUR_UINT){
					max.setText("12");}
				break;
			case ONE:
				v.setTag(QUARTER);
				image.setBackgroundResource(
						R.drawable.task_view_durable_shortcut);
				if(unitState == DurableObj.MINUTE_UNIT){
					max.setText("15");
				}else if(unitState == DurableObj.HOUR_UINT){
					max.setText("3");}
				break;
			default:
				break;
			}
			
		}
	}
	
	protected class OnUnitClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			int unitState = (Integer) v.getTag();
			ImageView image = (ImageView) v;
			View parent =  (View) v.getParent();
			
			EditText max = (EditText) parent.findViewById(
				R.id.task_view_durable_max);
			View shortcut = parent.findViewById(
					R.id.task_view_durable_shortcut);
			
			int state = (Integer) shortcut.getTag();
			if(unitState == DurableObj.MINUTE_UNIT){
				unitState = DurableObj.HOUR_UINT;
				image.setBackgroundResource(
						R.drawable.task_view_durable_unit_hour);
				v.setTag(DurableObj.HOUR_UINT);
				
				switch (state) {
				case QUARTER:
					max.setText("3");
				break;
				case HARF:
					max.setText("6");
					break;
				case HARF_AND_QUARTER:
					max.setText("9");
					break;
				case ONE:
					max.setText("12");
					break;
				default:
				break;}
				
			}else if(unitState == DurableObj.HOUR_UINT){
				unitState = DurableObj.MINUTE_UNIT;
				image.setBackgroundResource(
						R.drawable.task_view_durable_unit_mins);
				v.setTag(DurableObj.MINUTE_UNIT);
				
				switch (state) {
				case QUARTER:
					max.setText("15");
				break;
				case HARF:
					max.setText("30");
					break;
				case HARF_AND_QUARTER:
					max.setText("45");
					break;
				case ONE:
					max.setText("60");
					break;
				default:
				break;}
			
			}
		}
	}

	@Override
	public void updateView(View v) {}
	
	protected void notifyAlarm(DurableObj durable,long delay){
		if(delay > 0){
			durable.setAlarm(SingularityApplication.instance(), System.currentTimeMillis()+delay);
		}
		
	}
	
	protected void cancelAlarm(DurableObj durable){
		durable.cancelAlarm();
	}
}