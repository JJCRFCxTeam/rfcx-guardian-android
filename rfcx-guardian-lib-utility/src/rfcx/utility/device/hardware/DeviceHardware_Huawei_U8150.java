package rfcx.utility.device.hardware;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import rfcx.utility.misc.FileUtils;
import rfcx.utility.misc.ShellCommands;
import rfcx.utility.rfcx.RfcxLog;

public class DeviceHardware_Huawei_U8150 {

	public DeviceHardware_Huawei_U8150(String appRole) {

	}
	
	private static final String logTag = RfcxLog.generateLogTag("Utils", DeviceHardware_Huawei_U8150.class);
	
	public static final String DEVICE_NAME = "Huawei U8150";
		
	public static boolean isDevice_Huawei_U8150() {
		return DeviceHardwareUtils.getDeviceHardwareName().equalsIgnoreCase(DEVICE_NAME);
	}

	public static void checkOrResetGPSFunctionality(Context context) {

			// constructing gps.conf files
			String gpsDotConfContents = (new StringBuilder())
				.append("NTP_SERVER=it.pool.ntp.org").append("\n")
				.append("XTRA_SERVER_1=http://xtra1.gpsonextra.net/xtra.bin").append("\n")
				.append("XTRA_SERVER_2=http://xtra2.gpsonextra.net/xtra.bin").append("\n")
				.append("XTRA_SERVER_3=http://xtra3.gpsonextra.net/xtra.bin").append("\n")
				.append("\n")
				.append("SUPL_HOST=supl.google.com").append("\n")
				.append("SUPL_PORT=7276").append("\n")
				.append("DEBUG_LEVEL = 3").append("\n")
				.append("INTERMEDIATE_POS=0").append("\n")
				.append("ACCURACY_THRES=0").append("\n")
				.append("ENABLE_WIPER=1").append("\n")
				.append("SUPL_HOST=supl.google.com").append("\n")
				.append("SUPL_PORT=7276").append("\n")
				.append("CURRENT_CARRIER=common").append("\n")
				.append("DEFAULT_AGPS_ENABLE=TRUE").append("\n")
				.append("DEFAULT_SSL_ENABLE=FALSE").append("\n")
				.append("DEFAULT_USER_PLANE=TRUE").append("\n")
				.append("\n")
				.toString();

			String gpsDotConfCacheFilePath = context.getFilesDir().getAbsolutePath()+"/gps.conf";
			
			// writing gps.conf into place
			try {
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gpsDotConfCacheFilePath));
				bufferedWriter.write(gpsDotConfContents);
				bufferedWriter.close();
				FileUtils.chmod(gpsDotConfCacheFilePath, 0755);
			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			}

			String gpsDotConfFilePath = "/system/etc/gps.conf";
			
			if (		!(new File(gpsDotConfFilePath)).exists()
				||	!FileUtils.sha1Hash(gpsDotConfFilePath).equals(FileUtils.sha1Hash(gpsDotConfCacheFilePath))
				) {
				
				String allCmds =	"mount -o rw,remount /dev/block/mmcblk0p1 /system;\n"			// remounting partition
								+"rm -rf "+gpsDotConfFilePath+";\n"
								+"mv "+gpsDotConfCacheFilePath+" "+gpsDotConfFilePath+";\n"	// moving gps.conf file into place
								+"echo 'ro.con g.xtra_support=true' >> /system/build.prop;\n"	// add a line to the build.prop file
								;
				ShellCommands.executeCommandAsRootAndIgnoreOutput(allCmds, context);
						 
				// rebooting device
				ShellCommands.triggerRebootAsRoot(context);
				
			} else {
				Log.d(logTag, "GPS has already been activated on this device.");
			}
		
	}
	
	
	
}
