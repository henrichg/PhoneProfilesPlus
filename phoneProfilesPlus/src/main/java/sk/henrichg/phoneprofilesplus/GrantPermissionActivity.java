package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class GrantPermissionActivity extends Activity {

    private long profile_id;
    private List<Permissions.PermissionType> permissions;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        intent = getIntent();
        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Context context = getApplicationContext();

        boolean showRequestWriteSettings = false;
        boolean showRequestReadExternalStorage = false;
        boolean showRequestReadPhoneState = false;
        boolean showRequestProcessOutgoingCalls = false;

        Log.e("GrantPermissionActivity", "onStart - permissions.size="+permissions.size());

        for (Permissions.PermissionType permissionType : permissions) {
            if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS))
                showRequestWriteSettings = GlobalData.getShowRequestWriteSettingsPermission(context);
            if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE))
                showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE);
            if (permissionType.permission.equals(Manifest.permission.PROCESS_OUTGOING_CALLS))
                showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS);
        }

        if (showRequestWriteSettings || showRequestReadExternalStorage || showRequestReadPhoneState || showRequestProcessOutgoingCalls) {

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            Profile profile = dataWrapper.getProfileById(profile_id, false);

            String showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
            if (profile != null)
                showRequestString = showRequestString + "\"" + profile._name + "\" ";
            showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";

            if (showRequestWriteSettings) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadExternalStorage) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadPhoneState || showRequestProcessOutgoingCalls) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_phone) + "</b>";
                showRequestString = showRequestString + "<br>";
            }

            showRequestString = showRequestString + "<br>";
            showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GUIData.setTheme(this, true, false);
            GUIData.setLanguage(this.getBaseContext());

            final Activity _activity = this;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Permissions");
            dialogBuilder.setMessage(Html.fromHtml(showRequestString));
            dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _activity.finish();
                }
            });
            /*dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _activity.finish();
                }
            });*/
            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    _activity.finish();
                }
            });
            dialogBuilder.show();

        }
        else
            finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


}
