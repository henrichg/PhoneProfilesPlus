package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class DeviceOrientationJob extends Job {

    static final String JOB_TAG  = "DeviceOrientationJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "DeviceOrientationJob.onRunJob", "DeviceOrientationJob_onRunJob");

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return Result.SUCCESS;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ DeviceOrientationJob.onRunJob", "-----------");

            if (PhoneProfilesService.mDeviceDistance == PhoneProfilesService.DEVICE_ORIENTATION_DEVICE_IS_NEAR)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "now device is NEAR.");
            else
            if (PhoneProfilesService.mDeviceDistance == PhoneProfilesService.DEVICE_ORIENTATION_DEVICE_IS_FAR)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "now device is FAR");
            else
            if (PhoneProfilesService.mDeviceDistance == PhoneProfilesService.DEVICE_ORIENTATION_UNKNOWN)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "unknown distance");

            if (PhoneProfilesService.mDisplayUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_UP)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(D) now screen is facing up.");
            if (PhoneProfilesService.mDisplayUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_DOWN)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(D) now screen is facing down.");
            if (PhoneProfilesService.mDisplayUp == PhoneProfilesService.DEVICE_ORIENTATION_UNKNOWN)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(D) unknown display orientation.");

            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_UP)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now screen is facing up.");
            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_DISPLAY_DOWN)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now screen is facing down.");

            if (PhoneProfilesService.mSideUp == PhoneProfilesService.mDisplayUp)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now device is horizontal.");
            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_UP_SIDE_UP)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now up side is facing up.");
            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_DOWN_SIDE_UP)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now down side is facing up.");
            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now right side is facing up.");
            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_LEFT_SIDE_UP)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) now left side is facing up.");
            if (PhoneProfilesService.mSideUp == PhoneProfilesService.DEVICE_ORIENTATION_UNKNOWN)
                PPApplication.logE("DeviceOrientationJob.onRunJob", "(S) unknown side.");

            PPApplication.logE("@@@ DeviceOrientationJob.onRunJob", "-----------");

            // start events handler
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION, false);
        }
        
        return Result.SUCCESS;
    }

    static void start() {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .startNow()
                .build()
                .schedule();
    }

}
