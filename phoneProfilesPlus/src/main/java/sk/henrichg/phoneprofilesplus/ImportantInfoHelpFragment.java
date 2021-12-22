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
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.skydoves.expandablelayout.ExpandableLayout;

public class ImportantInfoHelpFragment extends Fragment {

    boolean firstInstallation = false;

    ExpandableLayout expandableLayoutSystem;
    ExpandableLayout expandableLayoutProfiles;
    ExpandableLayout expandableLayoutEvents;

    public ImportantInfoHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.important_info_fragment_important_info, container, false);
        return inflater.inflate(R.layout.important_info_fragment_important_info_expandable, container, false);
    }

    @SuppressLint({"SetTextI18n", "BatteryLife"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        expandableLayoutSystem = view.findViewById(R.id.fragment_important_info_expandable_system);
        expandableLayoutProfiles = view.findViewById(R.id.fragment_important_info_expandable_profiles);
        expandableLayoutEvents = view.findViewById(R.id.fragment_important_info_expandable_events);
        expandableLayoutSystem.setOnClickListener(v -> {
            if (!expandableLayoutSystem.isExpanded()) {
                expandableLayoutProfiles.collapse();
                expandableLayoutEvents.collapse();
            }
            expandableLayoutSystem.toggleLayout();
        });
        expandableLayoutProfiles.setOnClickListener(v -> {
            if (!expandableLayoutProfiles.isExpanded()) {
                expandableLayoutSystem.collapse();
                expandableLayoutEvents.collapse();
            }
            expandableLayoutProfiles.toggleLayout();
        });
        expandableLayoutEvents.setOnClickListener(v -> {
            if (!expandableLayoutEvents.isExpanded()) {
                expandableLayoutSystem.collapse();
                expandableLayoutProfiles.collapse();
            }
            expandableLayoutEvents.toggleLayout();
        });

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

        doOnViewCreated(view, this);

        if ((!firstInstallation) && (extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.VISIBLE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setText(getString(R.string.important_info_accessibility_service_new_version_2) +  " \u21D2");
            infoText1.setVisibility(View.VISIBLE);
            infoText1.setOnClickListener(v -> installExtender(getActivity(), false));
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

    @SuppressLint("SetTextI18n")
    static void doOnViewCreated(@NonNull View view, Fragment fragment) {

        final Activity activity = fragment.getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        /*        if (news4550) {
            if (Build.VERSION.SDK_INT >= 28) {
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                if (infoText21 != null) {
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
                }
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
                if (infoText22 != null)
                    infoText22.setVisibility(View.GONE);
                news = true;
            }
            else {
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
                if (infoText22 != null)
                    infoText22.setVisibility(View.GONE);
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location_news);
                if (infoText21 != null)
                    infoText21.setVisibility(View.GONE);
            }
        }
        else {*/
        TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
        if (infoText22 != null) {
            if (Build.VERSION.SDK_INT >= 28) {
                infoText22.setText(context.getString(R.string.important_info_profile_mobileCellsScanning_location) +  " \u21D2");
                infoText22.setOnClickListener(v -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            fragment.startActivity(intent);
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
            } else {
                infoText22.setVisibility(View.GONE);
            }
        }
        //}

        TextView infoText20 = view.findViewById(R.id.activity_info_notification_wifiScanning_location);
        if (infoText20 != null) {
            infoText20.setText(context.getString(R.string.important_info_profile_wifiScanning_location) +  " \u21D2");
            infoText20.setOnClickListener(v -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivity(intent);
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
        infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
        if (infoText22 != null) {
            infoText22.setText(context.getString(R.string.important_info_profile_bluetoothScanning_location) +  " \u21D2");
            infoText22.setOnClickListener(v -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivity(intent);
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

        TextView infoText10a = view.findViewById(R.id.activity_info_notification_app_standby);
        if (infoText10a != null) {
            infoText10a.setText(context.getString(R.string.important_info_android_doze_mode) +  " \u21D2");
            infoText10a.setOnClickListener(v -> {
//                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//                    String packageName = PPApplication.PACKAGE_NAME;
//                    if (pm.isIgnoringBatteryOptimizations(packageName)// ||
//                        //(!GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, context))
//                    ) {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivity(intent);
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
            });
        }

        TextView infoText670 = view.findViewById(R.id.activity_info_notification_do_not_kill_my_app);
        if (infoText670 != null) {
            infoText670.setText(fragment.getString(R.string.important_info_do_not_kill_my_app1) + " " +
                    fragment.getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_webSiteName) + " " +
                    fragment.getString(R.string.important_info_do_not_kill_my_app2) + " \u21D2");
            infoText670.setOnClickListener(v -> PPApplication.showDoNotKillMyAppDialog(activity));
        }

        TextView infoText40 = view.findViewById(R.id.activity_info_default_profile);
        if (infoText40 != null) {
            infoText40.setText(context.getString(R.string.important_info_default_profile) +  " \u21D2");
            infoText40.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                fragment.startActivity(intent);
            });
        }

        infoText10a = view.findViewById(R.id.activity_info_notification_application_settings);
        if (infoText10a != null) {
            infoText10a.setText(context.getString(R.string.important_info_android_look_application_settings) +  " \u21D2");
            infoText10a.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                fragment.startActivity(intent);
            });
        }

        TextView infoText100 = view.findViewById(R.id.activity_info_profile_activation2);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text2) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text3) + "</li>" +
                    "</ol>";
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        }
        infoText100 = view.findViewById(R.id.activity_info_profile_activation3);
        if (infoText100 != null) {
            String text =
                    fragment.getString(R.string.important_info_profile_activation_text4) + "\n" +
                            fragment.getString(R.string.important_info_profile_activation_text5) + "\n\n" +
                            fragment.getString(R.string.important_info_profile_activation_text6)
            ;
            infoText100.setText(text);
        }

        infoText100 = view.findViewById(R.id.activity_info_profile_activation9);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text9) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text10) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text11) + "</li>" +
                    "</ul>";
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }
        infoText100 = view.findViewById(R.id.activity_info_event_activation9);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text9) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text10) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_activation_text11) + "</li>" +
                    "</ul>";
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_preference_types);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_root) + "</li>" +
                    //"<li>" + fragment.getString(R.string.important_info_profile_settings) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_interactive) +
                    "</ul>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_3);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant_1_howTo_3) + "</li>" +
                    "</ol>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        }
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_4);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant_1_howTo_4) + "</li>" +
                    "</ol>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 2, 17));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_6);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant_1_howTo_6) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant_1_howTo_7) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant_1_howTo_8) +
                    "</ol>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        }
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_10);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.important_info_profile_grant_1_howTo_10) + "</li>" +
                    "</ol>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 4, 17));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_event_not_started_1);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.info_notification_event_not_started_2) + "</li>" +
                    "<li>" + fragment.getString(R.string.info_notification_event_not_started_3) + "</li>" +
                    "<li>" + fragment.getString(R.string.info_notification_event_not_started_4) + "</li>" +
                    "<li>" + fragment.getString(R.string.info_notification_event_priority_new) +
                    "</ol>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_event_event_sensors_2);
        if (infoText100 != null) {
            String text = "<ol>" +
                    "<li>" + fragment.getString(R.string.important_info_event_event_sensors_waiting) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_event_event_sensors_passed) + "</li>" +
                    "<li>" + fragment.getString(R.string.important_info_event_event_sensors_not_pased) + "</li>" +
                    "</ol>"
                    ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));
        }

        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_1);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.info_notification_manage_events_from_tasker_restart_events) + "</li>" +
                    "</ul>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_2);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.info_notification_manage_events_from_tasker_enable_run_for_event) + "</li>" +
                    "</ul>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_3);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.info_notification_manage_events_from_tasker_pause_event) + "</li>" +
                    "</ul>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_4);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.info_notification_manage_events_from_tasker_stop_event) + "</li>" +
                    "</ul>"
            ;
            infoText100.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));
        }

        TextView infoText41 = view.findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        if (infoText41 != null) {
            String str = "Send Intent [\n" +  //↵
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE\n" +
                    " Extra:profile_name:profile name\n" +
                    " Target:Activity\n" +
                    "]";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText41.setText(spannable);
        }

        TextView infoText42 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_restart_events);
        if (infoText42 != null) {
            String str = "Send Intent [\n" +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS\n" +
                    " Target:Activity\n" +
                    "]";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText42.setText(spannable);
        }
        TextView infoText43 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_enable_run_for_event);
        if (infoText43 != null) {
            String str = "Send Intent [\n" +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT\n" +
                    " Extra:event_name:event name\n" +
                    " Target:Activity\n" +
                    "]";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText43.setText(spannable);
        }
        TextView infoText44 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_pause_event);
        if (infoText44 != null) {
            String str = "Send Intent [\n" +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT\n" +
                    " Extra:event_name:event name\n" +
                    " Target:Activity\n" +
                    "]";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText44.setText(spannable);
        }
        TextView infoText45 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_stop_event);
        if (infoText45 != null) {
            String str = "Send Intent [\n" +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT\n" +
                    " Extra:event_name:event name\n" +
                    " Target:Activity\n" +
                    "]";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText45.setText(spannable);
        }

        TextView infoTextADBDownload = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_11);
        if (infoTextADBDownload != null) {
            //str = getString(R.string.important_info_profile_grant_1_howTo_11);
            String str = "https://developer.android.com/studio/releases/platform-tools.html";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoTextADBDownload.setText(spannable);
        }

        TextView infoTextGrant1Command = view.findViewById(R.id.activity_info_notification_dialog_info_grant_1_command);
        if (infoTextGrant1Command != null) {
            String str = "adb\u00A0shell\u00A0pm\u00A0grant\u00A0" + PPApplication.PACKAGE_NAME + "\u00A0" +
                    "android.permission.WRITE_SECURE_SETTINGS";
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoTextGrant1Command.setText(spannable);
        }

        if (view.findViewById(R.id.activity_info_notification_contact) != null) {
            AboutApplicationActivity.emailMe(view.findViewById(R.id.activity_info_notification_contact),
                    fragment.getString(R.string.important_info_contact),
                    "", fragment.getString(R.string.about_application_support_subject),
                    AboutApplicationActivity.getEmailBodyText(/*AboutApplicationActivity.EMAIL_BODY_SUPPORT, */activity),
                    /*true,*/ activity);
        }

        TextView translationTextView = view.findViewById(R.id.activity_info_translations);
        if (translationTextView != null) {
            String str1 = fragment.getString(R.string.about_application_translations);
            String str2 = str1 + " " + PPApplication.CROWDIN_URL + " \u21D2";
            Spannable spannable = new SpannableString(str2);
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
                        fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            };
            spannable.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            translationTextView.setText(spannable);
            translationTextView.setMovementMethod(LinkMovementMethod.getInstance());
            /*AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true, activity);*/
        }

        TextView helpForG1TextView = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_0);
        if (helpForG1TextView != null) {
            String str1 = fragment.getString(R.string.important_info_profile_grant_1_howTo_0) + " " +
                    fragment.getString(R.string.important_info_profile_grant_1_howTo_0_1) + ":";
            String str2 = str1 + " " + PPApplication.HELP_HOW_TO_GRANT_G1_URL + " \u21D2";
            Spannable spannable = new SpannableString(str2);
            //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    String url = PPApplication.HELP_HOW_TO_GRANT_G1_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            };
            spannable.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            helpForG1TextView.setText(spannable);
            helpForG1TextView.setMovementMethod(LinkMovementMethod.getInstance());
            /*AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true, activity);*/
        }
    }

    @SuppressLint("SetTextI18n")
    static private void installExtenderFromGitHub(Activity activity, boolean finishActivity) {
        if (activity == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
        }
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
        dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\"\n";
        dialogText = dialogText + activity.getString(R.string.install_extender_text2) + "\n";
        dialogText = dialogText + activity.getString(R.string.install_extender_text3);

        text.setText(dialogText);

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            //String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
            //String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL_1 + PPApplication.VERSION_NAME_EXTENDER_LATEST + PPApplication.GITHUB_PPPE_DOWNLOAD_URL_2;
            String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                if (finishActivity)
                    activity.finish();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });
        dialogBuilder.setCancelable(false);
        /*dialogBuilder.setOnCancelListener(dialog -> {
            if (finishActivity)
                activity.finish();
        });*/

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPE_RELEASES_URL + " \u21D2";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    dialog.cancel();
                    //if (activity != null)
                    if (finishActivity)
                        activity.finish();
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());


//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (/*(activity != null) &&*/ (!activity.isFinishing()))
            dialog.show();
    }

    @SuppressLint("SetTextI18n")
    static void installExtender(Activity activity, boolean finishActivity) {
        if (activity == null) {
            return;
        }

        if (PPApplication.deviceIsSamsung) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint("InflateParams")
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
                dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\".\n\n";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    activity.startActivity(intent);
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                if (finishActivity)
                    activity.finish();
            });
            dialogBuilder.setCancelable(false);
            /*dialogBuilder.setOnCancelListener(dialog -> {
                if (finishActivity)
                    activity.finish();
            });*/

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            button.setText(activity.getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub(activity, finishActivity);
            });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if (/*(activity != null) &&*/ (!activity.isFinishing()))
                dialog.show();
        }
/*        else if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            @SuppressLint("InflateParams")
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
                dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".\n\n";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("appmarket://details?id=sk.henrichg.phoneprofilesplusextender"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            //button.setText(getActivity().getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub();
            });

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
        }*/
        else
            installExtenderFromGitHub(activity, finishActivity);
    }

}
