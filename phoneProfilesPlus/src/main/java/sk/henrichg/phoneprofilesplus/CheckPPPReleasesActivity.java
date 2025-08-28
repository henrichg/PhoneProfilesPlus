package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

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

    //AlertDialog alertDialog = null;
    //View alertDialogLayout = null;
    //boolean newVersionDataExists = false;

    static final String EXTRA_MENU_ITEM_ID = "extra_menu_item_id";
    static final String EXTRA_CRITICAL_CHECK = "extra_critical_check";
    static final String EXTRA_NEW_VERSION_NAME = "extra_new_version_name";
    static final String EXTRA_NEW_VERSION_CODE = "extra_new_version_code";
    static final String EXTRA_NEW_VERSION_CRITICAL = "extra_new_version_critical";
    //static final String EXTRA_REFRESH_OPENED_DIALOG = "extra_refresh_opened_dialog";
    static final String EXTRA_FOR_GITHUB = "extra_for_github";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        menuItemId = intent.getIntExtra(EXTRA_MENU_ITEM_ID, 0);
        criticalCheck = intent.getBooleanExtra(EXTRA_CRITICAL_CHECK, false);
        newVersionName = intent.getStringExtra(EXTRA_NEW_VERSION_NAME);
        newVersionCode = intent.getIntExtra(EXTRA_NEW_VERSION_CODE, 0);
        newVersionCritical = intent.getBooleanExtra(EXTRA_NEW_VERSION_CRITICAL, false);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        if (menuItemId == 0) {
            menuItemId = criticalCheck ? -2 : -1;
        }
        else {
            newVersionName = "";
            newVersionCode = 0;
        }

        boolean _showDialog = true;
        if (menuItemId == R.id.menu_check_in_github) {
            if (Build.VERSION.SDK_INT < 33) {
                _showDialog = false;
                try {
                    //final Activity activity = this;
                    final Context appContext = getApplicationContext();
                    // Instantiate the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(appContext);
                    String url;
                    if (DebugVersion.enabled)
                        url = PPApplication.PPP_RELEASES_MD_DEBUG_URL;
                    else
                        url = PPApplication.PPP_RELEASES_MD_URL;
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET,
                            url,
                            response -> {
                                boolean updateReleasedVersion;
                                newVersionName = "";
                                newVersionCode = 0;

                                //String contents = response;

                                PPPReleaseData pppReleaseData =
                                        PPApplicationStatic.getReleaseData(response, true, appContext);

                                updateReleasedVersion = pppReleaseData != null;
                                if (updateReleasedVersion) {
                                    newVersionName = pppReleaseData.versionNameInReleases;
                                    newVersionCode = pppReleaseData.versionCodeInReleases;
                                    newVersionCritical = pppReleaseData.critical;
                                }

                                try {
                                    //if (updateReleasedVersion && (alertDialog != null)) {
                                        // Dialog is opened by showDialog() called before this download
                                        // of PPP_RELEASES_URL. Refresh views in it only.
                                        checkInGitHub(/*activity, true*/);
                                    //}
                                } catch (Exception e) {
//                            Log.e("CheckPPPReleasesActivity.onStart", Log.getStackTraceString(e));
                                }

                            },
                            error -> {
//                        Log.e("CheckPPPReleasesActivity.onStart", Log.getStackTraceString(error));
                            });
                    queue.add(stringRequest);

                } catch (Exception e) {
//            Log.e("CheckPPPReleasesActivity.onStart", Log.getStackTraceString(e));
                    _showDialog = true;
                }
            }
        }

        if (_showDialog)
            showDialog(this, menuItemId);
    }

    @Override
    public void finish()
    {
        //alertDialog = null;
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void showDialog(final Activity activity, int store) {

        PackageManager packageManager = activity.getPackageManager();
//        Intent intent = packageManager.getLaunchIntentForPackage("com.amazon.venezia");
//        boolean amazonAppStoreInstalled = (intent != null);
        Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.FDROID_PACKAGE_NAME);
        boolean fdroidInstalled = (intent != null);
        intent = packageManager.getLaunchIntentForPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
        boolean droidifyInstalled = (intent != null);
        //intent = packageManager.getLaunchIntentForPackage(PPApplication.GALAXY_STORE_PACKAGE_NAME);
        //boolean galaxyStoreInstalled = (intent != null);
        //intent = packageManager.getLaunchIntentForPackage(PPApplication.HUAWEI_APPGALLERY_PACKAGE_NAME);
        //boolean appGalleryInstalled = (intent != null);
        intent = packageManager.getLaunchIntentForPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
        boolean neostoreInstalled = (intent != null);
        intent = packageManager.getLaunchIntentForPackage(PPApplication.APKPURE_PACKAGE_NAME);
        boolean apkPureInstalled = (intent != null);

        boolean displayed = false;

        if (store == R.id.menu_check_in_fdroid) {
            checkInFDroid(/*activity*/);
            displayed = true;
        }
//        else
//        if (store == R.id.menu_check_in_galaxy_store) {
//            //if (galaxyStoreInstalled) {
//            checkInGalaxyStore(activity, galaxyStoreInstalled);
//            displayed = true;
//            //}
//        }
//        else
//        if (store == R.id.menu_check_in_amazon_appstore) {
//            checkInAmazonAppstore(activity);
//            displayed = true;
//        }
//        else
//        if (store == R.id.menu_check_in_appgallery) {
//            //if (appGalleryInstalled) {
//                checkInHuaweiAppGallery(activity);
//                displayed = true;
//            //}
//        }
        else
        if (store == R.id.menu_check_in_github) {
            if (Build.VERSION.SDK_INT < 33)
                checkInGitHub(/*activity, false*/);
            else
                checkInDroidIfy(/*activity,*/ true);
            displayed = true;
        }
        else
        if (store == R.id.menu_check_in_apkpure) {
            checkInAPKPure(/*activity*/);
            displayed = true;
        }
        else
        if (store == R.id.menu_check_in_droidify) {
            checkInDroidIfy(/*activity,*/ false);
            displayed = true;
        }
        else
        if (store == R.id.menu_check_in_neostore) {
            checkInNeoStore(/*activity*/);
            displayed = true;
        }

        if (!displayed) {
            if ((store == -1) || (store == -2)) {
                // -1: CheckPPPReleasesBroadcastReceiver
                // -2: CheckCriticalPPPReleasesBroadcastReceiver

                if (droidifyInstalled)
                    checkInDroidIfy(/*activity,*/ false);
                else if (neostoreInstalled)
                    checkInNeoStore(/*activity*/);
                else if (fdroidInstalled)
                    checkInFDroid(/*activity*/);
                else if (apkPureInstalled)
                    checkInAPKPure(/*activity*/);
                else {
                    if (Build.VERSION.SDK_INT < 33)
                        checkInGitHub(/*activity, false*/);
                    else
                        checkInDroidIfy(/*activity,*/ true);
                }
            }
/*
            if (store == -1) {
                // this is for
                // - CheckPPPReleasesBroadcastReceiver

                //if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && galaxyStoreInstalled)
                //    checkInGalaxyStore(activity, true);
                //else if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI && appGalleryInstalled)
                //    checkInHuaweiAppGallery(activity);
                //else {
//                    if (appGalleryInstalled)
//                        checkInHuaweiAppGallery(activity);
//                    else if (amazonAppStoreInstalled)
//                        checkInAmazonAppstore(activity);
//                    else
                    if (droidifyInstalled)
                        checkInDroidIfy(activity, false);
                    else if (neostoreInstalled)
                        checkInNeoStore(activity);
                    else if (fdroidInstalled)
                        checkInFDroid(activity);
                    else if (apkPureInstalled)
                        checkInAPKPure(activity);
                    else {
                        if (Build.VERSION.SDK_INT < 33)
                            checkInGitHub(activity, false);
                        else {
                            checkInDroidIfy(activity, true);
                        }
                    }
                //}
            } else {
                // this is for
                // - CheckCriticalPPPReleasesBroadcastReceiver
                if (Build.VERSION.SDK_INT < 33)
                    checkInGitHub(activity, false);
                else {
                    checkInDroidIfy(activity, true);
                }
            }
 */
        }
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void checkInGitHub(/*final Activity activity, final boolean refreshOpenedDialog*/) {
        //noinspection ExtractMethodRecommender
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_MENU_ITEM_ID, R.id.menu_check_in_github);
        bundle.putInt(EXTRA_NEW_VERSION_CODE, newVersionCode);
        bundle.putString(EXTRA_NEW_VERSION_NAME, newVersionName);
        bundle.putBoolean(EXTRA_CRITICAL_CHECK, criticalCheck);
        bundle.putBoolean(EXTRA_NEW_VERSION_CRITICAL, newVersionCritical);
        //bundle.putBoolean(EXTRA_REFRESH_OPENED_DIALOG, refreshOpenedDialog);

        CheckPPPReleasesDialog dialog = new CheckPPPReleasesDialog(this);
        dialog.setArguments(bundle);
        if (!isFinishing())
            dialog.showDialog();
    }

    @SuppressLint("InflateParams")
    private void checkInFDroid(/*final Activity activity*/) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_MENU_ITEM_ID, R.id.menu_check_in_fdroid);

        CheckPPPReleasesDialog dialog = new CheckPPPReleasesDialog(this);
        dialog.setArguments(bundle);
        if (!isFinishing())
            dialog.showDialog();

/*
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
*/
    }

    @SuppressLint("InflateParams")
    private void checkInAPKPure(/*final Activity activity*/) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_MENU_ITEM_ID, R.id.menu_check_in_apkpure);

        CheckPPPReleasesDialog dialog = new CheckPPPReleasesDialog(this);
        dialog.setArguments(bundle);
        if (!isFinishing())
            dialog.showDialog();

/*
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

//        boolean fdroidInstalled = false;
//        PackageManager pm = activity.getPackageManager();
//        try {
//            pm.getPackageInfo(PPApplication.FDROID_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
//            fdroidInstalled = true;
//        } catch (Exception ignored) {}
//        if (fdroidInstalled)
//            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
//        else
//            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);
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
*/
    }

    @SuppressLint("InflateParams")
    private void checkInDroidIfy(/*final Activity activity,*/ boolean forGitHub) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_MENU_ITEM_ID, R.id.menu_check_in_droidify);
        bundle.putBoolean(EXTRA_FOR_GITHUB, forGitHub);

        CheckPPPReleasesDialog dialog = new CheckPPPReleasesDialog(this);
        dialog.setArguments(bundle);
        if (!isFinishing())
            dialog.showDialog();
    }

    @SuppressLint("InflateParams")
    private void checkInNeoStore(/*final Activity activity*/) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_MENU_ITEM_ID, R.id.menu_check_in_neostore);

        CheckPPPReleasesDialog dialog = new CheckPPPReleasesDialog(this);
        dialog.setArguments(bundle);
        if (!isFinishing())
            dialog.showDialog();

/*
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

//        boolean fdroidInstalled = false;
//        PackageManager pm = activity.getPackageManager();
//        try {
//            pm.getPackageInfo(PPApplication.FDROID_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
//            fdroidInstalled = true;
//        } catch (Exception ignored) {}
//        if (fdroidInstalled)
//            layout = inflater.inflate(R.layout.dialog_for_fdroid_app, null);
//        else
//            layout = inflater.inflate(R.layout.dialog_for_fdroid, null);
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
*/
    }

}
