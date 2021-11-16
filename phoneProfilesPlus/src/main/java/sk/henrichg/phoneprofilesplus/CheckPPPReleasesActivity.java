package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class CheckPPPReleasesActivity extends AppCompatActivity {

    private int menuItemId = 0;
    private boolean criticalCheck = false;
    private String newVersionName = "";
    private int newVersionCode = 0;
    private boolean newVersionCritical = false;

    AlertDialog alertDialog = null;
    View alertDialogLayout = null;
    boolean newVersionDataExists = false;

    static final String EXTRA_MENU_ITEM_ID = "extra_menu_item_id";
    static final String EXTRA_CRITICAL_CHECK = "extra_critical_check";
    static final String EXTRA_NEW_VERSION_NAME = "extra_new_version_name";
    static final String EXTRA_NEW_VERSION_CODE = "extra_new_version_code";
    static final String EXTRA_NEW_VERSION_CRITICAL = "extra_new_version_critical";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplication.logE("CheckGitHubReleasesActivity.onCreate", "xxx");

        Intent intent = getIntent();
        menuItemId = intent.getIntExtra(EXTRA_MENU_ITEM_ID, 0);
        criticalCheck = intent.getBooleanExtra(EXTRA_CRITICAL_CHECK, false);
        newVersionName = intent.getStringExtra(EXTRA_NEW_VERSION_NAME);
        newVersionCode = intent.getIntExtra(EXTRA_NEW_VERSION_CODE, 0);
        newVersionCritical = intent.getBooleanExtra(EXTRA_NEW_VERSION_CRITICAL, false);

//        PPApplication.logE("CheckGitHubReleasesActivity.onCreate", "menuItemId="+menuItemId);
//        PPApplication.logE("CheckGitHubReleasesActivity.onCreate", "criticalCheck="+criticalCheck);
//        PPApplication.logE("CheckGitHubReleasesActivity.onCreate", "newVersionName="+newVersionName);
//        PPApplication.logE("CheckGitHubReleasesActivity.onCreate", "newVersionCode="+newVersionCode);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        if (menuItemId == 0) {
            menuItemId = criticalCheck ? -2 : -1;
        }
        else {
            newVersionName = "";
            newVersionCode = 0;
        }
        showDialog(this, menuItemId);

        if (menuItemId == R.id.menu_check_in_github) {
            try {
                final Context appContext = getApplicationContext();
                final Activity activity = this;
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(appContext);
                String url;
                if (DebugVersion.enabled)
                    url = PPApplication.PPP_RELEASES_DEBUG_URL;
                else
                    url = PPApplication.PPP_RELEASES_URL;
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET,
                        url,
                        response -> {
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver.doWork", "response="+response);

                            boolean updateReleasedVersion;
                            newVersionName = "";
                            newVersionCode = 0;

                            //noinspection UnnecessaryLocalVariable
                            String contents = response;

                            PPApplication.PPPReleaseData pppReleaseData =
                                    PPApplication.getReleaseData(contents, true, appContext);

                            updateReleasedVersion = pppReleaseData != null;
                            if (updateReleasedVersion) {
                                newVersionName = pppReleaseData.versionNameInReleases;
                                newVersionCode = pppReleaseData.versionCodeInReleases;
                                newVersionCritical = pppReleaseData.critical;
                            }

                            try {
                                if (updateReleasedVersion && (alertDialog != null)) {
                                    checkInGitHub(activity, true);
                                }
                            } catch (Exception e) {
//                            PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                            }

                        },
                        error -> {
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver.doWork", Log.getStackTraceString(error));
                        });
                queue.add(stringRequest);

            } catch (Exception e) {
//            PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
            }
        }

    }

    @Override
    public void finish()
    {
        alertDialog = null;
        super.finish();
        overridePendingTransition(0, 0);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void showDialog(final Activity activity, final int store) {

        if (store == R.id.menu_check_in_fdroid)
            checkInFDroid(activity);
        else
        if (store == R.id.menu_check_in_galaxy_store)
            checkInGalaxyStore(activity);
        else
        if (store == R.id.menu_check_in_amazon_appstore)
            checkInAmazonAppstore(activity);
        else
        if (store == R.id.menu_check_in_appgallery)
            checkInHuaweiAppGallery(activity);
        else
        if (store == R.id.menu_check_in_github)
            checkInGitHub(activity, false);
        else
        if (store == R.id.menu_check_in_apkpure)
            checkInAPKPure(activity);
        else
        if (store == -1) {
            // this is for
            // - CheckPPPReleasesBroadcastReceiver

            if (PPApplication.deviceIsSamsung)
                checkInGalaxyStore(activity);
            else
            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)
                checkInHuaweiAppGallery(activity);
            else {
                PackageManager packageManager = activity.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.amazon.venezia");
                boolean amazonAppStoreInstalled = (intent != null);
                intent = packageManager.getLaunchIntentForPackage("org.fdroid.fdroid");
                boolean fdroidInstalled = (intent != null);

                if (amazonAppStoreInstalled)
                    checkInAmazonAppstore(activity);
                else
                if (fdroidInstalled)
                    checkInFDroid(activity);
                else
                    checkInGitHub(activity, false);
            }
        }
        else
            // this is for
            // - CheckCriticalPPPReleasesBroadcastReceiver
            checkInGitHub(activity, false);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void checkInGitHub(final Activity activity, final boolean refresh) {
        int pppVersionCode = 0;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            pppVersionCode = PPApplication.getVersionCode(pInfo);
        } catch (Exception ignored) {
        }
        newVersionDataExists = (!newVersionName.isEmpty()) && (newVersionCode > 0);

        AlertDialog.Builder dialogBuilder = null;
        if (!refresh) {
            dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.menu_check_github_releases);
        }
        String message;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = activity.getString(R.string.check_github_releases_actual_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
        } catch (Exception e) {
            message = "";
        }

        message = message + "\n";
        if (newVersionDataExists) {
            message = message + activity.getString(R.string.check_github_releases_released_version) + " " + newVersionName + " (" + newVersionCode + ")";
            if (newVersionCritical)
                message = message + " - " + activity.getString(R.string.check_github_releases_version_critical);
        }
        else
            message = message + activity.getString(R.string.check_github_releases_released_version) + " " + getString(R.string.check_github_releases_version_checking);

        newVersionDataExists = newVersionDataExists && (newVersionCode > pppVersionCode);

        message = message + "\n\n";
        message = message + activity.getString(R.string.check_github_releases_install_info_1);

        if (!newVersionDataExists) {
            message = message + "\n";
            message = message + activity.getString(R.string.check_github_releases_install_info_2) + " ";
            message = message + activity.getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3);
        }

        if (criticalCheck) {
            message = message + "\n\n";
            message = message + activity.getString(R.string.check_github_releases_install_info_app_stores_release);
        }

        if (!refresh) {
            LayoutInflater inflater = activity.getLayoutInflater();
            alertDialogLayout = inflater.inflate(R.layout.dialog_install_extender, null);
            dialogBuilder.setView(alertDialogLayout);
        }

        TextView text;
        text = alertDialogLayout.findViewById(R.id.install_extender_dialog_info_text);
        text.setText(message);

        text = alertDialogLayout.findViewById(R.id.install_extender_dialog_github_releases);
        if (newVersionDataExists) {
            Button button = alertDialogLayout.findViewById(R.id.install_extender_dialog_showAssets);
            button.setText(activity.getString(R.string.install_extender_where_is_assets_button) + " \"Assets\"?");
            button.setVisibility(View.GONE);

            CharSequence str1 = activity.getString(R.string.install_extender_github_releases);
            CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPP_RELEASES_URL;
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
                        PPApplication.recordException(e);
                    }
                }
            };
            sbt.setSpan(clickableSpan, str1.length() + 1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());

            text.setVisibility(View.VISIBLE);
        }
        else {
            text.setVisibility(View.GONE);

            Button button = alertDialogLayout.findViewById(R.id.install_extender_dialog_showAssets);
            button.setText(activity.getString(R.string.install_extender_where_is_assets_button) + " \"Assets\"?");
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(activity, GitHubAssetsScreenshotActivity.class);
                intent.putExtra(GitHubAssetsScreenshotActivity.EXTRA_IMAGE, R.drawable.phoneprofilesplus_assets_screenshot);
                activity.startActivity(intent);
            });
        }

        if (!refresh) {
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setCancelable(true);
        }

        int buttonText = R.string.check_github_releases_go_to_github;
        if (newVersionDataExists)
            buttonText = R.string.alert_button_install;

        if (!refresh) {
            dialogBuilder.setPositiveButton(buttonText, (dialog, which) -> {
                String url;
                if (newVersionDataExists)
                    url = PPApplication.GITHUB_PPP_DOWNLOAD_URL_1 + newVersionName + PPApplication.GITHUB_PPP_DOWNLOAD_URL_2;
                else
                    url = PPApplication.GITHUB_PPP_RELEASES_URL;

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                activity.finish();
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);
            dialogBuilder.setOnCancelListener(dialog -> {
                alertDialog = null;
                activity.finish();
            });
            dialogBuilder.setOnDismissListener(dialog -> {
                alertDialog = null;
                activity.finish();
            });
            alertDialog = dialogBuilder.create();
        }
        else {
            Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null)
                button.setText(buttonText);
        }

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if ((!activity.isFinishing()) && (!refresh))
            alertDialog.show();

    }

    @SuppressLint("InflateParams")
    private void checkInFDroid(final Activity activity) {
        // org.fdroid.fdroid
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.menu_check_github_releases);
        String message;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = activity.getString(R.string.check_github_releases_actual_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";//\n";
        } catch (Exception e) {
            message = "";
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();

        boolean fdroidInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo("org.fdroid.fdroid", PackageManager.GET_ACTIVITIES);
            fdroidInstalled = true;
        } catch (Exception ignored) {}
        if (fdroidInstalled)
            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
        else
            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);

        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_fdroid_info_text);
        text.setText(message);

        text = layout.findViewById(R.id.dialog_for_fdroid_fdroid_application);
        CharSequence str1 = activity.getString(R.string.check_releases_fdroid_application);
        CharSequence str2 = str1 + " " + PPApplication.FDROID_APPLICATION_URL;
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
                String url = PPApplication.FDROID_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = layout.findViewById(R.id.dialog_for_fdroid_repository_with_ppp_to_configure);
        str1 = activity.getString(R.string.check_releases_fdroid_repository_with_ppp);
        str2 = str1 + " " + PPApplication.FDROID_REPOSITORY_URL;
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableSpan = new ClickableSpan() {
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
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = layout.findViewById(R.id.dialog_for_fdroid_go_to_repository_with_ppp);
        if (text != null) {
            str1 = activity.getString(R.string.check_releases_fdroid_go_to_repository_with_ppp);
            str2 = str1 + " " + PPApplication.FDROID_PPP_RELEASES_URL;
            sbt = new SpannableString(str2);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            clickableSpan = new ClickableSpan() {
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
                        PPApplication.recordException(e);
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
        int buttonRes = R.string.check_releases_go_to_fdroid;
        if (fdroidInstalled)
            buttonRes = R.string.check_releases_open_fdroid;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            if (_fdroidInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplus"));
                intent.setPackage("org.fdroid.fdroid");
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    //Log.e("CheckGitHubReleasesActivity.showDialog", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
            }
            else {
                String url = PPApplication.FDROID_PPP_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        dialogBuilder.setOnDismissListener(dialog -> activity.finish());
        alertDialog = dialogBuilder.create();

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

    @SuppressLint("InflateParams")
    private void checkInGalaxyStore(final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.menu_check_github_releases);
        String message;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = activity.getString(R.string.check_github_releases_actual_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";//\n";
        } catch (Exception e) {
            message = "";
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();
        layout = inflater.inflate(R.layout.dialog_for_galaxy_store, null);
        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_galaxy_store_info_text);
        text.setText(message);

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(R.string.check_releases_open_galaxy_store, (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplus"));
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        dialogBuilder.setOnDismissListener(dialog -> activity.finish());
        alertDialog = dialogBuilder.create();

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

    @SuppressLint("InflateParams")
    private void checkInAmazonAppstore(final Activity activity) {
        // com.amazon.venezia
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.menu_check_github_releases);
        String message;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = activity.getString(R.string.check_github_releases_actual_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";//\n";
        } catch (Exception e) {
            message = "";
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();
        layout = inflater.inflate(R.layout.dialog_for_amazon_appstore, null);
        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_amazon_appstore_info_text);
        text.setText(message);

        text = layout.findViewById(R.id.dialog_for_amazon_appstore_amazon_appstore_application);
        CharSequence str1 = activity.getString(R.string.check_releases_amazon_appstore_application);
        CharSequence str2 = str1 + " " + PPApplication.AMAZON_APPSTORE_APPLICATION_URL;
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
                String url = PPApplication.AMAZON_APPSTORE_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        PackageManager packageManager = activity.getPackageManager();
        Intent _intent = packageManager.getLaunchIntentForPackage("com.amazon.venezia");
        if (_intent != null) {
            dialogBuilder.setPositiveButton(R.string.check_releases_open_amazon_appstore, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("amzn://apps/android?p=sk.henrichg.phoneprofilesplus"));
                intent.setPackage("com.amazon.venezia");
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(activity);
                    dialogBuilder2.setMessage(R.string.check_releases_install_amazon_appstore);
                    //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder2.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog2 = dialogBuilder2.create();

//                            dialog2.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                    if (!activity.isFinishing())
                        dialog2.show();
                }
/*
                PackageManager packageManager = activity.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.amazon.venezia");
                if (intent != null) {
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(activity);
                        dialogBuilder2.setMessage(R.string.check_releases_install_amazon_appstore);
                        //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder2.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog2 = dialogBuilder2.create();

//                            dialog2.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!activity.isFinishing())
                            dialog2.show();
                    }
                }
 */
                activity.finish();
            });
        }
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        dialogBuilder.setOnDismissListener(dialog -> activity.finish());
        alertDialog = dialogBuilder.create();

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

    @SuppressLint("InflateParams")
    private void checkInHuaweiAppGallery(final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.menu_check_github_releases);
        String message;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = activity.getString(R.string.check_github_releases_actual_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";//\n";
        } catch (Exception e) {
            message = "";
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();
        layout = inflater.inflate(R.layout.dialog_for_appgallery, null);
        dialogBuilder.setView(layout);

        TextView text;
        text = layout.findViewById(R.id.dialog_for_appgallery_info_text);
        text.setText(message);

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(R.string.check_releases_open_appgallery, (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("appmarket://details?id=sk.henrichg.phoneprofilesplus"));
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        dialogBuilder.setOnDismissListener(dialog -> activity.finish());
        alertDialog = dialogBuilder.create();

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

    @SuppressLint("InflateParams")
    private void checkInAPKPure(final Activity activity) {
        // https://m.apkpure.com/p/sk.henrichg.phoneprofilesplus
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.menu_check_github_releases);
        String message;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = activity.getString(R.string.check_github_releases_actual_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";//\n";
        } catch (Exception e) {
            message = "";
        }

        View layout;
        LayoutInflater inflater = activity.getLayoutInflater();

        /*
        boolean fdroidInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo("org.fdroid.fdroid", PackageManager.GET_ACTIVITIES);
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
        text.setText(message);

        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(true);

        boolean apkPureInstalled = false;
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo("com.apkpure.aegon", PackageManager.GET_ACTIVITIES);
            apkPureInstalled = true;
        } catch (Exception ignored) {}

        final boolean _apkPureInstalled = apkPureInstalled;
        int buttonRes = R.string.check_releases_go_to_apkpure;
        if (apkPureInstalled)
            buttonRes = R.string.check_releases_open_apkpure;
        dialogBuilder.setPositiveButton(buttonRes, (dialog, which) -> {
            if (_apkPureInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=sk.henrichg.phoneprofilesplus"));
                intent.setPackage("com.apkpure.aegon");
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    //Log.e("CheckGitHubReleasesActivity.showDialog", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
            }
            else {
                String url = PPApplication.APKPURE_PPP_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            activity.finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());
        dialogBuilder.setOnDismissListener(dialog -> activity.finish());
        alertDialog = dialogBuilder.create();

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

}
