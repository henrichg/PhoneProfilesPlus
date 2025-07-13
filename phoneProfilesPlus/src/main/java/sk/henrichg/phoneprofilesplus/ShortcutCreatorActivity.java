package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public class ShortcutCreatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, true, false, false, true, false, false, false);

        super.onCreate(savedInstanceState);
        // animation
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] ShortcutCreatorActivity.onCreate", "xxx");

        //GlobalGUIRoutines.setLanguage(this);

    // set window dimensions ----------------------------------------------------------

    /*    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // display dimensions
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float popupWidth = displaymetrics.widthPixels;
        float popupMaxHeight = displaymetrics.heightPixels;
        //Display display = getWindowManager().getDefaultDisplay();
        //float popupWidth = display.getWidth();
        //float popupMaxHeight = display.getHeight();
        float popupHeight = 0;
        float actionBarHeight = 0;

        // action bar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        // set max. dimensions for display orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //popupWidth = Math.round(popupWidth / 100f * 50f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 50f;
        }
        else
        {
            //popupWidth = Math.round(popupWidth / 100f * 70f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 80f;
        }
        popupMaxHeight = popupMaxHeight / 100f * 90f;

        // add action bar height
        popupHeight = popupHeight + actionBarHeight;

        final float scale = getResources().getDisplayMetrics().density;

        // add list items height
        int profileCount = DatabaseHandler.getInstance(getApplicationContext()).getProfilesCount();
        ++profileCount; // for restart events
        if (profileCount > 0) {
            popupHeight = popupHeight + (60f * scale * profileCount); // item
            popupHeight = popupHeight + (1f * scale * (profileCount - 1)); // divider
        }
        else
            popupHeight = popupHeight + 60f * scale; // for empty TextView

        popupHeight = popupHeight + (20f * scale); // listview padding

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));

    */

    //-----------------------------------------------------------------------------------

        setContentView(R.layout.activity_shortcut_creator);

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        /*if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_shortcut_creator);
            getSupportActionBar().setElevation(0);
        }*/

        setTitle(R.string.title_activity_shortcut_creator);

        //databaseHandler = new DatabaseHandler(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    /*
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    //	Debug.stopMethodTracing();
    }
    */

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GlobalGUIRoutines.reloadActivity(this, false);
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
    }

}
