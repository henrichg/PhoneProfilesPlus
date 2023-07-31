package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LogCrashActivity extends AppCompatActivity {

    LinearLayout progressLinearLayout;
    ListView listView;

    private ArrayAdapter<String> logCrashAdapter;

    private static final String LOG_CRASH_TITLE = "Log/crash file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_crash);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(LOG_CRASH_TITLE);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        progressLinearLayout = findViewById(R.id.log_crah_activity_linla_progress);

        listView = findViewById(R.id.log_crah_activity_list);
        logCrashAdapter = new ArrayAdapter<>(this, R.layout.listitem_log_crash, R.id.log_crash_list_item_text);
        listView.setAdapter(logCrashAdapter);

        Button goToBtn = findViewById(R.id.log_crash_list_go_to_bottom);
        goToBtn.setOnClickListener(v -> listView.setSelection(logCrashAdapter.getCount() - 1));
        goToBtn = findViewById(R.id.log_crash_list_go_to_top);
        goToBtn.setOnClickListener(v -> listView.setSelection(0));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshListView(true);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_crash_menu, menu);
        return true;
    }
/*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_activity_log_play_pause);

        int theme = GlobalGUIRoutines.getTheme(false, false, false, false, false, getApplicationContext());
        if (theme != 0) {
            TypedArray a = getTheme().obtainStyledAttributes(theme, new int[]{R.attr.actionActivityLogPauseIcon});
            int attributeResourceId = a.getResourceId(0, 0);
            a.recycle();
            menuItem.setIcon(attributeResourceId);
        }

        if (PPApplication.prefActivityLogEnabled) {
            menuItem.setTitle(R.string.menu_activity_log_pause);
        }
        else {
            menuItem.setTitle(R.string.menu_activity_log_play);
        }
        return super.onPrepareOptionsMenu(menu);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else
        if (itemId == R.id.menu_show_log) {
            refreshListView(true);
            return true;
        }
        else
        if (itemId == R.id.menu_show_crash) {
            refreshListView(false);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //Cursor cursor = activityLogAdapter.getCursor();
        //if (cursor != null)
        //    cursor.close();
    }
    */

    void refreshListView(boolean showLog)
    {
        RefreshListViewAsyncTask refreshAsyncTask = new RefreshListViewAsyncTask(this, showLog);
        refreshAsyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        List<String> _fileList = null;
        final boolean _showLog;

        private final WeakReference<LogCrashActivity> activityWeakRef;

        public RefreshListViewAsyncTask(LogCrashActivity activity, boolean showLog) {
            this.activityWeakRef = new WeakReference<>(activity);
            _showLog = showLog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _fileList = new ArrayList<>();

            LogCrashActivity activity = activityWeakRef.get();
            if (activity != null) {
                activity.progressLinearLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            LogCrashActivity activity = activityWeakRef.get();
            if ((activity != null) && (!activity.isFinishing())) {
                File sd = activity.getApplicationContext().getExternalFilesDir(null);

                File logFile;
                if (_showLog)
                    logFile = new File(sd, PPApplication.LOG_FILENAME);
                else
                    logFile = new File(sd, CustomACRAReportingAdministrator.CRASH_FILENAME);
                if (logFile.exists()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(logFile));
                        String line;

                        //read line by line
                        while ((line = br.readLine()) != null) {
                            _fileList.add(line);
                        }
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                    }
                } else
                    _fileList.add("--- File not exists ---");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            LogCrashActivity activity = activityWeakRef.get();
            if ((activity != null) && (!activity.isFinishing())) {
                activity.progressLinearLayout.setVisibility(View.GONE);

                if (activity.getSupportActionBar() != null) {
                    if (_showLog)
                        activity.getSupportActionBar().setTitle(LOG_CRASH_TITLE+" - log.txt");
                    else
                        activity.getSupportActionBar().setTitle(LOG_CRASH_TITLE+" - crash.txt");
                }

                activity.logCrashAdapter.clear();
                activity.logCrashAdapter.addAll(_fileList);
                activity.logCrashAdapter.notifyDataSetChanged();
            }
        }

    }

}
