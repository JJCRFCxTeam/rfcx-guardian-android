package guardian.activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rfcx.guardian.guardian.R;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import guardian.RfcxGuardian;

public class MainActivity extends Activity {
	
	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, MainActivity.class);

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		RfcxGuardian app = (RfcxGuardian) getApplication();
		
		switch (item.getItemId()) {
		
		case R.id.menu_prefs:
			startActivity(new Intent(this, PrefsActivity.class));
			break;

		case R.id.menu_reboot:
			app.deviceControlUtils.runOrTriggerDeviceControl("reboot", app.getApplicationContext().getContentResolver());
			break;

		case R.id.menu_screenshot:
			app.deviceControlUtils.runOrTriggerDeviceControl("screenshot", app.getApplicationContext().getContentResolver());
			break;

		case R.id.menu_sntp_check:
			app.rfcxServiceHandler.triggerService("DateTimeSntpSyncJob", true);
			break;

		case R.id.menu_airplanemode_off:
			app.deviceControlUtils.runOrTriggerDeviceControl("airplanemode_off", app.getApplicationContext().getContentResolver());
			break;

		case R.id.menu_airplanemode_on:
			app.deviceControlUtils.runOrTriggerDeviceControl("airplanemode_on", app.getApplicationContext().getContentResolver());
			break;
		
		}
		
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		((RfcxGuardian) getApplication()).appPause();
	}
	
}
