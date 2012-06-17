package com.singularity.clover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SingularityPreference extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.singularity_preference);
		Preference feedback = findPreference("feed_back");
		feedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(android.content.Intent.EXTRA_EMAIL, 
						new String[]{"binxiaoking@126.com"});
				intent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.feed_back));     
				startActivity(Intent.createChooser(intent, ""));
				return true;
			}
		});
	}

}
