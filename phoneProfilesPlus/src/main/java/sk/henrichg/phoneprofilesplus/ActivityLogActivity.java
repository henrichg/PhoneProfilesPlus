package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.readystatesoftware.systembartint.SystemBarTintManager;


public class ActivityLogActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;
    private ListView listView;
    private ActivityLogAdapter activityLogAdapter;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_activity_log);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activity_log);
        }

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        listView = (ListView) findViewById(R.id.activity_log_list);

        // Setup cursor adapter using cursor from last step
        activityLogAdapter = new ActivityLogAdapter(getBaseContext(), dataWrapper.getDatabaseHandler().getActivityLogCursor());

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
        if (PPApplication.getActivityLogEnabled(getApplicationContext())) {
            TypedArray a = getTheme().obtainStyledAttributes(GlobalGUIRoutines.getTheme(false, false, getApplicationContext()), new int[]{R.attr.actionActivityLogPauseIcon});
            int attributeResourceId = a.getResourceId(0, 0);
            menuItem.setIcon(attributeResourceId);
            menuItem.setTitle(R.string.menu_activity_log_pause);
        }
        else {
            TypedArray a = getTheme().obtainStyledAttributes(GlobalGUIRoutines.getTheme(false, false, getApplicationContext()), new int[] {R.attr.actionActivityLogPlayIcon});
            int attributeResourceId = a.getResourceId(0, 0);
            menuItem.setIcon(attributeResourceId);
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
                        dataWrapper.getDatabaseHandler().clearActivityLog();
                        activityLogAdapter.reload(dataWrapper);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                dialogBuilder.show();
                return true;
            case R.id.menu_activity_log_play_pause:
                boolean enabled = PPApplication.getActivityLogEnabled(getApplicationContext());
                if (enabled)
                    dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_PAUSEDLOGGING, null, null, null, 0);
                PPApplication.setActivityLogEnabled(getApplicationContext(), !enabled);
                if (!enabled)
                    dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_STARTEDLOGGING, null, null, null, 0);
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
