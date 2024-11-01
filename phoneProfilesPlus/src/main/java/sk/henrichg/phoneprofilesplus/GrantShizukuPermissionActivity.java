package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import rikka.shizuku.Shizuku;

public class GrantShizukuPermissionActivity extends AppCompatActivity {

    private boolean rationaleAlreadyShown = false;

    /** @noinspection unused*/
    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
        //Log.e("GrantShizukuPermissionActivity.onRequestPermissionsResult", "granted="+granted);
        if (granted) {
            PPApplicationStatic.logE("GrantShizukuPermissionActivity.onRequestPermissionsResult", "*** Shizuku granted ***");
            Runnable runnable = () -> {
                RootUtils.settingsBinaryExists(false);
                RootUtils.serviceBinaryExists(false);
                //noinspection Convert2MethodRef
                RootUtils.getServicesList();
                ApplicationPreferences.applicationHyperOsWifiBluetoothDialogs(getApplicationContext());
                Permissions.setHyperOSWifiBluetoothDialogAppOp();

                // do activate profile/restart events aso for first start
                Data workData = new Data.Builder()
                        .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true)

                        .putBoolean(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, false)
                        .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION, "")
                        .putInt(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE, 0)
                        .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, "")

                        .putBoolean(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, true)

                        //.putBoolean(PhoneProfilesService.EXTRA_SHOW_TOAST, serviceIntent != null)
                        .build();

//                PPApplicationStatic.logE("[MAIN_WORKER_CALL] PhoneProfilesService.doForFirstStart", "xxxxxxxxxxxxxxxxxxxx");

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(PPApplication.AFTER_SHIZUKU_START_WORK_TAG)
                                .setInputData(workData)

                                .setInitialDelay(10, TimeUnit.SECONDS)

                                .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                                        //if (PPApplicationStatic.logEnabled()) {
//                                        ListenableFuture<List<WorkInfo>> statuses;
//                                        statuses = workManager.getWorkInfosForUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG);
//                                        try {
//                                            List<WorkInfo> workInfoList = statuses.get();
//                                        } catch (Exception ignored) {
//                                        }
//                                        //}

//                        PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doFirstStart", "keepResultsForAtLeast");
                        //workManager.enqueue(worker);
                        // !!! MUST BE APPEND_OR_REPLACE FOR EXTRA_START_FOR_EXTERNAL_APPLICATION !!!
                        workManager.enqueueUniqueWork(PPApplication.AFTER_SHIZUKU_START_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

            }; //);
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);

            setResult(Activity.RESULT_OK);
            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(getApplicationContext());
            //if (activateProfile) {
            //    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, interactive, null, true);
            //}
        }
        else
            showRationale();
    }

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] GrantShizukuPermissionActivity.onCreate", "xxx");
        //Log.e("GrantShizukuPermissionActivity.onCreate", "xxx");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart()
    {
        super.onStart();

        GlobalGUIRoutines.lockScreenOrientation(this, true);

        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return;
        }

        //noinspection StatementWithEmptyBody
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            //Log.e("GrantShizukuPermissionActivity.onStart", "granted");

            setResult(Activity.RESULT_OK);
            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(getApplicationContext());
        //} else
        //noinspection StatementWithEmptyBody
        //if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
        } else {
            // Request the permission
            //Log.e("GrantShizukuPermissionActivity.onStart", "Shizuku.requestPermission");
            Shizuku.requestPermission(Permissions.SZIZUKU_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        //Log.e("GrantShizukuPermissionActivity.onDestroy", "xxx");
    }
    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
        //Log.e("GrantShizukuPermissionActivity.finish", "xxx");
    }

    /*
    private void doShow() {
        new ShowActivityAsyncTask(this).execute();
    }
    */

    private void showRationale() {
        if (rationaleAlreadyShown) {
            setResult(Activity.RESULT_OK);
            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(getApplicationContext());
            //if (activateProfile) {
            //    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, interactive, null, true);
            //}
            return;
        }
        rationaleAlreadyShown = true;

        String _showRequestValue =
                getString(R.string.grant_shizuku_permissions_text1) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                getString(R.string.grant_shizuku_permissions_text2);

        GlobalGUIRoutines.setTheme(this, true, true, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        PPAlertDialog dialog = new PPAlertDialog(getString(R.string.permissions_alert_title),
                StringFormatUtils.fromHtml(_showRequestValue, true,  false, 0, 0, true),
                getString(android.R.string.ok), getString(android.R.string.cancel), null, null,
                (dialog1, which) -> Shizuku.requestPermission(Permissions.SZIZUKU_PERMISSION_REQUEST_CODE),
                (dialog2, which) -> finish(),
                null,
                dialog3 -> finish(),
                null,
                true, true,
                false, false,
                false,
                false,
                this
        );

        if (!isFinishing())
            dialog.show();
    }

}
