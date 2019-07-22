package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class NotUsedMobileCellsDetectedActivity extends AppCompatActivity {

    AlertDialog mDialog;
    TextView cellIdTextView;
    TextView lastConnectTimeTextView;
    TextView cellNameTextView;
    ListView lastRunningEventsListView;

    static int mobileCellId = Integer.MAX_VALUE;
    static long lastConnectedTime = 0;
    static String lastRunningEvents = "";

    static String EXTRA_MOBILE_CELL_ID = "mobile_cell_id";
    static String EXTRA_MOBILE_LAST_CONNECTED_TIME = "last_connected_time";
    static String EXTRA_MOBILE_LAST_RUNNING_EVENTS = "last_running_events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        PPApplication.logE("NotUsedMobileCellsDetectedActivity.onCreate", "xxx");

        Intent intent = getIntent();
        if (intent != null) {
            mobileCellId = intent.getIntExtra(EXTRA_MOBILE_CELL_ID, 0);
            lastConnectedTime = intent.getLongExtra(EXTRA_MOBILE_LAST_CONNECTED_TIME, 0);
            lastRunningEvents = intent.getStringExtra(EXTRA_MOBILE_LAST_RUNNING_EVENTS);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        // not working on Android 2.3.x
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/);
        GlobalGUIRoutines.setLanguage(this);

        PPApplication.logE("NotUsedMobileCellsDetectedActivity.onStart", "xxx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.not_used_mobile_cells_detected_title);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                NotUsedMobileCellsDetectedActivity.this.finish();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NotUsedMobileCellsDetectedActivity.this.finish();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_not_used_mobile_cells_detected, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                NotUsedMobileCellsDetectedActivity.this.onShow();
            }
        });

        cellIdTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_cell_id);
        lastConnectTimeTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_connection_time);
        cellNameTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_cells_name);
        lastRunningEventsListView = layout.findViewById(R.id.not_used_mobile_cells_dlg_last_running_events_listview);

        if (!isFinishing())
            mDialog.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void onShow() {
        new AsyncTask<Void, Integer, Void>() {

            DatabaseHandler db;
            List<MobileCellsData> _cellsList = null;
            String cellName;
            //final List<Profile> profileList = new ArrayList<>();

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                db = DatabaseHandler.getInstance(NotUsedMobileCellsDetectedActivity.this);
                _cellsList = new ArrayList<>();
                cellName = "";

                //listView.setVisibility(View.GONE);
                //linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                db.addMobileCellsToList(_cellsList, mobileCellId);
                if (!_cellsList.isEmpty())
                    cellName = _cellsList.get(0).name;

                /*boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator(activity);
                Profile profile;
                profile = DataWrapper.getNonInitializedProfile(
                        activity.getResources().getString(R.string.profile_name_default),
                        Profile.PROFILE_ICON_DEFAULT, 0);
                profile.generateIconBitmap(activity, false, 0xFF, false);
                if (applicationEditorPrefIndicator)
                    profile.generatePreferencesIndicator(activity, false, 0xFF);
                profileList.add(profile);
                for (int index = 0; index < 7; index++) {
                    profile = profileListFragment.activityDataWrapper.getPredefinedProfile(index, false, activity);
                    profile.generateIconBitmap(activity, false, 0xFF, false);
                    if (applicationEditorPrefIndicator)
                        profile.generatePreferencesIndicator(activity, false, 0xFF);
                    profileList.add(profile);
                }*/

                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                cellIdTextView.setText(getString(R.string.not_used_mobile_cells_detected_cell_id) + " " + mobileCellId);
                lastConnectTimeTextView.setText(getString(R.string.not_used_mobile_cells_detected_connection_time) + " " +
                        GlobalGUIRoutines.timeDateStringFromTimestamp(NotUsedMobileCellsDetectedActivity.this, lastConnectedTime));
                if (!cellName.isEmpty())
                cellNameTextView.setText(cellName);

                /*listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                AddProfileAdapter addProfileAdapter = new AddProfileAdapter(AddProfileDialog.this, activity, profileList);
                listView.setAdapter(addProfileAdapter);*/
            }

        }.execute();
    }

}
