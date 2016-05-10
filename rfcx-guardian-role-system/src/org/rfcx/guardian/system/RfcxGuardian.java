package org.rfcx.guardian.system;

import java.util.Calendar;

import org.rfcx.guardian.system.database.DataTransferDb;
import org.rfcx.guardian.system.database.DeviceStateDb;
import org.rfcx.guardian.system.database.ScreenShotDb;
import org.rfcx.guardian.system.device.DeviceCpuUsage;
import org.rfcx.guardian.system.service.DeviceScreenShotService;
import org.rfcx.guardian.system.service.DeviceSensorService;
import org.rfcx.guardian.system.service.DeviceStateService;
import org.rfcx.guardian.system.service.ServiceMonitorIntentService;
import org.rfcx.guardian.utility.rfcx.RfcxConstants;
import org.rfcx.guardian.utility.rfcx.RfcxDeviceId;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;
import org.rfcx.guardian.utility.rfcx.RfcxRole;
import org.rfcx.guardian.utility.device.DeviceBattery;
import org.rfcx.guardian.utility.device.DeviceDiskUsage;
import org.rfcx.guardian.utility.device.DeviceMobileNetwork;
import org.rfcx.guardian.utility.device.DeviceNetworkStats;
import org.rfcx.guardian.utility.device.DeviceScreenLock;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class RfcxGuardian extends Application {

	public String version;
	Context context;
	
	public static final String APP_ROLE = "System";

	private static final String TAG = "Rfcx-"+APP_ROLE+"-"+RfcxGuardian.class.getSimpleName();

	public RfcxDeviceId rfcxDeviceId = null; 
	public RfcxPrefs rfcxPrefs = null;
	
	// database access helpers
	public DeviceStateDb deviceStateDb = null;
	public DataTransferDb dataTransferDb = null;
	public ScreenShotDb screenShotDb = null;

	public boolean isConnected = false;
	public long lastConnectedAt = Calendar.getInstance().getTimeInMillis();
	public long lastDisconnectedAt = Calendar.getInstance().getTimeInMillis();
	
	public DeviceBattery deviceBattery = new DeviceBattery();
	public DeviceCpuUsage deviceCpuUsage = new DeviceCpuUsage();
	public DeviceScreenLock deviceScreenLock = new DeviceScreenLock();
	public DeviceNetworkStats deviceNetworkStats = new DeviceNetworkStats();
	
	// Background Services
	public boolean isRunning_DeviceState = false;
	public boolean isRunning_DeviceSensor = false;
	public boolean isRunning_DeviceScreenShot = false;
	
	// Repeating IntentServices
	public boolean isRunning_ServiceMonitor = false;
	
	private boolean hasRun_OnLaunchServiceTrigger = false;
	
	@Override
	public void onCreate() {
		
		super.onCreate();

		this.rfcxDeviceId = (new RfcxDeviceId()).init(getApplicationContext());
		this.rfcxPrefs = (new RfcxPrefs()).init(getApplicationContext(), APP_ROLE);
		
		this.version = RfcxRole.getRoleVersion(getApplicationContext(), TAG);
		rfcxPrefs.writeVersionToFile(this.version);
		
		setDbHandlers();
				
		initializeRoleServices(getApplicationContext());
		
	}
	
	public void onTerminate() {
		super.onTerminate();
	}
	
	public void appResume() {

	}
	
	public void appPause() {
		
	}
	
	public void initializeRoleServices(Context context) {
		if (!this.hasRun_OnLaunchServiceTrigger) {
			try {
				
				// Service Monitor
				triggerIntentService("ServiceMonitor", 
						System.currentTimeMillis(),
						3 * Math.round( this.rfcxPrefs.getPrefAsInt("audio_cycle_duration") / 1000 )
						);
				// background service for gathering system stats
				triggerService("DeviceState", true);
				// background service for gathering sensor stats
				triggerService("DeviceSensor", true);
				// background service for taking screenshots
				triggerService("ScreenShot", true);
				
				hasRun_OnLaunchServiceTrigger = true;
			} catch (Exception e) {
				Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC);
			}
		}
	}
	
	public void triggerIntentService(String intentServiceName, long startTimeMillis, int repeatIntervalSeconds) {
		Context context = getApplicationContext();
		if (startTimeMillis == 0) { startTimeMillis = System.currentTimeMillis(); }
		long repeatInterval = 300000;
		try { repeatInterval = repeatIntervalSeconds*1000; } catch (Exception e) { e.printStackTrace(); }
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if (intentServiceName.equals("ServiceMonitor")) {
			if (!this.isRunning_ServiceMonitor) {
				PendingIntent monitorServiceIntent = PendingIntent.getService(context, -1, new Intent(context, ServiceMonitorIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
				if (repeatIntervalSeconds == 0) { alarmManager.set(AlarmManager.RTC, startTimeMillis, monitorServiceIntent);
				} else { alarmManager.setInexactRepeating(AlarmManager.RTC, startTimeMillis, repeatInterval, monitorServiceIntent); }
			} else { Log.w(TAG, "Repeating IntentService 'ServiceMonitor' is already running..."); }
		} else {
			Log.w(TAG, "No IntentService named '"+intentServiceName+"'.");
		}
	}
	
	
	public void triggerService(String serviceName, boolean forceReTrigger) {
		context = getApplicationContext();
		if (forceReTrigger) Log.w(TAG,"Forcing [re]trigger of service "+serviceName);
		if (serviceName.equals("DeviceState")) {
			if (!this.isRunning_DeviceState || forceReTrigger) {
				context.stopService(new Intent(context, DeviceStateService.class));
				context.startService(new Intent(context, DeviceStateService.class));
			} else { Log.w(TAG, "Service '"+serviceName+"' is already running..."); }
		} else if (serviceName.equals("DeviceSensor")) {
			if (!this.isRunning_DeviceSensor || forceReTrigger) {
				context.stopService(new Intent(context, DeviceSensorService.class));
				context.startService(new Intent(context, DeviceSensorService.class));
			} else { Log.w(TAG, "Service '"+serviceName+"' is already running..."); }
		} else if (serviceName.equals("ScreenShot")) {
			if (!this.isRunning_DeviceScreenShot || forceReTrigger) {
				context.stopService(new Intent(context, DeviceScreenShotService.class));
				context.startService(new Intent(context, DeviceScreenShotService.class));
			} else { Log.w(TAG, "Service '"+serviceName+"' is already running..."); }
		} else {
			Log.w(TAG, "There is no service named '"+serviceName+"'.");
		}
	}
	
	public void stopService(String serviceName) {
		context = getApplicationContext();		
		if (serviceName.equals("DeviceState")) {
			context.stopService(new Intent(context, DeviceStateService.class));
		} else if (serviceName.equals("DeviceSensor")) {
			context.stopService(new Intent(context, DeviceSensorService.class));
		} else if (serviceName.equals("ScreenShot")) {
			context.stopService(new Intent(context, DeviceScreenShotService.class));
		} else {
			Log.e(TAG, "There is no service named '"+serviceName+"'.");
		}
	}
	
	private void setDbHandlers() {
		int versionNumber = RfcxRole.getRoleVersionValue(this.version, TAG);
		this.deviceStateDb = new DeviceStateDb(this,versionNumber);
		this.dataTransferDb = new DataTransferDb(this,versionNumber);
		this.screenShotDb = new ScreenShotDb(this,versionNumber);
	}
 

	
	
}
