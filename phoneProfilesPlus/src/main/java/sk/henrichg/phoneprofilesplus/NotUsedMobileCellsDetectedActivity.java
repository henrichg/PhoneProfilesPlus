package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;


public class NotUsedMobileCellsDetectedActivity extends AppCompatActivity {

    AlertDialog mDialog;
    private TextView cellIdTextView;
    private TextView lastConnectTimeTextView;
    TextView cellNameTextView;
    private ListView lastRunningEventsListView;
    private MobileCellNamesDialogX mMobileCellNamesDialog;

    private int mobileCellId = Integer.MAX_VALUE;
    private long lastConnectedTime = 0;
    private String lastRunningEvents = "";
    private String lastPausedEvents = "";

    private final List<Event> eventList = new ArrayList<>();

    static final String EXTRA_MOBILE_CELL_ID = "mobile_cell_id";
    static final String EXTRA_MOBILE_LAST_CONNECTED_TIME = "last_connected_time";
    static final String EXTRA_MOBILE_LAST_RUNNING_EVENTS = "last_running_events";
    static final String EXTRA_MOBILE_LAST_PAUSED_EVENTS = "last_paused_events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //PPApplication.logE("NotUsedMobileCellsDetectedActivity.onCreate", "xxx");

        Intent intent = getIntent();
        if (intent != null) {
            mobileCellId = intent.getIntExtra(EXTRA_MOBILE_CELL_ID, 0);
            lastConnectedTime = intent.getLongExtra(EXTRA_MOBILE_LAST_CONNECTED_TIME, 0);
            lastRunningEvents = intent.getStringExtra(EXTRA_MOBILE_LAST_RUNNING_EVENTS);
            lastPausedEvents = intent.getStringExtra(EXTRA_MOBILE_LAST_PAUSED_EVENTS);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GlobalGUIRoutines.lockScreenOrientation(this, true);

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false);
        //GlobalGUIRoutines.setLanguage(this);

        //PPApplication.logE("NotUsedMobileCellsDetectedActivity.onStart", "xxx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.not_used_mobile_cells_detected_title);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 1. test existence of mobile cell id in table, may be deleted
                // 2. test existence of event in table, may be deleted

                final int _mobileCellId = mobileCellId;
                final long _lastConnectedTime = lastConnectedTime;
                final String _lastRunningEvents = lastRunningEvents;
                final String _lastPausedEvents = lastPausedEvents;
                final String _cellName = cellNameTextView.getText().toString();

                final Context appContext = getApplicationContext();
                PPApplication.startHandlerThread(/*"NotUsedMobileCellsDetectedActivity.onClick"*/);
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NotUsedMobileCellsDetectedActivity_onClick");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=NotUsedMobileCellsDetectedActivity.onClick");

                            DatabaseHandler db = DatabaseHandler.getInstance(appContext);

                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_mobileCellId, _cellName,
                                    true, false, _lastConnectedTime, _lastRunningEvents, _lastPausedEvents, false));
                            db.saveMobileCellsList(localCellsList, true, true);

                            String[] eventIds = _lastRunningEvents.split("\\|");
                            for (String eventId : eventIds) {
                                if (!eventId.isEmpty()) {
                                    Event event = db.getEvent(Long.parseLong(eventId));
                                    if (event != null) {
                                        //PPApplication.logE("NotUsedMobileCellsDetectedActivity.onClick", "save cellId to event="+event._name);
                                        String cells = event._eventPreferencesMobileCells._cells;
                                        cells = PhoneStateScanner.addCellId(cells, mobileCellId);
                                        event._eventPreferencesMobileCells._cells = cells;
                                        db.updateMobileCellsCells(event);

                                        // broadcast for event preferences
                                        Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                        intent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                        intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _mobileCellId);
                                        intent.setPackage(appContext.getPackageName());
                                        appContext.sendBroadcast(intent);

                                        Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                        refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                        LocalBroadcastManager.getInstance(appContext).sendBroadcast(refreshIntent);
                                    }
                                }
                            }
                            eventIds = _lastPausedEvents.split("\\|");
                            for (String eventId : eventIds) {
                                if (!eventId.isEmpty()) {
                                    Event event = db.getEvent(Long.parseLong(eventId));
                                    if (event != null) {
                                        //PPApplication.logE("NotUsedMobileCellsDetectedActivity.onClick", "save cellId to event="+event._name);
                                        String cells = event._eventPreferencesMobileCells._cells;
                                        cells = PhoneStateScanner.addCellId(cells, mobileCellId);
                                        event._eventPreferencesMobileCells._cells = cells;
                                        db.updateMobileCellsCells(event);

                                        // broadcast for event preferences
                                        Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                        intent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                        intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _mobileCellId);
                                        intent.setPackage(appContext.getPackageName());
                                        appContext.sendBroadcast(intent);

                                        Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                        refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                        LocalBroadcastManager.getInstance(appContext).sendBroadcast(refreshIntent);
                                    }
                                }
                            }

                            //PPApplication.logE("[TEST BATTERY] NotUsedMobileCellsDetectedActivity.onStart()", "xxx");
                            PhoneStateScanner.handleEvents(appContext);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=NotUsedMobileCellsDetectedActivity.onClick");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });

                NotUsedMobileCellsDetectedActivity.this.finish();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final int _mobileCellId = mobileCellId;
                final long _lastConnectedTime = lastConnectedTime;
                final String _lastRunningEvents = lastRunningEvents;
                final String _lastPausedEvents = lastPausedEvents;
                final String _cellName = cellNameTextView.getText().toString();

                final Context appContext = getApplicationContext();
                PPApplication.startHandlerThread(/*"NotUsedMobileCellsDetectedActivity.onClick"*/);
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NotUsedMobileCellsDetectedActivity_onClick");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=NotUsedMobileCellsDetectedActivity.onClick");

                            DatabaseHandler db = DatabaseHandler.getInstance(appContext);

                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_mobileCellId, _cellName,
                                    true, false, _lastConnectedTime, _lastRunningEvents, _lastPausedEvents,
                                    true)); // do not detect again
                            db.saveMobileCellsList(localCellsList, true, true);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=NotUsedMobileCellsDetectedActivity.onClick");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });

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
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

                NotUsedMobileCellsDetectedActivity.this.onShow();
            }
        });

        cellIdTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_cell_id);
        lastConnectTimeTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_connection_time);
        cellNameTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_cells_name);
        lastRunningEventsListView = layout.findViewById(R.id.not_used_mobile_cells_dlg_last_running_events_listview);

        mMobileCellNamesDialog = new MobileCellNamesDialogX(this, null, false);
        cellNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!NotUsedMobileCellsDetectedActivity.this.isFinishing())
                        mMobileCellNamesDialog.show();
                }
            }
        );

        cellNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                String cellName = cellNameTextView.getText().toString();

                boolean anyChecked = false;
                for (Event event : eventList) {
                    if (event.getStatus() == 1) {
                        anyChecked = true;
                        break;
                    }
                }
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!cellName.isEmpty() && anyChecked);
            }
        });


        if (!isFinishing())
            mDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
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
            final List<Event> _eventList = new ArrayList<>();

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                db = DatabaseHandler.getInstance(NotUsedMobileCellsDetectedActivity.this);
                _cellsList = new ArrayList<>();
                cellName = "";
            }

            @Override
            protected Void doInBackground(Void... params) {
                db.addMobileCellsToList(_cellsList, mobileCellId);
                if (!_cellsList.isEmpty())
                    cellName = _cellsList.get(0).name;

                //eventList.clear();

                String[] eventIds = lastRunningEvents.split("\\|");
                for (String eventId : eventIds) {
                    if (!eventId.isEmpty()) {
                        Event event = db.getEvent(Long.parseLong(eventId));
                        if (event != null) {
                            event.setStatus(1); // use status of event for checkbox status
                            _eventList.add(event);
                        }
                    }
                }

                eventIds = lastPausedEvents.split("\\|");
                for (String eventId : eventIds) {
                    if (!eventId.isEmpty()) {
                        Event event = db.getEvent(Long.parseLong(eventId));
                        if (event != null) {
                            event.setStatus(1); // use status of event for checkbox status
                            _eventList.add(event);
                        }
                    }
                }

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

                eventList.clear();
                eventList.addAll(_eventList);

                NotUsedMobileCellsDetectedAdapter notUsedMobileCellsDetectedAdapter =
                        new NotUsedMobileCellsDetectedAdapter(NotUsedMobileCellsDetectedActivity.this, eventList);
                lastRunningEventsListView.setAdapter(notUsedMobileCellsDetectedAdapter);

                boolean anyChecked = false;
                for (Event event : eventList) {
                    if (event.getStatus() == 1) {
                        anyChecked = true;
                        break;
                    }
                }
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!cellName.isEmpty() && anyChecked);
            }

        }.execute();
    }

}
