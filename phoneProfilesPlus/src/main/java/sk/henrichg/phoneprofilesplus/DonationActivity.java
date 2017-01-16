package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class DonationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.loadPreferences(getApplicationContext());

        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donation);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (PPApplication.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.donation_activity_title);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DonationFragment donationFragment = DonationFragment.newInstance();

        ft.replace(R.id.donation_activity_container, donationFragment, "donationFragment");
        ft.commit();
    }

    /**
     * Needed for Google Play In-app Billing. It uses startIntentSenderForResult(). The result is not propagated to
     * the Fragment like in startActivityForResult(). Thus we need to propagate manually to our Fragment.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
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
