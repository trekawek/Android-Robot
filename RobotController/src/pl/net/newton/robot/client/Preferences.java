package pl.net.newton.robot.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setTitle("Preferences");
		
		Intent resultIntent = new Intent();
		setResult(Activity.RESULT_OK, resultIntent);
	}
}
