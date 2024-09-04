package sk.henrichg.phoneprofilesplus;

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
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

public class ActivityLogActivity extends AppCompatActivity
                                            implements AddedActivityLogListener {

    //private DataWrapper dataWrapper;
    private ListView listView;
    private LinearLayout progressLinearLayout;
    private ActivityLogAdapter activityLogAdapter;
    private TextView addedNewLogsText;
    AppCompatSpinner filterSpinner;

    private int selectedFilter = 0;

    private SetAdapterAsyncTask setAdapterAsyncTask = null;

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
        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ppp_activity_log);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activity_log);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        filterSpinner = findViewById(R.id.activity_log_filter_spinner);
        String[] filterItems = new String[] {
                getString(R.string.activity_log_filter_all),
                getString(R.string.activity_log_filter_blocked_calls)
        };
        HighlightedSpinnerAdapter filterSpinnerAdapter = new HighlightedSpinnerAdapter(
                this,
                R.layout.spinner_highlighted_filter,
                filterItems);
        filterSpinnerAdapter.setDropDownViewResource(R.layout.spinner_highlighted_dropdown);
        filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.highlighted_spinner_all_editor));
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
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if (!filterInitialized) {
                //    filterInitialized = true;
                //    return;
                //}
                if (filterSpinner.getAdapter() != null) {
                    //if (filterSpinner.getAdapter().getCount() <= position)
                    //    position = 0;
                    ((HighlightedSpinnerAdapter) filterSpinner.getAdapter()).setSelection(position);
                }

                int selectedFilter;
                switch (position) {
                    case 0:
                        //noinspection DuplicateBranchesInSwitch
                        selectedFilter = PPApplication.ALFILTER_ALL;
                        break;
                    case 1:
                        selectedFilter = PPApplication.ALFILTER_CALL_SCREENING_BLOCKED_CALL;
                        break;
                    default:
                        selectedFilter = PPApplication.ALFILTER_ALL;
                }
                selectFilterItem(selectedFilter);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //addedNewLogs = false;
        addedNewLogsText = findViewById(R.id.activity_log_header_added_new_logs);
        addedNewLogsText.setVisibility(View.GONE);

        //dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        listView = findViewById(R.id.activity_log_list);
        listView.setEmptyView(findViewById(R.id.activity_log_list_empty));
        progressLinearLayout = findViewById(R.id.activity_log_linla_progress);
        listView.setVisibility(View.GONE);
        progressLinearLayout.setVisibility(View.VISIBLE);

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
            activityLogAdapter.reload(getApplicationContext(), selectedFilter);
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
                        activityLogAdapter.reload(getApplicationContext(), selectedFilter);
                    },
                    null,
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    this
            );

            if (!isFinishing())
                dialog.show();
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
            activityLogAdapter.reload(getApplicationContext(), selectedFilter);
            listView.setSelection(0);
            invalidateOptionsMenu();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_help) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.activity_log_help_title);
            dialogBuilder.setCancelable(true);
            //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_info_preference, null);
            dialogBuilder.setView(layout);

            TextView infoTextView = layout.findViewById(R.id.info_pref_dialog_info_text);

            StringBuilder _value = new StringBuilder();

            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getString(R.string.activity_log_help_message_colors)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);

            int color = ContextCompat.getColor(this, R.color.altype_profile);
            String colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_profile_activation)).append(StringConstants.TAG_BREAK_HTML);

            color = ContextCompat.getColor(this, R.color.altype_eventStart);
            colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_event_start)).append(StringConstants.TAG_BREAK_HTML);

            color = ContextCompat.getColor(this, R.color.altype_eventEnd);
            colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_event_end)).append(StringConstants.TAG_BREAK_HTML);

            color = ContextCompat.getColor(this, R.color.altype_restartEvents);
            colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_restart_events)).append(StringConstants.TAG_BREAK_HTML);

            color = ContextCompat.getColor(this, R.color.altype_eventDelayStartEnd);
            colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_event_delay_start_end)).append(StringConstants.TAG_BREAK_HTML);

            color = ContextCompat.getColor(this, R.color.altype_error);
            colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_error)).append(StringConstants.TAG_BREAK_HTML);

            color = ContextCompat.getColor(this, R.color.altype_other);
            colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
            _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_others));

            _value.append(StringConstants.TAG_DOUBLE_BREAK_HTML);
            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getString(R.string.activity_log_help_message)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_DOUBLE_BREAK_HTML);

            _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
            _value.append("\"").append(getString(R.string.altype_mergedProfileActivation)).append(": X")
                    .append(StringConstants.CHAR_HARD_SPACE_HTML).append("[").append(StringConstants.CHAR_HARD_SPACE_HTML).append("Y").append(StringConstants.CHAR_HARD_SPACE_HTML).append("]\":")
                    .append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_mergedProfileActivation)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

            _value.append(StringConstants.TAG_BREAK_HTML);
            _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
            _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
            _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
            _value.append("\"").append(getString(R.string.altype_profileActivation)).append("\":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_data_profileName)).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_data_displayedInGUI)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

            _value.append(StringConstants.TAG_BREAK_HTML);
            _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
            _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
            _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
            _value.append("\"").append(getString(R.string.altype_mergedProfileActivation)).append("\":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_data_profileNameEventName)).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_data_displayedInGUI)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

            _value.append(StringConstants.TAG_BREAK_HTML);
            _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
            _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
            _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
            _value.append(getString(R.string.activity_log_help_message_data_otherProfileDataTypes)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_data_profileName_otherDataTypes)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

            _value.append(StringConstants.TAG_BREAK_HTML);
            _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
            _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
            _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
            _value.append(getString(R.string.activity_log_help_message_data_otherEventDataTypes)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
            _value.append(getString(R.string.activity_log_help_message_data_eventName_otherDataTypes)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

            infoTextView.setText(StringFormatUtils.fromHtml(_value.toString(), true, false, 0, 0, true));

            infoTextView.setClickable(true);
            infoTextView.setMovementMethod(LinkMovementMethod.getInstance());

            dialogBuilder.setPositiveButton(R.string.activity_log_help_close, null);
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
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

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

    private void selectFilterItem(int selectedFilter) {
        this.selectedFilter = selectedFilter;

        setAdapterAsyncTask =
                new SetAdapterAsyncTask(selectedFilter, this, getApplicationContext());
        setAdapterAsyncTask.execute();

        filterSpinner.setSelection(selectedFilter);
    }

    private static class SetAdapterAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<Context> contextWeakReference;
        private final WeakReference<ActivityLogActivity> activityWeakReference;

        int selectedFilter;
        Cursor activityLogCursor = null;

        public SetAdapterAsyncTask(final int selectedFilter,
                                   final ActivityLogActivity activity,
                                   final Context context) {
            this.contextWeakReference = new WeakReference<>(context);
            this.activityWeakReference = new WeakReference<>(activity);
            this.selectedFilter = selectedFilter;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();

            if (context != null) {
                activityLogCursor =
                        DatabaseHandler.getInstance(context.getApplicationContext()).getActivityLogCursor(selectedFilter);
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
                    activity.activityLogAdapter = new ActivityLogAdapter(activity.getBaseContext(), activityLogCursor);

                    // Attach cursor adapter to the ListView
                    activity.listView.setAdapter(activity.activityLogAdapter);
                    activity.activityLogAdapter.notifyDataSetChanged();

                    activity.progressLinearLayout.setVisibility(View.GONE);
                    activity.listView.setVisibility(View.VISIBLE);

                    activity.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                        }
                    });
                }
            }

        }

    }

}
