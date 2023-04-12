package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
    private TextView extenderVersionText = null;
    private TextView extenderLaunchText = null;
    private View extenderLaunchDivider = null;
    private TextView extenderAccessibilitySettings = null;
    private TextView extenderAccessibilitySettingsValue = null;
    private TextView extenderAccessibilitySettingsSummary = null;

    static final int RESULT_ACCESSIBILITY_SETTINGS = 2983;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preference = (ExtenderDialogPreference)getPreference();
        prefContext = preference.getContext();
        preference.fragment = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
        dialogBuilder.setTitle(R.string.pppextender_pref_dialog_title);
        dialogBuilder.setIcon(preference.getIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(R.string.pppextender_pref_dialog_close_button, null);

        LayoutInflater inflater = ((Activity)prefContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_extender_preference, null);
        dialogBuilder.setView(layout);

        extenderVersionText = layout.findViewById(R.id.extenderPrefDialog_extender_version);
        extenderLaunchText = layout.findViewById(R.id.extenderPrefDialog_extender_launch);
        extenderLaunchDivider = layout.findViewById(R.id.extenderPrefDialog_extender_launch_divider);
        extenderAccessibilitySettings = layout.findViewById(R.id.extenderPrefDialog_accessibility_settings);
        extenderAccessibilitySettingsValue = layout.findViewById(R.id.extenderPrefDialog_accessibility_settings_value);
        extenderAccessibilitySettingsSummary = layout.findViewById(R.id.extenderPrefDialog_accessibility_settings_summary);

        Button extenderInstallButton = layout.findViewById(R.id.extenderPrefDialog_extender_install_button);
        extenderInstallButton.setOnClickListener(v -> installPPPExtender(getActivity(), preference, false));

        Button extenderLaunchButton = layout.findViewById(R.id.extenderPrefDialog_extender_launch_button);
        extenderLaunchButton.setOnClickListener(v -> launchPPPExtender());

        Button extenderAccessibilitySettingsButton = layout.findViewById(R.id.extenderPrefDialog_accessibiloty_settings_button);
        extenderAccessibilitySettingsButton.setOnClickListener(v -> enableExtender(getActivity(), preference));

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
            String prefVolumeDataSummary;
            int extenderVersion = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isExtenderInstalled(prefContext);
            if (extenderVersion == 0) {
                prefVolumeDataSummary = "<b>" + prefContext.getString(R.string.profile_preferences_PPPExtender_not_installed_summary) + "</b>";

                if ((preference.installSummary != null) && (!preference.installSummary.isEmpty()))
                    prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" + preference.installSummary;
            }
            else {
                String extenderVersionName = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.getExtenderVersionName(prefContext);
                prefVolumeDataSummary =  prefContext.getString(R.string.install_extender_installed_version) +
                        " <b>" + extenderVersionName + " (" + extenderVersion + ")</b><br>";
                prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.install_extender_required_version) +
                        " <b>" + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")</b>";
                if (extenderVersion < PPApplication.VERSION_CODE_PPPPS_LATEST)
                    prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" + prefContext.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                  prefVolumeDataSummary = prefVolumeDataSummary + "<br> "; // "<br><br>" + prefContext.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
            }
            extenderVersionText.setText(StringFormatUtils.fromHtml(prefVolumeDataSummary, false, false, false, 0, 0, true));

            if ((preference.lauchSummary != null) && (!preference.lauchSummary.isEmpty())) {
                prefVolumeDataSummary = preference.lauchSummary;
                extenderLaunchDivider.setVisibility(View.GONE);
                extenderLaunchText.setVisibility(View.VISIBLE);
                extenderLaunchText.setText(prefVolumeDataSummary);
            } else {
                extenderLaunchText.setVisibility(View.GONE);
                extenderLaunchDivider.setVisibility(View.VISIBLE);
            }

            //-----
            int accessibilityEnabled;// = -99;
            if (extenderVersion == 0)
                // not installed
                accessibilityEnabled = -2;
            else
            if ((extenderVersion > 0) &&
                    (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST))
                // old version
                accessibilityEnabled = -1;
            else
                accessibilityEnabled = -98;
            if (accessibilityEnabled == -98) {
                // Extender is in right version
                if (sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(prefContext, false, true))
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
            extenderAccessibilitySettings.setText(prefContext.getString(R.string.event_preferences_applications_AccessibilitySettings_title) + ":"/* + summary*/);
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
        });

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
        dialogBuilder.setTitle(activity.getString(R.string.install_extender_dialog_title));

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";

        int extenderVersion = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " <b>" + extenderVersionName + " (" + extenderVersion + ")</b><br>";
        }
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " <b>" + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")</b><br><br>";
        dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\".<br><br>";
        dialogText = dialogText + activity.getString(R.string.install_extender_text2) + "<br><br>";
        dialogText = dialogText + activity.getString(R.string.install_extender_text3);

        dialogText = dialogText.replace("\n", "<br>");
        text.setText(StringFormatUtils.fromHtml(dialogText, false, false, false, 0, 0, true));

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
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    if (_preference != null)
                        _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if (_preference != null)
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

        dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
            //String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL_1 + PPApplication.VERSION_NAME_EXTENDER_LATEST + PPApplication.GITHUB_PPPE_DOWNLOAD_URL_2;
            String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                if (_preference != null)
                    _preference.fragment.dismiss();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
                if (_preference != null)
                    _preference.fragment.dismiss();
            }
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

    static void installPPPExtender(final Activity activity, final ExtenderDialogPreference _preference,
                                   boolean finishActivity) {
        if (activity == null) {
            return;
        }

        PackageManager packageManager = activity.getPackageManager();
        Intent _intent = packageManager.getLaunchIntentForPackage("com.sec.android.app.samsungapps");
        boolean galaxyStoreInstalled = (_intent != null);

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && galaxyStoreInstalled) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
                dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " <b>" + extenderVersionName + " (" + extenderVersion + ")</b><br>";
            }
            dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                    " <b>" + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")</b><br><br>";
            dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\".";

            text.setText(StringFormatUtils.fromHtml(dialogText, false, false, false, 0, 0, true));

            dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    activity.startActivity(intent);
                    if (_preference != null)
                        _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if (_preference != null)
                        _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                if (finishActivity)
                    activity.finish();
            });

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            button.setText(activity.getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub(activity, _preference, finishActivity);
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
/*        else if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
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
                    PPApplicationStatic.recordException(e);
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
            installExtenderFromGitHub(activity, _preference, finishActivity);
    }

    private void launchPPPExtender() {
        if (getActivity() == null) {
            return;
        }

        if (sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isExtenderInstalled(prefContext) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
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
                    activity
            );

            if (!activity.isFinishing())
                dialog.show();
        }

    }

}
