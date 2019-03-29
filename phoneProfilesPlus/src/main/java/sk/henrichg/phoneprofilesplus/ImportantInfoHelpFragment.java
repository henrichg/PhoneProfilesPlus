package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thelittlefireman.appkillermanager.managers.KillerManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ImportantInfoHelpFragment extends Fragment {

    public ImportantInfoHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_important_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final Activity activity = getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        int versionCode = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "versionCode="+versionCode);
        } catch (Exception ignored) {
        }

        boolean news = false;
        boolean newsLatest = (versionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "newsLatest="+newsLatest);
        boolean news4340 = (versionCode >= 4340) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news4340="+news4340);
        boolean news3985 = (versionCode >= 3985) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news3985="+news3985);
        boolean news3670 = (versionCode >= 3670); // news for PhoneProfilesPlusExtender - show it when not activated
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news3670="+news3670);
        boolean news3640 = ((versionCode >= 3640) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news3640="+news3640);
        boolean news2190 = ((versionCode >= 2190) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news2190="+news2190);
        boolean news1804 = ((versionCode >= 1804) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news1804="+news1804);
        boolean news1772 = ((versionCode >= 1772) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news1772="+news1772);

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);

        //noinspection StatementWithEmptyBody
        if (newsLatest) {
            // move this to newXXX, for switch off news

            if (Build.VERSION.SDK_INT >= 28) {
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                infoText21.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
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

            //news = news ||
            //        news_extender;
        }
        else {
            // move this to newXXX, for switch off news

            if (Build.VERSION.SDK_INT >= 28) {
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                infoText21.setVisibility(View.GONE);
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
                infoText22.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
            }
            else {
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
                infoText22.setVisibility(View.GONE);
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                infoText21.setVisibility(View.GONE);
            }
        }

        if (news4340) {
            news = true;

            int smsSensorsCount = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false);
            int callSensorsCount = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false);
            if ((smsSensorsCount == 0) && (callSensorsCount == 0)) {
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
        }

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

        if (news3670) {
            int applicationSensorsCount = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION, false);
            int orientationSensorsCount = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
            boolean news_extender = true;
            if ((applicationSensorsCount == 0) && (orientationSensorsCount == 0)) {
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
        }

        if (news3640) {
            news = true;
        }
        else {
            // empty this, for switch off news
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_profile_grant_news);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = view.findViewById(R.id.activity_info_notification_profile_grant_lookSectionProfiles_news);
            infoText2.setVisibility(View.GONE);
        }

        if (news2190) {
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
        }

        if (news1804) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                TextView infoText16 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root);
                infoText18.setVisibility(View.GONE);
                TextView infoText19 = view.findViewById(R.id.activity_info_notification_wifiScanning_location_news);
                infoText19.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
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
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
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
        else {
            TextView infoText15 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root_news);
            infoText15.setVisibility(View.GONE);
            TextView infoText17 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root_news);
            infoText17.setVisibility(View.GONE);
            TextView infoText19 = view.findViewById(R.id.activity_info_notification_wifiScanning_location_news);
            infoText19.setVisibility(View.GONE);
            TextView infoText20 = view.findViewById(R.id.activity_info_notification_wifiScanning_location);
            infoText20.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivity(intent);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positive != null) positive.setAllCaps(false);
                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                if (negative != null) negative.setAllCaps(false);
                            }
                        });*/
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
            TextView infoText21 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location_news);
            infoText21.setVisibility(View.GONE);
            TextView infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
            infoText22.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivity(intent);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positive != null) positive.setAllCaps(false);
                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                if (negative != null) negative.setAllCaps(false);
                            }
                        });*/
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
            TextView infoText10a = view.findViewById(R.id.activity_info_notification_app_standby);
            infoText10a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, context)) {
                        @SuppressLint("InlinedApi")
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivity(intent);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positive != null) positive.setAllCaps(false);
                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                if (negative != null) negative.setAllCaps(false);
                            }
                        });*/
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
        }

        if (news1772) {
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
                            if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                startActivity(intent);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                                if (!activity.isFinishing())
                                    dialog.show();
                            }
                        }
                    });
                    news = true;
                }
            //}
        }
        else {
            TextView infoText13 = view.findViewById(R.id.activity_info_notification_profile_zenMode_news);
            infoText13.setVisibility(View.GONE);

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
                infoText14.setVisibility(View.GONE);
            }
            else {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    infoText14.setText(R.string.important_info_profile_zenModeM);
                infoText14.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 23) {
            TextView infoText15 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root_news);
            infoText15.setVisibility(View.GONE);
            TextView infoText16 = view.findViewById(R.id.activity_info_notification_profile_ringerMode_root);
            infoText16.setVisibility(View.GONE);
            TextView infoText17 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root_news);
            infoText17.setVisibility(View.GONE);
            TextView infoText18 = view.findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root);
            infoText18.setVisibility(View.GONE);
            TextView infoText19 = view.findViewById(R.id.activity_info_notification_wifiScanning_location_news);
            infoText19.setVisibility(View.GONE);
            TextView infoText20 = view.findViewById(R.id.activity_info_notification_wifiScanning_location);
            infoText20.setVisibility(View.GONE);
            TextView infoText21 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location_news);
            infoText21.setVisibility(View.GONE);
            TextView infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
            infoText22.setVisibility(View.GONE);
            TextView infoText10a = view.findViewById(R.id.activity_info_notification_app_standby);
            infoText10a.setVisibility(View.GONE);
        }

        /*
        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText13 = view.findViewById(R.id.activity_info_notification_profile_zenMode_news);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = view.findViewById(R.id.activity_info_notification_profile_zenMode);
            infoText14.setVisibility(View.GONE);
        }
        */

        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context)) {
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
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
            /* currently not implemented in KillerManager
            if (!(device instanceof Sony)) {
                infoText = view.findViewById(R.id.activity_info_notification_sony_stamina_mode);
                infoText.setVisibility(View.GONE);
            }*/
            if (!Build.MANUFACTURER.equalsIgnoreCase("sony")) {
                infoText = view.findViewById(R.id.activity_info_notification_sony_stamina_mode);
                infoText.setVisibility(View.GONE);
            }
        }
        else {
            TextView infoText = view.findViewById(R.id.activity_info_notification_power_manager);
            infoText.setVisibility(View.GONE);
            if (!Build.MANUFACTURER.equalsIgnoreCase("sony")) {
                infoText = view.findViewById(R.id.activity_info_notification_sony_stamina_mode);
                infoText.setVisibility(View.GONE);
            }
        }

        TextView infoText40 = view.findViewById(R.id.activity_info_default_profile);
        infoText40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivity(intent);
            }
        });


        TextView infoText100 = view.findViewById(R.id.activity_info_profile_activation1);
        String text =
                getString(R.string.important_info_profile_activation_text1) + "\n" +
                getString(R.string.important_info_profile_activation_text2) + "\n" +
                getString(R.string.important_info_profile_activation_text3) + "\n" +
                getString(R.string.important_info_profile_activation_text4) + "\n" +
                getString(R.string.important_info_profile_activation_text5) + "\n\n" +
                getString(R.string.important_info_profile_activation_text6)
                ;
        infoText100.setText(text);
        infoText100 = view.findViewById(R.id.activity_info_profile_activation9);
        text =
                getString(R.string.important_info_profile_activation_text9) + "\n" +
                getString(R.string.important_info_profile_activation_text10) + "\n" +
                getString(R.string.important_info_profile_activation_text11) + "\n"
                ;
        infoText100.setText(text);
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_preference_types);
        text =
                getString(R.string.important_info_profile_grant) + "\n\n" +
                getString(R.string.important_info_profile_root) + "\n\n" +
                getString(R.string.important_info_profile_settings) + "\n\n" +
                getString(R.string.important_info_profile_interactive)
        ;
        infoText100.setText(text);
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_6);
        text =
                getString(R.string.important_info_profile_grant_1_howTo_6) + "\n" +
                getString(R.string.important_info_profile_grant_1_howTo_7) + "\n" +
                getString(R.string.important_info_profile_grant_1_howTo_8)
        ;
        infoText100.setText(text);
        infoText100 = view.findViewById(R.id.activity_info_notification_event_not_started);
        text =
                getString(R.string.info_notification_event_not_started) + "\n" +
                getString(R.string.info_notification_event_priority_new)
        ;
        infoText100.setText(text);

        TextView infoText41 = view.findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        String str = "Send Intent [\n" +  //↵
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE\n" +
                " Extra:profile_name:profile name\n" +
                " Target:Activity\n" +
                "]";
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
        str = "adb\u00A0shell\u00A0pm\u00A0grant\u00A0"+context.getPackageName()+"\u00A0" +
                                "android.permission.WRITE_SECURE_SETTINGS";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextGrant1Command.setText(spannable);

        AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_notification_contact),
                getString(R.string.important_info_contact),
                "", getString(R.string.about_application_support_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_SUPPORT, activity),
                true, activity);
        AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true, activity);


        if ((extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.VISIBLE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setVisibility(View.VISIBLE);
            infoText1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                }
            });
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

    }

}
