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

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            setResult(Activity.RESULT_OK);
            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(getApplicationContext());
            //if (activateProfile) {
            //    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, interactive, null, true);
            //}
        }
        // TODO - zobraz dialog a na positive zasa zavolaj
        //  Shizuku.requestPermission(Permissions.SZIZUKU_PERMISSION_REQUEST_CODE);
        //  vid GrantPermisisonActivity.showRationale()
        //else
        //    showRationale(context);
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
        //} else
        //noinspection StatementWithEmptyBody
        //if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
        } else {
            // Request the permission
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
    }
    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /*
    private void doShow() {
        new ShowActivityAsyncTask(this).execute();
    }
    */

}
