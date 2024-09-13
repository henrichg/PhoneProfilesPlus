package sk.henrichg.phoneprofilesplus;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;

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
        ppppsInstallButton.setOnClickListener(v -> installPPPPutSettings(getActivity(), preference, false));

        Button ppppsLaunchButton = layout.findViewById(R.id.ppppsPrefDialog_pppps_launch_button);
        ppppsLaunchButton.setOnClickListener(v -> launchPPPPutSettings());

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
            String prefVolumeDataSummary;
            int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(prefContext);
            if (ppppsVersion == 0) {
                prefVolumeDataSummary = StringConstants.TAG_BOLD_START_HTML + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_not_installed_summary) + StringConstants.TAG_BOLD_END_HTML;
                prefVolumeDataSummary = prefVolumeDataSummary +  StringConstants.TAG_DOUBLE_BREAK_HTML + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_install_summary);
            }
            else {
                String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(prefContext);
                prefVolumeDataSummary =  prefContext.getString(R.string.pppps_pref_dialog_install_pppps_installed_version) +
                        " "+StringConstants.TAG_BOLD_START_HTML + ppppsVersionName + " (" + ppppsVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
                prefVolumeDataSummary = prefVolumeDataSummary + prefContext.getString(R.string.install_extender_required_version) +
                        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_REQUIRED + " (" + PPApplication.VERSION_CODE_PPPPS_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML;
                if (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_REQUIRED)
                    prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_DOUBLE_BREAK_HTML + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_new_version_summary);
                else
                    prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_BREAK_HTML+StringConstants.CHAR_HARD_SPACE_HTML; //"<br><br>" + prefContext.getString(R.string.pppps_pref_dialog_PPPPutSettings_upgrade_summary);
            }
            ppppsVersionText.setText(StringFormatUtils.fromHtml(prefVolumeDataSummary, false,  false, 0, 0, true));

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

    private static void installPPPPutSettingsFromGitHub(final Activity activity,
                                                        final PPPPSDialogPreference _preference,
                                                        boolean finishActivity) {
        if (activity == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getString(R.string.install_pppps_dialog_title));

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_pppps, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text);

        String dialogText = "";

        int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(activity.getApplicationContext());
        if (ppppsVersion != 0) {
            String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + ppppsVersionName + " (" + ppppsVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        }
        //dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
        //        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_LATEST + " (" + PPApplication.VERSION_CODE_PPPPS_LATEST + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_REQUIRED + " (" + PPApplication.VERSION_CODE_PPPPS_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;

        dialogText = dialogText + activity.getString(R.string.install_pppps_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\""+StringConstants.TAG_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_text2) + StringConstants.TAG_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_text3) + StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + StringConstants.TAG_BOLD_START_HTML + activity.getString(R.string.install_pppps_text5) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_text4);

        dialogText = dialogText.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);
        text.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));
        text.setMovementMethod(LinkMovementMethod.getInstance());

        dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
            String url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;

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
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    if ((_preference != null) && (_preference.fragment != null))
                        _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                }
//            }
//            else {
//                try {
//                    String textToast = activity.getString(R.string.downloading_toast_text);
//                    PPApplication.showToast(activity.getApplicationContext(), textToast, Toast.LENGTH_LONG);
//
//                    Uri Download_Uri = Uri.parse(url);
//                    DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
//
//                    //Restrict the types of networks over which this download may proceed.
//                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//                    //Set whether this download may proceed over a roaming connection.
//                    request.setAllowedOverRoaming(false);
//                    //Set the title of this download, to be displayed in notifications (if enabled).
//                    request.setTitle(activity.getString(R.string.download_pppps_title));
//                    //Set a description of this download, to be displayed in notifications (if enabled)
//                    request.setDescription(activity.getString(R.string.downloading_file_description));
//                    //Set the local destination for the downloaded file to a path within the application's external files directory
//                    request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, "PPPPutSettings.apk");
//                    //request.allowScanningByMediaScanner();
//                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                    //Enqueue a new download and same the referenceId
//                    DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
//                    DownloadCompletedBroadcastReceiver.downloadReferencePPPPS = downloadManager.enqueue(request);
//                    if ((_preference != null) && (_preference.fragment != null))
//                        _preference.fragment.dismiss();
//                    if (finishActivity)
//                        activity.finish();
//                } catch (Exception e) {
//                    if ((_preference != null) && (_preference.fragment != null))
//                        _preference.fragment.dismiss();
//                    if (finishActivity)
//                        activity.finish();
//                }
//            }
        });
        //dialogBuilder.setCancelable(false);
//        dialogBuilder.setOnCancelListener(dialog -> {
//            if (finishActivity)
//                activity.finish();
//        });

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_pppps_from_github_dialog_github_releases);
        CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPPS_RELEASES_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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

    private static void installPPPPutSettingsFromGitHub34(final Activity activity,
                                                        final PPPPSDialogPreference _preference,
                                                        boolean finishActivity) {
        if (activity == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getString(R.string.install_pppps_dialog_title));

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_pppps_34, null);
        dialogBuilder.setView(layout);

        String dialogText = "";

        int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(activity.getApplicationContext());
        if (ppppsVersion != 0) {
            String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + ppppsVersionName + " (" + ppppsVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        }
        //dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
        //        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_LATEST + " (" + PPApplication.VERSION_CODE_PPPPS_LATEST + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_REQUIRED + " (" + PPApplication.VERSION_CODE_PPPPS_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;

        TextView text0 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text0);
        dialogText = dialogText + activity.getString(R.string.install_pppps_text10) + StringConstants.TAG_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_text11) + StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_text12) + StringConstants.TAG_BREAK_HTML;
        text0.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        TextView text1 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text1);
        String url = PPApplication.SHIUKU_HOW_TO_START_URL;
        dialogText = activity.getString(R.string.install_pppps_text13) + " " +
                StringConstants.TAG_URL_LINK_START_HTML + url + StringConstants.TAG_URL_LINK_START_URL_END_HTML + url+ StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML;
        text1.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));
        text1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView text2 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text2);
        url = PPApplication.INSTALL_WITH_OPTIONS_DOWNLOAD_URL;
        CharSequence str1Text2 = activity.getString(R.string.install_pppps_text14);
        CharSequence str2Text2 = str1Text2 + " " + url + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
        Spannable sbt = new SpannableString(str2Text2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1Text2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpanText2 = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                    // In One UI is bug in ststem downloader. Chrome, as default browser, do not downlaod
                    // For this reason used is DownloadManager
                    try {
                        String text = activity.getString(R.string.downloading_toast_text);
                        PPApplication.showToast(activity.getApplicationContext(), text, Toast.LENGTH_LONG);

                        String url = PPApplication.INSTALL_WITH_OPTIONS_DOWNLOAD_URL;
                        Uri Download_Uri = Uri.parse(url);
                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                        //Restrict the types of networks over which this download may proceed.
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        //Set whether this download may proceed over a roaming connection.
                        request.setAllowedOverRoaming(false);
                        //Set the title of this download, to be displayed in notifications (if enabled).
                        request.setTitle(activity.getString(R.string.download_installWithOptions_title));
                        //Set a description of this download, to be displayed in notifications (if enabled)
                        request.setDescription(activity.getString(R.string.downloading_file_description));
                        //Set the local destination for the downloaded file to a path within the application's external files directory
                        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, "InstallWithOptions.apk");
                        //request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        //Enqueue a new download and same the referenceId
                        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                        /*DownloadCompletedBroadcastReceiver.downloadReferenceInstallWithOptions =*/ downloadManager.enqueue(request);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(PPApplication.INSTALL_WITH_OPTIONS_DOWNLOAD_URL));
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        };
        sbt.setSpan(clickableSpanText2, str1Text2.length()+1, str2Text2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text2.setText(sbt);
        text2.setMovementMethod(LinkMovementMethod.getInstance());

        TextView text3 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text3);
        url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;
        /*dialogText = activity.getString(R.string.install_pppps_text15) + " " +
                StringConstants.TAG_URL_LINK_START_HTML + url + StringConstants.TAG_URL_LINK_START_URL_END_HTML + url+ StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML +
                StringConstants.TAG_BREAK_HTML;
        text3.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));*/
        CharSequence str1Text3 = activity.getString(R.string.install_pppps_text15);
        CharSequence str2Text3 = str1Text3 + " " + url + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
        sbt = new SpannableString(str2Text3);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1Text3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpanText3 = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                    // In One UI is bug in ststem downloader. Chrome, as default browser, do not downlaod
                    // For this reason used is DownloadManager
                    try {
                        String text = activity.getString(R.string.downloading_toast_text);
                        PPApplication.showToast(activity.getApplicationContext(), text, Toast.LENGTH_LONG);

                        String url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;
                        Uri Download_Uri = Uri.parse(url);
                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                        //Restrict the types of networks over which this download may proceed.
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        //Set whether this download may proceed over a roaming connection.
                        request.setAllowedOverRoaming(false);
                        //Set the title of this download, to be displayed in notifications (if enabled).
                        request.setTitle(activity.getString(R.string.download_pppps_title));
                        //Set a description of this download, to be displayed in notifications (if enabled)
                        request.setDescription(activity.getString(R.string.downloading_file_description));
                        //Set the local destination for the downloaded file to a path within the application's external files directory
                        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, "PPPPutSettings.apk");
                        //request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        //Enqueue a new download and same the referenceId
                        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                        /*DownloadCompletedBroadcastReceiver.downloadReferencePPPPS =*/ downloadManager.enqueue(request);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(PPApplication.GITHUB_PPPPS_DOWNLOAD_URL));
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        };
        sbt.setSpan(clickableSpanText3, str1Text3.length()+1, str2Text3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text3.setText(sbt);
        text3.setMovementMethod(LinkMovementMethod.getInstance());

        TextView text4 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text4);
        dialogText = activity.getString(R.string.install_pppps_text16);
        text4.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        TextView text5 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text5);
        dialogText = activity.getString(R.string.install_pppps_text17);
        text5.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        TextView text6 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text6);
        dialogText = activity.getString(R.string.install_pppps_text18);
        text6.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        TextView text7 = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text7);
        dialogText =  StringConstants.TAG_BREAK_HTML + StringConstants.TAG_BOLD_START_HTML + activity.getString(R.string.install_pppps_text5) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_text4);
        dialogText = dialogText.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);
        text7.setText(StringFormatUtils.fromHtml(dialogText, false,  false, 0, 0, true));

        //dialogBuilder.setCancelable(false);
        /*dialogBuilder.setOnCancelListener(dialog -> {
            if (finishActivity)
                activity.finish();
        });*/

        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });
        /*dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });*/

        final AlertDialog dialog = dialogBuilder.create();

        TextView textGitHub = layout.findViewById(R.id.install_pppps_from_github_dialog_github_releases);
        CharSequence str1GitHub = activity.getString(R.string.install_extender_github_releases);
        CharSequence str2GitHub = str1GitHub + " " + PPApplication.GITHUB_PPPPS_RELEASES_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
        Spannable sbtGitHub = new SpannableString(str2GitHub);
        sbtGitHub.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1GitHub.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpanGitHub = new ClickableSpan() {
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
        sbtGitHub.setSpan(clickableSpanGitHub, str1GitHub.length()+1, str2GitHub.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textGitHub.setText(sbtGitHub);
        textGitHub.setMovementMethod(LinkMovementMethod.getInstance());

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
                                        final PPPPSDialogPreference _preference,
                                        boolean finishActivity) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.DROIDIFY_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return;
        } catch (Exception ignored) {}

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.install_pppps_dialog_title);

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();
        layout = inflater.inflate(R.layout.dialog_install_pppe_install_droidify, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppe_install_droidify_info_text);

        String dialogText = "";

        int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(activity.getApplicationContext());
        if (ppppsVersion != 0) {
            String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(activity.getApplicationContext());
            dialogText = dialogText + activity.getString(R.string.install_extender_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + ppppsVersionName + " (" + ppppsVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        }
        //dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
        //        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_REQUIRED + " (" + PPApplication.VERSION_CODE_PPPPS_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
        dialogText = dialogText + activity.getString(R.string.install_pppps_install_droidify_text);
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

    static void installPPPPutSettings(final Activity activity,
                                      final PPPPSDialogPreference _preference,
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

        if (Build.VERSION.SDK_INT < 34) {
            // for Android 14+ is required to use adb command or InstallWithOptions for
            // PPPPutSettings installation, because target sdk is 22.

            if (droidifyInstalled || neostoreInstalled || fdroidInstalled) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(R.string.install_pppps_dialog_title);

                LayoutInflater inflater = activity.getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_install_pppps_from_store, null);
                dialogBuilder.setView(layout);

                TextView text = layout.findViewById(R.id.install_pppps_from_store_dialog_info_text);

                String dialogText = "";

                int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(activity.getApplicationContext());
                if (ppppsVersion != 0) {
                    String extenderVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(activity.getApplicationContext());
                    dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_installed_version) + " " + StringConstants.TAG_BOLD_START_HTML + extenderVersionName + " (" + ppppsVersion + ")" + StringConstants.TAG_BOLD_END_HTML + StringConstants.TAG_BREAK_HTML;
                }
                //dialogText = dialogText + activity.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
                //        " " + StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_LATEST + " (" + PPApplication.VERSION_CODE_PPPPS_LATEST + ")" + StringConstants.TAG_BOLD_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML;
                dialogText = dialogText + activity.getString(R.string.install_extender_required_version) +
                        " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_PPPPS_REQUIRED + " (" + PPApplication.VERSION_CODE_PPPPS_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
                dialogText = dialogText + activity.getString(R.string.install_pppps_text1) + " \"" + activity.getString(R.string.alert_button_install) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML;
                dialogText = dialogText + StringConstants.TAG_BOLD_START_HTML + activity.getString(R.string.install_pppps_text5) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
                text.setText(StringFormatUtils.fromHtml(dialogText, false, false, 0, 0, true));

                dialogBuilder.setPositiveButton(activity.getString(R.string.alert_button_install), (dialog, which) -> {
                    //noinspection IfStatementWithIdenticalBranches
                    if (droidifyInstalled) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=sk.henrichg.pppputsettings"));
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
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=sk.henrichg.pppputsettings"));
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
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=sk.henrichg.pppputsettings"));
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
                    }
                });
                dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (finishActivity)
                        activity.finish();
                });

                //Button button = layout.findViewById(R.id.install_pppps_from_store_dialog_installFromGitHub);

                final AlertDialog dialog = dialogBuilder.create();

                /*
                button.setText(activity.getString(R.string.alert_button_install_extender_from_github));
                button.setOnClickListener(v -> {
                    dialog.cancel();
                    installPPPPutSettingsFromGitHub(activity, _preference, finishActivity);
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
            } else {
                if (Build.VERSION.SDK_INT < 33)
                    installPPPPutSettingsFromGitHub(activity, _preference, finishActivity);
                else
                    installDroidIfy(activity, _preference, finishActivity);
            }
        } else
            installPPPPutSettingsFromGitHub34(activity, _preference, finishActivity);
    }

    private void launchPPPPutSettings() {
        if (getActivity() == null) {
            return;
        }

        if (ActivateProfileHelper.isPPPPutSettingsInstalled(prefContext) > 0) {
            PackageManager packageManager = prefContext.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_PPPPS);
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    preference.fragment.dismiss();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    preference.fragment.dismiss();
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
                        false,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

}
