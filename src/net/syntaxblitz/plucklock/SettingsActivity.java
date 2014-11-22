package net.syntaxblitz.plucklock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsactivity);

		Intent accelerometerIntent = new Intent(getBaseContext(),
				AccelerometerService.class);
		getBaseContext().startService(accelerometerIntent);
		
		this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		final SharedPreferences.Editor editor = prefs.edit();

		final EditText thresholdEdit = (EditText) this
				.findViewById(R.id.threshold_value);
		thresholdEdit.setText("" + prefs.getFloat("threshold_pref_key", 1));
		thresholdEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try {
					float newVal = Float.valueOf(s.toString());
					if (newVal < .5) { 
						Toast.makeText(getBaseContext(), getResources().getString(R.string.threshold_too_low), Toast.LENGTH_SHORT).show();
					}
					if (newVal > 10) {
						Toast.makeText(getBaseContext(), getResources().getString(R.string.threshold_too_high), Toast.LENGTH_SHORT).show();
					}
					else {
						editor.putFloat("threshold_pref_key", newVal);
						editor.commit();
					}
				} catch (NumberFormatException e) {
				}
			}
		});
		
		final ComponentName adminComponent = new ComponentName(this,
				AdminReceiver.class);

		if (!prefs.getBoolean("has_disabled_device_admin", false)) {	// user has never unchecked it, ever 
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
			startActivity(intent);
		}
		
		final DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		final CheckBox deviceManagerCheck = (CheckBox) findViewById(R.id.enable_device_admin);
		final TextView enable_device_admin_description = (TextView) findViewById(R.id.enable_device_admin_description);
		
		deviceManagerCheck.setChecked(dpm.isAdminActive(adminComponent));
		deviceManagerCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if (isChecked) {	// we're not here to be DRY like those ruby freaks
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
					startActivity(intent);
					enable_device_admin_description.setText(R.string.enable_device_admin_ON);
				} else {
					editor.putBoolean("has_disabled_device_admin", true).commit();
					dpm.removeActiveAdmin(adminComponent);
					enable_device_admin_description.setText(R.string.enable_device_admin_OFF);
				}
			}
		});
		// for first time, when there is no change in the state of the checkbox
		if (deviceManagerCheck.isChecked()) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
			startActivity(intent);
			enable_device_admin_description.setText(R.string.enable_device_admin_ON);	
			}
		else {
			editor.putBoolean("has_disabled_device_admin", true).commit();
			dpm.removeActiveAdmin(adminComponent);
			enable_device_admin_description.setText(R.string.enable_device_admin_OFF);
			}
	}
}
