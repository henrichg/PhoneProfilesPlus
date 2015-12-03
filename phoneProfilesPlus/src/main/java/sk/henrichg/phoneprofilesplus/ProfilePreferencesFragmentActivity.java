package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ProfilePreferencesFragmentActivity extends AppCompatActivity
{
    private long profile_id = 0;
    int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        GUIData.setTheme(this, false, false); // must by called before super.onCreate()
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_preferences);

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

        profile_id = getIntent().getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        newProfileMode = getIntent().getIntExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);

        if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
            getSupportActionBar().setTitle(R.string.title_activity_default_profile_preferences);
        else
            getSupportActionBar().setTitle(R.string.title_activity_profile_preferences);
        
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile_id);
            arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
            if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
                arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE);
            else
                arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
            ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.activity_profile_preferences_container, fragment, "ProfilePreferencesFragment").commit();
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void finish() {

        ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
        if (fragment != null)
            profile_id = fragment.profile_id;

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile_id);
        returnIntent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        setResult(RESULT_OK,returnIntent);

        super.finish();
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

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GUIData.reloadActivity(this);
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
        if (fragment != null)
            fragment.doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // handle your back button code here
            ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
            if ((fragment != null) && (fragment.isActionModeActive()))
            {
                fragment.finishActionMode(ProfilePreferencesFragment.BUTTON_CANCEL);
                return true; // consumes the back key event - ActionMode is not finished
            }
            else
                return super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }


}
