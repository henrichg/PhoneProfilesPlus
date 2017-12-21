package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ImportantInfoHelpFragment extends Fragment {

    public ImportantInfoHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_important_info, container, false);
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
            versionCode = pInfo.versionCode;
        } catch (Exception e) {
            //e.printStackTrace();
        }

        boolean news = false;
        boolean newsLatest = (versionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        boolean news3670 = (versionCode >= 3670); // news for PhoneProfilesPlusExtender - show it when not activated
        boolean news3640 = ((versionCode >= 3640) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news2190 = ((versionCode >= 2190) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1804 = ((versionCode >= 1804) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1772 = ((versionCode >= 1772) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));

        int extenderVersion = ForegroundApplicationChangedBroadcastReceiver.isExtenderInstalled(context);

        //noinspection StatementWithEmptyBody
        if (newsLatest) {
            // empty this, for switch off news
        }
        else {
            // empty this, for switch off news
        }

        if (news3670) {
            news = true;
            if (extenderVersion > 0) { // extender is installed
                news = false;
                TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_text1);
                infoText1.setVisibility(View.GONE);
            }
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
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_dialog_info_text50_news);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = view.findViewById(R.id.activity_info_notification_dialog_info_text50_profiles_news);
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
                TextView infoText16 = view.findViewById(R.id.activity_info_notification_dialog_info_text16);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = view.findViewById(R.id.activity_info_notification_dialog_info_text18);
                infoText18.setVisibility(View.GONE);
                TextView infoText19 = view.findViewById(R.id.activity_info_notification_dialog_info_text19);
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
                            dialogBuilder.show();
                        }
                    }
                });
                TextView infoText20 = view.findViewById(R.id.activity_info_notification_dialog_info_text20);
                infoText20.setVisibility(View.GONE);
                TextView infoText21 = view.findViewById(R.id.activity_info_notification_dialog_info_text21);
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
                            dialogBuilder.show();
                        }
                    }
                });
                TextView infoText22 = view.findViewById(R.id.activity_info_notification_dialog_info_text22);
                infoText22.setVisibility(View.GONE);
                news = true;
            }
        }
        else {
            TextView infoText15 = view.findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText17 = view.findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText19 = view.findViewById(R.id.activity_info_notification_dialog_info_text19);
            infoText19.setVisibility(View.GONE);
            TextView infoText20 = view.findViewById(R.id.activity_info_notification_dialog_info_text20);
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
                        dialogBuilder.show();
                    }
                }
            });
            TextView infoText21 = view.findViewById(R.id.activity_info_notification_dialog_info_text21);
            infoText21.setVisibility(View.GONE);
            TextView infoText22 = view.findViewById(R.id.activity_info_notification_dialog_info_text22);
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
                        dialogBuilder.show();
                    }
                }
            });
            TextView infoText10a = view.findViewById(R.id.activity_info_notification_dialog_info_text10a);
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
                        dialogBuilder.show();
                    }
                }
            });
        }

        if (news1772) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);

                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    TextView infoText13 = view.findViewById(R.id.activity_info_notification_dialog_info_text13);
                    infoText13.setVisibility(View.GONE);
                }
                else {
                    TextView infoText13 = view.findViewById(R.id.activity_info_notification_dialog_info_text13);
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
                                dialogBuilder.show();
                            }
                        }
                    });
                    news = true;
                }
            }
        }
        else {
            TextView infoText13 = view.findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);
            }
            else {
                TextView infoText14 = view.findViewById(R.id.activity_info_notification_dialog_info_text14);
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
                            dialogBuilder.show();
                        }
                    }
                });
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 23) {
            TextView infoText15 = view.findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText16 = view.findViewById(R.id.activity_info_notification_dialog_info_text16);
            infoText16.setVisibility(View.GONE);
            TextView infoText17 = view.findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText18 = view.findViewById(R.id.activity_info_notification_dialog_info_text18);
            infoText18.setVisibility(View.GONE);
            TextView infoText19 = view.findViewById(R.id.activity_info_notification_dialog_info_text19);
            infoText19.setVisibility(View.GONE);
            TextView infoText20 = view.findViewById(R.id.activity_info_notification_dialog_info_text20);
            infoText20.setVisibility(View.GONE);
            TextView infoText21 = view.findViewById(R.id.activity_info_notification_dialog_info_text21);
            infoText21.setVisibility(View.GONE);
            TextView infoText22 = view.findViewById(R.id.activity_info_notification_dialog_info_text22);
            infoText22.setVisibility(View.GONE);
            TextView infoText10a = view.findViewById(R.id.activity_info_notification_dialog_info_text10a);
            infoText10a.setVisibility(View.GONE);
        }

        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText13 = view.findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = view.findViewById(R.id.activity_info_notification_dialog_info_text14);
            infoText14.setVisibility(View.GONE);
        }

        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context)) {
            TextView infoText3 = view.findViewById(R.id.activity_info_notification_dialog_info_text3);
            infoText3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "categorySystem");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivity(intent);
                }
            });
        }
        else {
            TextView infoText3 = view.findViewById(R.id.activity_info_notification_dialog_info_text3);
            infoText3.setVisibility(View.GONE);
        }

        TextView infoText40 = view.findViewById(R.id.activity_info_default_profile);
        infoText40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "profileActivationCategory");
                //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivity(intent);
            }
        });

        TextView infoText41 = view.findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        String str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE\n" +
                " Extra:profile_name:profile name\n" +
                " Target:Activity\n" +
                "]";
        Spannable spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText41.setText(spannable);
        TextView infoText42 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_restart_events);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText42.setText(spannable);
        TextView infoText43 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_enable_run_for_event);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText43.setText(spannable);
        TextView infoText44 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_pause_event);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText44.setText(spannable);
        TextView infoText45 = view.findViewById(R.id.activity_info_manage_events_from_tasker_params_stop_event);
        str = "Send Intent [\n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText45.setText(spannable);

        TextView infoTextADBDownload = view.findViewById(R.id.activity_info_notification_dialog_info_text61);
        str = getString(R.string.important_info_profile_grant_1_howTo_11);
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextADBDownload.setText(spannable);

        TextView infoTextGrant1Command = view.findViewById(R.id.activity_info_notification_dialog_info_grant_1_command);
        str = getString(R.string.important_info_profile_grant_1_howTo_9a) + "\u00A0" +
                context.getPackageName() + "\u00A0" +
                getString(R.string.important_info_profile_grant_1_howTo_9b);
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(getActivity())), 0, str.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextGrant1Command.setText(spannable);

        if (PPApplication.newExtender && (extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER)) {
            news = true;
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.VISIBLE);
        }
        else {
            TextView infoText1 = view.findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.GONE);
        }

        if (!news) {
            TextView infoTextNews = view.findViewById(R.id.activity_info_notification_dialog_news);
            infoTextNews.setVisibility(View.GONE);
        }

    }

}
