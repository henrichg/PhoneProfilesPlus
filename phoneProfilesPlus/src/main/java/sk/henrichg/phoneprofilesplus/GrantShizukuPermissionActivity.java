package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import rikka.shizuku.Shizuku;

public class GrantShizukuPermissionActivity extends AppCompatActivity {

    private boolean rationaleAlreadyShown = false;

    /** @noinspection unused*/
    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
        Log.e("GrantShizukuPermissionActivity.onRequestPermissionsResult", "granted="+granted);
        if (granted) {
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
        Log.e("GrantShizukuPermissionActivity.onCreate", "xxx");
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
            Log.e("GrantShizukuPermissionActivity.onStart", "granted");

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
            Log.e("GrantShizukuPermissionActivity.onStart", "Shizuku.requestPermission");
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
        Log.e("GrantShizukuPermissionActivity.onDestroy", "xxx");
    }
    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
        Log.e("GrantShizukuPermissionActivity.finish", "xxx");
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
                this
        );

        if (!isFinishing())
            dialog.show();
    }

}
