package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class CheckPPPReleasesDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private CheckPPPReleasesActivity activity;

    public CheckPPPReleasesDialog() {
    }

    public CheckPPPReleasesDialog(CheckPPPReleasesActivity activity)
    {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (CheckPPPReleasesActivity) getActivity();
        if (this.activity != null) {
            //GlobalGUIRoutines.lockScreenOrientation(activity);

            int store = 0;
            Bundle arguments = getArguments();
            if (arguments != null) {
                store = arguments.getInt(CheckPPPReleasesActivity.EXTRA_MENU_ITEM_ID, 0);
            }
            if (store == R.id.menu_check_in_github) {
                String newVersionName = arguments.getString(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_NAME, "");
                int newVersionCode = arguments.getInt(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CODE, 0);
                boolean newVersionCritical= arguments.getBoolean(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CRITICAL, false);
                boolean criticalCheck= arguments.getBoolean(CheckPPPReleasesActivity.EXTRA_CRITICAL_CHECK, false);
                //booleam refreshOpenedDialog = arguments.getBoolean(CheckPPPReleasesActivity.EXTRA_REFRESH_OPENED_DIALOG, true);
                mDialog = checkInGitHub(newVersionName, newVersionCode, newVersionCritical, criticalCheck);
            }
            if (store == R.id.menu_check_in_droidify) {
                boolean forGitHub = arguments.getBoolean(CheckPPPReleasesActivity.EXTRA_FOR_GITHUB, false);
                mDialog = checkInDroidIfy(forGitHub);
            }
            if (store == R.id.menu_check_in_fdroid) {
                mDialog = checkInFDroid();
            }
            if (store == R.id.menu_check_in_neostore) {
                mDialog = checkInNeoStore();
            }
            if (store == R.id.menu_check_in_apkpure) {
                mDialog = checkInAPKPure();
            }
        }
        return mDialog;
    }

    /*
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.e("CheckPPPReleasesDialog.onDismiss", "ttttttttttttt");
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
            activity.finish();
        }
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "CHECK_PPP_RELEASES_DIALOG");
        }
    }

    @SuppressLint("SetTextI18n")
    private AlertDialog checkInGitHub(
            String newVersionName,
            int newVersionCode,
            boolean newVersionCritical,
            boolean criticalCheck
    ) {
        int pppVersionCode = 0;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            pppVersionCode = PPApplicationStatic.getVersionCode(pInfo);
        } catch (Exception ignored) {
        }

        boolean newVersionDataExists = (!newVersionName.isEmpty()) && (newVersionCode > 0);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.menu_check_github_releases), null);
        //dialogBuilder.setTitle(R.string.menu_check_github_releases);

        String message = StringConstants.TAG_BOLD_START_HTML + getString(R.string.ppp_app_name) + StringConstants.TAG_BOLD_END_HTML + StringConstants.TAG_BREAK_HTML;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = message + StringConstants.TAG_BREAK_HTML + activity.getString(R.string.check_github_releases_installed_version) + " " + StringConstants.TAG_BOLD_START_HTML + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")" + StringConstants.TAG_BOLD_END_HTML;
        } catch (Exception e) {
            message = StringConstants.TAG_BREAK_HTML;
        }

        message = message + StringConstants.TAG_BREAK_HTML;
        if (newVersionDataExists) {
            message = message + activity.getString(R.string.check_github_releases_released_version) + " " + StringConstants.TAG_BOLD_START_HTML + newVersionName + " (" + newVersionCode + ")" + StringConstants.TAG_BOLD_END_HTML;
            if (newVersionCritical)
                message = message + " - " + activity.getString(R.string.check_github_releases_version_critical);
        } else
            message = message + activity.getString(R.string.check_github_releases_released_version) + " " + getString(R.string.check_github_releases_version_checking);

        newVersionDataExists = newVersionDataExists && (newVersionCode > pppVersionCode);

        message = message + StringConstants.TAG_DOUBLE_BREAK_HTML;
        message = message + activity.getString(R.string.check_github_releases_install_info_1);

        if (!newVersionDataExists) {
            message = message + StringConstants.TAG_BREAK_HTML;
            message = message + activity.getString(R.string.check_github_releases_install_info_2) + " ";
            message = message + activity.getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3);
        }

        if (criticalCheck) {
            message = message + StringConstants.TAG_DOUBLE_BREAK_HTML;
            message = message + activity.getString(R.string.check_github_releases_install_info_app_stores_release);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View alertDialogLayout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(alertDialogLayout);

        TextView text;
        text = alertDialogLayout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);
        message = message.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(message, false, false, 0, 0, true));

        final String ASSETS = " \"Assets\"?";

        text = alertDialogLayout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        if (newVersionDataExists) {
            Button button = alertDialogLayout.findViewById(R.id.install_ppp_pppe_from_github_dialog_showAssets);
            //noinspection DataFlowIssue
            button.setText(activity.getString(R.string.install_extender_where_is_assets_button) + ASSETS);
            button.setVisibility(View.GONE);

            CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
            CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPP_RELEASES_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                    String url = PPApplication.GITHUB_PPP_RELEASES_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            //noinspection DataFlowIssue
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());

            text.setVisibility(View.VISIBLE);
        } else {
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);

            Button button = alertDialogLayout.findViewById(R.id.install_ppp_pppe_from_github_dialog_showAssets);
            //noinspection DataFlowIssue
            button.setText(activity.getString(R.string.install_extender_where_is_assets_button) + ASSETS);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(activity, GitHubAssetsScreenshotActivity.class);
                intent.putExtra(GitHubAssetsScreenshotActivity.EXTRA_IMAGE, R.drawable.phoneprofilesplus_assets_screenshot);
                activity.startActivity(intent);
            });
        }

        if (Build.VERSION.SDK_INT >= 33) {
            TextView text2 = alertDialogLayout.findViewById(R.id.install_ppp_pppe_from_github_dialog_apk_installation);
            //noinspection DataFlowIssue
            text2.setVisibility(View.VISIBLE);
            String str = activity.getString(R.string.check_releases_install_from_apk_note1) +
                    " " + activity.getString(R.string.install_ppp_store_droidify) +
                    activity.getString(R.string.check_releases_install_from_apk_note2_ppp);
            text2.setText(str);
        }

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        int buttonText = R.string.check_github_releases_go_to_github;
        if (newVersionDataExists)
            buttonText = R.string.alert_button_install;

        boolean _newVersionDataExists = newVersionDataExists;
        dialogBuilder.setPositiveButton(buttonText, (dialog, which) -> {
            String url;
            if (_newVersionDataExists) {
                url = PPApplication.GITHUB_PPP_DOWNLOAD_URL;
                //url = PPApplication.GITHUB_PPP_DOWNLOAD_URL_1 + newVersionName + PPApplication.GITHUB_PPP_DOWNLOAD_URL_2;

                //if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                activity.finish();
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
                            request.setTitle(activity.getString(R.string.download_PPP_title));
                            //Set a description of this download, to be displayed in notifications (if enabled)
                            request.setDescription(activity.getString(R.string.downloading_file_description));
                            //Set the local destination for the downloaded file to a path within the application's external files directory
                            request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, "PhoneProfilesPlus.apk");
                            //request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            //Enqueue a new download and same the referenceId
                            DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadCompletedBroadcastReceiver.downloadReferencePPP = downloadManager.enqueue(request);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                        activity.finish();
                    }*/
            } else {
                url = PPApplication.GITHUB_PPP_RELEASES_URL;

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                activity.finish();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
        //dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        //dialogBuilder.setOnDismissListener(dialog -> activity.finish());

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

    @SuppressLint("InflateParams")
    private AlertDialog checkInFDroid() {
        // org.fdroid.fdroid
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.menu_check_github_releases), null);
        //dialogBuilder.setTitle(R.string.menu_check_github_releases);

        String message = StringConstants.TAG_BOLD_START_HTML + getString(R.string.ppp_app_name) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = message + StringConstants.TAG_BREAK_HTML + activity.getString(R.string.check_github_releases_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")"+StringConstants.TAG_BOLD_END_HTML;
        } catch (Exception e) {
            message = StringConstants.TAG_BREAK_HTML;
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();

        boolean fdroidInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.FDROID_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            fdroidInstalled = true;
        } catch (Exception ignored) {}
        if (fdroidInstalled)
            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
        else
            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);

        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_fdroid_info_text);
        message = message.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(message, false,  false, 0, 0, true));

        text = layout.findViewById(R.id.dialog_for_fdroid_fdroid_application);
        if (text != null) {
            if (!fdroidInstalled) {
                CharSequence str1 = activity.getString(R.string.check_releases_fdroid_ppp_release);
                CharSequence str2 = str1 + " " + activity.getString(R.string.check_releases_ppp_release_clik_to_show) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                        String url = PPApplication.FDROID_PPP_RELEASES_URL;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        try {
                            activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                };
                sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
                text.setText(sbt);
                text.setMovementMethod(LinkMovementMethod.getInstance());

                if (Build.VERSION.SDK_INT >= 33) {
                    TextView text2 = layout.findViewById(R.id.dialog_for_fdroid_apk_installation);
                    //noinspection DataFlowIssue
                    text2.setVisibility(View.VISIBLE);
                    String str = activity.getString(R.string.check_releases_install_from_apk_note1) +
                            " " + activity.getString(R.string.install_ppp_store_fdroid) +
                            activity.getString(R.string.check_releases_install_from_apk_note2_ppp);
                    text2.setText(str);
                }
            }
            else
                text.setVisibility(View.GONE);
        }

        text = layout.findViewById(R.id.dialog_for_fdroid_repository_with_ppp_to_configure);
        CharSequence str1 = activity.getString(R.string.check_releases_fdroid_repository_with_ppp);
        CharSequence str2 = str1 + " " + PPApplication.FDROID_REPOSITORY_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = PPApplication.FDROID_REPOSITORY_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = layout.findViewById(R.id.dialog_for_fdroid_go_to_repository_with_ppp);
        if (text != null) {
            str1 = activity.getString(R.string.check_releases_fdroid_go_to_repository_with_ppp);
            str2 = str1 + " " + PPApplication.FDROID_PPP_RELEASES_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
            sbt = new SpannableString(str2);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    String url = PPApplication.FDROID_PPP_RELEASES_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        }

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        final boolean _fdroidInstalled = fdroidInstalled;
        int buttonRes = R.string.alert_button_install_store;
        if (fdroidInstalled)
            buttonRes = R.string.check_releases_open_fdroid;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            if (_fdroidInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplus"));
                intent.setPackage(PPApplication.FDROID_PACKAGE_NAME);
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    //Log.e("CheckPPPReleasesActivity.checkInFDroid", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
            }
            else {
                String url = PPApplication.FDROID_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
        //dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        //dialogBuilder.setOnDismissListener(dialog -> activity.finish());

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

    @SuppressLint("InflateParams")
    private AlertDialog checkInAPKPure() {
        // https://m.apkpure.com/p/sk.henrichg.phoneprofilesplus
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.menu_check_github_releases), null);
        //dialogBuilder.setTitle(R.string.menu_check_github_releases);

        String message = StringConstants.TAG_BOLD_START_HTML + getString(R.string.ppp_app_name) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = message + StringConstants.TAG_BREAK_HTML + activity.getString(R.string.check_github_releases_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")"+StringConstants.TAG_BOLD_END_HTML;
        } catch (Exception e) {
            message = StringConstants.TAG_BREAK_HTML;
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();

        /*
        boolean fdroidInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.FDROID_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            fdroidInstalled = true;
        } catch (Exception ignored) {}
        if (fdroidInstalled)
            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
        else
            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);
         */
        layout = inflater.inflate(R.layout.dialog_for_apkpure, null);

        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_apkpure_info_text);
        message = message.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);
        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(message, false,  false, 0, 0, true));

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        boolean apkPureInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.APKPURE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            apkPureInstalled = true;
        } catch (Exception ignored) {}

        text = layout.findViewById(R.id.dialog_for_apkpure_appkpure_application);
        View buttonsDivider = layout.findViewById(R.id.dialog_for_apkpure_buttonsDivider);
        if (!apkPureInstalled) {
            //noinspection DataFlowIssue
            text.setVisibility(View.VISIBLE);
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.VISIBLE);

            CharSequence str1 = activity.getString(R.string.check_releases_apkpure_ppp_release);
            CharSequence str2 = str1 + " " + activity.getString(R.string.check_releases_ppp_release_clik_to_show) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                    String url = PPApplication.APKPURE_PPP_RELEASES_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());

            if (Build.VERSION.SDK_INT >= 33) {
                TextView text2 = layout.findViewById(R.id.dialog_for_apkpure_apk_installation);
                //noinspection DataFlowIssue
                text2.setVisibility(View.VISIBLE);
                String str = activity.getString(R.string.check_releases_install_from_apk_note1) +
                        " " + activity.getString(R.string.install_ppp_store_apkpure) +
                        activity.getString(R.string.check_releases_install_from_apk_note2_ppp);
                text2.setText(str);
            }
        } else {
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
            //buttonsDivider.setVisibility(View.GONE);
        }

        final boolean _apkPureInstalled = apkPureInstalled;
        int buttonRes = R.string.alert_button_install_store;
        if (apkPureInstalled)
            buttonRes = R.string.check_releases_open_apkpure;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            if (_apkPureInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplus"));
                intent.setPackage(PPApplication.APKPURE_PACKAGE_NAME);
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    //Log.e("CheckPPPReleasesActivity.checkInAPKPure", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
            }
            else {
                String url = PPApplication.APKPURE_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
        //dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        //dialogBuilder.setOnDismissListener(dialog -> activity.finish());

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

    @SuppressLint("InflateParams")
    private AlertDialog checkInDroidIfy(boolean forGitHub) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.menu_check_github_releases), null);
        //dialogBuilder.setTitle(R.string.menu_check_github_releases);

        String message = StringConstants.TAG_BOLD_START_HTML + getString(R.string.ppp_app_name) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = message + StringConstants.TAG_BREAK_HTML + activity.getString(R.string.check_github_releases_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")"+StringConstants.TAG_BOLD_END_HTML;
        } catch (Exception e) {
            message = StringConstants.TAG_BREAK_HTML;
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();

        /*
        boolean fdroidInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.FDROID_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            fdroidInstalled = true;
        } catch (Exception ignored) {}
        if (fdroidInstalled)
            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
        else
            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);
         */
        layout = inflater.inflate(R.layout.dialog_for_droidify, null);

        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_droidify_info_text);
        message = message.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);

        if (forGitHub) {
            message = message + StringConstants.TAG_DOUBLE_BREAK_HTML + activity.getString(R.string.check_releases_github_download_not_supported);
        }

        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(message, false,  false, 0, 0, true));

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        boolean droidifyInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.DROIDIFY_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            droidifyInstalled = true;
        } catch (Exception ignored) {}

        text = layout.findViewById(R.id.dialog_for_droidify_droidify_application);
        View buttonsDivider = layout.findViewById(R.id.dialog_for_droidify_buttonsDivider);
        if (!droidifyInstalled) {
            //noinspection DataFlowIssue
            text.setVisibility(View.VISIBLE);
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.VISIBLE);

            CharSequence str1 = activity.getString(R.string.check_releases_droidify_ppp_release);
            CharSequence str2 = str1 + " " + activity.getString(R.string.check_releases_ppp_release_clik_to_show) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                    String url = PPApplication.IZZY_PPP_RELEASES_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());

            if (Build.VERSION.SDK_INT >= 33) {
                TextView text2 = layout.findViewById(R.id.dialog_for_droidify_apk_installation);
                //noinspection DataFlowIssue
                text2.setVisibility(View.VISIBLE);
                String str = activity.getString(R.string.check_releases_install_from_apk_note1) +
                        " " + activity.getString(R.string.install_ppp_store_droidify) +
                        activity.getString(R.string.check_releases_install_from_apk_note2_ppp);
                text2.setText(str);
            }
        } else {
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
            //buttonsDivider.setVisibility(View.GONE);
        }

        final boolean _droidifyInstalled = droidifyInstalled;
        int buttonRes = R.string.alert_button_install_store;
        if (droidifyInstalled)
            buttonRes = R.string.check_releases_open_droidify;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            if (_droidifyInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplus"));
                intent.setPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    //Log.e("CheckPPPReleasesActivity.checkInDroidify", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
            }
            else {
                String url = PPApplication.DROIDIFY_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
        //dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        //dialogBuilder.setOnDismissListener(dialog -> activity.finish());

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

    @SuppressLint("InflateParams")
    private AlertDialog checkInNeoStore() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                activity.getString(R.string.menu_check_github_releases), null);
        //dialogBuilder.setTitle(R.string.menu_check_github_releases);

        String message = StringConstants.TAG_BOLD_START_HTML + getString(R.string.ppp_app_name) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = message + StringConstants.TAG_BREAK_HTML + activity.getString(R.string.check_github_releases_installed_version) + " "+StringConstants.TAG_BOLD_START_HTML + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")"+StringConstants.TAG_BOLD_END_HTML;
        } catch (Exception e) {
            message = StringConstants.TAG_BREAK_HTML;
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();

        /*
        boolean fdroidInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.FDROID_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            fdroidInstalled = true;
        } catch (Exception ignored) {}
        if (fdroidInstalled)
            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
        else
            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);
         */
        layout = inflater.inflate(R.layout.dialog_for_neostore, null);

        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_neostore_info_text);
        message = message.replace(StringConstants.CHAR_NEW_LINE, StringConstants.TAG_BREAK_HTML);

        //noinspection DataFlowIssue
        text.setText(StringFormatUtils.fromHtml(message, false,  false, 0, 0, true));

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        boolean neostoreInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PPApplication.NEOSTORE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            neostoreInstalled = true;
        } catch (Exception ignored) {}

        text = layout.findViewById(R.id.dialog_for_neostore_neostore_application);
        View buttonsDivider = layout.findViewById(R.id.dialog_for_neostore_buttonsDivider);
        if (!neostoreInstalled) {
            //noinspection DataFlowIssue
            text.setVisibility(View.VISIBLE);
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.VISIBLE);

            CharSequence str1 = activity.getString(R.string.check_releases_neostore_ppp_release);
            CharSequence str2 = str1 + " " + activity.getString(R.string.check_releases_ppp_release_clik_to_show) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                    String url = PPApplication.IZZY_PPP_RELEASES_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());

            if (Build.VERSION.SDK_INT >= 33) {
                TextView text2 = layout.findViewById(R.id.dialog_for_neostore_apk_installation);
                //noinspection DataFlowIssue
                text2.setVisibility(View.VISIBLE);
                String str = activity.getString(R.string.check_releases_install_from_apk_note1) +
                        " " + activity.getString(R.string.install_ppp_store_neostore) +
                        activity.getString(R.string.check_releases_install_from_apk_note2_ppp);
                text2.setText(str);
            }
        } else {
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
            //buttonsDivider.setVisibility(View.GONE);
        }

        final boolean _neostoreInstalled = neostoreInstalled;
        int buttonRes = R.string.alert_button_install_store;
        if (neostoreInstalled)
            buttonRes = R.string.check_releases_open_neostore;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            if (_neostoreInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplus"));
                intent.setPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    //Log.e("CheckPPPReleasesActivity.checkInNeoStore", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
            }
            else {
                String url = PPApplication.NEOSTORE_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
        //dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        //dialogBuilder.setOnDismissListener(dialog -> activity.finish());

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

}
