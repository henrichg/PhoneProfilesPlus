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
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class MobileCellsPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private MobileCellsPreferenceX preference;

    private AlertDialog mRenameDialog;
    private AlertDialog mSelectorDialog;
    private AlertDialog mSortDialog;

    private TextView cellFilter;
    private TextView cellName;
    private TextView connectedCell;
    private MobileCellsPreferenceAdapterX listAdapter;
    private MobileCellNamesDialogX mMobileCellsFilterDialog;
    private MobileCellNamesDialogX mMobileCellNamesDialog;
    private AppCompatImageButton addCellButton;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private Button rescanButton;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    private RefreshListViewBroadcastReceiver refreshListViewBroadcastReceiver;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (MobileCellsPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_mobile_cells_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        //Log.e("MobileCellsPreferenceFragmentX.onBindDialogView", "xxx");

        refreshListViewBroadcastReceiver = new MobileCellsPreferenceFragmentX.RefreshListViewBroadcastReceiver(/*preference*/);
        LocalBroadcastManager.getInstance(prefContext).registerReceiver(refreshListViewBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView"));

        PPApplication.forceStartPhoneStateScanner(prefContext);
        MobileCellsPreferenceX.forceStart = true;

        cellFilter = view.findViewById(R.id.mobile_cells_pref_dlg_cells_filter_name);
        if ((preference.cellFilter == null) || preference.cellFilter.isEmpty()) {
            if (preference.value.isEmpty())
                cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_all);
            else
                cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_selected);
        }
        else
            cellFilter.setText(preference.cellFilter);
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

        cellName = view.findViewById(R.id.mobile_cells_pref_dlg_cells_name);
        connectedCell = view.findViewById(R.id.mobile_cells_pref_dlg_connectedCell);

        ListView cellsListView = view.findViewById(R.id.mobile_cells_pref_dlg_listview);
        listAdapter = new MobileCellsPreferenceAdapterX(prefContext, preference);
        cellsListView.setAdapter(listAdapter);

        //refreshListView(false);

        /*
        cellsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cellName.setText(cellsList.get(position).name);
            }

        });
        */

        mMobileCellsFilterDialog = new MobileCellNamesDialogX((Activity)prefContext, preference, true);
        cellFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity)prefContext).isFinishing())
                    mMobileCellsFilterDialog.show();
            }
        });

        mMobileCellNamesDialog = new MobileCellNamesDialogX((Activity)prefContext, preference, false);
        cellName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity)prefContext).isFinishing())
                    mMobileCellNamesDialog.show();
            }
        });

        final AppCompatImageButton editIcon = view.findViewById(R.id.mobile_cells_pref_dlg_rename);
        TooltipCompat.setTooltipText(editIcon, getString(R.string.mobile_cells_pref_dlg_rename_cell_button_tooltip));
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((Activity)prefContext).isFinishing()) {
                    mRenameDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.mobile_cells_pref_dlg_cell_rename_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.mobileCellsRenameArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.mobileCellsRenameArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final DatabaseHandler db = DatabaseHandler.getInstance(prefContext);
                                    switch (which) {
                                        case 0:
                                        case 1:
                                            db.renameMobileCellsList(preference.filteredCellsList, cellName.getText().toString(), which == 0, preference.value);
                                            break;
                                        case 2:
                                            db.renameMobileCellsList(preference.filteredCellsList, cellName.getText().toString(), false, null);
                                            break;
                                    }
                                    refreshListView(false, Integer.MAX_VALUE);
                                    //dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
        AppCompatImageButton changeSelectionIcon = view.findViewById(R.id.mobile_cells_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.mobile_cells_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity)prefContext).isFinishing()) {
                    mSelectorDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.pref_dlg_change_selection_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.mobileCellsChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.mobileCellsChangeSelectionArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            preference.value = "";
                                            break;
                                        case 1:
                                            for (MobileCellsData cell : preference.filteredCellsList) {
                                                if (cell.name.equals(cellName.getText().toString()))
                                                    preference.addCellId(cell.cellId);
                                            }
                                            break;
                                        case 2:
                                            preference.value = "";
                                            for (MobileCellsData cell : preference.filteredCellsList) {
                                                preference.addCellId(cell.cellId);
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
            }
        });
        final AppCompatImageButton sortIcon = view.findViewById(R.id.mobile_cells_pref_dlg_sort);
        TooltipCompat.setTooltipText(sortIcon, getString(R.string.mobile_cells_pref_dlg_button_tooltip));
        sortIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((Activity)prefContext).isFinishing()) {
                    mSortDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.mobile_cells_pref_dlg_cell_sort_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setSingleChoiceItems(R.array.mobileCellsSortArray, preference.sortCellsBy, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    preference.sortCellsBy = which;
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.sortIcon.onClickListener", "sortCellsBy="+preference.sortCellsBy);
                                    refreshListView(false, Integer.MAX_VALUE);
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });

        final AppCompatImageButton helpIcon = view.findViewById(R.id.mobile_cells_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelpPopupWindowX.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, getDialog(), R.string.mobile_cells_pref_dlg_help);
            }
        });

        rescanButton = view.findViewById(R.id.mobile_cells_pref_dlg_rescanButton);
        //rescanButton.setAllCaps(false);
        if (PPApplication.hasSystemFeature(prefContext, PackageManager.FEATURE_TELEPHONY)) {
            TelephonyManager telephonyManager = (TelephonyManager) prefContext.getSystemService(Context.TELEPHONY_SERVICE);
            if ((telephonyManager != null) && (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)) {
                rescanButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Permissions.grantMobileCellsDialogPermissions(prefContext))
                            refreshListView(true, Integer.MAX_VALUE);
                    }
                });
            }
            else
                rescanButton.setEnabled(false);
        }
        else
            rescanButton.setEnabled(false);

        addCellButton = view.findViewById(R.id.mobile_cells_pref_dlg_addCellButton);
        TooltipCompat.setTooltipText(addCellButton, getString(R.string.mobile_cells_pref_dlg_add_button_tooltip));
        addCellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preference.registeredCellData != null) {
                    preference.addCellId(preference.registeredCellData.cellId);
                    refreshListView(false, preference.registeredCellData.cellId);
                }
            }
        });

        locationEnabledStatusTextView = view.findViewById(R.id.mobile_cells_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.mobile_cells_pref_dlg_locationSystemSettingsButton);
        TooltipCompat.setTooltipText(locationSystemSettingsButton, getString(R.string.location_settings_button_tooltip));

        setLocationEnableStatus();

        refreshListView(false, Integer.MAX_VALUE);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        //Log.e("MobileCellsPreferenceFragmentX.onDialogClosed", "xxx");

        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((mRenameDialog != null) && mRenameDialog.isShowing())
            mRenameDialog.dismiss();
        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
        if ((mSortDialog != null) && mSortDialog.isShowing())
            mSortDialog.dismiss();

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        if (refreshListViewBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(prefContext).unregisterReceiver(refreshListViewBroadcastReceiver);
            refreshListViewBroadcastReceiver = null;
        }

        MobileCellsPreferenceX.forceStart = false;
        PPApplication.restartPhoneStateScanner(prefContext, false);

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        if (Build.VERSION.SDK_INT >= 28) {
            String statusText;
            if (!PhoneProfilesService.isLocationEnabled(prefContext)) {
                /*if (Build.VERSION.SDK_INT < 28)
                    statusText = prefContext.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                            prefContext.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                else*/
                    statusText = prefContext.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                            "* " + prefContext.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";

                locationEnabledStatusTextView.setText(statusText);

                locationSystemSettingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() != null) {
                            boolean ok = false;
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    getActivity().startActivityForResult(intent, EventsPrefsFragment.RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
                                    ok = true;
                                } catch (Exception ignored) {}
                            }
                            if (!ok) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
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
                                if (!((Activity)prefContext).isFinishing())
                                    dialog.show();
                            }
                        }
                    }
                });

                locationEnabledStatusTextView.setVisibility(View.VISIBLE);
                locationSystemSettingsButton.setVisibility(View.VISIBLE);
                rescanButton.setVisibility(View.GONE);
            } else {
                locationEnabledStatusTextView.setVisibility(View.GONE);
                locationSystemSettingsButton.setVisibility(View.GONE);
                rescanButton.setVisibility(View.VISIBLE);
            }
        }
        else {
            locationEnabledStatusTextView.setVisibility(View.GONE);
            locationSystemSettingsButton.setVisibility(View.GONE);
            rescanButton.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(final boolean forRescan, final int renameCellId)
    {
        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            String _cellName;
            List<MobileCellsData> _cellsList = null;
            List<MobileCellsData> _filteredCellsList = null;
            String _cellFilterValue;
            String _value;
            int _sortCellsBy;

            MobileCellsData _registeredCellData;
            boolean _registeredCellInTable;
            boolean _registeredCellInValue;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                _cellName = cellName.getText().toString();
                _cellsList = new ArrayList<>();
                _filteredCellsList = new ArrayList<>();
                _cellFilterValue = cellFilter.getText().toString();
                _value = preference.value;
                _sortCellsBy = preference.sortCellsBy;

                if (preference.registeredCellData != null) {
                    _registeredCellData = new MobileCellsData(preference.registeredCellData.cellId,
                                                                preference.registeredCellData.name,
                                                                preference.registeredCellData.connected,
                                                                preference.registeredCellData._new,
                                                                preference.registeredCellData.lastConnectedTime,
                                                                preference.registeredCellData.lastRunningEvents,
                                                                preference.registeredCellData.lastPausedEvents,
                                                                preference.registeredCellData.doNotDetect);
                }
                _registeredCellInTable = preference.registeredCellInTable;
                _registeredCellInValue = preference.registeredCellInValue;

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

                    /*if (PhoneStateScanner.isValidCellId(PhoneStateScanner.registeredCell))
                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", " **** registeredCell="+PhoneStateScanner.registeredCell);
                    else
                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "**** registeredCell=NOT valid");*/

                    // add all from table
                    DatabaseHandler db = DatabaseHandler.getInstance(prefContext.getApplicationContext());
                    db.addMobileCellsToList(_cellsList, 0);

                    _registeredCellData = null;
                    _registeredCellInTable = false;
                    _registeredCellInValue = false;

                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isPhoneStateScannerStarted()) {
                        // add registered cell
                        //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "search registered cell from scanner");
                        for (MobileCellsData cell : _cellsList) {
                            if (cell.cellId == PhoneStateScanner.registeredCell) {
                                cell.connected = true;
                                _registeredCellData = cell;
                                _registeredCellInTable = true;
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - found");
                                break;
                            }
                        }
                        if (!_registeredCellInTable && PhoneStateScanner.isValidCellId(PhoneStateScanner.registeredCell)) {
                            //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - not found - add it to list");
                            _registeredCellData = new MobileCellsData(PhoneStateScanner.registeredCell,
                                    _cellName, true, true,
                                    PhoneStateScanner.lastConnectedTime,
                                    PhoneStateScanner.lastRunningEventsNotOutside,
                                    PhoneStateScanner.lastPausedEventsOutside,
                                    false);
                            _cellsList.add(_registeredCellData);
                        }
                        /*if (!_registeredCellInTable) {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - NOT added into list");
                        }
                        else {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - registeredCellData.cellId="+_registeredCellData.cellId);
                        }*/
                    }

                    // add all from value
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "search cells from preference value");
                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "_value=" + _value);
                    }*/
                    String[] splits = _value.split("\\|");
                    for (String cell : splits) {
                        if (cell.isEmpty())
                            continue;

                        boolean found = false;
                        for (MobileCellsData mCell : _cellsList) {
                            if (cell.equals(Integer.toString(mCell.cellId))) {
                                found = true;
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - found");
                                break;
                            }
                        }
                        if (!found) {
                            try {
                                int iCell = Integer.parseInt(cell);
                                _cellsList.add(new MobileCellsData(iCell, _cellName, false, false, 0, "", "", false));
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - not found - add it to list");
                            } catch (Exception ignored) {
                            }
                        }
                        if (_registeredCellData != null) {
                            //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registeredCellData.cellId="+registeredCellData.cellId);
                            //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - cell="+cell);
                            if (Integer.valueOf(cell) == _registeredCellData.cellId)
                                _registeredCellInValue = true;
                        }
                    }
                    /*if (PPApplication.logEnabled()) {
                        if (!_registeredCellInValue) {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registered cell is NOT in value");
                        } else {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registered cell is in value");
                        }
                    }*/

                    // save all from value + registeredCell to table
                    db.saveMobileCellsList(_cellsList, true, false);

                    // rename cell added by "plus" icon
                    if (PhoneStateScanner.isValidCellId(renameCellId)) {
                        String val = String.valueOf(renameCellId);
                        db.renameMobileCellsList(_cellsList, _cellName, false, val);
                    }

                    if (_sortCellsBy == 0)
                        Collections.sort(_cellsList, new MobileCellsPreferenceFragmentX.SortByNameList());
                    else
                        Collections.sort(_cellsList, new MobileCellsPreferenceFragmentX.SortByConnectionList());


                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells into filtered list");
                    _filteredCellsList.clear();
                    splits = _value.split("\\|");
                    for (MobileCellsData cellData : _cellsList) {
                        if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_selected))) {
                            for (String cell : splits) {
                                if (cell.equals(Integer.toString(cellData.cellId))) {
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells into filtered list - added selected cellId="+cellData.cellId);
                                    _filteredCellsList.add(cellData);
                                    break;
                                }
                            }
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_without_name))) {
                            if (cellData.name.isEmpty())
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_new))) {
                            if (cellData._new)
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_all))) {
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

                preference.cellsList = new ArrayList<>(_cellsList);
                preference.filteredCellsList = new ArrayList<>(_filteredCellsList);
                listAdapter.notifyDataSetChanged();

                if (cellName.getText().toString().isEmpty()) {
                    boolean found = false;
                    for (MobileCellsData cell : preference.filteredCellsList) {
                        if (preference.isCellSelected(cell.cellId) && (!cell.name.isEmpty())) {
                            // cell name = first selected filtered cell name. (???)
                            cellName.setText(cell.name);
                            found = true;
                        }
                    }
                    if (!found) {
                        // cell name = event name
                        SharedPreferences sharedPreferences = preference.getSharedPreferences();
                        cellName.setText(sharedPreferences.getString(Event.PREF_EVENT_NAME, ""));
                    }
                }

                if (_registeredCellData != null) {
                    preference.registeredCellData = new MobileCellsData(_registeredCellData.cellId,
                                                                        _registeredCellData.name,
                                                                        _registeredCellData.connected,
                                                                        _registeredCellData._new,
                                                                        _registeredCellData.lastConnectedTime,
                                                                        _registeredCellData.lastRunningEvents,
                                                                        _registeredCellData.lastPausedEvents,
                                                                        _registeredCellData.doNotDetect);
                }
                preference.registeredCellInTable = _registeredCellInTable;
                preference.registeredCellInValue = _registeredCellInValue;

                String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell) + " ";
                if (preference.registeredCellData != null) {
                    if (!preference.registeredCellData.name.isEmpty())
                        connectedCellName = connectedCellName + preference.registeredCellData.name + ", ";
                    String cellFlags = "";
                    if (preference.registeredCellData._new)
                        cellFlags = cellFlags + "N";
                    //if (registeredCellData.connected)
                    //    cellFlags = cellFlags + "C";
                    if (!cellFlags.isEmpty())
                        connectedCellName = connectedCellName + "(" + cellFlags + ") ";
                    connectedCellName = connectedCellName + preference.registeredCellData.cellId;
                }
                connectedCell.setText(connectedCellName);
                GlobalGUIRoutines.setImageButtonEnabled((preference.registeredCellData != null) && !(preference.registeredCellInTable && preference.registeredCellInValue),
                        addCellButton, R.drawable.ic_button_add, prefContext);
            }

        };

        rescanAsyncTask.execute();
    }

    private class SortByNameList implements Comparator<MobileCellsData> {

        public int compare(MobileCellsData lhs, MobileCellsData rhs) {
            if (GlobalGUIRoutines.collator != null) {
                String _lhs = "";
                if (lhs._new)
                    _lhs = _lhs + "0000";
                else
                    _lhs = _lhs + "0001";
                if (lhs.name.isEmpty())
                    _lhs = _lhs + "0002";
                else
                    _lhs = _lhs + "0003" + lhs.name;
                _lhs = _lhs + "-" + lhs.cellId;

                String _rhs = "";
                if (rhs._new)
                    _rhs = _rhs + "0000";
                else
                    _rhs = _rhs + "0001";
                if (rhs.name.isEmpty())
                    _rhs = _rhs + "0002";
                else
                    _rhs = _rhs + "0003" + rhs.name;
                _rhs = _rhs + "-" + rhs.cellId;
                return GlobalGUIRoutines.collator.compare(_lhs, _rhs);
            }
            else
                return 0;
        }

    }

    private class SortByConnectionList implements Comparator<MobileCellsData> {

        public int compare(MobileCellsData lhs, MobileCellsData rhs) {
            if (GlobalGUIRoutines.collator != null) {
                String _lhs;// = "";
                /*if (lhs._new)
                    _lhs = _lhs + "0000";
                else
                    _lhs = _lhs + "0001";
                if (lhs.name.isEmpty())
                    _lhs = _lhs + "0002";
                else
                    _lhs = _lhs + "0003" + lhs.name;
                _lhs = _lhs + "-" + lhs.cellId;*/
                _lhs = String.valueOf(lhs.lastConnectedTime);

                String _rhs;// = "";
                /*if (rhs._new)
                    _rhs = _rhs + "0000";
                else
                    _rhs = _rhs + "0001";
                if (rhs.name.isEmpty())
                    _rhs = _rhs + "0002";
                else
                    _rhs = _rhs + "0003" + rhs.name;
                _rhs = _rhs + "-" + rhs.cellId;*/
                _rhs = String.valueOf(rhs.lastConnectedTime);
                return GlobalGUIRoutines.collator.compare(_rhs, _lhs);
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
                //noinspection SwitchStatementWithTooFewBranches
                switch (item.getItemId()) {
                    case R.id.mobile_cells_pref_item_menu_delete:
                        DatabaseHandler db = DatabaseHandler.getInstance(_context);
                        db.deleteMobileCell(cellId);
                        preference.removeCellId(cellId);
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

        //final MobileCellsPreferenceX preference;

        /*RefreshListViewBroadcastReceiver(MobileCellsPreferenceX preference) {
            this.preference = preference;
        }*/

        @Override
        public void onReceive(final Context context, Intent intent) {
            //PPApplication.logE("MobileCellsPreferenceFragmentX.RefreshListViewBroadcastReceiver", "xxx");
            //if (preference != null)
            //    preference.refreshListView(false, Integer.MAX_VALUE);
            refreshListView(false, Integer.MAX_VALUE);
        }
    }

    void setCellNameText(String text) {
        cellName.setText(text);
    }

    String getCellNameText() {
        return  cellName.getText().toString();
    }

    void setCellFilterText(String text) {
        cellFilter.setText(text);
    }

}
