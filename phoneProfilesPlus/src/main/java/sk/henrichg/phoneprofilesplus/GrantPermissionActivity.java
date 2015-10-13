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
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class GrantPermissionActivity extends Activity {

    private long profile_id;
    private boolean mergedProfile;
    private List<Permissions.PermissionType> permissions;
    private int startupSource;
    private boolean interactive;
    private boolean forGUI;
    private boolean monochrome;
    private int monochromeValue;
    private String eventNotificationSound;
    private boolean log;

    private Profile profile;
    private DataWrapper dataWrapper;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 909090;
    private static final int PERMISSIONS_REQUEST_CODE = 909091;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        Intent intent = getIntent();
        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        mergedProfile = intent.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        startupSource = intent.getIntExtra(Permissions.EXTRA_STARTUP_SOURCE, GlobalData.STARTUP_SOURCE_ACTIVATOR);
        interactive = intent.getBooleanExtra(Permissions.EXTRA_INTERACTIVE, true);
        forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);
        eventNotificationSound = intent.getStringExtra(Permissions.EXTRA_EVENT_NOTIFICATION_SOUND);
        log = intent.getBooleanExtra(Permissions.EXTRA_LOG, false);

        dataWrapper = new DataWrapper(getApplicationContext(), forGUI, monochrome, monochromeValue);
        profile = dataWrapper.getProfileById(profile_id, mergedProfile);
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

            String showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
            if (profile != null)
                showRequestString = showRequestString + "\"" + profile._name + "\" ";
            showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";

            if (showRequestWriteSettings) {
                Log.e("GrantPermissionActivity","onStart - showRequestWriteSettings");
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadExternalStorage) {
                Log.e("GrantPermissionActivity","onStart - showRequestReadExternalStorage");
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadPhoneState || showRequestProcessOutgoingCalls) {
                Log.e("GrantPermissionActivity","onStart - showRequestReadPhoneState");
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
            dialogBuilder.setTitle(R.string.permissions_alert_title);
            dialogBuilder.setMessage(Html.fromHtml(showRequestString));
            dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(true);
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
                    activateProfile();
                }
            });
            dialogBuilder.show();

        }
        else {
            requestPermissions(true);
        }
    }

    @Override
    protected void onDestroy()
    {
        //dataWrapper = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                activateProfile();
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {
            requestPermissions(false);
        }
    }

    private void requestPermissions(boolean writeSettings) {

        if (writeSettings) {
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                    break;
                }
                else
                    requestPermissions(false);
            }
        }
        else {
            List<String> permList = new ArrayList<String>();
            for (Permissions.PermissionType permissionType : permissions) {
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                        (!permList.contains(permissionType.permission))) {
                    Log.e("GrantPermissionActivity", "requestPermissions - permission=" + permissionType.permission);
                    permList.add(permissionType.permission);
                }
            }

            Log.e("GrantPermissionActivity", "requestPermissions - permList.size=" + permList.size());
            if (permList.size() > 0) {

                String[] permArray = new String[permList.size()];
                for (int i = 0; i < permList.size(); i++) permArray[i] = permList.get(i);

                ActivityCompat.requestPermissions(this, permArray, PERMISSIONS_REQUEST_CODE);
            }
            else
                activateProfile();
        }
    }

    private void activateProfile() {
        List<Permissions.PermissionType> permissions = Permissions.checkProfilePermissions(getApplicationContext(), profile);

        if (permissions.size() == 0) {
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getApplicationContext());
            dataWrapper._activateProfile(profile, mergedProfile, startupSource, interactive, this, eventNotificationSound, log);
        }
        finish();
    }
}
