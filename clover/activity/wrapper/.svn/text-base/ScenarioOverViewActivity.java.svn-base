package com.singularity.clover.activity.wrapper;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.singularity.clover.R;
import com.singularity.clover.activity.entity.TaskOverViewActivity;
import com.singularity.clover.activity.wrapper.ScenarioOverViewActivity.ImageAdapter.ViewHolder;
import com.singularity.clover.entity.EntityPool;
import com.singularity.clover.entity.wrapper.Scenario;

public class ScenarioOverViewActivity extends Activity {
	public final static int OUT_CUSTOM_SCENAIRO = 0;
	public final static int OUT_ADD_SCENARIO = 1;
	public final static int OUT_TASK_OVERVIEW = 2;

	public final static String TASK_OVERVIEW_DATA = "scenario";
	
	private ArrayList<Long> scenarioIds;
	private OnImageViewClick onViewClick = new OnImageViewClick();
	
	public class ImageAdapter extends BaseAdapter {
		private Context _context;
			
		public ImageAdapter(Context _context) {
			super();
			this._context = _context;
		}

		@Override
		public int getCount() {
			return scenarioIds.size();
		}

		@Override
		public Object getItem(int pos) {
			return scenarioIds.get(pos);
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
				imageView.setLayoutParams(new GridView.LayoutParams(48, 48));
				imageView.setOnClickListener(onViewClick);
				holder = new ViewHolder();
			} else {
				imageView = (ImageView) convertView;
				holder = (ViewHolder) imageView.getTag();
			}
			holder.scenarioId = scenarioIds.get(position);
			Scenario it = (Scenario) EntityPool.instance().
				forId(holder.scenarioId, Scenario.TAG);
			imageView.setTag(holder);
			imageView.setImageResource(it.getResId());
			return imageView;
		}
		
		public class ViewHolder{
			long scenarioId;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scenariooverview_layout);

		scenarioIds = EntityPool.instance().
			getPrototype(Scenario.TAG).loadTable(null,null);
	    if(scenarioIds == null){
	    	scenarioIds = new ArrayList<Long>();
	    	//testSetup();
	    }else{}    
		
		GridView gridview = (GridView) findViewById(R.id.scenario_gridview);
	    ImageAdapter adapter = new ImageAdapter(this);
	    gridview.setAdapter(adapter);  	
	}
	
	private class OnImageViewClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			Intent intent = new Intent(ScenarioOverViewActivity.this,
					TaskOverViewActivity.class);
			intent.setAction(TaskOverViewActivity.TASK_OVERVIEWBY_SCENARIO);
			intent.putExtra(TASK_OVERVIEW_DATA,holder.scenarioId);
			startActivityForResult(intent, OUT_TASK_OVERVIEW);
		}	
	}
	
	/*private void testSetup(){
		Scenario it = new Scenario("Go out", R.drawable.scenario_go_out);
		it.store();
		scenarioIds.add(it.getId());
		
		it = new Scenario("At home", R.drawable.scenario_home);
		it.store();
		scenarioIds.add(it.getId());
		
		it = new Scenario("Meeting",R.drawable.scenario_meet_someone);
		it.store();
		scenarioIds.add(it.getId());
	}*/
}
