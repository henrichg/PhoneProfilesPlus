package sk.henrichg.phoneprofilesplus;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

public class ActivityLogActivity extends AppCompatActivity
                                            implements AddedActivityLogListener {

    //ActivityLogActivity mActivity;
    //private DataWrapper dataWrapper;
    private ListView listView;
    private LinearLayout progressLinearLayout;
    private ActivityLogAdapter activityLogAdapter;
    private TextView addedNewLogsText;
    AppCompatSpinner filterSpinner;
    Toolbar subToolbar;
    LinearLayout listHeader;
    Button activatedProfileButton;

    private int selectedFilter = 0;

    long mActivatedProfileFilter = Profile.PROFILE_NO_ACTIVATE;
    long mEventFilter = 0;

    private SetAdapterAsyncTask setAdapterAsyncTask = null;

    static final String EXTRA_SELECTED_FILTER = "EXTRA_SELECTED_FILTER";
    static final String EXTRA_ACTIVATED_PROFILE_FILTER = "EXTRA_ACTIVATED_PROFILE_FILTER";
    static final String EXTRA_EVENT_FILTER = "EXTRA_EVENT_FILTER";

    //boolean addedNewLogs = false;

    @Override
    public void addedActivityLog() {
        //addedNewLogs = true;
        addedNewLogsText.setVisibility(View.VISIBLE);
    }

    static private class AddedActivityLogBroadcastReceiver extends BroadcastReceiver {

        private final AddedActivityLogListener listener;

        public AddedActivityLogBroadcastReceiver(AddedActivityLogListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.addedActivityLog();
        }

    }
    private AddedActivityLogBroadcastReceiver addedActivityLogBroadcastReceiver;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //mActivity = this;

        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ppp_activity_log);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        mActivatedProfileFilter = getIntent().getLongExtra(EXTRA_ACTIVATED_PROFILE_FILTER, Profile.PROFILE_NO_ACTIVATE);
        mEventFilter = getIntent().getLongExtra(EXTRA_EVENT_FILTER, 0);
        selectedFilter = getIntent().getIntExtra(EXTRA_SELECTED_FILTER, 0);

        Toolbar toolbar = findViewById(R.id.activity_log_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activity_log);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        activatedProfileButton = findViewById(R.id.activity_log_filter_activated_profile);

        filterSpinner = findViewById(R.id.activity_log_filter_spinner);
        String[] filterItems = new String[] {
                getString(R.string.activity_log_filter_all),
                getString(R.string.activity_log_filter_blocked_calls),
                getString(R.string.activity_log_filter_errors),
                getString(R.string.activity_log_filter_events_lifecycle),
                getString(R.string.activity_log_filter_event_start),
                getString(R.string.activity_log_filter_event_end),
                getString(R.string.activity_log_filter_event_stop),
                getString(R.string.activity_log_filter_restart_events),
                getString(R.string.activity_log_filter_profile_activations),
        };
        PPSpinnerAdapter filterSpinnerAdapter = new PPSpinnerAdapter(
                this,
                R.layout.ppp_spinner_filter,
                filterItems);
        filterSpinnerAdapter.setDropDownViewResource(R.layout.ppp_spinner_dropdown);
        filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        //filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.highlighted_spinner_all_editor));
/*        switch (appTheme) {
            case "dark":
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_dark));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_white));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
//            case "dlight":
//                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
//                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
//                break;
            default:
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
        }*/
        //filterInitialized = false;
        filterSpinner.setAdapter(filterSpinnerAdapter);
//        filterSpinner.setEnabled(false);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if (!filterInitialized) {
                //    filterInitialized = true;
                //    return;
                //}
                if (filterSpinner.getAdapter() != null) {
                    //if (filterSpinner.getAdapter().getCount() <= position)
                    //    position = 0;
                    ((PPSpinnerAdapter) filterSpinner.getAdapter()).setSelection(position);
                }

                int selectedFilter;
                switch (position) {
                    case 0:
                        //noinspection DuplicateBranchesInSwitch
                        selectedFilter = PPApplication.ALFILTER_ALL;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 1:
                        selectedFilter = PPApplication.ALFILTER_CALL_SCREENING_BLOCKED_CALL;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 2:
                        selectedFilter = PPApplication.ALFITER_ERRORS;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 3:
                        selectedFilter = PPApplication.ALFILTER_EVENTS_LIFECYCLE;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 4:
                        selectedFilter = PPApplication.ALFILTER_EVENT_START;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 5:
                        selectedFilter = PPApplication.ALFILTER_EVENT_END;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 6:
                        selectedFilter = PPApplication.ALFILTER_EVENT_STOP;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 7:
                        selectedFilter = PPApplication.ALFILTER_RESTART_EVENTS;
                        activatedProfileButton.setVisibility(View.GONE);
                        break;
                    case 8:
                        selectedFilter = PPApplication.ALFITER_PROFILE_ACTIVATION;
                        activatedProfileButton.setVisibility(View.VISIBLE);
                        break;
                    default:
                        selectedFilter = PPApplication.ALFILTER_ALL;
                        activatedProfileButton.setVisibility(View.GONE);
                }
                selectFilterItem(selectedFilter);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        activatedProfileButton.setOnClickListener(v -> {
            if (!isFinishing()) {
                Bundle bundle = new Bundle();
                bundle.putLong(EXTRA_ACTIVATED_PROFILE_FILTER, mActivatedProfileFilter);
                bundle.putLong(EXTRA_SELECTED_FILTER, selectedFilter);
                ActivityLogActivatedProfileFilterDialog dialog = new ActivityLogActivatedProfileFilterDialog((ActivityLogActivity) filterSpinner.getContext());
                dialog.setArguments(bundle);
                dialog.showDialog();
            }
        });
        // TODO sem daj volanie dialogu na vyber udalsoti

        //addedNewLogs = false;
        addedNewLogsText = findViewById(R.id.activity_log_header_added_new_logs);
        //noinspection DataFlowIssue
        addedNewLogsText.setVisibility(View.GONE);

        //dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        listView = findViewById(R.id.activity_log_list);
        //noinspection DataFlowIssue
        listView.setEmptyView(findViewById(R.id.activity_log_list_empty));
        progressLinearLayout = findViewById(R.id.activity_log_linla_progress);
        listView.setVisibility(View.GONE);
        progressLinearLayout.setVisibility(View.VISIBLE);

        subToolbar = findViewById(R.id.activity_log_subToolbar);
        listHeader = findViewById(R.id.activity_log_liLa2);

        ViewGroup activityLogRoot = findViewById(R.id.activity_log_root);
        //noinspection DataFlowIssue
        final LayoutTransition layoutTransition = activityLogRoot.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        if (GlobalGUIRoutines.areSystemAnimationsEnabled(getApplicationContext())) {
            if (getResources().getBoolean(R.bool.forceHideHeaderOrBottomBar)) {
                listView.setOnScrollListener(new ActivityLogAutoHideShowListHeaderScrollListener() {
                    @Override
                    public void onHide() {
                        if (!layoutTransition.isRunning()) {
                            listHeader.setVisibility(View.GONE);
                            subToolbar.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onShow() {
                        if (!layoutTransition.isRunning()) {
                            subToolbar.setVisibility(View.VISIBLE);
                            listHeader.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        addedActivityLogBroadcastReceiver = new AddedActivityLogBroadcastReceiver(this);
        int receiverFlags = 0;
        if (Build.VERSION.SDK_INT >= 34)
            receiverFlags = RECEIVER_NOT_EXPORTED;
        registerReceiver(addedActivityLogBroadcastReceiver,
                new IntentFilter(PPApplication.ACTION_ADDED_ACIVITY_LOG), receiverFlags);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart() {
        super.onStart();

        setAdapterAsyncTask =
                new SetAdapterAsyncTask(selectedFilter, this, getApplicationContext());
        setAdapterAsyncTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_log, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*MenuItem menuItem = menu.findItem(R.id.menu_settingsX);
        menuItem.setTitle(getString(R.string.menu_settings) + "  >");*/
        MenuItem menuItem = menu.findItem(R.id.menu_activity_log_play_pause);

        //int theme = GlobalGUIRoutines.getTheme(false, false, /*false,*/ false, false, false, false, getApplicationContext());
        //if (theme != 0) {
        //TypedArray a = getTheme().obtainStyledAttributes(theme, new int[]{R.attr.actionActivityLogPauseIcon});
        //int attributeResourceId = a.getResourceId(0, 0);
        //a.recycle();
        //menuItem.setIcon(attributeResourceId);
        menuItem.setIcon(R.drawable.ic_action_activity_log_pause);
        //}

        if (PPApplication.prefActivityLogEnabled) {
            menuItem.setTitle(R.string.menu_activity_log_pause);
        } else {
            menuItem.setTitle(R.string.menu_activity_log_play);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_reload) {
            //addedNewLogs = false;
            addedNewLogsText.setVisibility(View.GONE);
            activityLogAdapter.reload(this, selectedFilter);
            listView.setSelection(0);
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_clear) {
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.activity_log_clear_alert_title),
                    getString(R.string.activity_log_clear_alert_message),
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
                        //addedNewLogs = false;
                        addedNewLogsText.setVisibility(View.GONE);
                        DatabaseHandler.getInstance(getApplicationContext()).clearActivityLog();
                        activityLogAdapter.reload(this, selectedFilter);
                    },
                    null,
                    null,
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    false,
                    this
            );

            if (!isFinishing())
                dialog.showDialog();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_play_pause) {
            boolean enabled = PPApplication.prefActivityLogEnabled;
            if (enabled)
                PPApplicationStatic.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_PAUSED_LOGGING, null, null, "");
            PPApplicationStatic.setActivityLogEnabled(getApplicationContext(), !enabled);
            if (!enabled)
                PPApplicationStatic.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_STARTED_LOGGING, null, null, "");
            activityLogAdapter.reload(this, selectedFilter);
            listView.setSelection(0);
            invalidateOptionsMenu();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_help) {
            Intent intent = new Intent(getBaseContext(), ActivityLogHelpActivity.class);
            startActivity(intent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        EditorActivity.itemDragPerformed = false;

        try {
            unregisterReceiver(addedActivityLogBroadcastReceiver);
        } catch (Exception ignored) {}
        addedActivityLogBroadcastReceiver = null;

        Cursor cursor = activityLogAdapter.getCursor();
        if (cursor != null)
            cursor.close();

        if ((setAdapterAsyncTask != null) &&
                setAdapterAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setAdapterAsyncTask.cancel(true);
        setAdapterAsyncTask = null;

        /*
        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
        */
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(EXTRA_ACTIVATED_PROFILE_FILTER, mActivatedProfileFilter);
        savedInstanceState.putLong(EXTRA_EVENT_FILTER, mEventFilter);
        savedInstanceState.putInt(EXTRA_SELECTED_FILTER, selectedFilter);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mActivatedProfileFilter = savedInstanceState.getLong(EXTRA_ACTIVATED_PROFILE_FILTER, Profile.PROFILE_NO_ACTIVATE);
        mEventFilter = savedInstanceState.getLong(EXTRA_EVENT_FILTER, 0);
        selectedFilter = savedInstanceState.getInt(EXTRA_SELECTED_FILTER, 0);
    }

    private void selectFilterItem(int selectedFilter) {
        this.selectedFilter = selectedFilter;

        setAdapterAsyncTask =
                new SetAdapterAsyncTask(selectedFilter, this, getApplicationContext());
        setAdapterAsyncTask.execute();
    }

    private static class SetAdapterAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<Context> contextWeakReference;
        private final WeakReference<ActivityLogActivity> activityWeakReference;

        final int _selectedFilter;
        Cursor activityLogCursor = null;

        public SetAdapterAsyncTask(final int selectedFilter,
                                   final ActivityLogActivity activity,
                                   final Context context) {
            this.contextWeakReference = new WeakReference<>(context);
            this.activityWeakReference = new WeakReference<>(activity);
            this._selectedFilter = selectedFilter;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            ActivityLogActivity activity = activityWeakReference.get();

            if ((context != null) && (activity != null)) {
                String profileName = "";
                if ((activity.mActivatedProfileFilter != 0) &&
                        (activity.mActivatedProfileFilter != Profile.PROFILE_NO_ACTIVATE))
                    profileName = DatabaseHandler.getInstance(context.getApplicationContext()).
                            getProfileName(activity.mActivatedProfileFilter);
                activityLogCursor =
                        DatabaseHandler.getInstance(context.getApplicationContext())
                                .getActivityLogCursor(_selectedFilter, profileName);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            ActivityLogActivity activity = activityWeakReference.get();

            if ((context != null) && (activity != null)) {
                if (activityLogCursor != null) {
                    activity.activityLogAdapter = new ActivityLogAdapter(activity, activityLogCursor);

                    // Attach cursor adapter to the ListView
                    activity.listView.setAdapter(activity.activityLogAdapter);
                    activity.activityLogAdapter.notifyDataSetChanged();

                    activity.progressLinearLayout.setVisibility(View.GONE);
                    activity.listView.setVisibility(View.VISIBLE);

                    activity.listView.setOnItemClickListener((parent, view, position, id) -> {
                        ActivityLogAdapter adapter = (ActivityLogAdapter) parent.getAdapter();
                        Cursor cursor = adapter.getCursor();
                        cursor.moveToPosition(position);
                        int logTypeIndex = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_LOG_TYPE);
                        int logType = cursor.getInt(logTypeIndex);

                        if (logType == PPApplication.ALTYPE_CALL_SCREENING_BLOCKED_CALL) {
//                                Log.e("ActivityLogActivity.onItemClick", "blocked call");
                            int telNumberIndex = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_PROFILE_NAME);
                            String telNumber = cursor.getString(telNumberIndex);
//                                Log.e("ActivityLogActivity.onItemClick", "telNumber="+telNumber);
                            if (!telNumber.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + telNumber));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                parent.getContext().startActivity(intent);
//                                    Log.e("ActivityLogActivity.onItemClick", "dialer started");
                            }
                        }
                    });
                }
            }

        }

    }

    void setActivatedPorfilesFilter(int _selectedFilter) {
        selectFilterItem(_selectedFilter);
    }

    void setEventFilter(int _selectedFilter) {
        selectFilterItem(_selectedFilter);
    }

}
