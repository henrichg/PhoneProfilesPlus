package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

/**
 * This job is called only from WifiSSIDPreference and BluetoothNamePreference
 */

class ScannerJob extends Job {

    static final String JOB_TAG  = "ScannerJob";

    static final String EXTRA_SCANNER_TYPE = "scanner_type";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "ScannerJob.onRunJob", "ScannerJob_onRunJob");

        Bundle bundle = params.getTransientExtras();

        String scannerType = bundle.getString(EXTRA_SCANNER_TYPE);
        PPApplication.logE("%%%% ScannerJob.onRunJob", "scannerType="+scannerType);

        Scanner scanner = new Scanner(appContext);
        scanner.doScan(scannerType);

        return Result.SUCCESS;
    }

    static void start(String scannerType) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SCANNER_TYPE, scannerType);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

}
