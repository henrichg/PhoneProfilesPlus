package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
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
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/** @noinspection ExtractMethodRecommender*/
public class ImportantInfoHelpFragment extends Fragment {

    boolean firstInstallation = false;

    //ExpandableLayout expandableLayoutSystem;
    //ExpandableLayout expandableLayoutProfiles;
    //ExpandableLayout expandableLayoutEvents;

    public ImportantInfoHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.important_info_fragment_important_info, container, false);
        return inflater.inflate(R.layout.fragment_important_info_important_info_expandable, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImportantInfoActivity activity = (ImportantInfoActivity)getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        activity.expandableLayoutSystem = view.findViewById(R.id.fragment_important_info_expandable_system);
        activity.expandableLayoutProfiles = view.findViewById(R.id.fragment_important_info_expandable_profiles);
        activity.expandableLayoutEvents = view.findViewById(R.id.fragment_important_info_expandable_events);
        activity.expandableLayoutSystem.setOnClickListener(v -> {
            if (!activity.expandableLayoutSystem.isExpanded()) {
                activity.expandableLayoutProfiles.collapse();
                activity.expandableLayoutEvents.collapse();
            }
            activity.expandableLayoutSystem.toggleLayout();
        });
        activity.expandableLayoutProfiles.setOnClickListener(v -> {
            if (!activity.expandableLayoutProfiles.isExpanded()) {
                activity.expandableLayoutSystem.collapse();
                activity.expandableLayoutEvents.collapse();
            }
            activity.expandableLayoutProfiles.toggleLayout();
        });
        activity.expandableLayoutEvents.setOnClickListener(v -> {
            if (!activity.expandableLayoutEvents.isExpanded()) {
                activity.expandableLayoutSystem.collapse();
                activity.expandableLayoutProfiles.collapse();
            }
            activity.expandableLayoutEvents.toggleLayout();
        });

        int versionCode = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            versionCode = PPApplicationStatic.getVersionCode(pInfo);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        boolean news = false;
        boolean newsLatest = (!firstInstallation) && (versionCode >= PPApplication.PPP_VERSION_CODE_FOR_IMPORTANT_INFO_NEWS);

        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
        int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(context);

        if (newsLatest) {
            news = PPApplication.SHOW_IMPORTANT_INFO_NEWS; // news is enabled, news must be also in layout
        }

        doOnViewCreated(view, this);

        if ((!firstInstallation) && (extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED)) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.VISIBLE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setText(getString(R.string.important_info_accessibility_service_new_version_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            infoText1.setVisibility(View.VISIBLE);
            infoText1.setOnClickListener(v -> ExtenderDialogPreferenceFragment.installPPPExtender(getActivity(), null, false));
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.GONE);
            infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setVisibility(View.GONE);
        }

        if ((!firstInstallation) && (ppppsVersion != 0) && (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_REQUIRED)) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_pppps_new_version);
            infoText1.setVisibility(View.VISIBLE);
            infoText1 = view.findViewById(R.id.activity_info_notification_pppps_new_version_2);
            infoText1.setText(getString(R.string.important_info_pppps_new_version_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            infoText1.setVisibility(View.VISIBLE);
            infoText1.setOnClickListener(v -> PPPPSDialogPreferenceFragment.installPPPPutSettings(getActivity(), null, false));
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_pppps_new_version);
            infoText1.setVisibility(View.GONE);
            infoText1 = view.findViewById(R.id.activity_info_notification_pppps_new_version_2);
            infoText1.setVisibility(View.GONE);
        }


        TextView infoTextNews = view.findViewById(R.id.activity_info_notification_news);
        TextView infoTextnews1 = view.findViewById(R.id.important_info_news_1);
        if (!news) {
            infoTextNews.setVisibility(View.GONE);
            if (infoTextnews1 != null)
                infoTextnews1.setVisibility(View.GONE);
        } else {
            infoTextNews.setVisibility(View.VISIBLE);
            infoTextNews.setText("*** " + getString(R.string.important_info_news) + " ***");

            //TODO add textVews of News
            if (infoTextnews1 != null) {
                infoTextnews1.setVisibility(View.VISIBLE);
            }
        }

    }

    @SuppressLint("SetTextI18n")
    static void doOnViewCreated(@NonNull View view, Fragment fragment) {

        final Activity activity = fragment.getActivity();
        if (activity == null)
            return;

        final Context context = activity.getApplicationContext();

        TextView infoText22 = view.findViewById(R.id.activity_info_notification_mobileCellsScanning_location);
        if (infoText22 != null) {
            if (Build.VERSION.SDK_INT >= 28) {
                infoText22.setText(context.getString(R.string.important_info_profile_mobileCellsScanning_location) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
                infoText22.setOnClickListener(v -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            fragment.startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                activity.getString(R.string.location_settings_button_tooltip),
                                activity.getString(R.string.setting_screen_not_found_alert),
                                activity.getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                activity
                        );

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
            infoText20.setText(context.getString(R.string.important_info_profile_wifiScanning_location) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            infoText20.setOnClickListener(v -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivity(intent);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            activity.getString(R.string.location_settings_button_tooltip),
                            activity.getString(R.string.setting_screen_not_found_alert),
                            activity.getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.show();
                }
            });
        }
        infoText22 = view.findViewById(R.id.activity_info_notification_bluetoothScanning_location);
        if (infoText22 != null) {
            infoText22.setText(context.getString(R.string.important_info_profile_bluetoothScanning_location) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            infoText22.setOnClickListener(v -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivity(intent);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            activity.getString(R.string.location_settings_button_tooltip),
                            activity.getString(R.string.setting_screen_not_found_alert),
                            activity.getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.show();
                }
            });
        }

        TextView infoText10a = view.findViewById(R.id.activity_info_notification_app_standby);
        if (infoText10a != null) {
            infoText10a.setText(context.getString(R.string.important_info_android_doze_mode) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
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
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            activity.getString(R.string.phone_profiles_pref_applicationBatteryOptimization),
                            activity.getString(R.string.setting_screen_not_found_alert),
                            activity.getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.show();
                }
            });
        }

        TextView infoText670 = view.findViewById(R.id.activity_info_notification_do_not_kill_my_app);
        if (infoText670 != null) {
            infoText670.setText(fragment.getString(R.string.important_info_do_not_kill_my_app1) + " " +
                    fragment.getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_webSiteName) + " " +
                    fragment.getString(R.string.important_info_do_not_kill_my_app2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            infoText670.setOnClickListener(v -> PPApplicationStatic.showDoNotKillMyAppDialog(activity));
        }

        TextView infoText40 = view.findViewById(R.id.activity_info_default_profile);
        if (infoText40 != null) {
            infoText40.setText(context.getString(R.string.important_info_default_profile) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            infoText40.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_PROFILE_ACTIVATION_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                fragment.startActivity(intent);
            });
        }

        infoText10a = view.findViewById(R.id.activity_info_notification_application_settings);
        if (infoText10a != null) {
            infoText10a.setText(context.getString(R.string.important_info_android_look_application_settings) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
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
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                             fragment.getString(R.string.important_info_profile_activation_text2) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_activation_text3) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 1, 17, false));
        }
        infoText100 = view.findViewById(R.id.activity_info_profile_activation3);
        if (infoText100 != null) {
            String text =
                    fragment.getString(R.string.important_info_profile_activation_text4) + StringConstants.CHAR_NEW_LINE +
                            fragment.getString(R.string.important_info_profile_activation_text5) + StringConstants.STR_DOUBLE_NEWLINE +
                            fragment.getString(R.string.important_info_profile_activation_text6)
            ;
            infoText100.setText(text);
        }

        /*
        infoText100 = view.findViewById(R.id.activity_info_profile_activation9);
        if (infoText100 != null) {
            String text = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.traffic_light_green) + " " + fragment.getString(R.string.important_info_profile_activation_text9) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.traffic_light_orange) + " " + fragment.getString(R.string.important_info_profile_activation_text10) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.traffic_light_red) + " " + fragment.getString(R.string.important_info_profile_activation_text11) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }
        infoText100 = view.findViewById(R.id.activity_info_event_activation9);
        if (infoText100 != null) {
            String text = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.traffic_light_green) + " " + fragment.getString(R.string.important_info_profile_activation_text9) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.traffic_light_orange) + " " + fragment.getString(R.string.important_info_profile_activation_text10) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.traffic_light_red) + " " + fragment.getString(R.string.important_info_profile_activation_text11) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }
        */

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_preference_types);
        if (infoText100 != null) {
            String text =StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.important_info_profile_install_pppps) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_grant) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_root) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.phone_profiles_pref_grantShizukuPermission_summary1) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    //"<li>" + fragment.getString(R.string.important_info_profile_settings) + "</li>" +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_interactive) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML
                    ;
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_3);
        if (infoText100 != null) {
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                        fragment.getString(R.string.important_info_profile_grant_1_howTo_3) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 1, 17, false));
        }
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_4);
        if (infoText100 != null) {
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                        fragment.getString(R.string.important_info_profile_grant_1_howTo_4) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 2, 17, false));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_6);
        if (infoText100 != null) {
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.important_info_profile_grant_1_howTo_6) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_grant_1_howTo_7) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_grant_1_howTo_8) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 1, 17, false));
        }
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_10);
        if (infoText100 != null) {
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                        fragment.getString(R.string.important_info_profile_grant_1_howTo_10) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 4, 17, false));
        }
        infoText100 = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_20);
        if (infoText100 != null) {
            String text = fragment.getString(R.string.important_info_profile_grant_1_howTo_20_1) + StringConstants.TAG_BREAK_HTML +
                    fragment.getString(R.string.important_info_profile_grant_1_howTo_20_2) +
                    StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.important_info_profile_grant_1_howTo_20_3) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_grant_1_howTo_20_4) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_profile_grant_1_howTo_20_5) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML +
                    fragment.getString(R.string.important_info_profile_grant_1_howTo_20_6);
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_event_not_started_1);
        if (infoText100 != null) {
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.info_notification_event_not_started_2) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.info_notification_event_not_started_3) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.info_notification_event_not_started_4) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.info_notification_event_priority_new) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 1, 17, false));
        }

        infoText100 = view.findViewById(R.id.activity_info_notification_event_event_sensors_2);
        if (infoText100 != null) {
            String text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML +
                                                               fragment.getString(R.string.important_info_event_event_sensors_waiting) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_event_event_sensors_passed) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + fragment.getString(R.string.important_info_event_event_sensors_not_pased) +
                    StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML
                    ;
            infoText100.setText(StringFormatUtils.fromHtml(text, false,  true, 1, 17, false));
        }

        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_1);
        if (infoText100 != null) {
            String text = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                        fragment.getString(R.string.info_notification_manage_events_from_tasker_restart_events) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_2);
        if (infoText100 != null) {
            String text = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                        fragment.getString(R.string.info_notification_manage_events_from_tasker_enable_run_for_event) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }
        /*
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_3);
        if (infoText100 != null) {
            String text = "<ul>" +
                    "<li>" + fragment.getString(R.string.info_notification_manage_events_from_tasker_pause_event) + "</li>" +
                    "</ul>"
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, true, true, false, 0, 0, false));
        }
        */
        infoText100 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_4);
        if (infoText100 != null) {
            String text = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                        fragment.getString(R.string.info_notification_manage_events_from_tasker_stop_event) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML
            ;
            infoText100.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));
        }

        TextView infoText41 = view.findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        if (infoText41 != null) {
            String str = "Send Intent ["+StringConstants.CHAR_NEW_LINE +  //↵
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE"+StringConstants.CHAR_NEW_LINE +
                    " Extra:profile_name:profile name"+StringConstants.CHAR_NEW_LINE +
                    " Target:Activity"+StringConstants.CHAR_NEW_LINE +
                    "]";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText41.setText(spannable);
        }

        TextView infoText42 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_restart_events);
        if (infoText42 != null) {
            String str = "Send Intent ["+StringConstants.CHAR_NEW_LINE +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS"+StringConstants.CHAR_NEW_LINE +
                    " Target:Activity"+StringConstants.CHAR_NEW_LINE +
                    "]";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText42.setText(spannable);
        }
        TextView infoText43 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_enable_run_for_event);
        if (infoText43 != null) {
            String str = "Send Intent ["+StringConstants.CHAR_NEW_LINE +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT"+StringConstants.CHAR_NEW_LINE +
                    " Extra:event_name:event name"+StringConstants.CHAR_NEW_LINE +
                    " Target:Activity"+StringConstants.CHAR_NEW_LINE +
                    "]";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText43.setText(spannable);
        }
        /*
        TextView infoText44 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_pause_event);
        if (infoText44 != null) {
            String str = "Send Intent [\n" +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT\n" +
                    " Extra:event_name:event name\n" +
                    " Target:Activity\n" +
                    "]";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText44.setText(spannable);
        }
        */
        TextView infoText45 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_stop_event);
        if (infoText45 != null) {
            String str = "Send Intent ["+StringConstants.CHAR_NEW_LINE +
                    " Action:sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT"+StringConstants.CHAR_NEW_LINE +
                    " Extra:event_name:event name"+StringConstants.CHAR_NEW_LINE +
                    " Target:Activity"+StringConstants.CHAR_NEW_LINE +
                    "]";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoText45.setText(spannable);
        }

        TextView infoTextADBDownload = view.findViewById(R.id.activity_info_notification_profile_grant_1_howTo_11);
        if (infoTextADBDownload != null) {
            //str = getString(R.string.important_info_profile_grant_1_howTo_11);
            String str = "https://developer.android.com/studio/releases/platform-tools.html";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoTextADBDownload.setText(spannable);
        }

        TextView infoTextGrant1Command = view.findViewById(R.id.activity_info_notification_dialog_info_grant_1_command);
        if (infoTextGrant1Command != null) {
            String str = "adb"+StringConstants.CHAR_HARD_SPACE+"shell"+StringConstants.CHAR_HARD_SPACE+"pm"+StringConstants.CHAR_HARD_SPACE+"grant"+StringConstants.CHAR_HARD_SPACE + PPApplication.PACKAGE_NAME + StringConstants.CHAR_HARD_SPACE +
                    "android.permission.WRITE_SECURE_SETTINGS";
            Spannable spannable = new SpannableString(str);
            //spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(activity)), 0, str.length(),
            //        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.activityCommandBackgroundColor)),
                    0, str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            infoTextGrant1Command.setText(spannable);
        }

        if (view.findViewById(R.id.activity_info_notification_contact) != null) {
            /*GlobalUtils.emailMe(view.findViewById(R.id.activity_info_notification_contact),
                    fragment.getString(R.string.important_info_contact),
                    "", fragment.getString(R.string.about_application_support_subject),
                    GlobalUtils.getEmailBodyTextactivity),
                    activity);*/
            final TextView supportText = view.findViewById(R.id.activity_info_notification_contact);
            if (supportText != null) {
                supportText.setText(context.getString(R.string.important_info_support) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
                supportText.setOnClickListener(v -> {
                    PopupMenu popup;
                    popup = new PopupMenu(activity, supportText, Gravity.START | Gravity.BOTTOM);
                    new MenuInflater(activity).inflate(R.menu.menu_support, popup.getMenu());

                    Menu menu = popup.getMenu();
                    MenuItem menuItem = menu.findItem(R.id.menu_discord);
                    if (menuItem != null) {
                        SubMenu subMenu = menuItem.getSubMenu();
                        if (subMenu != null) {
                            Drawable triangle = ContextCompat.getDrawable(activity, R.drawable.ic_submenu_triangle);
                            if (triangle != null) {
                                triangle.setTint(ContextCompat.getColor(activity, R.color.activitySecondaryTextColor));
                                SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                                triangle.setBounds(0,
                                        GlobalGUIRoutines.sip(1),
                                        GlobalGUIRoutines.sip(10.5f),
                                        GlobalGUIRoutines.sip(8.5f));
                                headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                subMenu.setHeaderTitle(headerTitle);
                            }
                        }
                    }

                    ImportantInfoActivity importantInfoActivity = (ImportantInfoActivity) activity;
                    popup.setOnMenuItemClickListener(importantInfoActivity::supportMenu);

                    if (!activity.isFinishing())
                        popup.show();
                });
            }

        }

        TextView translationTextView = view.findViewById(R.id.activity_info_translations);
        if (translationTextView != null) {
            String str1 = fragment.getString(R.string.about_application_translations);
            String str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.CROWDIN_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                        PPApplicationStatic.recordException(e);
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
            String str2;
            if (DebugVersion.enabled)
                str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.HELP_HOW_TO_GRANT_G1_URL_DEVEL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
            else
                str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.HELP_HOW_TO_GRANT_G1_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                    String url;
                    if (DebugVersion.enabled)
                        url = PPApplication.HELP_HOW_TO_GRANT_G1_URL_DEVEL;
                    else
                        url = PPApplication.HELP_HOW_TO_GRANT_G1_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
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

/*
        TextView configureNotificationInfoText = view.findViewById(R.id.important_info_notification_settings_configure);
        if (configureNotificationInfoText != null) {
            configureNotificationInfoText.setText(context.getString(R.string.important_info_notification_settings_configure) +  " »»");
            configureNotificationInfoText.setOnClickListener(v -> {

                synchronized (PPApplication.applicationPreferencesMutex) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                    editor.putString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, "0");
                    ApplicationPreferences.notificationNotificationStyle = "0";
                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, false);
                    ApplicationPreferences.notificationUseDecoration = false;
                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, true);
                    ApplicationPreferences.notificationShowProfileIcon = true;
                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, true);
                    ApplicationPreferences.notificationPrefIndicator = true;
                    editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, "0");
                    ApplicationPreferences.notificationLayoutType = "0";
                    editor.apply();
                }

//
//                if (PhoneProfilesService.getInstance() != null) {
//                    synchronized (PPApplication.applicationPreferencesMutex) {
//                        PPApplication.doNotShowProfileNotification = true;
//                    }
//                    PhoneProfilesService.getInstance().clearProfileNotification();
//                }
//
                //PhoneProfilesService.getInstance().showProfileNotification(false, true, true);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] ImportantInfoHelpFragment.doOnViewCreated", "call of PPAppNotification.forceDrawNotification");
                PPAppNotification.forceDraweNotification(context.getApplicationContext());

                PPApplication.showToast(context,
                        context.getString(R.string.important_info_notification_settings_toast),
                        Toast.LENGTH_SHORT);
            });
        }
*/
        TextView helpForPPPPSTextView = view.findViewById(R.id.activity_info_notification_profile_pppps_howTo_2);
        if (helpForPPPPSTextView != null) {
            String str1 = fragment.getString(R.string.important_info_profile_pppps_howTo_3) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
            Spannable spannable = new SpannableString(str1);
            //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    PPPPSDialogPreferenceFragment.installPPPPutSettings(activity, null, false);
                }
            };
            spannable.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            helpForPPPPSTextView.setText(spannable);
            helpForPPPPSTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView helpForShizukuDownloadTextView = view.findViewById(R.id.activity_info_notification_profile_shizuku_howTo_2);
        if (helpForShizukuDownloadTextView != null) {
            String str1 = fragment.getString(R.string.important_info_profile_shizuku_howTo_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
            Spannable spannable = new SpannableString(str1);
            //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    String url = "https://shizuku.rikka.app/download/";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            spannable.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            helpForShizukuDownloadTextView.setText(spannable);
            helpForShizukuDownloadTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView helpForShizukuSetupTextView = view.findViewById(R.id.activity_info_notification_profile_shizuku_howTo_3);
        if (helpForShizukuSetupTextView != null) {
            String str1 = fragment.getString(R.string.important_info_profile_shizuku_howTo_3) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
            Spannable spannable = new SpannableString(str1);
            //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    String url = "https://shizuku.rikka.app/guide/setup/";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            spannable.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            helpForShizukuSetupTextView.setText(spannable);
            helpForShizukuSetupTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }
/*
    static private void installExtenderFromGitHub(Activity activity, boolean finishActivity) {
        if (activity == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";
        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
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
                PPApplicationStatic.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });
        dialogBuilder.setCancelable(false);

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPE_RELEASES_URL + "\u00A0»»";
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
                    PPApplicationStatic.recordException(e);
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

        if (!activity.isFinishing())
            dialog.show();
    }

    static void installExtender(Activity activity, boolean finishActivity) {
        if (activity == null) {
            return;
        }

        PackageManager packageManager = activity.getPackageManager();
        Intent _intent = packageManager.getLaunchIntentForPackage(PPApplication.GALAXY_STORE_PACKAGE_NAME);
        boolean galaxyStoreInstalled = (_intent != null);

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && galaxyStoreInstalled) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
                dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\".";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    activity.startActivity(intent);
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                if (finishActivity)
                    activity.finish();
            });
            dialogBuilder.setCancelable(false);

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

            if (!activity.isFinishing())
                dialog.show();
        }
        else
            installExtenderFromGitHub(activity, finishActivity);
    }

    static void installPPPPutSettings(Activity activity,
                                      boolean finishActivity) {
        if (activity == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.install_pppps_dialog_title);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_pppps, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text);

        String dialogText = "";

        dialogText = dialogText + activity.getString(R.string.install_pppps_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\"\n";
        dialogText = dialogText + activity.getString(R.string.install_pppps_text2) + "\n";
        dialogText = dialogText + activity.getString(R.string.install_pppps_text3) + "\n\n";
        dialogText = dialogText + activity.getString(R.string.install_pppps_text4);
        text.setText(dialogText);

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            String url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                if (finishActivity)
                    activity.finish();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });
        dialogBuilder.setCancelable(false);

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_pppps_from_github_dialog_github_releases);
        CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPPS_RELEASES_URL + "\u00A0»»";
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
                String url = PPApplication.GITHUB_PPPPS_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    dialog.cancel();
                    //if (activity != null)
                    if (finishActivity)
                        activity.finish();
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
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

        if (!activity.isFinishing())
            dialog.show();

    }
*/
}
