package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GrantDrawOverAppsActivity extends AppCompatActivity {

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditorActivity.itemDragPerformed = false;

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] GrantDrawOverAppsActivity.onCreate", "xxx");
        //Log.e("GrantDrawOverAppsActivity.onCreate", "xxx");

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart()
    {
        super.onStart();

        final Activity activity = this;

        GlobalGUIRoutines.lockScreenOrientation(this/*, false*/);

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.phone_profiles_pref_drawOverlaysPermissions);
        dialogBuilder.setPositiveButton(R.string.alert_button_grant, (dialog, which) -> {
            boolean ok = false;
            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getApplicationContext())) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse(PPApplication.INTENT_DATA_PACKAGE +PPApplication.PACKAGE_NAME));
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    ok = true;
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                activity.finish();
            }
            if (!ok) {
                PPAlertDialog dialog2 = new PPAlertDialog(
                        getString(R.string.phone_profiles_pref_drawOverlaysPermissions),
                        getString(R.string.setting_screen_not_found_alert),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        dialog1 -> finish(),
                        null,
                        true, true,
                        false, false,
                        true,
                        false,
                        activity
                );

                if (!activity.isFinishing())
                    dialog2.show();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel,  (dialog, which) -> finish());

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_draw_over_apps, null);
        dialogBuilder.setView(layout);

        if (Build.VERSION.SDK_INT >= 33) {
            TextView text = layout.findViewById(R.id.dialog_draw_over_apps_when_not_possible_text);
            CharSequence str1 = getString(R.string.grant_draw_over_apps_dialog_when_not_possible_text) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
            Spannable sbt = new SpannableString(str1);
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    String restrictedSettingsText = StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML + StringConstants.TAG_URL_LINK_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_4) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_5) + StringConstants.TAG_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_6) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.DROIDIFY_INSTALLATION_SITE + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_10) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML + StringConstants.TAG_URL_LINK_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_7) + " " +
                            "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_export) + "\"." + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_8) + " " +
                            "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_import) + "\".";

                    PPAlertDialog dialog2 = new PPAlertDialog(
                            getString(R.string.phone_profiles_pref_drawOverlaysPermissions),
                            StringFormatUtils.fromHtml(restrictedSettingsText, true, false, 0, 0, true),
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
                            true,
                            activity
                    );

                    dialog2.show();
                }
            };
            sbt.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
            //noinspection DataFlowIssue
            text.setVisibility(View.VISIBLE);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            TextView text = layout.findViewById(R.id.dialog_draw_over_apps_when_not_possible_text);
            //noinspection DataFlowIssue
            text.setVisibility(View.GONE);
        }

        mDialog = dialogBuilder.create();
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);

        if (!isFinishing())
            mDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /*
    private void doShow() {
        new ShowActivityAsyncTask(this).execute();
    }
    */

}
