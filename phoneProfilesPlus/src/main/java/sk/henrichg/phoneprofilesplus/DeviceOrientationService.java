package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class DeviceOrientationService extends WakefulIntentService {

    public DeviceOrientationService() {
        super("DeviceOrientationService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PPApplication.logE("##### DeviceOrientationService.doWakefulWork", "xxx");

            Context appContext = getApplicationContext();

            if (!PPApplication.getApplicationStarted(appContext, true))
                // application is not started
                return;

            if (Event.getGlobalEventsRunning(appContext))
            {
                PPApplication.logE("@@@ DeviceOrientationService.doWakefulWork", "-----------");

                if (PhoneProfilesService.mDeviceDistance == PhoneProfilesService.DEVICE_ORIENTATION_DEVICE_IS_NEAR)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "now device is NEAR.");
                else
                if (PhoneProfilesService.mDeviceDistance == PhoneProfilesService.DEVICE_ORIENTATION_DEVICE_IS_FAR)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "now device is FAR");
                else
                if (PhoneProfilesService.mDeviceDistance == PhoneProfilesService.DEVICE_ORIENTATION_UNKNOWN)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "unknown distance");

                if (PhoneProfilesService.mDisplayUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_UP)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(D) now screen is facing up.");
                if (PhoneProfilesService.mDisplayUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_DOWN)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(D) now screen is facing down.");
                if (PhoneProfilesService.mDisplayUp == PhoneProfilesService.DEVICE_ORIENTATION_UNKNOWN)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(D) unknown display orientation.");

                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_UP)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now screen is facing up.");
                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_DOWN)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now screen is facing down.");

                if (PhoneProfilesService.mSideUp == PhoneProfilesService.mDisplayUp)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now device is horizontal.");
                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_UP_SIDE_UP)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now up side is facing up.");
                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_DOWN_SIDE_UP)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now down side is facing up.");
                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now right side is facing up.");
                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_LEFT_SIDE_UP)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) now left side is facing up.");
                if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_UNKNOWN)
                    PPApplication.logE("DeviceOrientationService.doWakefulWork", "(S) unknown side.");

                PPApplication.logE("@@@ DeviceOrientationService.doWakefulWork", "-----------");

                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) == 0) {
                    if (PhoneProfilesService.instance != null) {
                        PPApplication.stopOrientationScanner(appContext);
                    }
                    dataWrapper.invalidateDataWrapper();
                    return;
                }

                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_DEVICE_ORIENTATION);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            }
        }
    }

}
