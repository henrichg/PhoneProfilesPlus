package sk.henrichg.phoneprofilesplus;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.readystatesoftware.systembartint.SystemBarTintManager;


public class ActivityLogActivity extends AppCompatActivity {

    DataWrapper dataWrapper;
    ListView listView;
    ActivityLogAdapter activityLogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        GUIData.setTheme(this, false, false); // must by called before super.onCreate()
        GUIData.setLanguage(getBaseContext());

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
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_activity_log);

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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_activity_log_reload:
                activityLogAdapter.reload(dataWrapper);
                //listView.smoothScrollToPosition(0);
                listView.setSelection(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
