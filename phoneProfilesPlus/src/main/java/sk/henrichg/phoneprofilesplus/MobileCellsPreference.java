package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.Settings;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MobileCellsPreference extends DialogPreference {

    private String value;
    //private String persistedValue;
    private List<MobileCellsData> cellsList;
    List<MobileCellsData> filteredCellsList;
    //long event_id;

    private MobileCellsData registeredCellData;
    private boolean registeredCellInTable;
    private boolean registeredCellInValue;

    private final Context context;

    private AlertDialog mDialog;
    private AlertDialog mRenameDialog;
    private AlertDialog mSelectorDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    TextView cellFilter;
    TextView cellName;
    private TextView connectedCell;
    private MobileCellsPreferenceAdapter listAdapter;
    private MobileCellNamesDialog mMobileCellsFilterDialog;
    private MobileCellNamesDialog mMobileCellNamesDialog;
    private AppCompatImageButton addCellButton;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    //private PhoneStateChangedBroadcastReceiver phoneStateChangedBroadcastReceiver;
    private RefreshListViewBroadcastReceiver refreshListViewBroadcastReceiver;

    //private boolean dialogCanceled = true;
    static boolean forceStart;

    //private static final String PREF_SHOW_HELP = "mobile_cells_pref_show_help";

    public MobileCellsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        cellsList = new ArrayList<>();
        filteredCellsList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {

        //persistedValue = getPersistedString("");
        //value = persistedValue;
        value = getPersistedString("");

        /*
        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(PhoneStateScanner.ACTION_PHONE_STATE_CHANGED);
        phoneStateChangedBroadcastReceiver = new PhoneStateChangedBroadcastReceiver(this);
        //context.registerReceiver(EventPreferencesNestedFragment.phoneStateChangedBroadcastReceiver, intentFilter);
        LocalBroadcastManager.getInstance(context).registerReceiver(phoneStateChangedBroadcastReceiver, new IntentFilter("PhoneStateChangedBroadcastReceiver_preference"));
        */

        refreshListViewBroadcastReceiver = new RefreshListViewBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(refreshListViewBroadcastReceiver, new IntentFilter("MobileCellsPreference_refreshListView"));

        PPApplication.forceStartPhoneStateScanner(context);
        forceStart = true;

        /*
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.deleteMobileCell(2826251);
        db.deleteMobileCell(2843189);
        db.deleteMobileCell(2877237);
        db.deleteMobileCell(2649653);
        db.deleteMobileCell(2649613);
        */

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                    //Log.d("MobileCellsPreference.onPositive", "1");
                    if (callChangeListener(value))
                    {
                        //Log.d("MobileCellsPreference.onPositive", "2");
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.saveMobileCellsList(cellsList, false, false);
                        persistString(value);
                    }
                }
                //dialogCanceled = false;
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_mobile_cells_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                refreshListView(false, Integer.MAX_VALUE);
            }
        });

        //progressLinearLayout = layout.findViewById(R.id.mobile_cells_pref_dlg_linla_progress);
        //dataRelativeLayout = layout.findViewById(R.id.mobile_cells_pref_dlg_rella_data);

        //noinspection ConstantConditions
        cellFilter = layout.findViewById(R.id.mobile_cells_pref_dlg_cells_filter_name);
        if (value.isEmpty())
            cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_all);
        else
            cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_selected);
        cellFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshListView(false, Integer.MAX_VALUE);
            }
        });

        cellName = layout.findViewById(R.id.mobile_cells_pref_dlg_cells_name);
        connectedCell = layout.findViewById(R.id.mobile_cells_pref_dlg_connectedCell);

        ListView cellsListView = layout.findViewById(R.id.mobile_cells_pref_dlg_listview);
        listAdapter = new MobileCellsPreferenceAdapter(context, this);
        cellsListView.setAdapter(listAdapter);

        //refreshListView(false);

        /*
        cellsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cellName.setText(cellsList.get(position).name);
            }

        });
        */

        mMobileCellsFilterDialog = new MobileCellNamesDialog((Activity)context, this, true);
        cellFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMobileCellsFilterDialog.show();
            }
        });

        mMobileCellNamesDialog = new MobileCellNamesDialog((Activity)context, this, false);
        cellName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMobileCellNamesDialog.show();
            }
        });

        final AppCompatImageButton editIcon = layout.findViewById(R.id.mobile_cells_pref_dlg_rename);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRenameDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.mobile_cells_pref_dlg_cell_rename_title)
                        .setCancelable(true)
                        .setNegativeButton(getNegativeButtonText(), null)
                        //.setSingleChoiceItems(R.array.mobileCellsRenameArray, 0, new DialogInterface.OnClickListener() {
                        .setItems(R.array.mobileCellsRenameArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final DatabaseHandler db = DatabaseHandler.getInstance(context);
                                switch (which) {
                                    case 0:
                                    case 1:
                                        db.renameMobileCellsList(filteredCellsList, cellName.getText().toString(), which == 0, value);
                                        break;
                                    case 2:
                                        db.renameMobileCellsList(filteredCellsList, cellName.getText().toString(), false, null);
                                        break;
                                }
                                refreshListView(false, Integer.MAX_VALUE);
                                //dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        AppCompatImageButton changeSelectionIcon = layout.findViewById(R.id.mobile_cells_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.pref_dlg_change_selection_title)
                        .setCancelable(true)
                        .setNegativeButton(getNegativeButtonText(), null)
                        //.setSingleChoiceItems(R.array.mobileCellsChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                        .setItems(R.array.mobileCellsChangeSelectionArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        value = "";
                                        break;
                                    case 1:
                                        for (MobileCellsData cell : filteredCellsList) {
                                            if (cell.name.equals(cellName.getText().toString()))
                                                addCellId(cell.cellId);
                                        }
                                        break;
                                    case 2:
                                        value = "";
                                        for (MobileCellsData cell : filteredCellsList) {
                                            addCellId(cell.cellId);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView(false, Integer.MAX_VALUE);
                                //dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        final AppCompatImageButton helpIcon = layout.findViewById(R.id.mobile_cells_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelpPopupWindow.showPopup(helpIcon, (Activity)context, mDialog, R.string.mobile_cells_pref_dlg_help);
            }
        });

        final Button rescanButton = layout.findViewById(R.id.mobile_cells_pref_dlg_rescanButton);
        //rescanButton.setAllCaps(false);
        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if ((telephonyManager != null) && (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)) {
                rescanButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Permissions.grantMobileCellsDialogPermissions(context))
                            refreshListView(true, Integer.MAX_VALUE);
                    }
                });
            }
            else
                rescanButton.setEnabled(false);
        }
        else
            rescanButton.setEnabled(false);

        addCellButton = layout.findViewById(R.id.mobile_cells_pref_dlg_addCellButton);
        addCellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registeredCellData != null) {
                    addCellId(registeredCellData.cellId);
                    refreshListView(false, registeredCellData.cellId);
                }
            }
        });

        final TextView locationEnabledStatusTextView = layout.findViewById(R.id.mobile_cells_pref_dlg_locationEnableStatus);
        String statusText;
        if (!PhoneProfilesService.isLocationEnabled(context)) {
            if (Build.VERSION.SDK_INT < 28)
                statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                        context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
            else
                statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                        "* " +context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";
        } else {
            statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
        }
        locationEnabledStatusTextView.setText(statusText);

        AppCompatImageButton locationSystemSettingsButton = layout.findViewById(R.id.mobile_cells_pref_dlg_locationSystemSettingsButton);
        locationSystemSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    context.startActivity(intent);
                }
                else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                    if (!((Activity)context).isFinishing())
                        dialog.show();
                }
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);

        /*
        if (phoneStateChangedBroadcastReceiver != null) {
            //getActivity().unregisterReceiver(phoneStateChangedBroadcastReceiver);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(phoneStateChangedBroadcastReceiver);
            phoneStateChangedBroadcastReceiver = null;
        }
        */
        if (refreshListViewBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(refreshListViewBroadcastReceiver);
            refreshListViewBroadcastReceiver = null;
        }

        forceStart = false;
        //if (!dialogCanceled)
            PPApplication.restartPhoneStateScanner(context, false);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mRenameDialog != null) && mRenameDialog.isShowing())
            mRenameDialog.dismiss();
        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
        if ((mDialog != null) && mDialog.isShowing()) {
            //dialogCanceled = true;
            mDialog.dismiss();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if(restoreValue)
        {
            value = getPersistedString(value);
        }
        else
        {
            value = (String)defaultValue;
            persistString(value);
        }
        
    }    

    /*
    public String getCells() {
        return value;
    }
    */

    @SuppressWarnings("StringConcatenationInLoop")
    void addCellId(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cell.equals(sCellId)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + sCellId;
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeCellId(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        value = "";
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (!cell.equals(sCellId)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + cell;
                }
            }
        }
    }

    boolean isCellSelected(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        for (String cell : splits) {
            if (cell.equals(sCellId))
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(final boolean forRescan, final int renameCellId)
    {
        if ((mDialog != null) && mDialog.isShowing()) {
            rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

                String _cellName;
                List<MobileCellsData> _cellsList = null;
                List<MobileCellsData> _filteredCellsList = null;
                String _cellFilterValue;
                String _value;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    _cellName = cellName.getText().toString();
                    _cellsList = new ArrayList<>();
                    _filteredCellsList = new ArrayList<>();
                    _cellFilterValue = cellFilter.getText().toString();
                    _value = value;

                    //dataRelativeLayout.setVisibility(View.GONE);
                    //progressLinearLayout.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    synchronized (PPApplication.phoneStateScannerMutex) {

                        if (forRescan) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isPhoneStateScannerStarted()) {
                                PhoneProfilesService.getInstance().getPhoneStateScanner().getRegisteredCell();

                                //try { Thread.sleep(200); } catch (InterruptedException e) { }
                                //SystemClock.sleep(200);
                                //PPApplication.sleep(200);
                            }
                        }

                        if (PhoneStateScanner.isValidCellId(PhoneStateScanner.registeredCell))
                            PPApplication.logE("MobileCellsPreference.refreshListView", " **** registeredCell="+PhoneStateScanner.registeredCell);
                        else
                            PPApplication.logE("MobileCellsPreference.refreshListView", "**** registeredCell=NOT valid");

                        // add all from table
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.addMobileCellsToList(_cellsList/*, false*/);

                        registeredCellData = null;
                        registeredCellInTable = false;
                        registeredCellInValue = false;

                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isPhoneStateScannerStarted()) {
                            // add registered cell
                            PPApplication.logE("MobileCellsPreference.refreshListView", "search registered cell from scanner");
                            for (MobileCellsData cell : _cellsList) {
                                if (cell.cellId == PhoneStateScanner.registeredCell) {
                                    cell.connected = true;
                                    registeredCellData = cell;
                                    registeredCellInTable = true;
                                    PPApplication.logE("MobileCellsPreference.refreshListView", "add registered cell from scanner - found");
                                    break;
                                }
                            }
                            if (!registeredCellInTable && PhoneStateScanner.isValidCellId(PhoneStateScanner.registeredCell)) {
                                PPApplication.logE("MobileCellsPreference.refreshListView", "add registered cell from scanner - not found - add it to list");
                                registeredCellData = new MobileCellsData(PhoneStateScanner.registeredCell,
                                        _cellName, true, true, PhoneStateScanner.lastConnectedTime);
                                _cellsList.add(registeredCellData);
                            }
                            if (!registeredCellInTable) {
                                PPApplication.logE("MobileCellsPreference.refreshListView", "add registered cell from scanner - NOT added into list");
                            }
                            else {
                                PPApplication.logE("MobileCellsPreference.refreshListView", "add registered cell from scanner - registeredCellData.cellId="+registeredCellData.cellId);
                            }
                        }

                        // add all from value
                        PPApplication.logE("MobileCellsPreference.refreshListView", "search cells from preference value");
                        PPApplication.logE("MobileCellsPreference.refreshListView", "_value="+_value);
                        String[] splits = value.split("\\|");
                        for (String cell : splits) {
                            boolean found = false;
                            for (MobileCellsData mCell : _cellsList) {
                                if (cell.equals(Integer.toString(mCell.cellId))) {
                                    found = true;
                                    PPApplication.logE("MobileCellsPreference.refreshListView", "add cells from preference value - found");
                                    break;
                                }
                            }
                            if (!found) {
                                try {
                                    int iCell = Integer.parseInt(cell);
                                    _cellsList.add(new MobileCellsData(iCell, _cellName, false, false, 0));
                                    PPApplication.logE("MobileCellsPreference.refreshListView", "add cells from preference value - not found - add it to list");
                                } catch (Exception ignored) {
                                }
                            }
                            if (registeredCellData != null) {
                                //PPApplication.logE("MobileCellsPreference.refreshListView", "add cells from preference value - registeredCellData.cellId="+registeredCellData.cellId);
                                //PPApplication.logE("MobileCellsPreference.refreshListView", "add cells from preference value - cell="+cell);
                                if (Integer.valueOf(cell) == registeredCellData.cellId)
                                    registeredCellInValue = true;
                            }
                        }
                        if (!registeredCellInValue) {
                            PPApplication.logE("MobileCellsPreference.refreshListView", "add cells from preference value - registered cell is NOT in value");
                        }
                        else {
                            PPApplication.logE("MobileCellsPreference.refreshListView", "add cells from preference value - registered cell is in value");
                        }

                        // save all from value + registeredCell to table
                        db.saveMobileCellsList(_cellsList, true, false);

                        // rename cell added by "plus" icon
                        if (PhoneStateScanner.isValidCellId(renameCellId)) {
                            String val = String.valueOf(renameCellId);
                            db.renameMobileCellsList(_cellsList, _cellName, false, val);
                        }

                        Collections.sort(_cellsList, new SortList());


                        PPApplication.logE("MobileCellsPreference.refreshListView", "add cells into filtered list");
                        _filteredCellsList.clear();
                        splits = _value.split("\\|");
                        for (MobileCellsData cellData : _cellsList) {
                            if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_selected))) {
                                for (String cell : splits) {
                                    if (cell.equals(Integer.toString(cellData.cellId))) {
                                        PPApplication.logE("MobileCellsPreference.refreshListView", "add cells into filtered list - added selected cellId="+cellData.cellId);
                                        _filteredCellsList.add(cellData);
                                        break;
                                    }
                                }
                            } else if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_without_name))) {
                                if (cellData.name.isEmpty())
                                    _filteredCellsList.add(cellData);
                            } else if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_new))) {
                                if (cellData._new)
                                    _filteredCellsList.add(cellData);
                            } else if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_all))) {
                                _filteredCellsList.add(cellData);
                            } else {
                                if (_cellFilterValue.equals(cellData.name))
                                    _filteredCellsList.add(cellData);
                            }
                        }

                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    cellsList = new ArrayList<>(_cellsList);
                    filteredCellsList = new ArrayList<>(_filteredCellsList);
                    listAdapter.notifyDataSetChanged();

                    if (cellName.getText().toString().isEmpty()) {
                        boolean found = false;
                        for (MobileCellsData cell : filteredCellsList) {
                            if (isCellSelected(cell.cellId) && (!cell.name.isEmpty())) {
                                // cell name = first selected filtered cell name. (???)
                                cellName.setText(cell.name);
                                found = true;
                            }
                        }
                        if (!found) {
                            // cell name = event name
                            SharedPreferences sharedPreferences = getSharedPreferences();
                            cellName.setText(sharedPreferences.getString(Event.PREF_EVENT_NAME, ""));
                        }
                    }

                    String connectedCellName = context.getString(R.string.mobile_cells_pref_dlg_connected_cell) + " ";
                    if (registeredCellData != null) {
                        if (!registeredCellData.name.isEmpty())
                            connectedCellName = connectedCellName + registeredCellData.name + ", ";
                        String cellFlags = "";
                        if (registeredCellData._new)
                            cellFlags = cellFlags + "N";
                        //if (registeredCellData.connected)
                        //    cellFlags = cellFlags + "C";
                        if (!cellFlags.isEmpty())
                            connectedCellName = connectedCellName + "(" + cellFlags + ") ";
                        connectedCellName = connectedCellName + registeredCellData.cellId;
                    }
                    connectedCell.setText(connectedCellName);
                    GlobalGUIRoutines.setImageButtonEnabled((registeredCellData != null) && !(registeredCellInTable && registeredCellInValue),
                            addCellButton, R.drawable.ic_button_add, context);

                    //progressLinearLayout.setVisibility(View.GONE);
                    //dataRelativeLayout.setVisibility(View.VISIBLE);

                    /*
                    for (int position = 0; position < cellsList.size() - 1; position++) {
                        if (Integer.toString(cellsList.get(position).cellId).equals(value)) {
                            cellsListView.setSelection(position);
                            break;
                        }
                    }
                    */
                }

            };

            rescanAsyncTask.execute();
        }
    }

    private class SortList implements Comparator<MobileCellsData> {

        public int compare(MobileCellsData lhs, MobileCellsData rhs) {
            if (GlobalGUIRoutines.collator != null) {
                String _lhs = "";
                if (lhs._new)
                    _lhs = _lhs + "\uFFFF";
                if (lhs.name.isEmpty())
                    _lhs = _lhs + "\uFFFF";
                else
                    _lhs = _lhs + lhs.name;
                _lhs = _lhs + "-" + lhs.cellId;

                String _rhs = "";
                if (rhs._new)
                    _rhs = _rhs + "\uFFFF";
                if (rhs.name.isEmpty())
                    _rhs = _rhs + "\uFFFF";
                else
                    _rhs = _rhs + rhs.name;
                _rhs = _rhs + "-" + rhs.cellId;
                return GlobalGUIRoutines.collator.compare(_lhs, _rhs);
            }
            else
                return 0;
        }

    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.mobile_cells_pref_item_edit, popup.getMenu());

        final int cellId = (int)view.getTag();
        final Context _context = context;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mobile_cells_pref_item_menu_delete:
                        DatabaseHandler db = DatabaseHandler.getInstance(_context);
                        db.deleteMobileCell(cellId);
                        removeCellId(cellId);
                        refreshListView(false, Integer.MAX_VALUE);
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    public class RefreshListViewBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsPreference preference;

        RefreshListViewBroadcastReceiver(MobileCellsPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            PPApplication.logE("MobileCellsPreference.RefreshListViewBroadcastReceiver", "xxx");
            if ((preference != null) && (preference.mDialog != null) && preference.mDialog.isShowing())
                preference.refreshListView(false, Integer.MAX_VALUE);
        }
    }

}