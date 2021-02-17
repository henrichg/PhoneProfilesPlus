package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

@SuppressWarnings("WeakerAccess")
public class ImportantInfoHelpFragment extends Fragment {

    int scrollTo = 0;
    boolean firstInstallation = false;

    public ImportantInfoHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.important_info_fragment_important_info, container, false);
    }

    @SuppressLint({"SetTextI18n", "BatteryLife"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final Activity activity = getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        int versionCode = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            versionCode = PPApplication.getVersionCode(pInfo);
            //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "versionCode="+versionCode);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        boolean news = false;
        boolean newsLatest = (!firstInstallation) && (versionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "newsLatest="+newsLatest);

        /*
        boolean news4550 = (!firstInstallation) && (versionCode >= 4550) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news4550="+news4550);
        boolean news4340 = (!firstInstallation) && (versionCode >= 4340) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news4340="+news4340);
        boolean news3985 = (!firstInstallation) && (versionCode >= 3985) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news3985="+news3985);
        boolean news3670 = (!firstInstallation) && ((versionCode >= 3670) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news3670="+news3670);
        boolean news3640 = (!firstInstallation) && ((versionCode >= 3640) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news3640="+news3640);
        boolean news2190 = (!firstInstallation) && ((versionCode >= 2190) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news2190="+news2190);
        boolean news1804 = (!firstInstallation) && ((versionCode >= 1804) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news1804="+news1804);
        boolean news1772 = (!firstInstallation) && ((versionCode >= 1772) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news1772="+news1772);
        */

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);

        //noinspection StatementWithEmptyBody
        if (newsLatest) {
            // move this to newXXX, for switch off news

            //news = news ||
            //        news_extender;
        }
        else {
            // move this to newXXX, for switch off news

        }

/*        if (news4550) {
            if (Build.VERSION.SDK_INT >= 28) {
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                infoText21.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception ignored) {}
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
                infoText22.setVisibility(View.GONE);
                news = true;
            }
            else {
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
                infoText22.setVisibility(View.GONE);
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                infoText21.setVisibility(View.GONE);
            }
        }
        else {*/
            TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
            if (Build.VERSION.SDK_INT >= 28) {
                //TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                //infoText21.setVisibility(View.GONE);
                infoText22.setOnClickListener(v -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!activity.isFinishing())
                            dialog.show();
                    }
                });
            }
            else {
            infoText22.setVisibility(View.GONE);
                //TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                //infoText21.setVisibility(View.GONE);
            }
        //}

        /*if (news4340) {
            news = true;

            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            dataWrapper.fillEventList();
            boolean sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS);
            if (!sensorExists)
                sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL);
            if (!sensorExists) {
                // extender is not needed
                TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_text2);
                infoText1.setVisibility(View.GONE);
            }
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_text2);
            infoText1.setVisibility(View.GONE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_text3);
            infoText1.setVisibility(View.GONE);
        }*/

        /*
        if (news3985) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_privacy_policy_backup_files_2_news);
            infoText1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://sites.google.com/site/phoneprofilesplus/home/privacy-policy";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                }
            });
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_privacy_policy_backup_files_news);
            infoText1.setVisibility(View.GONE);
            infoText1 = view.findViewById(R.id.activity_info_notification_privacy_policy_backup_files_2_news);
            infoText1.setVisibility(View.GONE);
        }
        */

        /*if (news3670) {
            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            dataWrapper.fillEventList();
            boolean sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION);
            if (!sensorExists)
                sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION);
            boolean news_extender = true;
            if (!sensorExists) {
                // extender is installed or not needed
                news_extender = false;
                TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_text1);
                infoText1.setVisibility(View.GONE);
            }
            news = news || news_extender;
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_text1);
            infoText1.setVisibility(View.GONE);
        }*/

        /*if (news3640) {
            news = true;
        }
        else {
            // empty this, for switch off news
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_profile_grant_news);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = view.findViewById(R.id.activity_info_notification_profile_grant_lookSectionProfiles_news);
            infoText2.setVisibility(View.GONE);
        }*/

        /*if (news2190) {
            TextView infoText1 = view.findViewById(R.id.activity_info_event_start_order1);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = view.findViewById(R.id.activity_info_event_start_order2);
            infoText2.setVisibility(View.GONE);
            news = true;
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_event_start_order1_news);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = view.findViewById(R.id.activity_info_event_start_order2_news);
            infoText2.setVisibility(View.GONE);
        }*/

        /*if (news1804) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                TextView infoText16 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root);
                infoText18.setVisibility(View.GONE);
                TextView infoText19 = view.findViewById(R.id.activity_info_notification_wifiScanning_location_news);
                infoText19.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception ignored) {}
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
                TextView infoText20 = view.findViewById(R.id.activity_info_notification_wifiScanning_location);
                infoText20.setVisibility(View.GONE);
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location_news);
                infoText21.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception ignored) {}
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
                infoText22.setVisibility(View.GONE);
                news = true;
            }
        }
        else {*/
            //TextView infoText15 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root_news);
            //infoText15.setVisibility(View.GONE);
            //TextView infoText17 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root_news);
            //infoText17.setVisibility(View.GONE);
            //TextView infoText19 = view.findViewById(R.id.activity_info_notification_wifiScanning_location_news);
            //infoText19.setVisibility(View.GONE);
            //if (Build.VERSION.SDK_INT >= 23) {
                TextView infoText20 = view.findViewById(R.id.activity_info_notification_wifiScanning_location);
                infoText20.setOnClickListener(v -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!activity.isFinishing())
                            dialog.show();
                    }
                });
                //TextView infoText21 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location_news);
                //infoText21.setVisibility(View.GONE);
                infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
                infoText22.setOnClickListener(v -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!activity.isFinishing())
                            dialog.show();
                    }
                });

                TextView infoText10a = view.findViewById(R.id.activity_info_notification_app_standby);
                infoText10a.setOnClickListener(v -> {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    String packageName = PPApplication.PACKAGE_NAME;
                    if (pm.isIgnoringBatteryOptimizations(packageName)) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, context)) {
                            try {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });

                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    } else {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, context)) {
                            try {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + packageName));
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception ignored) {
                            }
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
            //}
        //}

        /*if (news1772) {
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
                infoText14.setVisibility(View.GONE);

                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    TextView infoText13 = view.findViewById(R.id.activity_info_notification_profile_zenMode_news);
                    infoText13.setVisibility(View.GONE);
                }
                else {
                    TextView infoText13 = view.findViewById(R.id.activity_info_notification_profile_zenMode_news);
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        infoText13.setText(R.string.important_info_profile_zenModeM);
                    infoText13.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean ok = false;
                            if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                                try {
                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                    startActivity(intent);
                                    ok = true;
                                } catch (Exception ignored) {}
                            }
                            if (!ok) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });
                                if (!activity.isFinishing())
                                    dialog.show();
                            }
                        }
                    });
                    news = true;
                }
            //}
        }
        else {*/
            //TextView infoText13 = view.findViewById(R.id.activity_info_notification_profile_zenMode_news);
            //infoText13.setVisibility(View.GONE);

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if (/*(android.os.Build.VERSION.SDK_INT >= 23) &&*/ (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
                infoText14.setVisibility(View.GONE);
            }
            else {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
                //if (android.os.Build.VERSION.SDK_INT >= 23)
                //    infoText14.setText(R.string.important_info_profile_zenModeM);
                infoText14.setOnClickListener(v -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, context)) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!activity.isFinishing())
                            dialog.show();
                    }
                });
            }
        //}

        /*if (android.os.Build.VERSION.SDK_INT < 23) {
            //TextView infoText15 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root_news);
            //infoText15.setVisibility(View.GONE);
            TextView infoText16 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root);
            infoText16.setVisibility(View.GONE);
            //TextView infoText17 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root_news);
            //infoText17.setVisibility(View.GONE);
            TextView infoText18 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root);
            infoText18.setVisibility(View.GONE);
            //TextView infoText19 = view.findViewById(R.id.activity_info_notification_wifiScanning_location_news);
            //infoText19.setVisibility(View.GONE);
            TextView infoText20 = view.findViewById(R.id.activity_info_notification_wifiScanning_location);
            infoText20.setVisibility(View.GONE);
            //TextView infoText21 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location_news);
            //infoText21.setVisibility(View.GONE);
            TextView infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
            infoText22.setVisibility(View.GONE);
            TextView infoText10a = view.findViewById(R.id.activity_info_notification_app_standby);
            infoText10a.setVisibility(View.GONE);
        }*/

        /*
        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText13 = view.findViewById(R.id.activity_info_notification_profile_zenMode_news);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
            infoText14.setVisibility(View.GONE);
        }
        */

        /*
        if (ApplicationPreferences.preferences.getBoolean(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES, true)) {
            // detection of volumes merge = volumes are merged
            TextView infoText3 = view.findViewById(R.id.activity_info_notification_unlink_ringer_notification_volumes);
            infoText3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "categorySystemRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivity(intent);
                }
            });
        }
        else {
            TextView infoText3 = view.findViewById(R.id.activity_info_notification_unlink_ringer_notification_volumes);
            infoText3.setVisibility(View.GONE);
        }
        */

        /*
        KillerManager.init(activity);
        if (KillerManager.isActionAvailable(activity, KillerManager.Actions.ACTION_POWERSAVING)) {
            TextView infoText = view.findViewById(R.id.activity_info_notification_power_manager);
            infoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        KillerManager.doActionPowerSaving(activity);
                    }catch (Exception e) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
        }
        else {
            TextView infoText = view.findViewById(R.id.activity_info_notification_power_manager);
            infoText.setVisibility(View.GONE);
        }
        */

        TextView infoText670 = view.findViewById(R.id.activity_info_notification_do_not_kill_my_app);
        infoText670.setText(getString(R.string.important_info_do_not_kill_my_app1) + " " +
                getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_webSiteName) + " " +
                getString(R.string.important_info_do_not_kill_my_app2));
        infoText670.setOnClickListener(v -> PPApplication.showDoNotKillMyAppDialog(ImportantInfoHelpFragment.this));

        TextView infoText40 = view.findViewById(R.id.activity_info_default_profile);
        infoText40.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
            //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
            startActivity(intent);
        });


        infoText10a = view.findViewById(R.id.activity_info_notification_application_settings);
        infoText10a.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
            //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
            startActivity(intent);
        });

        TextView infoText100 = view.findViewById(R.id.activity_info_profile_activation2);
        String text = "<ol>"+
                        "<li>"+getString(R.string.important_info_profile_activation_text2) + "</li>" +
                        "<li>"+getString(R.string.important_info_profile_activation_text3) + "</li>" +
                      "</ol>"
                ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        infoText100 = view.findViewById(R.id.activity_info_profile_activation3);
        text =
                getString(R.string.important_info_profile_activation_text4) + "\n" +
                getString(R.string.important_info_profile_activation_text5) + "\n\n" +
                getString(R.string.important_info_profile_activation_text6)
                ;
        infoText100.setText(text);

        infoText100.setText(text);
        infoText100 = view.findViewById(R.id.activity_info_profile_activation9);
        text =  "<ul>"+
                "<li>" + getString(R.string.important_info_profile_activation_text9) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_activation_text10) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_activation_text11) + "</li>" +
                "</ul>"
                ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_preference_types);
        text =  "<ul>"+
                "<li>" + getString(R.string.important_info_profile_grant) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_root) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_settings) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_interactive) +
                "</ul>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_3);
        text =  "<ol>" +
                "<li>" + getString(R.string.important_info_profile_grant_1_howTo_3) + "</li>" +
                "</ol>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_4);
        text =  "<ol>" +
                "<li>" + getString(R.string.important_info_profile_grant_1_howTo_4) + "</li>" +
                "</ol>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 2, 17));

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_6);
        text =  "<ol>" +
                "<li>" + getString(R.string.important_info_profile_grant_1_howTo_6) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_grant_1_howTo_7) + "</li>" +
                "<li>" + getString(R.string.important_info_profile_grant_1_howTo_8) +
                "</ol>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_10);
        text =  "<ol>" +
                "<li>" + getString(R.string.important_info_profile_grant_1_howTo_10) + "</li>" +
                "</ol>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 4, 17));


        infoText100 = view.findViewById(R.id.activity_info_notification_event_not_started_1);
        text =  "<ol>" +
                "<li>" + getString(R.string.info_notification_event_not_started_2) + "</li>" +
                "<li>" + getString(R.string.info_notification_event_not_started_3) + "</li>" +
                "<li>" + getString(R.string.info_notification_event_not_started_4) + "</li>" +
                "<li>" + getString(R.string.info_notification_event_priority_new) +
                "</ol>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));

        TextView infoText41 = view.findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        String str = "Send Intent [\n" +  //↵
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE\n" +
                " Extra:profile_name:profile name\n" +
                " Target:Activity\n" +
                "]";

        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_1);
        text =  "<ul>"+
                "<li>" + getString(R.string.info_notification_manage_events_from_tasker_restart_events) + "</li>" +
                "</ul>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_2);
        text =  "<ul>"+
                "<li>" + getString(R.string.info_notification_manage_events_from_tasker_enable_run_for_event) + "</li>" +
                "</ul>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_3);
        text =  "<ul>"+
                "<li>" + getString(R.string.info_notification_manage_events_from_tasker_pause_event) + "</li>" +
                "</ul>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_4);
        text =  "<ul>"+
                "<li>" + getString(R.string.info_notification_manage_events_from_tasker_stop_event) + "</li>" +
                "</ul>"
        ;
        infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));


        Spannable spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText41.setText(spannable);
        TextView infoText42 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_restart_events);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText42.setText(spannable);
        TextView infoText43 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_enable_run_for_event);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText43.setText(spannable);
        TextView infoText44 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_pause_event);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText44.setText(spannable);
        TextView infoText45 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_stop_event);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText45.setText(spannable);

        TextView infoTextADBDownload = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_11);
        str = getString(R.string.important_info_profile_grant_1_howTo_11);
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextADBDownload.setText(spannable);

        TextView infoTextGrant1Command = view.findViewById(R.id.activity_info_notification_dialog_info_grant_1_command);
        str = "adb\u00A0shell\u00A0pm\u00A0grant\u00A0"+PPApplication.PACKAGE_NAME+"\u00A0" +
                                "android.permission.WRITE_SECURE_SETTINGS";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextGrant1Command.setText(spannable);

        AboutApplicationActivity.emailMe(view.findViewById(R.id.activity_info_notification_contact),
                getString(R.string.important_info_contact),
                "", getString(R.string.about_application_support_subject),
                AboutApplicationActivity.getEmailBodyText(/*AboutApplicationActivity.EMAIL_BODY_SUPPORT, */activity),
                /*true,*/ activity);

        TextView translationTextView = view.findViewById(R.id.activity_info_translations);
        String str1 = getString(R.string.about_application_translations);
        String str2 = str1 + " " + PPApplication.CROWDIN_URL;
        spannable = new SpannableString(str2);
        //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.CROWDIN_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        spannable.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        translationTextView.setText(spannable);
        translationTextView.setMovementMethod(LinkMovementMethod.getInstance());
        /*AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true, activity);*/

        if ((!firstInstallation) && (extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.VISIBLE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setVisibility(View.VISIBLE);
            infoText1.setOnClickListener(v -> installExtender(getString(R.string.event_preferences_PPPExtenderInstallInfo_summary) + "\n\n" +
                    getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_2) + " " +
                    getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3)));
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.GONE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setVisibility(View.GONE);
        }

        if (!news) {
            TextView infoTextNews = view.findViewById(R.id.activity_info_notification_news);
            infoTextNews.setVisibility(View.GONE);
        }

        if ((scrollTo != 0) && (savedInstanceState == null)) {
            final ScrollView scrollView = view.findViewById(R.id.fragment_important_info_scroll_view);
            final View viewToScroll = view.findViewById(scrollTo);
            if ((scrollView != null) && (viewToScroll != null)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ImportantInfoHelpFragment.onViewCreated");
                    scrollView.scrollTo(0, viewToScroll.getTop());
                }, 200);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void installExtender(String dialogText) {
        if (getActivity() == null) {
            return;
        }

        /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);
        dialogBuilder.setMessage(dialogText);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);*/

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_install_extender, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_extender_dialog_info_text);
        text.setText(dialogText);

        Button button = layout.findViewById(R.id.install_extender_dialog_showAssets);
        button.setText(getActivity().getString(R.string.install_extender_where_is_assets_button) + " \"Assets\"?");
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GitHubAssetsScreenshotActivity.class);
            intent.putExtra(GitHubAssetsScreenshotActivity.EXTRA_IMAGE, R.drawable.phoneprofilesplusextender_assets_screenshot);
            startActivity(intent);
        });

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();
    }

}
