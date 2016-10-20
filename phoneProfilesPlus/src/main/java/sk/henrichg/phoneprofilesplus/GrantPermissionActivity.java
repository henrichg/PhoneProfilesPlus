package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GrantPermissionActivity extends Activity {

    private int grantType;
    private List<Permissions.PermissionType> permissions;
    private long profile_id;
    private boolean mergedProfile;
    private boolean onlyNotification;
    private boolean mergedNotification;
    private boolean forGUI;
    private boolean monochrome;
    private int monochromeValue;
    private int startupSource;
    private boolean interactive;
    private boolean log;
    private String applicationDataPath;
    private long event_id;
    private boolean activateProfile;

    private Profile profile;
    private Event event;
    private DataWrapper dataWrapper;

    private boolean started = false;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 909090;
    private static final int PERMISSIONS_REQUEST_CODE = 909091;
    private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE = 909092;

    private static final String NOTIFICATION_DELETED_ACTION = "sk.henrichg.phoneprofilesplus.PERMISSIONS_NOTIFICATION_DELETED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        Intent intent = getIntent();
        grantType = intent.getIntExtra(Permissions.EXTRA_GRANT_TYPE, 0);
        onlyNotification = intent.getBooleanExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        mergedNotification = false;
        if (permissions == null) {
            permissions = GlobalData.getMergedPermissions(getApplicationContext());
            mergedNotification = true;
        }

        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        mergedProfile = intent.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
        forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);
        startupSource = intent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, GlobalData.STARTUP_SOURCE_ACTIVATOR);
        interactive = intent.getBooleanExtra(Permissions.EXTRA_INTERACTIVE, true);
        log = intent.getBooleanExtra(Permissions.EXTRA_LOG, false);
        applicationDataPath = intent.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH);
        activateProfile = intent.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, true);

        event_id = intent.getLongExtra(GlobalData.EXTRA_EVENT_ID, 0);

        dataWrapper = new DataWrapper(getApplicationContext(), forGUI, monochrome, monochromeValue);
        profile = dataWrapper.getProfileById(profile_id, mergedProfile);
        event = dataWrapper.getEventById(event_id);

        //Log.e("GrantPermissionActivity", "onShow grantType="+grantType);
        //Log.e("GrantPermissionActivity", "onShow permissions.size()="+permissions.size());
        //Log.e("GrantPermissionActivity", "onShow onlyNotification="+onlyNotification);
        //if (profile != null)
        //    Log.e("GrantPermissionActivity", "onShow profile._name="+profile._name);
        //if (event != null)
        //    Log.e("GrantPermissionActivity", "onShow event._name="+event._name);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (started) return;
        started = true;

        final Context context = getApplicationContext();

        if (permissions.size() == 0) {
            // called from notification - recheck permissions
            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                boolean granted = Permissions.checkInstallTone(context);
                if (!granted) {
                    permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_INSTALL_TONE, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                }
                else {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else
            if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                boolean granted = Permissions.checkPlayRingtoneNotification(context);
                if (!granted) {
                    permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION, Manifest.permission.READ_EXTERNAL_STORAGE));
                }
                else {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else
            if (grantType == Permissions.GRANT_TYPE_EVENT) {
                // get permissions from shared preferences and recheck it
                permissions = Permissions.recheckPermissions(context, GlobalData.getMergedPermissions(context));
                mergedNotification = true;
                if (permissions.size() == 0) {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else {
                // get permissions from shared preferences and recheck it
                permissions = Permissions.recheckPermissions(context, GlobalData.getMergedPermissions(context));
                mergedNotification = true;
                if (permissions.size() == 0) {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
        }

        boolean showRequestWriteSettings = false;
        boolean showRequestAccessNotificationPolicy = false;
        boolean showRequestReadExternalStorage = false;
        boolean showRequestReadPhoneState = false;
        boolean showRequestProcessOutgoingCalls = false;
        boolean showRequestWriteExternalStorage = false;
        boolean showRequestReadCalendar = false;
        boolean showRequestReadContacts = false;
        boolean showRequestReceiveSMS = false;
        boolean showRequestReadSMS = false;
        boolean showRequestAccessCoarseLocation = false;
        boolean showRequestAccessFineLocation = false;

        //Log.d("GrantPermissionActivity.onStart", "permissions.size="+permissions.size());

        for (Permissions.PermissionType permissionType : permissions) {
            //Log.d("GrantPermissionActivity.onStart", "permissionType.permission="+permissionType.permission);
            //Log.d("GrantPermissionActivity.onStart", "Manifest.permission.WRITE_SETTINGS="+Manifest.permission.WRITE_SETTINGS);

            if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS))
                showRequestWriteSettings = GlobalData.getShowRequestWriteSettingsPermission(context);
            if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY))
                showRequestAccessNotificationPolicy = GlobalData.getShowRequestAccessNotificationPolicyPermission(context);
            if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE))
                showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE);
            if (permissionType.permission.equals(Manifest.permission.PROCESS_OUTGOING_CALLS))
                showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS);
            if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showRequestWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionType.permission.equals(Manifest.permission.READ_CALENDAR))
                showRequestReadCalendar = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR);
            if (permissionType.permission.equals(Manifest.permission.READ_CONTACTS))
                showRequestReadContacts = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS);
            if (permissionType.permission.equals(Manifest.permission.RECEIVE_SMS))
                showRequestReceiveSMS = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS);
            if (permissionType.permission.equals(Manifest.permission.READ_SMS))
                showRequestReadSMS = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS);
            if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                showRequestAccessCoarseLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION))
                showRequestAccessFineLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //Log.d("GrantPermissionActivity.onStart", "showRequestWriteSettings="+showRequestWriteSettings);
        //Log.d("GrantPermissionActivity.onStart", "showRequestReadExternalStorage="+showRequestReadExternalStorage);
        //Log.d("GrantPermissionActivity.onStart", "showRequestReadPhoneState="+showRequestReadPhoneState);
        //Log.d("GrantPermissionActivity.onStart", "showRequestProcessOutgoingCalls="+showRequestProcessOutgoingCalls);
        //Log.d("GrantPermissionActivity.onStart", "showRequestWriteExternalStorage="+showRequestWriteExternalStorage);
        //Log.d("GrantPermissionActivity.onStart", "showRequestReadCalendar="+showRequestReadCalendar);
        //Log.d("GrantPermissionActivity.onStart", "showRequestReadContacts="+showRequestReadContacts);
        //Log.d("GrantPermissionActivity.onStart", "showRequestReceiveSMS="+showRequestReceiveSMS);
        //Log.d("GrantPermissionActivity.onStart", "showRequestReadSMS="+showRequestReadSMS);
        //Log.d("GrantPermissionActivity.onStart", "showRequestAccessCoarseLocation="+showRequestAccessCoarseLocation);
        //Log.d("GrantPermissionActivity.onStart", "showRequestAccessFineLocation="+showRequestAccessFineLocation);
        //Log.d("GrantPermissionActivity.onStart", "showRequestAccessNotificationPolicy="+showRequestAccessNotificationPolicy);

        if (showRequestWriteSettings ||
                showRequestReadExternalStorage ||
                showRequestReadPhoneState ||
                showRequestProcessOutgoingCalls ||
                showRequestWriteExternalStorage ||
                showRequestReadCalendar ||
                showRequestReadContacts ||
                showRequestReceiveSMS ||
                showRequestReadSMS ||
                showRequestAccessCoarseLocation ||
                showRequestAccessFineLocation ||
                showRequestAccessNotificationPolicy) {

            if (onlyNotification) {
                int notificationID;
                NotificationCompat.Builder mBuilder;
                Intent intent = new Intent(context, GrantPermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                    String ntitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                    String ntext = context.getString(R.string.permissions_for_install_tone_big_text_notification);
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        ntitle = context.getString(R.string.app_name);
                        ntext = context.getString(R.string.permissions_for_install_tone_text_notification) + ": " +
                                context.getString(R.string.permissions_for_install_tone_big_text_notification);
                    }
                    mBuilder =   new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(ntitle) // title for notification
                            .setContentText(ntext)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(ntext))
                            .setAutoCancel(true); // clear notification after click
                    notificationID = GlobalData.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID;
                }
                else
                if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                    String ntitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                    String ntext = context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        ntitle = context.getString(R.string.app_name);
                        ntext = context.getString(R.string.permissions_for_install_tone_text_notification) + ": " +
                                context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
                    }
                    mBuilder =   new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(ntitle) // title for notification
                            .setContentText(ntext)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(ntext))
                            .setAutoCancel(true); // clear notification after click
                    notificationID = GlobalData.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID;
                }
                else
                if (grantType == Permissions.GRANT_TYPE_EVENT) {
                    String ntitle = context.getString(R.string.permissions_for_event_text_notification);
                    String ntext = "";
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        ntitle = context.getString(R.string.app_name);
                        ntext = context.getString(R.string.permissions_for_event_text_notification)+": ";
                    }
                    if (mergedNotification) {
                        ntext = ntext + context.getString(R.string.permissions_for_event_text1m) + " " +
                                context.getString(R.string.permissions_for_event_big_text_notification);
                    }
                    else {
                        ntext = ntext + context.getString(R.string.permissions_for_event_text1) + " ";
                        if (event != null)
                            ntext = ntext + "\"" + event._name + "\" ";
                        ntext = ntext + context.getString(R.string.permissions_for_event_big_text_notification);
                    }
                    mBuilder =   new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(ntitle) // title for notification
                            .setContentText(ntext) // message for notification
                            .setAutoCancel(true); // clear notification after click
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ntext));
                    Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
                    PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, 0);
                    mBuilder.setDeleteIntent(deletePendingIntent);

                    intent.putExtra(GlobalData.EXTRA_EVENT_ID, event._id);
                    notificationID = GlobalData.GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID;
                }
                else {
                    String ntitle = context.getString(R.string.permissions_for_profile_text_notification);
                    String ntext = "";
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        ntitle = context.getString(R.string.app_name);
                        ntext = context.getString(R.string.permissions_for_profile_text_notification)+": ";
                    }
                    if (mergedProfile || mergedNotification) {
                        ntext = ntext + context.getString(R.string.permissions_for_profile_text1m) + " " +
                                context.getString(R.string.permissions_for_profile_big_text_notification);
                    }
                    else {
                        ntext = ntext + context.getString(R.string.permissions_for_profile_text1) + " ";
                        if (profile != null)
                            ntext = ntext + "\"" + profile._name + "\" ";
                        ntext = ntext + context.getString(R.string.permissions_for_profile_big_text_notification);
                    }
                    mBuilder =   new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(ntitle) // title for notification
                            .setContentText(ntext) // message for notification
                            .setAutoCancel(true); // clear notification after click
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ntext));
                    Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
                    PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, 0);
                    mBuilder.setDeleteIntent(deletePendingIntent);

                    intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                    intent.putExtra(Permissions.EXTRA_FOR_GUI, forGUI);
                    intent.putExtra(Permissions.EXTRA_MONOCHROME, monochrome);
                    intent.putExtra(Permissions.EXTRA_MONOCHROME_VALUE, monochromeValue);
                    notificationID = GlobalData.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID;
                }
                permissions.clear();
                intent.putExtra(Permissions.EXTRA_GRANT_TYPE, grantType);
                intent.putParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES, (ArrayList<Permissions.PermissionType>) permissions);
                intent.putExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
                intent.putExtra(GlobalData.EXTRA_STARTUP_SOURCE, startupSource);
                intent.putExtra(Permissions.EXTRA_INTERACTIVE, interactive);
                intent.putExtra(Permissions.EXTRA_LOG, log);

                PendingIntent pi = PendingIntent.getActivity(context, grantType, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                if (android.os.Build.VERSION.SDK_INT >= 16)
                    mBuilder.setPriority(Notification.PRIORITY_MAX);
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }
                NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(notificationID, mBuilder.build());

                finish();
                return;
            }
            else {
                String showRequestString = "";

                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                    showRequestString = context.getString(R.string.permissions_for_install_tone_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION)
                    showRequestString = context.getString(R.string.permissions_for_play_ringtone_notification_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                    showRequestString = context.getString(R.string.permissions_for_wallpaper_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                    showRequestString = context.getString(R.string.permissions_for_custom_profile_icon_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_EXPORT)
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
                else
                if (grantType == Permissions.GRANT_TYPE_EVENT){
                    if (mergedNotification) {
                        showRequestString = context.getString(R.string.permissions_for_event_text1m) + " ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_event_text2) + "<br><br>";
                    }
                    else {
                        showRequestString = context.getString(R.string.permissions_for_event_text1) + " ";
                        if (event != null)
                            showRequestString = showRequestString + "\"" + event._name + "\" ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_event_text2) + "<br><br>";
                    }
                }
                else {
                    if (mergedProfile || mergedNotification) {
                        showRequestString = context.getString(R.string.permissions_for_profile_text1m) + " ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    }
                    else {
                        showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
                        if (profile != null)
                            showRequestString = showRequestString + "\"" + profile._name + "\" ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    }
                }

                if (showRequestWriteSettings) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestWriteSettings");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadExternalStorage || showRequestWriteExternalStorage) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadExternalStorage");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadPhoneState || showRequestProcessOutgoingCalls) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadPhoneState");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_phone) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadCalendar) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadCalendar");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_calendar) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadContacts) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadContacts");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_contacts) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReceiveSMS || showRequestReadSMS) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReceiveSMS");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_sms) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestAccessCoarseLocation || showRequestAccessFineLocation) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadCalendar");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_location) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestAccessNotificationPolicy) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestAccesNotificationPolicy");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_access_notification_policy) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }

                showRequestString = showRequestString + "<br>";

                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_install_tone_text2);
                else if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_play_ringtone_notification_text2);
                else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_wallpaper_text2);
                else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_custom_profile_icon_text2);
                else if (grantType == Permissions.GRANT_TYPE_EXPORT)
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
                else
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

                // set theme and language for dialog alert ;-)
                // not working on Android 2.3.x
                GUIData.setTheme(this, true, false);
                GUIData.setLanguage(this.getBaseContext());

                final boolean _showRequestWriteSettings = showRequestWriteSettings;
                final boolean _showRequestAccessNotificationPolicy = showRequestAccessNotificationPolicy;

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(Html.fromHtml(showRequestString));
                dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int iteration = 3;
                        if (_showRequestWriteSettings)
                            iteration = 1;
                        else if (_showRequestAccessNotificationPolicy)
                            iteration = 2;
                        requestPermissions(iteration);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                        Permissions.releaseReferences();
                        if (mergedNotification)
                            GlobalData.clearMergedPermissions(context);
                    }
                });
                dialogBuilder.show();
            }
        }
        else {
            GlobalData.logE("GrantPermissionActivity.onStart","no show request dialog");
            requestPermissions(3);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.

                boolean allGranted = true;
                for (int i=0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    finishGrant();
                } else {
                    if (!onlyNotification) {
                        Context context = getApplicationContext();
                        Toast msg = Toast.makeText(context,
                                context.getResources().getString(R.string.app_name) + ": " +
                                        context.getResources().getString(R.string.toast_permissions_not_granted),
                                Toast.LENGTH_SHORT);
                        msg.show();
                    }
                    finish();
                    Permissions.releaseReferences();
                    if (mergedNotification)
                        GlobalData.clearMergedPermissions(getApplicationContext());
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context context = getApplicationContext();
        if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {

            if (!Settings.System.canWrite(context)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(R.string.permissions_write_settings_not_allowed_confirm);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GlobalData.setShowRequestWriteSettingsPermission(context, false);
                        requestPermissions(2);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GlobalData.setShowRequestWriteSettingsPermission(context, true);
                        requestPermissions(2);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestPermissions(2);
                    }
                });
                dialogBuilder.show();
            }
            else {
                GlobalData.setShowRequestWriteSettingsPermission(context, true);
                requestPermissions(2);
            }
        }
        if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(R.string.permissions_access_notification_policy_not_allowed_confirm);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GlobalData.setShowRequestAccessNotificationPolicyPermission(context, false);
                        requestPermissions(3);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GlobalData.setShowRequestAccessNotificationPolicyPermission(context, true);
                        requestPermissions(3);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestPermissions(3);
                    }
                });
                dialogBuilder.show();
            }
            else {
                GlobalData.setShowRequestAccessNotificationPolicyPermission(context, true);
                requestPermissions(3);
            }
        }
    }

    private void requestPermissions(int iteration) {

        if (iteration == 1) {
            boolean writeSettingsFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    writeSettingsFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                    break;
                }
            }
            if (!writeSettingsFound)
                requestPermissions(2);
        }
        else
        if (iteration == 2) {
            boolean accessNotificationPolicyFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                    accessNotificationPolicyFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivityForResult(intent, ACCESS_NOTIFICATION_POLICY_REQUEST_CODE);
                    break;
                }
            }
            if (!accessNotificationPolicyFound)
                requestPermissions(3);
        }
        else {
            List<String> permList = new ArrayList<String>();
            for (Permissions.PermissionType permissionType : permissions) {
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                    (!permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) &&
                    (!permList.contains(permissionType.permission))) {
                    //Log.e("GrantPermissionActivity", "requestPermissions - permission=" + permissionType.permission);
                    //if (ContextCompat.checkSelfPermission(getApplicationContext(), permissionType.permission) != PackageManager.PERMISSION_GRANTED)
                        permList.add(permissionType.permission);
                }
            }

            GlobalData.logE("GrantPermissionActivity.requestPermissions", "permList.size=" + permList.size());
            if (permList.size() > 0) {

                String[] permArray = new String[permList.size()];
                for (int i = 0; i < permList.size(); i++) permArray[i] = permList.get(i);

                ActivityCompat.requestPermissions(this, permArray, PERMISSIONS_REQUEST_CODE);
            }
            else
                finishGrant();
        }
    }

    private void finishGrant() {
        Context context = getApplicationContext();

        ActivateProfileHelper activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(dataWrapper, Permissions.profileActivationActivity, context);

        if (forGUI && (profile != null))
        {
            // regenerate profile icon
            dataWrapper.refreshProfileIcon(profile, monochrome, monochromeValue);
        }

        if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
            //finishAffinity();
            finish();
            Permissions.removeInstallToneNotification(context);
            FirstStartService.installTone(FirstStartService.TONE_ID, FirstStartService.TONE_NAME, context, true);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
            //finishAffinity();
            finish();
            Permissions.removePlayRingtoneNotificationNotification(context);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WALLPAPER) {
            finish();
            if (Permissions.imageViewPreference != null)
                Permissions.imageViewPreference.startGallery();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            finish();
            if (Permissions.profileIconPreference != null)
                Permissions.profileIconPreference.startGallery();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT) {
            finish();
            if (Permissions.editorActivity != null)
                Permissions.editorActivity.doExportData();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_IMPORT) {
            finish();
            if (Permissions.editorActivity != null)
                Permissions.editorActivity.doImportData(applicationDataPath);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG) {
            if (Permissions.wifiSSIDPreference != null)
                Permissions.wifiSSIDPreference.refreshListView(true);
            if (Permissions.bluetoothNamePreference != null)
                Permissions.bluetoothNamePreference.refreshListView(true);
            dataWrapper.restartEvents(false, true, false);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0)
                WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0)
                BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, false);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0)
                GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, false, false);
            finish();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CALENDAR_DIALOG) {
            if (Permissions.calendarsMultiSelectDialogPreference != null)
                Permissions.calendarsMultiSelectDialogPreference.refreshListView();
            dataWrapper.restartEvents(false, true, false);
            finish();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CONTACT_DIALOG) {
            if (Permissions.contactsMultiSelectDialogPreference != null)
                Permissions.contactsMultiSelectDialogPreference.refreshListView();
            if (Permissions.contactGroupsMultiSelectDialogPreference != null)
                Permissions.contactGroupsMultiSelectDialogPreference.refreshListView();
            dataWrapper.restartEvents(false, true, false);
            finish();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EVENT) {
            //finishAffinity();
            finish();
            Permissions.removeEventNotification(context);
            dataWrapper.restartEvents(false, true, false);
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0)
                        WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false);
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0)
                        BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, false);
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0)
                        GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, false, false);
                    break;
                }
            }
        }
        else
        if (grantType == Permissions.GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY) {
            if (Permissions.locationGeofenceEditorActivity != null) {
                Permissions.locationGeofenceEditorActivity.refreshActivity(true);
            }
            dataWrapper.restartEvents(false, true, false);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0)
                WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0)
                BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, false);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0)
                GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, false, false);
            finish();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            finish();
            if (Permissions.brightnessDialogPreference != null)
                Permissions.brightnessDialogPreference.enableViews();
        }
        else {
            // Profile permission

            GlobalData.logE("GrantPermissionActivity.finishGrant", "profile");
            GlobalData.logE("GrantPermissionActivity.finishGrant", "startupSource="+startupSource);
            GlobalData.logE("GrantPermissionActivity.finishGrant", "interactive="+interactive);

            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(context);
            if (activateProfile) {
                if ((Permissions.profileActivationActivity != null) && (profile._askForDuration)) {
                    FastAccessDurationDialog dlg = new FastAccessDurationDialog(Permissions.profileActivationActivity,
                            profile, dataWrapper, startupSource, interactive, log);
                    dlg.show();
                } else
                    dataWrapper._activateProfile(profile, mergedProfile, startupSource, interactive,
                            Permissions.profileActivationActivity, log);
            }
        }
        Permissions.releaseReferences();
        if (mergedNotification)
            GlobalData.clearMergedPermissions(context);

        //if (grantType != Permissions.GRANT_TYPE_PROFILE) {
            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if ((activatedProfile == null) || (activatedProfile._id == profile_id))
                activateProfileHelper.showNotification(profile);
            activateProfileHelper.updateWidget();

            Intent intent5 = new Intent();
            intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
            intent5.putExtra(RefreshGUIBroadcastReceiver.EXTRA_REFRESH_ICONS, true);
            context.sendBroadcast(intent5);
        //}
    }

}
