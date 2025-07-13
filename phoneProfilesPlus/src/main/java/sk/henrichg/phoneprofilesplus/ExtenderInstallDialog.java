package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class ExtenderInstallDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private AppCompatActivity activity;

    public ExtenderInstallDialog() {
    }

    public ExtenderInstallDialog(AppCompatActivity activity)
    {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (this.activity != null) {
            //GlobalGUIRoutines.lockScreenOrientation(activity);

            int store = 0;
            boolean finishActivity = false;
            Bundle arguments = getArguments();
            if (arguments != null) {
                store = arguments.getInt(ExtenderDialogPreferenceFragment.EXTRA_STORE, 0);
                finishActivity = arguments.getBoolean(ExtenderDialogPreferenceFragment.EXTRA_FINISH_ACTIVITY, false);
            }

            if (store == ExtenderDialogPreferenceFragment.STORE_GITHUB) {
                mDialog = installExtenderFromGitHub(activity, finishActivity);
            }
            if (store == ExtenderDialogPreferenceFragment.STORE_DROIDIFY) {
                mDialog = installFromDroidIfy(activity, finishActivity);
            }
            if (store == ExtenderDialogPreferenceFragment.STORE_ALL) {
                mDialog = installPPPExtender(activity, finishActivity);
            }
        }
        return mDialog;
    }

    /*
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.e("ExtenderInstallDialog.onDismiss", "ttttttttttttt");
        if (activity != null) {
            //GlobalGUIRoutines.unlockScreenOrientation(activity);
            activity.finish();
        }
    }
    */

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (activity != null) {
            //GlobalGUIRoutines.unlockScreenOrientation(activity);

            boolean finishActivity = false;
            Bundle arguments = getArguments();
            if (arguments != null)
                finishActivity = arguments.getBoolean(ExtenderDialogPreferenceFragment.EXTRA_FINISH_ACTIVITY, false);
            if (finishActivity)
                activity.finish();
        }
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "EXTENDER_INSTALL_DIALOG");
        }
    }

    @SuppressLint("InflateParams")
    private AlertDialog installFromDroidIfy(final Activity activity,
                                            boolean finishActivity) {
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
            //if ((_preference != null) && (_preference.fragment != null))
            //    _preference.fragment.dismiss();
            if (finishActivity)
                activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (finishActivity)
                activity.finish();
        });
        //dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        //dialogBuilder.setOnDismissListener(dialog -> activity.finish());

        //noinspection UnnecessaryLocalVariable
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

        return alertDialog;
    }

    private AlertDialog installExtenderFromGitHub(final Activity activity,
                                                  boolean finishActivity) {
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
                    //if ((_preference != null) && (_preference.fragment != null))
                    //    _preference.fragment.dismiss();
                    if (finishActivity)
                        activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    //if ((_preference != null) && (_preference.fragment != null))
                    //    _preference.fragment.dismiss();
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
                //if ((_preference != null) && (_preference.fragment != null))
                //    _preference.fragment.dismiss();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
                //if ((_preference != null) && (_preference.fragment != null))
                //    _preference.fragment.dismiss();
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

        //noinspection UnnecessaryLocalVariable
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

        return dialog;
    }

    private AlertDialog installPPPExtender(final Activity activity,
                                   boolean finishActivity) {

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
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                } else if (neostoreInstalled) {
                    intent.setPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                } else /*if (fdroidInstalled)*/ {
                    intent.setPackage(PPApplication.FDROID_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    }
                } /*else {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                    try {
                        activity.startActivity(intent);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
                        if (finishActivity)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                        //if ((_preference != null) && (_preference.fragment != null))
                        //    _preference.fragment.dismiss();
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

            //noinspection UnnecessaryLocalVariable
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

          return dialog;
        }
        else {
            if (Build.VERSION.SDK_INT < 33)
                return installExtenderFromGitHub(activity, /*_preference,*/ finishActivity);
            else
                return installFromDroidIfy(activity, /*_preference,*/ finishActivity);
        }
    }

}
