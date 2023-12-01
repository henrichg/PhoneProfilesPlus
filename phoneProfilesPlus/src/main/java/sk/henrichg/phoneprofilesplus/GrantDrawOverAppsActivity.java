package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GrantDrawOverAppsActivity extends AppCompatActivity {

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        GlobalGUIRoutines.lockScreenOrientation(this, true);

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
                finish();
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
                        this
                );

                if (!isFinishing())
                    dialog2.show();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel,  (dialog, which) -> finish());

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_draw_over_apps, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.dialog_draw_over_apps_when_not_possible_text);
        CharSequence str1 = getString(R.string.grant_draw_over_apps_dialog_when_not_possible_text) + StringConstants.CHAR_NEW_LINE;
        CharSequence str2 = str1 + "https://techforesta.com/display-over-other-apps-feature-not-available/" + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = "https://techforesta.com/display-over-other-apps-feature-not-available/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length(), str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

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
