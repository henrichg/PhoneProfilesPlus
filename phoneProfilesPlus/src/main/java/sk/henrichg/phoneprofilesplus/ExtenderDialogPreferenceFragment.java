package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ExtenderDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ExtenderDialogPreference preference;

    // Layout widgets
    private AlertDialog mDialog;
    /** @noinspection FieldCanBeLocal*/
    private TextView extenderVersionText = null;
    /** @noinspection FieldCanBeLocal*/
    private TextView extenderLaunchText = null;
//    private View extenderLaunchDivider = null;
    /** @noinspection FieldCanBeLocal*/
    private TextView extenderAccessibilitySettings = null;
    /** @noinspection FieldCanBeLocal*/
    private TextView extenderAccessibilitySettingsValue = null;
    /** @noinspection FieldCanBeLocal*/
    private TextView extenderAccessibilitySettingsSummary = null;

    static final int RESULT_ACCESSIBILITY_SETTINGS = 2983;

//TODO title

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preference = (ExtenderDialogPreference)getPreference();
        prefContext = preference.getContext();
        preference.fragment = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
        GlobalGUIRoutines.setCustomDialogTitle(prefContext, dialogBuilder, false,
                getString(R.string.pppextender_pref_dialog_title), null);
        //dialogBuilder.setTitle(R.string.pppextender_pref_dialog_title);
        dialogBuilder.setIcon(preference.getIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(R.string.pppextender_pref_dialog_close_button, null);

        LayoutInflater inflater = ((Activity)prefContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_extender_preference, null);
        dialogBuilder.setView(layout);

        extenderVersionText = layout.findViewById(R.id.extenderPrefDialog_extender_version);
        extenderLaunchText = layout.findViewById(R.id.extenderPrefDialog_extender_launch);
//        extenderLaunchDivider = layout.findViewById(R.id.extenderPrefDialog_extender_launch_divider);
        extenderAccessibilitySettings = layout.findViewById(R.id.extenderPrefDialog_accessibility_settings);
        extenderAccessibilitySettingsValue = layout.findViewById(R.id.extenderPrefDialog_accessibility_settings_value);
        extenderAccessibilitySettingsSummary = layout.findViewById(R.id.extenderPrefDialog_accessibility_settings_summary);

        Button extenderInstallButton = layout.findViewById(R.id.extenderPrefDialog_extender_install_button);
        //noinspection DataFlowIssue
        extenderInstallButton.setOnClickListener(v -> installPPPExtender(getActivity(), preference, false));

        Button extenderLaunchButton = layout.findViewById(R.id.extenderPrefDialog_extender_launch_button);
        //noinspection DataFlowIssue
        extenderLaunchButton.setOnClickListener(v -> launchPPPExtender());

        Button extenderAccessibilitySettingsButton = layout.findViewById(R.id.extenderPrefDialog_accessibiloty_settings_button);
        //noinspection DataFlowIssue
        extenderAccessibilitySettingsButton.setOnClickListener(v -> enableExtender(getActivity(), preference));

        mDialog = dialogBuilder.create();

        //mDialog.setOnShowListener(dialog -> {
            String prefVolumeDataSummary;
            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(prefContext);
            if (extenderVersion == 0) {
                prefVolumeDataSummary = StringConstants.TAG_BOLD_START_HTML + prefContext.getString(R.string.profile_preferences_PPPExtender_not_installed_summary) + StringConstants.TAG_BOLD_END_HTML;

                if ((preference.installSummary != null) && (!preference.installSummary.isEmpty()))
                    prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_DOUBLE_BREAK_HTML + preference.installSummary;
            }
            else {
                String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(prefContext);
                prefVolumeDataSummary =  prefContext.getString(R.string.install_extender_installed_version) +
                        " "+StringConstants.TAG_BOLD_START_HTML + extenderVersionName + " (" + extenderVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
                prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.install_extender_required_version) +
                        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_REQUIRED + " (" + PPApplication.VERSION_CODE_EXTENDER_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML;
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED)
                    prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_DOUBLE_BREAK_HTML + prefContext.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                //else
                //  prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_BREAK_HTML+StringConstants.CHAR_HARD_SPACE_HTML; // "<br><br>" + prefContext.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
            }
            extenderVersionText.setText(StringFormatUtils.fromHtml(prefVolumeDataSummary, false,  false, 0, 0, true));

            if ((preference.lauchSummary != null) && (!preference.lauchSummary.isEmpty())) {
                prefVolumeDataSummary = preference.lauchSummary;
                //extenderLaunchDivider.setVisibility(View.GONE);
                extenderLaunchText.setVisibility(View.VISIBLE);
                extenderLaunchText.setText(prefVolumeDataSummary);
            } else {
                extenderLaunchText.setVisibility(View.GONE);
                //extenderLaunchDivider.setVisibility(View.VISIBLE);
            }

            //-----
            int accessibilityEnabled;// = -99;
            if (extenderVersion == 0)
                // not installed
                accessibilityEnabled = -2;
            else
            if ((extenderVersion > 0) &&
                    (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED))
                // old version
                accessibilityEnabled = -1;
            else
                accessibilityEnabled = -98;
            if (accessibilityEnabled == -98) {
                // Extender is in right version
                if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(prefContext, false, true))
                    // accessibility enabled
                    accessibilityEnabled = 1;
                else
                    // accessibility disabled
                    accessibilityEnabled = 0;
            }
            //if (accessibilityEnabled == -99)
            //    accessibilityEnabled = 1;
            boolean _accessibilityEnabled = accessibilityEnabled == 1;
            boolean preferenceValueOK = true;
            if ((preference.enbaleExtenderPreferenceNameToTest != null) && (!preference.enbaleExtenderPreferenceNameToTest.isEmpty())) {
                String preferenceValue = preference.getSharedPreferences().getString(preference.enbaleExtenderPreferenceNameToTest, "");
                preferenceValueOK = preferenceValue.equals(preference.enbaleExtenderPreferenceValueToTest);
            }
            String value;
            String summary = null;
            if (preferenceValueOK) {
                if (_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                    value = prefContext.getString(R.string.accessibility_service_enabled);
                else {
                    if (accessibilityEnabled == -1) {
                        value = prefContext.getString(R.string.accessibility_service_not_used);
                        summary = prefContext.getString(R.string.preference_not_used_extender_reason) + " " +
                                prefContext.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else {
                        value = prefContext.getString(R.string.accessibility_service_disabled);
                        //if ((preference.enableExtenderSummaryDisabled != null) && (!preference.enableExtenderSummaryDisabled.isEmpty()))
                        //    summary = summary + "\n\n" + preference.enableExtenderSummaryDisabled;
                        //else
                        //    summary = summary + "\n\n" + prefContext.getString(R.string.event_preferences_applications_AccessibilitySettingsForExtender_summary);
                    }
                }
            }
            else {
                value = prefContext.getString(R.string.accessibility_service_not_used);
            }

            String title = prefContext.getString(R.string.event_preferences_applications_AccessibilitySettings_title) + ":";
            if (Build.VERSION.SDK_INT >= 33) {
                if (!(_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)))
                    title = title + StringConstants.CHAR_NEW_LINE + prefContext.getString(R.string.event_preferences_applications_AccessibilitySettings_subTitle_A13);
            }
            extenderAccessibilitySettings.setText(title);

            extenderAccessibilitySettingsValue.setText("[ " + value + " ]");
            if (summary != null) {
                extenderAccessibilitySettingsSummary.setText(summary);
                extenderAccessibilitySettingsSummary.setVisibility(View.VISIBLE);
            }
            else {
                extenderAccessibilitySettingsSummary.setVisibility(View.GONE);
            }

            //----

            //enableViews();
        //});

        return mDialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
        preference.fragment = null;
    }

    /*
    private void enableViews() {
        boolean checked = generateChBtn.isChecked();
        informationIconRBtn.setEnabled(checked);
        exclamationIconRBtn.setEnabled(checked);
        profileIconRBtn.setEnabled(checked);
        notificationTitleEdtText.setEnabled(checked);
        notificationBodyEdtText.setEnabled(checked);
        iconTypeLabel.setEnabled(checked);
        notificationTitleLabel.setEnabled(checked);
        notificationBodyLabel.setEnabled(checked);

        String value = notificationTitleEdtText.getText().toString();
        Button okButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        okButton.setEnabled((!value.isEmpty()) || (!generateChBtn.isChecked()));

    }
    */

    private static void installExtenderFromGitHub(final Activity activity,
                                                  final ExtenderDialogPreference _preference,
                                                  boolean finishActivity) {
        if (activity == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.install_extender_dialog_title), null);
        //dialogBuilder.setTitle(activity.getString(R.string.install_extender_dialog_title));

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";

        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + extenderVersionName + " (" + extenderVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        }
//        dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
//                " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " " + StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_REQUIRED + " (" + PPApplication.VERSION_CODE_EXTENDER_REQUIRED + ")" + StringConstants.TAG_BOLD_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_text2) + StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_text3)+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + StringConstants.TAG_BOLD_START_HTML + activity.getString(R.string.install_pppps_text5) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;

        dialogText = dialogText.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPE_RELEASES_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= 33) {
            TextView text2 = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_apk_installation);
            //noinspection DataFlowIssue
            text2.setVisibility(View.VISIBLE);
            String str = activity.getString(R.string.check_releases_install_from_apk_note1) +
                    " " + activity.getString(R.string.install_ppp_store_droidify) +
                    activity.getString(R.string.check_releases_install_from_apk_note2_pppe);
            text2.setText(str);
        }

        dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
            //String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL_1 + PPApplication.VERSION_NAME_EXTENDER_LATEST + PPApplication.GITHUB_PPPE_DOWNLOAD_URL_2;
            String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL;

            // DownloadManager not working in Huawei P40
            // https://stackoverflow.com/questions/44093939/how-to-use-downloadmanager-on-huawei
            //if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                }
            /*} else {
                try {
                    String textToast = activity.getString(R.string.downloading_toast_text);
                    PPApplication.showToast(activity.getApplicationContext(), textToast, Toast.LENGTH_LONG);

                    Uri Download_Uri = Uri.parse(url);
                    DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                    //Restrict the types of networks over which this download may proceed.
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    //Set whether this download may proceed over a roaming connection.
                    request.setAllowedOverRoaming(false);
                    //Set the title of this download, to be displayed in notifications (if enabled).
                    request.setTitle(activity.getString(R.string.download_PPPE_title));
                    //Set a description of this download, to be displayed in notifications (if enabled)
                    request.setDescription(activity.getString(R.string.downloading_file_description));
                    //Set the local destination for the downloaded file to a path within the application's external files directory
                    request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, "PhoneProfilesPlusExtender.apk");
                    //request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    //Enqueue a new download and same the referenceId
                    DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadCompletedBroadcastReceiver.downloadReferencePPPE = downloadManager.enqueue(request);
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                }
            }*/
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });
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

        if (!activity.isFinishing())
            dialog.show();
    }

    @SuppressLint("InflateParams")
    private static void installDroidIfy(final Activity activity,
                                        final ExtenderDialogPreference _preference,
                                        boolean finishActivity) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.DROIDIFY_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return;
        } catch (Exception ignored) {}

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.install_extender_dialog_title), null);
        //dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();
        layout = inflater.inflate(R.layout.dialog_install_pppe_install_droidify, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppe_install_droidify_info_text);

        String dialogText = "";

        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + extenderVersionName + " (" + extenderVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        }
        //dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
        //        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_REQUIRED + " (" + PPApplication.VERSION_CODE_EXTENDER_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_install_droidify_text);
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        //View buttonsDivider = layout.findViewById(R.id.install_pppe_install_droidify_buttonsDivider);
        //buttonsDivider.setVisibility(View.VISIBLE);

        int buttonRes = R.string.alert_button_install_store;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            String url = PPApplication.DROIDIFY_APPLICATION_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            if ((_preference != null) && (_preference.fragment != null))
                _preference.fragment.dismiss();
            if (finishActivity)
                activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        dialogBuilder.setOnDismissListener(dialog -> activity.finish());
        AlertDialog alertDialog = dialogBuilder.create();

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
            alertDialog.show();
    }

    static void installPPPExtender(final Activity activity, final ExtenderDialogPreference _preference,
                                   boolean finishActivity) {
        if (activity == null) {
            return;
        }

        PackageManager packageManager = activity.getPackageManager();
        Intent _intent = packageManager.getLaunchIntentForPackage(PPApplication.FDROID_PACKAGE_NAME);
        boolean fdroidInstalled = (_intent != null);
        _intent = packageManager.getLaunchIntentForPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
        boolean droidifyInstalled = (_intent != null);
        _intent = packageManager.getLaunchIntentForPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
        boolean neostoreInstalled = (_intent != null);
        //_intent = packageManager.getLaunchIntentForPackage(PPApplication.GALAXY_STORE_PACKAGE_NAME);
        //boolean galaxyStoreInstalled = (_intent != null);
//        Log.e("ExtenderDialogPreferenceFragment.installPPPExtender", "fdroidInstalled="+fdroidInstalled);
//        Log.e("ExtenderDialogPreferenceFragment.installPPPExtender", "droidifyInstalled="+droidifyInstalled);
//        Log.e("ExtenderDialogPreferenceFragment.installPPPExtender", "galaxyStoreInstalled="+galaxyStoreInstalled);

        if (droidifyInstalled || neostoreInstalled || fdroidInstalled /*|| galaxyStoreInstalled*/) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.install_extender_dialog_title), null);
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
                dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + extenderVersionName + " (" + extenderVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
            }
            //dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
            //        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
            dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                    " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_REQUIRED + " (" + PPApplication.VERSION_CODE_EXTENDER_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
            dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML;
            dialogText = dialogText + StringConstants.TAG_BOLD_START_HTML + activity.getString(R.string.install_pppps_text5) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
            //noinspection DataFlowIssue
            text.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

            /*
            text = layout.findViewById(R.id.install_pppe_from_store_dialog_github_releases);
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
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            */

            dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplusextender"));
                if (droidifyInstalled) {
                    intent.setPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                } else if (neostoreInstalled) {
                    intent.setPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                } else /*if (fdroidInstalled)*/ {
                    intent.setPackage(PPApplication.FDROID_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                } /*else {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                    try {
                        activity.startActivity(intent);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        if ((_preference != null) && (_preference.fragment != null))
                            _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                }*/
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                if (finishActivity)
                    activity.finish();
            });

            //Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            /*
            button.setText(activity.getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub(activity, _preference, finishActivity);
            });
            */

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
        else {
            if (Build.VERSION.SDK_INT < 33)
                installExtenderFromGitHub(activity, _preference, finishActivity);
            else
                installDroidIfy(activity, _preference, finishActivity);
        }
    }

    private void launchPPPExtender() {
        if (getActivity() == null) {
            return;
        }

        if (PPExtenderBroadcastReceiver.isExtenderInstalled(prefContext) != 0) {
            PackageManager packageManager = prefContext.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    if (preference != null)
                        preference.fragment.dismiss();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if (preference != null)
                        preference.fragment.dismiss();
                }
            }
        }
        else {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.event_preferences_applications_LaunchExtender_title),
                        getString(R.string.event_preferences_extender_not_installed),
                        getString(android.R.string.ok),
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
                        false,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

    static void enableExtender(final Activity activity, final ExtenderDialogPreference _preference) {
        if (activity == null) {
            return;
        }

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, activity)) {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                activity.startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                ok = true;
                if (_preference != null)
                    _preference.fragment.dismiss();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
                if (_preference != null)
                    _preference.fragment.dismiss();
            }
        }
        if (!ok) {
            PPAlertDialog dialog = new PPAlertDialog(
                    activity.getString(R.string.event_preferences_applications_AccessibilitySettings_title),
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
                    false,
                    activity
            );

            if (!activity.isFinishing())
                dialog.show();
        }

    }

}
