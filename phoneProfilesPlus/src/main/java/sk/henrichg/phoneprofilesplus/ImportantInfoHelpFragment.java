package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ImportantInfoHelpFragment extends Fragment {

    public ImportantInfoHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_important_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final Activity activity = getActivity();
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
        boolean news2190 = ((versionCode >= 2190) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1804 = ((versionCode >= 1804) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1772 = ((versionCode >= 1772) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));

        if (newsLatest) {
            // empty this, for switch off news
            news = true;
        }
        else {
            // empty this, for switch off news
        }

        if (news2190) {
            // empty this, for switch off news
            TextView infoText1 = (TextView) view.findViewById(R.id.activity_info_event_start_order1);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = (TextView) view.findViewById(R.id.activity_info_event_start_order2);
            infoText2.setVisibility(View.GONE);
            news = true;
        }
        else {
            // empty this, for switch off news
            TextView infoText1 = (TextView) view.findViewById(R.id.activity_info_event_start_order1_news);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = (TextView) view.findViewById(R.id.activity_info_event_start_order2_news);
            infoText2.setVisibility(View.GONE);
        }

        if (news1804) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                TextView infoText16 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text16);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text18);
                infoText18.setVisibility(View.GONE);
                TextView infoText19 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text19);
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
                TextView infoText20 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text20);
                infoText20.setVisibility(View.GONE);
                TextView infoText21 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text21);
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
                TextView infoText22 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text22);
                infoText22.setVisibility(View.GONE);
                news = true;
            }
        }
        else {
            TextView infoText15 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText17 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText19 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text19);
            infoText19.setVisibility(View.GONE);
            TextView infoText20 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text20);
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
            TextView infoText21 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text21);
            infoText21.setVisibility(View.GONE);
            TextView infoText22 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text22);
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
            TextView infoText10a = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text10a);
            infoText10a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, context)) {
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
                TextView infoText14 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);

                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    TextView infoText13 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text13);
                    infoText13.setVisibility(View.GONE);
                }
                else {
                    TextView infoText13 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text13);
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
            TextView infoText13 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                TextView infoText14 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);
            }
            else {
                TextView infoText14 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text14);
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
            TextView infoText15 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText16 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text16);
            infoText16.setVisibility(View.GONE);
            TextView infoText17 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText18 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text18);
            infoText18.setVisibility(View.GONE);
            TextView infoText19 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text19);
            infoText19.setVisibility(View.GONE);
            TextView infoText20 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text20);
            infoText20.setVisibility(View.GONE);
            TextView infoText21 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text21);
            infoText21.setVisibility(View.GONE);
            TextView infoText22 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text22);
            infoText22.setVisibility(View.GONE);
            TextView infoText10a = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text10a);
            infoText10a.setVisibility(View.GONE);
        }

        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText13 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text14);
            infoText14.setVisibility(View.GONE);
        }

        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context)) {
            TextView infoText3 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text3);
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
            TextView infoText3 = (TextView) view.findViewById(R.id.activity_info_notification_dialog_info_text3);
            infoText3.setVisibility(View.GONE);
        }

        TextView infoText40 = (TextView) view.findViewById(R.id.activity_info_default_profile);
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

        TextView infoText41 = (TextView)view.findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        infoText41.setText("Send Intent [ \n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE\n" +
                " Extra:profile_name:profile name\n" +
                " Target:Activity\n" +
                "]");
        TextView infoText42 = (TextView)view.findViewById(R.id.activity_info_manage_events_from_tasker_params_restart_events);
        infoText42.setText("Send Intent [ \n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS\n" +
                " Target:Activity\n" +
                "]");
        TextView infoText43 = (TextView)view.findViewById(R.id.activity_info_manage_events_from_tasker_params_enable_run_for_event);
        infoText43.setText("Send Intent [ \n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]");
        TextView infoText44 = (TextView)view.findViewById(R.id.activity_info_manage_events_from_tasker_params_pause_event);
        infoText44.setText("Send Intent [ \n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]");
        TextView infoText45 = (TextView)view.findViewById(R.id.activity_info_manage_events_from_tasker_params_stop_event);
        infoText45.setText("Send Intent [ \n" +
                " Action:sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT\n" +
                " Extra:event_name:event name\n" +
                " Target:Activity\n" +
                "]");

        TextView infoTextGrant1Command = (TextView)view.findViewById(R.id.activity_info_notification_dialog_info_grant_1_command);
        infoTextGrant1Command.setText(getString(R.string.important_info_profile_grant_1_howTo_9a) + " " +
                context.getPackageName() + " " +
                getString(R.string.important_info_profile_grant_1_howTo_9b));

        if (!news) {
            TextView infoTextNews = (TextView) view.findViewById(R.id.activity_info_notification_dialog_news);
            infoTextNews.setVisibility(View.GONE);
        }

    }

}
