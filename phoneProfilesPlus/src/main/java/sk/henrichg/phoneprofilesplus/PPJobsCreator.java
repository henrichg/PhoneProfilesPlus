package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class PPJobsCreator implements JobCreator {

    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SearchCalendarEventsJob.JOB_TAG:
                return new SearchCalendarEventsJob();
            case SearchCalendarEventsJob.JOB_TAG_SHORT:
                return new SearchCalendarEventsJob();
            case WifiScanJob.JOB_TAG:
                return new WifiScanJob();
            case WifiScanJob.JOB_TAG_SHORT:
                return new WifiScanJob();
            case BluetoothScanJob.JOB_TAG:
                return new BluetoothScanJob();
            case BluetoothScanJob.JOB_TAG_SHORT:
                return new BluetoothScanJob();
            case GeofenceScannerJob.JOB_TAG:
                return new GeofenceScannerJob();
            case GeofenceScannerJob.JOB_TAG_START:
                return new GeofenceScannerJob();
            case AboutApplicationJob.JOB_TAG:
                return new AboutApplicationJob();

            case PackageReplacedJob.JOB_TAG:
                return new PackageReplacedJob();
            case FirstStartJob.JOB_TAG:
                return new FirstStartJob();
            case ExecuteRadioProfilePrefsJob.JOB_TAG:
                return new ExecuteRadioProfilePrefsJob();
            case ExecuteVolumeProfilePrefsJob.JOB_TAG:
                return new ExecuteVolumeProfilePrefsJob();
            case ExecuteWallpaperProfilePrefsJob.JOB_TAG:
                return new ExecuteWallpaperProfilePrefsJob();
            case ExecuteRunApplicationsProfilePrefsJob.JOB_TAG:
                return new ExecuteRunApplicationsProfilePrefsJob();
            case ExecuteRootProfilePrefsJob.JOB_TAG:
                return new ExecuteRootProfilePrefsJob();
            case ProfileDurationJob.JOB_TAG:
                return new ProfileDurationJob();
            case ScreenOnOffJob.JOB_TAG:
                return new ScreenOnOffJob();
            case PhoneCallJob.JOB_TAG:
                return new PhoneCallJob();
            case DashClockJob.JOB_TAG:
                return new DashClockJob();
            case EventsHandlerJob.JOB_TAG:
                return new EventsHandlerJob();
            case ScannerJob.JOB_TAG:
                return new ScannerJob();
            case WifiJob.JOB_TAG:
                return new WifiJob();
            case BluetoothJob.JOB_TAG:
                return new BluetoothJob();
            case BatteryJob.JOB_TAG:
                return new BatteryJob();
            case WifiAPStateChangeJob.JOB_TAG:
                return new WifiAPStateChangeJob();

            default:
                return null;
        }
    }

}
