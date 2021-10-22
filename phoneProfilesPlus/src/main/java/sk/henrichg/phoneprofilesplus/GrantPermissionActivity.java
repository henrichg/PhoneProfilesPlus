package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

//import me.drakeet.support.toast.ToastCompat;

public class GrantPermissionActivity extends AppCompatActivity {

    private int grantType;
    private ArrayList<Permissions.PermissionType> permissions;
    static private ArrayList<Permissions.PermissionType> permissionsForRecheck;
    private boolean mergedProfile;
    private boolean forceGrant;
    //private boolean mergedNotification;
    //private boolean forGUI;
    //private boolean monochrome;
    //private int monochromeValue;
    private int startupSource;
    private boolean interactive;
    private String applicationDataPath;
    private boolean activateProfile;
    private boolean grantAlsoContacts;
    private boolean grantAlsoBackgroundLocation;
    //private boolean forceStartScanner;
    private boolean fromNotification;

    private Profile profile;
    private Event event;
    private DataWrapper dataWrapper;

    private boolean started = false;

    private boolean showRequestWriteSettings = false;
    //private boolean showRequestAccessNotificationPolicy = false;
    private boolean showRequestDrawOverlays = false;
    private boolean showRequestReadExternalStorage = false;
    private boolean showRequestReadPhoneState = false;
    private boolean showRequestWriteExternalStorage = false;
    private boolean showRequestReadCalendar = false;
    private boolean showRequestReadContacts = false;
    private boolean showRequestAccessCoarseLocation = false;
    private boolean showRequestAccessFineLocation = false;
    private boolean showRequestAccessBackgroundLocation = false;
    private boolean showRequestCamera = false;

    private boolean[][] whyPermissionType = null;
    private boolean rationaleAlreadyShown = false;

    private boolean restoredInstanceState;

    //private AsyncTask geofenceEditorAsyncTask = null;

    private static final int PERMISSIONS_REQUEST_CODE = 9090;
    private static final int BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 9091;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 9091;
    //private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE = 9092;
    private static final int DRAW_OVERLAYS_REQUEST_CODE = 9093;
    //private static final int WRITE_SETTINGS_REQUEST_CODE_FORCE_GRANT = 9094;
    //private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE_FORCE_GRANT = 9095;
    //private static final int DRAW_OVERLAYS_REQUEST_CODE_FORCE_GRANT = 9096;

    private static final String EXTRA_WITH_RATIONALE = PPApplication.PACKAGE_NAME + ".EXTRA_WITH_RATIONALE";

    //static final String NOTIFICATION_DELETED_ACTION = PPApplication.PACKAGE_NAME + ".GrantPermissionActivity.PERMISSIONS_NOTIFICATION_DELETED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        grantType = intent.getIntExtra(Permissions.EXTRA_GRANT_TYPE, 0);
        boolean onlyNotification = intent.getBooleanExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
        forceGrant = intent.getBooleanExtra(Permissions.EXTRA_FORCE_GRANT, false);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        //if (permissions != null)
        //    PPApplication.logE("GrantPermissionActivity.onCreate", "permissions.size()="+permissions.size());
        permissionsForRecheck = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        //if (permissionsForRecheck != null)
        //    PPApplication.logE("GrantPermissionActivity.onCreate", "permissionsForRecheck.size()="+permissionsForRecheck.size());
        /*mergedNotification = false;
        if (permissions == null) {
            permissions = Permissions.getMergedPermissions(getApplicationContext());
            mergedNotification = true;
        }*/

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        mergedProfile = intent.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
        //forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        //monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        //monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
        interactive = intent.getBooleanExtra(Permissions.EXTRA_INTERACTIVE, true);
        applicationDataPath = intent.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH);
        activateProfile = intent.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, true)/* && (profile_id != Profile.SHARED_PROFILE_ID)*/;
        grantAlsoContacts = intent.getBooleanExtra(Permissions.EXTRA_GRANT_ALSO_CONTACTS, true);
        //forceStartScanner = intent.getBooleanExtra(Permissions.EXTRA_FORCE_START_SCANNER, false);
        grantAlsoBackgroundLocation = intent.getBooleanExtra(Permissions.EXTRA_GRANT_ALSO_BACKGROUND_LOCATION, false);

        fromNotification = intent.getBooleanExtra(Permissions.EXTRA_FROM_NOTIFICATION, false);

        long event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0/*monochrome, monochromeValue*/, false, DataWrapper.IT_FOR_EDITOR, 0f);
        //if (profile_id != Profile.SHARED_PROFILE_ID)
            profile = dataWrapper.getProfileById(profile_id, false, false, mergedProfile);
        //else
        //    profile = Profile.getProfileFromSharedPreferences(getApplicationContext(), PPApplication.SHARED_PROFILE_PREFS_NAME);
        event = dataWrapper.getEventById(event_id);

        restoredInstanceState = savedInstanceState != null;
        if (restoredInstanceState) {
            started = savedInstanceState.getBoolean("started", false);
        }

        if (!started && onlyNotification) {
            showNotification();
            started = true;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        GlobalGUIRoutines.lockScreenOrientation(this, true);

        if (started) return;
        started = true;

        if ((grantType == Permissions.GRANT_TYPE_PROFILE) && (profile == null)) {
            finish();
            return;
        }
        if ((grantType == Permissions.GRANT_TYPE_EVENT) && (event == null)) {
            finish();
            return;
        }

        final Context context = getApplicationContext();

        if (fromNotification) {
            // called from notification - recheck permissions
            /*if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                boolean granted = Permissions.checkInstallTone(context, permissions);
                if (granted) {
                    Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                            context.getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else*/
            if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                boolean granted = Permissions.checkPlayRingtoneNotification(context, grantAlsoContacts, permissions);
                if (granted) {
                    PPApplication.showToast(context.getApplicationContext(),
                            context.getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    finish();
                    return;
                }
            }
            else
            if (grantType == Permissions.GRANT_TYPE_EVENT) {
                // get permissions from shared preferences and recheck it
                /*permissions = Permissions.recheckPermissions(context, Permissions.getMergedPermissions(context));
                mergedNotification = true;*/
                permissions = Permissions.recheckPermissions(context, permissions);
                if (permissions.size() == 0) {
                    PPApplication.showToast(context.getApplicationContext(),
                            context.getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    finish();
                    return;
                }
            }
            /*else
            if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE) {
                boolean granted = Permissions.checkLogToFile(context, permissions);
                if (granted) {
                    Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                            context.getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }*/
            else {
                // get permissions from shared preferences and recheck it
                /*permissions = Permissions.recheckPermissions(context, Permissions.getMergedPermissions(context));
                mergedNotification = true;*/
                permissions = Permissions.recheckPermissions(context, permissions);
                if (permissions.size() == 0) {
                    PPApplication.showToast(context.getApplicationContext(),
                            context.getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    finish();
                    return;
                }
            }
        }

        if (!restoredInstanceState) {
            boolean withRationale = canShowRationale(context, false);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("GrantPermissionActivity.onStart", "withRationale=" + withRationale);
                PPApplication.logE("GrantPermissionActivity.onStart", "showRequestWriteSettings=" + showRequestWriteSettings);
                //PPApplication.logE("GrantPermissionActivity.onStart", "showRequestAccessNotificationPolicy=" + showRequestAccessNotificationPolicy);
                PPApplication.logE("GrantPermissionActivity.onStart", "showRequestDrawOverlays=" + showRequestDrawOverlays);
            }*/

            int iteration = 4;
            if (showRequestWriteSettings)
                iteration = 1;
            //else if (showRequestAccessNotificationPolicy)
            //    iteration = 2;
            else if (showRequestDrawOverlays)
                iteration = 3;

            requestPermissions(iteration, withRationale);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    /*
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if ((geofenceEditorAsyncTask != null) && !geofenceEditorAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            geofenceEditorAsyncTask.cancel(true);
        }
    }
    */

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("started", started);
    }

    private boolean canShowRationale(Context context, boolean forceGrant) {
        showRequestWriteSettings = false;
        //showRequestAccessNotificationPolicy = false;
        showRequestDrawOverlays = false;
        showRequestReadExternalStorage = false;
        showRequestReadPhoneState = false;
        showRequestWriteExternalStorage = false;
        showRequestReadCalendar = false;
        showRequestReadContacts = false;
        showRequestAccessCoarseLocation = false;
        showRequestAccessFineLocation = false;
        showRequestAccessBackgroundLocation = false;
        showRequestCamera = false;

        if (permissions != null) {
            whyPermissionType = new boolean[20][100];

            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    showRequestWriteSettings = Permissions.getShowRequestWriteSettingsPermission(context) || forceGrant;
                    whyPermissionType[0][permissionType.type] = true;
                }
                /*if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                    showRequestAccessNotificationPolicy = Permissions.getShowRequestAccessNotificationPolicyPermission(context) || forceGrant;
                    whyPermissionType[1][permissionType.type] = true;
                }*/
                if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    showRequestDrawOverlays = Permissions.getShowRequestDrawOverlaysPermission(context) || forceGrant;
                    whyPermissionType[2][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[3][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                    showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[4][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showRequestWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[6][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.READ_CALENDAR)) {
                    showRequestReadCalendar = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[7][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.READ_CONTACTS)) {
                    showRequestReadContacts = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[8][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showRequestAccessFineLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[13][permissionType.type] = true;
                }
                if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    showRequestAccessCoarseLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[12][permissionType.type] = true;
                }
                if (Build.VERSION.SDK_INT >= 29) {
                    if (permissionType.permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        showRequestAccessBackgroundLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
//                        Log.e("GrantPermissionActivity.canShowRationale", "ACCESS_BACKGROUND_LOCATION showRequestAccessBackgroundLocation="+showRequestAccessBackgroundLocation);
//                        Log.e("GrantPermissionActivity.canShowRationale", "ACCESS_BACKGROUND_LOCATION forceGrant="+forceGrant);
//                        Log.e("GrantPermissionActivity.canShowRationale", "ACCESS_BACKGROUND_LOCATION permissionType.type="+permissionType.type);
                        whyPermissionType[14][permissionType.type] = true;
                    }
                }
                if (permissionType.permission.equals(Manifest.permission.CAMERA)) {
                    showRequestCamera = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                    whyPermissionType[15][permissionType.type] = true;
                }
            }
        }

        return (showRequestWriteSettings ||
                showRequestReadExternalStorage ||
                showRequestReadPhoneState ||
                showRequestWriteExternalStorage ||
                showRequestReadCalendar ||
                showRequestReadContacts ||
                showRequestAccessCoarseLocation ||
                showRequestAccessFineLocation ||
                showRequestAccessBackgroundLocation ||
                //showRequestAccessNotificationPolicy ||
                showRequestDrawOverlays||
                showRequestCamera);
    }

    private void showRationale(final Context context) {
        if (rationaleAlreadyShown)
            finishGrant();
        rationaleAlreadyShown = true;

        if (canShowRationale(context, forceGrant)) {
            //PPApplication.logE("GrantPermissionActivity.showRationale", "can show");

            /*if (onlyNotification) {
                showNotification(context);
            }
            else {*/
            String showRequestString;

            /*if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                showRequestString = context.getString(R.string.permissions_for_install_tone_text1) + "<br><br>";
            else*/ if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION)
                showRequestString = context.getString(R.string.permissions_for_play_ringtone_notification_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_IMAGE_WALLPAPER)
                showRequestString = context.getString(R.string.permissions_for_wallpaper_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_WALLPAPER_FOLDER)
                showRequestString = context.getString(R.string.permissions_for_wallpaper_folder_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                showRequestString = context.getString(R.string.permissions_for_custom_profile_icon_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                showRequestString = context.getString(R.string.permissions_for_export_app_data_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_EXPORT_AND_EMAIL)
                showRequestString = context.getString(R.string.permissions_for_export_app_data_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                showRequestString = context.getString(R.string.permissions_for_import_app_data_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_wifi_bt_scan_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_CALENDAR_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_calendar_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_CONTACT_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_contacts_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY)
                showRequestString = context.getString(R.string.permissions_for_location_geofence_editor_activity_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_brightness_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_mobile_cells_scan_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_mobile_cells_registration_dialog_text1) + "<br><br>";
            //else if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE)
            //    showRequestString = context.getString(R.string.permissions_for_log_to_file_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR)
                showRequestString = context.getString(R.string.permissions_for_export_app_data_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_CONNECT_TO_SSID_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_connect_to_ssid_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_BACKGROUND_LOCATION)
                showRequestString = context.getString(R.string.permissions_for_background_location_text1) + "<br><br>";
            else
            if (grantType == Permissions.GRANT_TYPE_EVENT){
                    /*if (mergedNotification) {
                        showRequestString = context.getString(R.string.permissions_for_event_text1m) + " ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_event_text2) + "<br><br>";
                    }
                    else {*/
                showRequestString = context.getString(R.string.permissions_for_event_text1) + " ";
                if (event != null)
                    showRequestString = showRequestString + "\"" + event._name + "\" ";
                showRequestString = showRequestString + context.getString(R.string.permissions_for_event_text2) + "<br><br>";
                //}
            }
            else {
                if (mergedProfile/* || mergedNotification*/) {
                    showRequestString = context.getString(R.string.permissions_for_profile_text1m) + " ";
                }
                else {
                    showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
                    if (profile != null)
                        showRequestString = showRequestString + "\"" + profile._name + "\" ";
                }
                showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
            }

            String whyString = "";
            if (showRequestWriteSettings) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[0]);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestReadExternalStorage || showRequestWriteExternalStorage) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_storage) + "</b>";
                boolean[] permissionTypes = new boolean[100];
                for (int i = 0; i < 100; i++) {
                    permissionTypes[i] = whyPermissionType[3][i] || whyPermissionType[6][i];
                }
                String whyPermissionString = getWhyPermissionString(permissionTypes);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestReadPhoneState) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_phone) + "</b>";
                boolean[] permissionTypes = new boolean[100];
                for (int i = 0; i < 100; i++) {
                    permissionTypes[i] = whyPermissionType[4][i] || whyPermissionType[5][i];
                }
                String whyPermissionString = getWhyPermissionString(permissionTypes);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestReadCalendar) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_calendar) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[7]);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestReadContacts) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_contacts) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[8]);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestAccessCoarseLocation || showRequestAccessFineLocation /*|| showRequestAccessBackgroundLocation*/) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_location) + "</b>";
                boolean[] permissionTypes = new boolean[100];
                for (int i = 0; i < 100; i++) {
                    permissionTypes[i] = whyPermissionType[12][i] || whyPermissionType[13][i] /*|| whyPermissionType[14][i]*/;
                }
                String whyPermissionString = getWhyPermissionString(permissionTypes);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestAccessBackgroundLocation) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_background_location) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[14]);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            /*if (showRequestAccessNotificationPolicy) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_access_notification_policy) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[1]);
                if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }*/
            if (showRequestDrawOverlays) {
                whyString = whyString + "<li>";
                if (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))
                    whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_draw_overlays) + "</b>";
                else
                    whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_draw_overlays_miui) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[2]);
                //if (whyPermissionString != null)
                    whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (showRequestCamera) {
                whyString = whyString + "<li>";
                whyString = whyString + "<b>" + context.getString(R.string.permission_group_name_camera) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[15]);
                //if (whyPermissionString != null)
                whyString = whyString + whyPermissionString;
                whyString = whyString + "</li>";
            }
            if (!whyString.isEmpty())
                showRequestString = "<ul>" + whyString + "</ul>";

            showRequestString = showRequestString + "<br>";

            /*if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_install_tone_text2);
            else*/ if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_play_ringtone_notification_text2);
            else if (grantType == Permissions.GRANT_TYPE_IMAGE_WALLPAPER)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_wallpaper_text2);
            else if (grantType == Permissions.GRANT_TYPE_WALLPAPER_FOLDER)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_wallpaper_folder_text2);
            else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_custom_profile_icon_text2);
            else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_export_app_data_text2);
            else if (grantType == Permissions.GRANT_TYPE_EXPORT_AND_EMAIL)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_export_app_data_text2);
            else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_import_app_data_text2);
            else if (grantType == Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_wifi_bt_scan_dialog_text2);
            else if (grantType == Permissions.GRANT_TYPE_CALENDAR_DIALOG)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_calendar_dialog_text2);
            else if (grantType == Permissions.GRANT_TYPE_CONTACT_DIALOG)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_contacts_dialog_text2);
            else if (grantType == Permissions.GRANT_TYPE_EVENT)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_event_text3);
            else if (grantType == Permissions.GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_location_geofence_editor_activity_text2);
            else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_brightness_dialog_text2);
            //else if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE)
            //    showRequestString = showRequestString + context.getString(R.string.permissions_for_log_to_file_text2);
            else if (grantType == Permissions.GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_export_app_data_text2);
            else if (grantType == Permissions.GRANT_TYPE_CONNECT_TO_SSID_DIALOG)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_connect_to_ssid_dialog_text2);
            else if (grantType == Permissions.GRANT_TYPE_BACKGROUND_LOCATION)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_background_location_text2);
            else
                showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

            // set theme and language for dialog alert ;-)
            GlobalGUIRoutines.setTheme(this, true, true/*, false*/, false, false);
            //GlobalGUIRoutines.setLanguage(this);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.permissions_alert_title);
            dialogBuilder.setMessage(GlobalGUIRoutines.fromHtml(showRequestString, true, false, 0, 0));
            dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                int iteration = 4;
                if (showRequestWriteSettings)
                    iteration = 1;
                //else if (showRequestAccessNotificationPolicy)
                //    iteration = 2;
                else if (showRequestDrawOverlays)
                    iteration = 3;
                requestPermissions(iteration, canShowRationale(context, false));
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> finish());
            dialogBuilder.setOnCancelListener(dialog -> finish());
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!isFinishing())
                dialog.show();
            //}
        }
        else {
            //PPApplication.logE("GrantPermissionActivity.showRationale", "can not show");
            showRequestWriteSettings = false;
            //showRequestAccessNotificationPolicy = false;
            showRequestDrawOverlays = false;

            if (permissions != null) {
                for (Permissions.PermissionType permissionType : permissions) {
                    if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                        showRequestWriteSettings = true;
                    }
                    /*if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                        showRequestAccessNotificationPolicy = true;
                    }*/
                    if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                        showRequestDrawOverlays = true;
                    }
                }
            }

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("GrantPermissionActivity.showRationale", "showRequestWriteSettings=" + showRequestWriteSettings);
                //PPApplication.logE("GrantPermissionActivity.showRationale", "showRequestAccessNotificationPolicy="+showRequestAccessNotificationPolicy);
                PPApplication.logE("GrantPermissionActivity.showRationale", "showRequestDrawOverlays=" + showRequestDrawOverlays);
            }*/

            int iteration = 4;
            if (showRequestWriteSettings)
                iteration = 1;
            //else if (showRequestAccessNotificationPolicy)
            //    iteration = 2;
            else if (showRequestDrawOverlays)
                iteration = 3;

            requestPermissions(iteration, canShowRationale(context, false));
        }
    }

    private String getWhyPermissionString(boolean[] whyPermissionTypes) {
        String s = "";
        for (int permissionType = 0; permissionType < 100; permissionType++) {
            if (whyPermissionTypes[permissionType]) {
                switch (permissionType) {
                    //case Permissions.PERMISSION_PROFILE_VOLUME_PREFERENCES:
                    //    break;
                    case Permissions.PERMISSION_PROFILE_VIBRATION_ON_TOUCH:
                        s = getString(R.string.permission_why_profile_vibration_on_touch);
                        break;
                    case Permissions.PERMISSION_PROFILE_RINGTONES:
                        s = getString(R.string.permission_why_profile_ringtones);
                        break;
                    case Permissions.PERMISSION_PROFILE_SCREEN_TIMEOUT:
                        s = getString(R.string.permission_why_profile_screen_timeout);
                        break;
                    case Permissions.PERMISSION_PROFILE_SCREEN_BRIGHTNESS:
                        s = getString(R.string.permission_why_profile_screen_brightness);
                        break;
                    case Permissions.PERMISSION_PROFILE_AUTOROTATION:
                        s = getString(R.string.permission_why_profile_autorotation);
                        break;
                    case Permissions.PERMISSION_PROFILE_IMAGE_WALLPAPER:
                    case Permissions.PERMISSION_PROFILE_WALLPAPER_FOLDER:
                        s = getString(R.string.permission_why_profile_wallpaper);
                        break;
                    case Permissions.PERMISSION_PROFILE_RADIO_PREFERENCES:
                        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI && (Build.VERSION.SDK_INT >= 28))
                            s = getString(R.string.permission_why_profile_radio_preferences_emui);
                        else
                            s = getString(R.string.permission_why_profile_radio_preferences);
                        break;
                    case Permissions.PERMISSION_PROFILE_PHONE_STATE_BROADCAST:
                        s = getString(R.string.permission_why_profile_phone_state_broadcast);
                        break;
                    case Permissions.PERMISSION_PROFILE_CUSTOM_PROFILE_ICON:
                        s = getString(R.string.permission_why_profile_custom_profile_icon);
                        break;
                    /*case Permissions.PERMISSION_INSTALL_TONE:
                        s = getString(R.string.permission_why_install_tone);
                        break;*/
                    case Permissions.PERMISSION_EXPORT:
                        s = getString(R.string.permission_why_export);
                        break;
                    case Permissions.PERMISSION_IMPORT:
                        s = getString(R.string.permission_why_import);
                        break;
                    case Permissions.PERMISSION_EVENT_CALENDAR_PREFERENCES:
                        s = getString(R.string.permission_why_event_calendar_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_CALL_PREFERENCES:
                        s = getString(R.string.permission_why_event_call_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_ORIENTATION_PREFERENCES:
                        s = getString(R.string.permission_why_event_orientation_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_SMS_PREFERENCES:
                        s = getString(R.string.permission_why_event_sms_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_RADIO_SWITCH_PREFERENCES:
                        s = getString(R.string.permission_why_event_radio_switch_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_LOCATION_PREFERENCES:
                        s = getString(R.string.permission_why_event_location_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_WIFI_PREFERENCES:
                        s = getString(R.string.permission_why_event_wifi_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_BLUETOOTH_PREFERENCES:
                        s = getString(R.string.permission_why_event_bluetooth_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_BLUETOOTH_SWITCH_PREFERENCES:
                        s = getString(R.string.permission_why_event_bluetooth_preferences_emui);
                        break;
                    case Permissions.PERMISSION_EVENT_MOBILE_CELLS_PREFERENCES:
                        s = getString(R.string.permission_why_event_mobile_cells_preferences);
                        break;
                    case Permissions.PERMISSION_EVENT_CONTACTS_PREFERENCE:
                        s = getString(R.string.permission_why_event_contacts_preference);
                        break;
                    case Permissions.PERMISSION_PROFILE_NOTIFICATION_LED:
                        s = getString(R.string.permission_why_profile_notification_led);
                        break;
                    case Permissions.PERMISSION_PROFILE_VIBRATE_WHEN_RINGING:
                        s = getString(R.string.permission_why_profile_vibrate_when_ringing);
                        break;
                    case Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION:
                        s = getString(R.string.permission_why_play_ringtone_notification);
                        break;
//                    case Permissions.PERMISSION_PROFILE_ACCESS_NOTIFICATION_POLICY:
//                        s = getString(R.string.permission_why_profile_access_notification_policy);
//                        break;
                    case Permissions.PERMISSION_PROFILE_LOCK_DEVICE:
                        s = getString(R.string.permission_why_profile_lock_device);
                        break;
                    case Permissions.PERMISSION_RINGTONE_PREFERENCE:
                        s = getString(R.string.permission_why_ringtone_preference);
                        break;
                    case Permissions.PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING:
                        s = getString(R.string.permission_why_profile_dtmf_tone_when_dialing);
                        break;
                    case Permissions.PERMISSION_PROFILE_SOUND_ON_TOUCH:
                        s = getString(R.string.permission_why_profile_sound_on_touch);
                        break;
                    case Permissions.PERMISSION_BRIGHTNESS_PREFERENCE:
                        s = getString(R.string.permission_why_brightness_preference);
                        break;
                    case Permissions.PERMISSION_IMAGE_WALLPAPER_PREFERENCE:
                    case Permissions.PERMISSION_WALLPAPER_FOLDER_PREFERENCE:
                        s = getString(R.string.permission_why_wallpaper_preference);
                        break;
                    case Permissions.PERMISSION_CUSTOM_PROFILE_ICON_PREFERENCE:
                        s = getString(R.string.permission_why_custom_profile_icon_preference);
                        break;
                    case Permissions.PERMISSION_LOCATION_PREFERENCE:
                        s = getString(R.string.permission_why_location_preference);
                        break;
                    case Permissions.PERMISSION_CALENDAR_PREFERENCE:
                        s = getString(R.string.permission_why_calendar_preference);
                        break;
                    //case Permissions.PERMISSION_LOG_TO_FILE:
                    //    s = getString(R.string.permission_why_log_to_file);
                    //    break;
                    case Permissions.PERMISSION_EVENT_TIME_PREFERENCES:
                        s = getString(R.string.permission_why_event_time_preferences);
                        break;
                    case Permissions.PERMISSION_PROFILE_ALWAYS_ON_DISPLAY:
                        s = getString(R.string.permission_why_profile_always_on_display);
                        break;
                    case Permissions.PERMISSION_PROFILE_CONNECT_TO_SSID_PREFERENCE:
                        s = getString(R.string.permission_why_profile_connect_to_ssid_preference);
                        break;
                    case Permissions.PERMISSION_PROFILE_SCREEN_ON_PERMANENT:
                        s = getString(R.string.permission_why_profile_screen_on_permanent);
                        break;
                    case Permissions.PERMISSION_PROFILE_CAMERA_FLASH:
                        s = getString(R.string.permission_why_profile_camera_flash);
                        break;
                    case Permissions.PERMISSION_BACGROUND_LOCATION:
                        s = getString(R.string.permission_why_profile_background_location);
                        break;
                }
            }
        }
        if (s.isEmpty())
            return s;
        else
            return " - " + s;
    }

    private void showNotification() {
        final Context context = getApplicationContext();
        if (canShowRationale(context, false)) {
            int notificationID;
            String notificationTag;
            NotificationCompat.Builder mBuilder;

            PPApplication.createGrantPermissionNotificationChannel(context);

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
            /*if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                String nTitle = context.getString(R.string.permissions_notification_text);
                String nText = context.getString(R.string.permissions_for_install_tone_big_text_notification);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.ppp_app_name);
                    nText = context.getString(R.string.permissions_notification_text) + ": " +
                            context.getString(R.string.permissions_for_install_tone_big_text_notification);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.primary))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click
                notificationID = PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID;
            } else*/ if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                String nTitle = context.getString(R.string.permissions_notification_text);
                String nText = context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
//                if (android.os.Build.VERSION.SDK_INT < 24) {
//                    nTitle = context.getString(R.string.ppp_app_name);
//                    nText = context.getString(R.string.permissions_notification_text) + ": " +
//                            context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
//                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click
                notificationID = PPApplication.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID;
                notificationTag = PPApplication.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_TAG;
            } /*else if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE) {
                String nTitle = context.getString(R.string.permissions_notification_text);
                String nText = context.getString(R.string.permissions_for_log_to_file_big_text_notification);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.ppp_app_name);
                    nText = context.getString(R.string.permissions_notification_text) + ": " +
                            context.getString(R.string.permissions_for_log_to_file_big_text_notification);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click
                notificationID = PPApplication.GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID;
                notificationTag = PPApplication.GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_TAG;
            }*/ else if (grantType == Permissions.GRANT_TYPE_EVENT) {
                String nTitle = context.getString(R.string.permissions_for_event_text_notification);
                String nText = "";
//                if (android.os.Build.VERSION.SDK_INT < 24) {
//                    nTitle = context.getString(R.string.ppp_app_name);
//                    nText = context.getString(R.string.permissions_for_event_text_notification) + ": ";
//                }
            /*if (mergedNotification) {
                nText = nText + context.getString(R.string.permissions_for_event_text1m) + " " +
                        context.getString(R.string.permissions_for_event_big_text_notification);
            }
            else {*/
                nText = nText + context.getString(R.string.permissions_for_event_text1) + " ";
                if (event != null)
                    nText = nText + "\"" + event._name + "\" ";
                nText = nText + context.getString(R.string.permissions_for_event_big_text_notification);
                //}
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText) // message for notification
                        .setAutoCancel(true); // clear notification after click
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
                //Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
                //PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                //mBuilder.setDeleteIntent(deletePendingIntent);

                if (event != null) {
                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                    notificationID = PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id;
                    notificationTag = PPApplication.GRANT_EVENT_PERMISSIONS_NOTIFICATION_TAG+"_"+event._id;
                } else {
                    notificationID = PPApplication.GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID;
                    notificationTag = PPApplication.GRANT_EVENT_PERMISSIONS_NOTIFICATION_TAG;
                }
            } else {
                String nTitle = context.getString(R.string.permissions_for_profile_text_notification);
                String nText = "";
//                if (android.os.Build.VERSION.SDK_INT < 24) {
//                    nTitle = context.getString(R.string.ppp_app_name);
//                    nText = context.getString(R.string.permissions_for_profile_text_notification) + ": ";
//                }
                if (mergedProfile/* || mergedNotification*/) {
                    nText = nText + context.getString(R.string.permissions_for_profile_text1m) + " " +
                            context.getString(R.string.permissions_for_profile_big_text_notification);
                } else {
                    nText = nText + context.getString(R.string.permissions_for_profile_text1) + " ";
                    if (profile != null)
                        nText = nText + "\"" + profile._name + "\" ";
                    nText = nText + context.getString(R.string.permissions_for_profile_big_text_notification);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText) // message for notification
                        .setAutoCancel(true); // clear notification after click
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
                //Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
                //PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                //mBuilder.setDeleteIntent(deletePendingIntent);

                //intent.putExtra(Permissions.EXTRA_FOR_GUI, forGUI);
                //intent.putExtra(Permissions.EXTRA_MONOCHROME, monochrome);
                //intent.putExtra(Permissions.EXTRA_MONOCHROME_VALUE, monochromeValue);

                if (profile != null) {
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    notificationID = PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id;
                    notificationTag = PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_TAG+"_"+profile._id;
                } else {
                    notificationID = PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID;
                    notificationTag = PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_TAG;
                }
            }
            //permissions.clear();
            intent.putExtra(Permissions.EXTRA_GRANT_TYPE, grantType);
            intent.putParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES, permissions);
            //intent.putExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
            intent.putExtra(Permissions.EXTRA_FROM_NOTIFICATION, true);
            intent.putExtra(Permissions.EXTRA_FORCE_GRANT, forceGrant);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            intent.putExtra(Permissions.EXTRA_INTERACTIVE, interactive);
            intent.putExtra(Permissions.EXTRA_MERGED_PROFILE, mergedProfile);
            intent.putExtra(Permissions.EXTRA_ACTIVATE_PROFILE, activateProfile);
            intent.putExtra(Permissions.EXTRA_GRANT_ALSO_CONTACTS, grantAlsoContacts);
            intent.putExtra(Permissions.EXTRA_GRANT_ALSO_BACKGROUND_LOCATION, grantAlsoBackgroundLocation);

            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pi = PendingIntent.getActivity(context, grantType, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mBuilder.setOnlyAlertOnce(true);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            //}
            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
            try {
                // do not cancel, mBuilder.setOnlyAlertOnce(true); will not be working
                // mNotificationManager.cancel(notificationID);
                mNotificationManager.notify(notificationTag, notificationID, mBuilder.build());
            } catch (Exception e) {
                //Log.e("GrantPermissionActivity.showNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "requestCode=" + requestCode);

        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
            case BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
//                PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "grantResults.length="+grantResults.length);

//                for (String permission : permissions) {
//                    PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "permission=" + permission);
//                }

                boolean allGranted = true;
                for (int grantResult : grantResults) {
//                    PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "grantResult=" + grantResult);
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        //forceGrant = false;
                        break;
                    }
                }

                Context context = getApplicationContext();
                for (Permissions.PermissionType permissionType : this.permissions) {
                    if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                        if (!Settings.System.canWrite(context)) {
                            allGranted = false;
                            break;
                        }
                    }
//                    PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "allGranted=" + allGranted);

                    if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                                allGranted = false;
                                break;
                            }
                        }
                    }
//                    PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "allGranted=" + allGranted);

                    if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                        if (!Settings.canDrawOverlays(context)) {
                            allGranted = false;
                            break;
                        }
                    }
//                    PPApplication.logE("GrantPermissionActivity.onRequestPermissionsResult", "allGranted=" + allGranted);
                }

                if (allGranted) {
                    finishGrant();
                } else {
                    showRationale(context);
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Context context = getApplicationContext();
        final boolean withRationale = (data == null) || data.getBooleanExtra(EXTRA_WITH_RATIONALE, true);
        if ((requestCode == WRITE_SETTINGS_REQUEST_CODE)/* || (requestCode == WRITE_SETTINGS_REQUEST_CODE_FORCE_GRANT)*/) {
            if (!Settings.System.canWrite(context)) {
                //forceGrant = false;
                //if (!forceGrant) {
                    // set theme and language for dialog alert ;-)
                    GlobalGUIRoutines.setTheme(this, true, true/*, false*/, false, false);
                    //GlobalGUIRoutines.setLanguage(this);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    dialogBuilder.setMessage(R.string.permissions_write_settings_not_allowed_confirm);
                    dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, (dialog, which) -> {
                        Permissions.setShowRequestWriteSettingsPermission(context, false);
                        if (rationaleAlreadyShown)
                            removePermission(Manifest.permission.WRITE_SETTINGS);
                        requestPermissions(3/*2*/, withRationale);
                    });
                    dialogBuilder.setNegativeButton(R.string.permission_ask_button, (dialog, which) -> {
                        Permissions.setShowRequestWriteSettingsPermission(context, true);
                        if (rationaleAlreadyShown)
                            removePermission(Manifest.permission.WRITE_SETTINGS);
                        requestPermissions(3/*2*/, withRationale);
                    });
                    dialogBuilder.setOnCancelListener(dialog -> {
                        if (rationaleAlreadyShown)
                            removePermission(Manifest.permission.WRITE_SETTINGS);
                        requestPermissions(3/*2*/, withRationale);
                    });
                    AlertDialog dialog = dialogBuilder.create();

//                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    if (!isFinishing())
                        dialog.show();
                /*}
                else {
                    //if (requestCode == WRITE_SETTINGS_REQUEST_CODE)
                        requestPermissions(2);
                    //else
                    //    finishGrant();
                }*/
            }
            else {
                Permissions.setShowRequestWriteSettingsPermission(context, true);
                //if (requestCode == WRITE_SETTINGS_REQUEST_CODE)
                    requestPermissions(3/*2*/, withRationale);
                //else
                //    finishGrant();
            }
        }
/*        if ((requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE))// || (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE_FORCE_GRANT))
        {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                    //forceGrant = false;
                    //if (!forceGrant) {
                        // set theme and language for dialog alert ;-)
                        GlobalGUIRoutines.setTheme(this, true, true, false);
                        GlobalGUIRoutines.setLanguage(this);

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                        dialogBuilder.setTitle(R.string.permissions_alert_title);
                        dialogBuilder.setMessage(R.string.permissions_access_notification_policy_not_allowed_confirm);
                        dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Permissions.setShowRequestAccessNotificationPolicyPermission(context, false);
                                if (rationaleAlreadyShown)
                                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                                requestPermissions(3, withRationale);
                            }
                        });
                        dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                                if (rationaleAlreadyShown)
                                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                                requestPermissions(3, withRationale);
                            }
                        });
                        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (rationaleAlreadyShown)
                                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                                requestPermissions(3, withRationale);
                            }
                        });
                        AlertDialog dialog = dialogBuilder.create();
//                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                            @Override
//                            public void onShow(DialogInterface dialog) {
//                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                if (positive != null) positive.setAllCaps(false);
//                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                if (negative != null) negative.setAllCaps(false);
//                            }
//                        });
                        if (!isFinishing())
                            dialog.show();
//                    }
//                    else {
//                        //if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)
//                            requestPermissions(3);
//                        //else
//                        //    finishGrant();
//                    }
                } else {
                    Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    //if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)
                        requestPermissions(3, withRationale);
                    //else
                    //    finishGrant();
                }
            }
            else {
                //if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)
                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                    requestPermissions(3, withRationale);
                //else
                //    finishGrant();
            }
        }*/
        if ((requestCode == DRAW_OVERLAYS_REQUEST_CODE)/* || (requestCode == DRAW_OVERLAYS_REQUEST_CODE_FORCE_GRANT)*/) {
            if (!Settings.canDrawOverlays(context)) {
                //forceGrant = false;
                //if (!forceGrant) {
                    // set theme and language for dialog alert ;-)
                    GlobalGUIRoutines.setTheme(this, true, true/*, false*/, false, false);
                    //GlobalGUIRoutines.setLanguage(this);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    if (Build.VERSION.SDK_INT >= 29) {
                        dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_alway_required);
                        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                            requestPermissions(4, withRationale);
                        });
                    }
                    else {
                        if (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))
                            dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm);
                        else
                            dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm_miui);
                        dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, (dialog, which) -> {
                            Permissions.setShowRequestDrawOverlaysPermission(context, false);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                            requestPermissions(4, withRationale);
                        });
                        dialogBuilder.setNegativeButton(R.string.permission_ask_button, (dialog, which) -> {
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                            requestPermissions(4, withRationale);
                        });
                    }
                    dialogBuilder.setOnCancelListener(dialog -> {
                        if (rationaleAlreadyShown)
                            removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                        requestPermissions(4, withRationale);
                    });
                    AlertDialog dialog = dialogBuilder.create();

//                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    if (!isFinishing())
                        dialog.show();
                /*}
                else {
                    //if (requestCode == DRAW_OVERLAYS_REQUEST_CODE)
                        requestPermissions(4);
                    //else
                    //    finishGrant();
                }*/
            }
            else {
                Permissions.setShowRequestDrawOverlaysPermission(context, true);
                //if (requestCode == DRAW_OVERLAYS_REQUEST_CODE)
                    requestPermissions(4, withRationale);
                //else
                //    finishGrant();
            }
        }
        if ((requestCode == Permissions.REQUEST_CODE + grantType)/* || (requestCode == Permissions.REQUEST_CODE_FORCE_GRANT + grantType)*/) {

            boolean finishActivity;// = false;
            boolean permissionsChanged;// = Permissions.getPermissionsChanged(context);

            boolean calendarPermission = Permissions.checkCalendar(context);
            permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
            //PPApplication.logE("GrantPermissionActivity.onActivityResult", "calendarPermission="+permissionsChanged);
            // finish Editor when permission is disabled
            finishActivity = permissionsChanged && (!calendarPermission);
            if (!permissionsChanged) {
                boolean contactsPermission = Permissions.checkContacts(context);
                permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "contactsPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!contactsPermission);
            }
            if (!permissionsChanged) {
                boolean locationPermission = Permissions.checkLocation(context);
                permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "locationPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!locationPermission);
            }
            if (!permissionsChanged) {
                boolean smsPermission = Permissions.checkSMS(context);
                permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "smsPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!smsPermission);
            }
            if (!permissionsChanged) {
                boolean phonePermission = Permissions.checkPhone(context);
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "phonePermission="+phonePermission);
                permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "permissionsChanged="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!phonePermission);
            }
            if (!permissionsChanged) {
                boolean storagePermission = Permissions.checkReadStorage(context);
                permissionsChanged = Permissions.getReadStoragePermission(context) != storagePermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "storagePermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!storagePermission);
            }
            if (!permissionsChanged) {
                boolean storagePermission = Permissions.checkWriteStorage(context);
                permissionsChanged = Permissions.getWriteStoragePermission(context) != storagePermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "storagePermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!storagePermission);
            }
            if (!permissionsChanged) {
                boolean cameraPermission = Permissions.checkCamera(context);
                permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "cameraPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!cameraPermission);
            }
            if (!permissionsChanged) {
                boolean microphonePermission = Permissions.checkMicrophone(context);
                permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "microphonePermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!microphonePermission);
            }
            if (!permissionsChanged) {
                boolean sensorsPermission = Permissions.checkSensors(context);
                permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                //PPApplication.logE("GrantPermissionActivity.onActivityResult", "sensorsPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!sensorsPermission);
            }

            Permissions.saveAllPermissions(context, permissionsChanged);
            //PPApplication.logE("GrantPermissionActivity.onActivityResult", "permissionsChanged="+permissionsChanged);

            if (permissionsChanged) {
//                PPApplication.logE("###### PPApplication.updateGUI", "from=GrantPermissionActivity.onActivityResult");
                PPApplication.updateGUI(true, false, context);

                if (finishActivity) {
                    setResult(Activity.RESULT_CANCELED);
                    finishAffinity();
                }
            }

            if (!finishActivity) {
                boolean granted = false;
                if (permissions != null) {
                    for (Permissions.PermissionType permissionType : permissions) {
                        if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }
                        if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }
                        if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }
                        if (permissionType.permission.equals(Manifest.permission.READ_CALENDAR)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }
                        if (permissionType.permission.equals(Manifest.permission.READ_CONTACTS)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }

                        if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }
                        if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                        }
                        if (Build.VERSION.SDK_INT >= 29) {
                            if (permissionType.permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
//                                Log.e("GrantPermissionActivity", "onActivityResult = ACCESS_BACKGROUND_LOCATION granted="+granted);
                            }
                        }

                    }
                }
                if (granted)
                    finishGrant();
                else
                    showRationale(context);
            }
        }
    }

    private void requestPermissions(int iteration, boolean withRationale) {
//        PPApplication.logE("GrantPermissionActivity.requestPermission","iteration="+iteration);

        if (permissions == null)
            return;
        if (isFinishing())
            return;

        if (iteration == 1) {
            boolean writeSettingsFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, getApplicationContext())) {
                            writeSettingsFound = true;
                            final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + PPApplication.PACKAGE_NAME));
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            //noinspection deprecation
                            startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                            break;
                        }
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", PPApplication.PACKAGE_NAME);
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            startActivityForResult(localIntent, WRITE_SETTINGS_REQUEST_CODE);
                            writeSettingsFound = true;
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", PPApplication.PACKAGE_NAME);
                                intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                                startActivityForResult(localIntent, WRITE_SETTINGS_REQUEST_CODE);
                                writeSettingsFound = true;
                            } catch (Exception e1) {
                                writeSettingsFound = false;
                            }
                        }
                    }*/
                }
            }
            if (!writeSettingsFound)
                requestPermissions(3/*2*/, withRationale);
        }
        /*else
        if (iteration == 2) {
            boolean accessNotificationPolicyFound = false;
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            for (Permissions.PermissionType permissionType : permissions) {
                if (no60 && permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getApplicationContext())) {
                    accessNotificationPolicyFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                    startActivityForResult(intent, ACCESS_NOTIFICATION_POLICY_REQUEST_CODE);
                    break;
                }
            }
            if (!accessNotificationPolicyFound)
                requestPermissions(3, withRationale);
        }*/
        else
        if (iteration == 3) {
            boolean drawOverlaysFound = false;
            //boolean api25 = android.os.Build.VERSION.SDK_INT >= 25;
            for (Permissions.PermissionType permissionType : permissions) {
                if (/*api25 && */permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getApplicationContext())) {
                            drawOverlaysFound = true;
                            final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + PPApplication.PACKAGE_NAME));
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            //noinspection deprecation
                            startActivityForResult(intent, DRAW_OVERLAYS_REQUEST_CODE);
                            break;
                        }
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", PPApplication.PACKAGE_NAME);
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            startActivityForResult(localIntent, DRAW_OVERLAYS_REQUEST_CODE);
                            drawOverlaysFound = true;
                            break;
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", PPApplication.PACKAGE_NAME);
                                intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                                startActivityForResult(localIntent, DRAW_OVERLAYS_REQUEST_CODE);
                                drawOverlaysFound = true;
                                break;
                            } catch (Exception e1) {
                                drawOverlaysFound = false;
                            }
                        }
                    }*/
                }
            }
            if (!drawOverlaysFound)
                requestPermissions(4, withRationale);
        }
        else {
            boolean grantBackgroundLocation = false;
            List<String> permList = new ArrayList<>();
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    grantBackgroundLocation = true;
                }
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                    (!permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) &&
                    (!permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) &&
                    (!permList.contains(permissionType.permission))) {
//                    PPApplication.logE("GrantPermissionActivity.requestPermissions", "permissionType.permission=" + permissionType.permission);

                    //if (ContextCompat.checkSelfPermission(getApplicationContext(), permissionType.permission) != PackageManager.PERMISSION_GRANTED)
                        permList.add(permissionType.permission);
                }
            }

//            PPApplication.logE("GrantPermissionActivity.requestPermissions", "permList.size=" + permList.size());
//            PPApplication.logE("GrantPermissionActivity.requestPermissions", "withRationale=" + withRationale);
//            PPApplication.logE("GrantPermissionActivity.requestPermissions", "rationaleAlreadyShown=" + rationaleAlreadyShown);
            if (permList.size() > 0) {
                if (!withRationale && rationaleAlreadyShown) {
//                    PPApplication.logE("GrantPermissionActivity.requestPermissions", "start application settings");
                    Permissions.saveAllPermissions(getApplicationContext(), false);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:"+PPApplication.PACKAGE_NAME));
                    if (GlobalGUIRoutines.activityIntentExists(intent, getApplicationContext())) {
//                        PPApplication.logE("GrantPermissionActivity.requestPermissions", "intent found");
                        //noinspection deprecation
                        startActivityForResult(intent, Permissions.REQUEST_CODE/*_FORCE_GRANT*/ + grantType);
                    }
                    else {
//                        PPApplication.logE("GrantPermissionActivity.requestPermissions", "intent not found");
                        finishGrant();
                    }
                }
                else {
                    String[] permArray = new String[permList.size()];
                    for (int i = 0; i < permList.size(); i++) {
                        permArray[i] = permList.get(i);
//                        PPApplication.logE("GrantPermissionActivity.requestPermissions", "permArray[i]=" + permArray[i]);
                    }

//                    Log.e("GrantPermissionActivity.recheckPermissions", "grantBackgroundLocation="+grantBackgroundLocation);
                    if (grantBackgroundLocation)
                        ActivityCompat.requestPermissions(this, permArray, BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE);
                    else
                        ActivityCompat.requestPermissions(this, permArray, PERMISSIONS_REQUEST_CODE);
                }
            }
            else {
                Context context = getApplicationContext();
                boolean allGranted = true;
                for (Permissions.PermissionType permissionType : permissions) {
                    if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                        if (!Settings.System.canWrite(context)) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                                allGranted = false;
                                break;
                            }
                        }
                    }
                    if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                        if (!Settings.canDrawOverlays(context)) {
                            allGranted = false;
                            break;
                        }
                    }
                }
                if (allGranted)
                    finishGrant();
                else
                    showRationale(context);
            }
        }
    }

    private void removePermission(final String permission) {
        if (permissions != null) {
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(permission)) {
                    permissions.remove(permissionType);
                    break;
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void finishGrant() {
        final Context context = getApplicationContext();

        if (grantAlsoBackgroundLocation) {
//            Log.e("GrantPermissionActivity.finishGrant", "ACCESS_BACKGROUND_LOCATION");
            Permissions.grantBackgroundLocation(context, this);
            finish();
        }

        PPApplication.registerContentObservers(context);
        PPApplication.registerCallbacks(context);

        /*
        if (forGUI && (profile != null))
        {
            // regenerate profile icon
            dataWrapper.refreshProfileIcon(profile, monochrome, monochromeValue);
        }
        */

        /*if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
            //finishAffinity();
            finish();
            Permissions.removeInstallToneNotification(context);
            TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, context);
        }
        else*/
        if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
            //finishAffinity();
            setResult(Activity.RESULT_OK);
            finish();
            Permissions.removePlayRingtoneNotificationNotification(context);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_IMAGE_WALLPAPER) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.wallpaperViewPreference != null)
                Permissions.wallpaperViewPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WALLPAPER_FOLDER) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.wallpaperViewPreference != null)
                Permissions.wallpaperViewPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.profileIconPreference != null)
                Permissions.profileIconPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT) {
            setResult(Activity.RESULT_OK);
            finish();
            //if (Permissions.editorActivity != null)
            //    Permissions.editorActivity.doExportData();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT_AND_EMAIL) {
            setResult(Activity.RESULT_OK);
            finish();
            //if (Permissions.editorActivity != null)
            //    Permissions.editorActivity.doExportData();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_IMPORT) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Permissions.EXTRA_APPLICATION_DATA_PATH, applicationDataPath);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
            //if (Permissions.editorActivity != null)
            //    Permissions.editorActivity.doImportData(applicationDataPath);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR) {
            setResult(Activity.RESULT_OK);
            finish();
            //if (Permissions.editorActivity != null)
            //    Permissions.editorActivity.doExportData();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.wifiSSIDPreference != null)
                Permissions.wifiSSIDPreference.refreshListView(true, "");*/
            /*if (Permissions.bluetoothNamePreference != null)
                Permissions.bluetoothNamePreference.refreshListView(true, "");*/

            //PPApplication.logE("[RJS] GrantPermissionActivity.finishGrant", "restart wifi and bluetooth scanners");
            PPApplication.restartWifiScanner(context);
            PPApplication.restartBluetoothScanner(context);

            //dataWrapper.restartEvents(false, true/*, false*/, false);
            dataWrapper.restartEventsWithDelay(5, true, false, false, PPApplication.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CALENDAR_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.calendarsMultiSelectDialogPreference != null)
                Permissions.calendarsMultiSelectDialogPreference.refreshListView(true);*/
            //dataWrapper.restartEvents(false, true/*, false*/, false);
            dataWrapper.restartEventsWithDelay(5, true, false, false, PPApplication.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CONTACT_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.contactsMultiSelectDialogPreference != null)
                Permissions.contactsMultiSelectDialogPreference.refreshListView(true);*/
            /*if (Permissions.contactGroupsMultiSelectDialogPreference != null)
                Permissions.contactGroupsMultiSelectDialogPreference.refreshListView(true);*/
            //dataWrapper.restartEvents(false, true/*, false*/, false);
            dataWrapper.restartEventsWithDelay(5, true, false, false, PPApplication.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EVENT) {
            setResult(Activity.RESULT_OK);
            //finishAffinity();
            finish();
            Permissions.removeEventNotification(context);
            if (permissions != null) {
                for (Permissions.PermissionType permissionType : permissions) {

                    if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ((Build.VERSION.SDK_INT >= 29) && permissionType.permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                    ) {
                        //PPApplication.logE("[RJS] GrantPermissionActivity.finishGrant", "restart all scanners");
                        // for screenOn=true -> used only for geofence scanner - start scan with GPS On
                        PPApplication.restartAllScanners(context, false);
                        break;
                    }
                }
            }
            //dataWrapper.restartEvents(false, true/*, false*/, false);
            dataWrapper.restartEventsWithDelay(5, true, false, false, PPApplication.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY) {
            //PPApplication.logE("[RJS] GrantPermissionActivity.finishGrant", "restart geofence scanner");
            PPApplication.restartLocationScanner(context);

            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.locationGeofenceEditorActivity != null)
                Permissions.locationGeofenceEditorActivity.getLastLocation();*/

            //dataWrapper.restartEvents(false, true/*, false*/, false);
            dataWrapper.restartEventsWithDelay(5, true, false, false, PPApplication.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.brightnessDialogPreference != null)
                Permissions.brightnessDialogPreference.enableViews();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.mobileCellsPreference != null)
                Permissions.mobileCellsPreference.refreshListView(true);*/
            //dataWrapper.restartEvents(false, true/*, false*/, false);
            dataWrapper.restartEventsWithDelay(5, true, false, false, PPApplication.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.mobileCellsRegistrationDialogPreference != null)
                Permissions.mobileCellsRegistrationDialogPreference.startRegistration();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.ringtonePreference != null)
                Permissions.ringtonePreference.refreshListView();*/
        }
        /*else
        if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE) {
            //finishAffinity();
            finish();
            Permissions.removeLogToFileNotification(context);
        }*/
        else
        if (grantType == Permissions.GRANT_TYPE_CONNECT_TO_SSID_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.contactsMultiSelectDialogPreference != null)
                Permissions.contactsMultiSelectDialogPreference.refreshListView(true);*/
            /*if (Permissions.contactGroupsMultiSelectDialogPreference != null)
                Permissions.contactGroupsMultiSelectDialogPreference.refreshListView(true);*/
            //dataWrapper.restartEvents(false, true/*, false*/, false);
            //dataWrapper.restartEventsWithDelay(5, false, /*false,*/ DataWrapper.ALTYPE_UNDEFINED);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_BACKGROUND_LOCATION) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.contactsMultiSelectDialogPreference != null)
                Permissions.contactsMultiSelectDialogPreference.refreshListView(true);*/
            /*if (Permissions.contactGroupsMultiSelectDialogPreference != null)
                Permissions.contactGroupsMultiSelectDialogPreference.refreshListView(true);*/
            //dataWrapper.restartEvents(false, true/*, false*/, false);
            //dataWrapper.restartEventsWithDelay(5, false, /*false,*/ DataWrapper.ALTYPE_UNDEFINED);
        }
        else {
            // Profile permission

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("GrantPermissionActivity.finishGrant", "profile");
                PPApplication.logE("GrantPermissionActivity.finishGrant", "startupSource=" + startupSource);
                //PPApplication.logE("GrantPermissionActivity.finishGrant", "interactive="+interactive);
            }*/

            /*Intent returnIntent = new Intent();
            returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            returnIntent.putExtra(Permissions.EXTRA_MERGED_PROFILE, mergedProfile);
            returnIntent.putExtra(Permissions.EXTRA_ACTIVATE_PROFILE, activateProfile);
            setResult(Activity.RESULT_OK,returnIntent);*/

            setResult(Activity.RESULT_OK);
            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(context);
            if (activateProfile) {
                //PPApplication.logE("&&&&&&& GrantPermissionActivity.finishGrant", "called is DataWrapper.activateProfileFromMainThread");
                dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, interactive, null, true);
            }
        }

        if (permissionsForRecheck != null) {
            permissions = Permissions.recheckPermissions(context, permissionsForRecheck);
            if (permissions.size() != 0) {
                PPApplication.showToast(context.getApplicationContext(),
                        context.getString(R.string.toast_permissions_not_granted),
                        Toast.LENGTH_LONG);
            }
        }

        //Permissions.releaseReferences();
        /*if (mergedNotification)
            Permissions.clearMergedPermissions(context);*/

        //if (grantType != Permissions.GRANT_TYPE_PROFILE) {
//            PPApplication.logE("###### PPApplication.updateGUI", "from=GrantPermissionActivity.finishGrant");
            PPApplication.updateGUI(true, false, getApplicationContext());
        //}
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
