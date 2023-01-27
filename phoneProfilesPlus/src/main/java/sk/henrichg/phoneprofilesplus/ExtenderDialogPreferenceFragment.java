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

public class ExtenderDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ExtenderDialogPreference preference;

    // Layout widgets
    private AlertDialog mDialog;
    private TextView extenderVersionText = null;
    private TextView extenderLaunchText = null;

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

        Button extenderInstallButton = layout.findViewById(R.id.extenderPrefDialog_extender_install_button);
        extenderInstallButton.setOnClickListener(v -> installPPPExtender(getActivity(), preference));

        Button extenderLaunchButton = layout.findViewById(R.id.extenderPrefDialog_extender_launch_button);
        extenderLaunchButton.setOnClickListener(v -> launchPPPExtender());

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
            String prefVolumeDataSummary;
            int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(prefContext);
            if (ppppsVersion == 0) {
                prefVolumeDataSummary = prefContext.getString(R.string.pppextender_pref_dialog_PPPExtender_not_installed_summary) + "\n\n";

                if ((preference.installSummary != null) && (!preference.installSummary.isEmpty()))
                    prefVolumeDataSummary = prefVolumeDataSummary + "\n\n" + preference.installSummary;
            }
            else {
                String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(prefContext);
                prefVolumeDataSummary =  prefContext.getString(R.string.pppextender_pref_dialog_PPPExtender_installed_summary) +
                        " " + ppppsVersionName + " (" + ppppsVersion + ")\n\n";
                if (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_LATEST)
                    prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.pppextender_pref_dialog_PPPExtender_new_version_summary);
                else
                    prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
            }
            extenderVersionText.setText(prefVolumeDataSummary);

            if ((preference.lauchSummary != null) && (!preference.lauchSummary.isEmpty())) {
                prefVolumeDataSummary = preference.lauchSummary;
                extenderLaunchText.setText(prefVolumeDataSummary);
            } else
                extenderLaunchText.setText(R.string.empty_string);

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

    private static void installExtenderFromGitHub(final Activity activity, final ExtenderDialogPreference preference) {
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

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
        }
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
        dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\".\n\n";
        dialogText = dialogText + activity.getString(R.string.install_extender_text2) + "\n\n";
        dialogText = dialogText + activity.getString(R.string.install_extender_text3);

        text.setText(dialogText);

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
                    if (preference != null)
                        preference.fragment.dismiss();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                    if (preference != null)
                        preference.fragment.dismiss();
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
                if (preference != null)
                    preference.fragment.dismiss();
            } catch (Exception e) {
                PPApplication.recordException(e);
                if (preference != null)
                    preference.fragment.dismiss();
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

        if (!activity.isFinishing())
            dialog.show();
    }

    static void installPPPExtender(final Activity activity, final ExtenderDialogPreference preference) {
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

            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(activity.getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(activity.getApplicationContext());
                dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + activity.getString(R.string.install_extender_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\".";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    activity.startActivity(intent);
                    if (preference != null)
                        preference.fragment.dismiss();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                    if (preference != null)
                        preference.fragment.dismiss();
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            button.setText(activity.getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub(activity, preference);
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
            installExtenderFromGitHub(activity, preference);
    }

    private void launchPPPExtender() {
        if (getActivity() == null) {
            return;
        }

        if (PPPExtenderBroadcastReceiver.isExtenderInstalled(prefContext) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
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
                    PPApplication.recordException(e);
                    if (preference != null)
                        preference.fragment.dismiss();
                }
            }
        }
        else {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.pppextender_pref_dialog_launchPPPExtender_title),
                        getString(R.string.pppextender_pref_dialog_PPPExtender_not_installed),
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
