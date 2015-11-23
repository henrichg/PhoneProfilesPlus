package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ImportantInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GlobalData.loadPreferences(getApplicationContext());

        // must by called before super.onCreate() for PreferenceActivity
        GUIData.setTheme(this, false, false); // must by called before super.onCreate()
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_important_info);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.info_notification_title);

        PackageInfo pinfo = null;
        int versionCode = 0;
        Context context = getApplicationContext();
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }

        boolean news = true;
        if (versionCode <= ImportantInfoNotification.VERSION_CODE_FOR_NEWS) {
            // old installed version, no show news
            TextView infoTextNews = (TextView) findViewById(R.id.activity_info_notification_dialog_news);
            infoTextNews.setVisibility(View.GONE);
            news = false;
        }

        if (android.os.Build.VERSION.SDK_INT < 23) {
            TextView infoText15 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText16 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text16);
            infoText16.setVisibility(View.GONE);
            TextView infoText17 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText18 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text18);
            infoText18.setVisibility(View.GONE);
        }
        else {
            if (!news) {
                TextView infoText15 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text15);
                infoText15.setVisibility(View.GONE);
                TextView infoText17 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text17);
                infoText17.setVisibility(View.GONE);
            }
            else {
                TextView infoText16 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text16);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text18);
                infoText18.setVisibility(View.GONE);
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText1 = (TextView)findViewById(R.id.activity_info_notification_dialog_info_text1);
            infoText1.setVisibility(View.GONE);
            TextView infoText11 = (TextView)findViewById(R.id.activity_info_notification_dialog_info_text11);
            infoText11.setVisibility(View.GONE);

            TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
            infoText14.setVisibility(View.GONE);
        }
        else {
            if (versionCode > 1772) { // deployed version for this info
                // new installed version, no show old news
                TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
                infoText13.setVisibility(View.GONE);
                TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        startActivity(intent);
                        finish();
                    }
                });
            }
            else {
                TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);
                TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
                infoText13.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }

        TextView infoText3 = (TextView)findViewById(R.id.activity_info_notification_dialog_info_text3);
        infoText3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, GlobalData.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
                intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "checkbox");
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
