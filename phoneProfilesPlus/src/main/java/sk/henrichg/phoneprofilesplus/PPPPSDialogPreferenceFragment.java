package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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

public class PPPPSDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private PPPPSDialogPreference preference;

    // Layout widgets
    private AlertDialog mDialog;
    private TextView ppppsVersionText = null;
    private TextView ppppsLaunchText = null;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preference = (PPPPSDialogPreference)getPreference();
        prefContext = preference.getContext();
        preference.fragment = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
        dialogBuilder.setTitle(R.string.pppps_pref_dialog_title);
        dialogBuilder.setIcon(preference.getIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(R.string.pppps_pref_dialog_close_button, null);

        LayoutInflater inflater = ((Activity)prefContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_pppps_preference, null);
        dialogBuilder.setView(layout);

        ppppsVersionText = layout.findViewById(R.id.ppppsPrefDialog_pppps_version);
        ppppsLaunchText = layout.findViewById(R.id.ppppsPrefDialog_pppps_launch);

        Button ppppsInstallButton = layout.findViewById(R.id.ppppsPrefDialog_pppps_install_button);
        ppppsInstallButton.setOnClickListener(v -> installPPPPutSettings());

        Button ppppsLaunchButton = layout.findViewById(R.id.ppppsPrefDialog_pppps_launch_button);
        ppppsLaunchButton.setOnClickListener(v -> launchPPPPutSettings());

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
            String prefVolumeDataSummary;
            int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(prefContext);
            if (ppppsVersion == 0) {
                prefVolumeDataSummary = prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_not_installed_summary) + "\n\n";
                prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_install_summary);
            }
            else {
                String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(prefContext);
                prefVolumeDataSummary =  prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_installed_summary) +
                        " " + ppppsVersionName + " (" + ppppsVersion + ")\n\n";
                if (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_LATEST)
                    prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_new_version_summary);
                else
                    prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_upgrade_summary);
            }
            ppppsVersionText.setText(prefVolumeDataSummary);

            ppppsLaunchText.setText(R.string.pppps_pref_dialog_PPPPutSettings_modify_system_settings);

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

    private void installPPPPutSettings() {
        if (getActivity() == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_pppps_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_pppps, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text);

        String dialogText = "";

        int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(getActivity().getApplicationContext());
        if (ppppsVersion != 0) {
            String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(getActivity().getApplicationContext());
            dialogText = dialogText + getString(R.string.pppps_pref_dialog_install_pppps_installed_version) + " " + ppppsVersionName + " (" + ppppsVersion + ")\n";
        }
        dialogText = dialogText + getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
                " " + PPApplication.VERSION_NAME_PPPPS_LATEST + " (" + PPApplication.VERSION_CODE_PPPPS_LATEST + ")\n\n";

        dialogText = dialogText + getString(R.string.install_pppps_text1) + " \"" + getString(R.string.alert_button_install) + "\"\n";
        dialogText = dialogText + getString(R.string.install_pppps_text2) + "\n";
        dialogText = dialogText + getString(R.string.install_pppps_text3) + "\n\n";
        dialogText = dialogText + getString(R.string.install_pppps_text4);
        text.setText(dialogText);

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            String url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setCancelable(false);
        /*dialogBuilder.setOnCancelListener(dialog -> {
            if (finishActivity)
                activity.finish();
        });*/

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_pppps_from_github_dialog_github_releases);
        CharSequence str1 = getString(R.string.install_extender_github_releases);
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
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
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

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();

    }

    private void launchPPPPutSettings() {
        if (getActivity() == null) {
            return;
        }

        if (ActivateProfileHelper.isPPPPutSettingsInstalled(prefContext) >= PPApplication.VERSION_CODE_PPPPS_LATEST) {
            PackageManager packageManager = prefContext.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_PPPPS);
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }
        else {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.pppps_pref_dialog_launchPPPPS_title),
                        getString(R.string.pppps_pref_dialog_pppps_not_installed),
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

}
