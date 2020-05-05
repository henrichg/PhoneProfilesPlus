package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



public class ActivityLogActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;
    private ListView listView;
    private ActivityLogAdapter activityLogAdapter;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_activity_log);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activity_log);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        listView = findViewById(R.id.activity_log_list);

        // Setup cursor adapter using cursor from last step
        activityLogAdapter = new ActivityLogAdapter(getBaseContext(), DatabaseHandler.getInstance(getApplicationContext()).getActivityLogCursor());

        // Attach cursor adapter to the ListView
        listView.setAdapter(activityLogAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_log, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*MenuItem menuItem = menu.findItem(R.id.menu_settingsX);
        menuItem.setTitle(getResources().getString(R.string.menu_settings) + "  >");*/
        MenuItem menuItem = menu.findItem(R.id.menu_activity_log_play_pause);
        if (PPApplication.prefActivityLogEnabled) {
            int theme = GlobalGUIRoutines.getTheme(false, false, /*false,*/ false, getApplicationContext());
            if (theme != 0) {
                TypedArray a = getTheme().obtainStyledAttributes(theme, new int[]{R.attr.actionActivityLogPauseIcon});
                int attributeResourceId = a.getResourceId(0, 0);
                a.recycle();
                menuItem.setIcon(attributeResourceId);
            }
            menuItem.setTitle(R.string.menu_activity_log_pause);
        }
        else {
            int theme = GlobalGUIRoutines.getTheme(false, false, /*false,*/ false, getApplicationContext());
            if (theme != 0) {
                TypedArray a = getTheme().obtainStyledAttributes(theme, new int[]{R.attr.actionActivityLogPlayIcon});
                int attributeResourceId = a.getResourceId(0, 0);
                a.recycle();
                menuItem.setIcon(attributeResourceId);
            }
            menuItem.setTitle(R.string.menu_activity_log_play);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_activity_log_reload:
                activityLogAdapter.reload(dataWrapper);
                listView.setSelection(0);
                return true;
            case R.id.menu_activity_log_clear:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.activity_log_clear_alert_title);
                dialogBuilder.setMessage(R.string.activity_log_clear_alert_message);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHandler.getInstance(getApplicationContext()).clearActivityLog();
                        activityLogAdapter.reload(dataWrapper);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

                if (!isFinishing())
                    dialog.show();
                return true;
            case R.id.menu_activity_log_play_pause:
                boolean enabled = PPApplication.prefActivityLogEnabled;
                if (enabled)
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_PAUSED_LOGGING, null, null, null, 0, "");
                PPApplication.setActivityLogEnabled(getApplicationContext(), !enabled);
                if (!enabled)
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_STARTED_LOGGING, null, null, null, 0, "");
                activityLogAdapter.reload(dataWrapper);
                listView.setSelection(0);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Cursor cursor = activityLogAdapter.getCursor();
        if (cursor != null)
            cursor.close();
    }

}
