package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MobileCellsPreference extends DialogPreference {

    private String value;
    private String persistedValue;
    private List<MobileCellsData> cellsList = null;
    List<MobileCellsData> filteredCellsList = null;

    private final Context context;

    private MaterialDialog mDialog;
    private MaterialDialog mRenameDialog;
    private MaterialDialog mSelectorDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    TextView cellFilter;
    TextView cellName;
    private MobileCellsPreferenceAdapter listAdapter;
    private MobileCellNamesDialog mMobileCellsFilterDialog;
    private MobileCellNamesDialog mMobileCellNamesDialog;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    private PhoneStateChangedBroadcastReceiver phoneStateChangedBroadcastReceiver;

    static boolean forceStart;

    private static final String PREF_SHOW_HELP = "mobile_cells_pref_show_help";

    public MobileCellsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        cellsList = new ArrayList<>();
        filteredCellsList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {

        persistedValue = getPersistedString("");
        value = persistedValue;

        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(PhoneStateScanner.ACTION_PHONE_STATE_CHANGED);
        phoneStateChangedBroadcastReceiver = new PhoneStateChangedBroadcastReceiver(this);
        //context.registerReceiver(EventPreferencesNestedFragment.phoneStateChangedBroadcastReceiver, intentFilter);
        LocalBroadcastManager.getInstance(context).registerReceiver(phoneStateChangedBroadcastReceiver, new IntentFilter("PhoneStateChangedBroadcastReceiver_preference"));

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

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.mobile_cells_pref_dlg_rescan_button)
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_mobile_cells_pref_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (Permissions.grantMobileCellsDialogPermissions(context, MobileCellsPreference.this))
                            refreshListView(true);
                    }
                });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        //progressLinearLayout = layout.findViewById(R.id.mobile_cells_pref_dlg_linla_progress);
        //dataRelativeLayout = layout.findViewById(R.id.mobile_cells_pref_dlg_rella_data);

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
                refreshListView(false);
            }
        });

        cellName = layout.findViewById(R.id.mobile_cells_pref_dlg_cells_name);

        ListView cellsListView = layout.findViewById(R.id.mobile_cells_pref_dlg_listview);
        listAdapter = new MobileCellsPreferenceAdapter(context, this);
        cellsListView.setAdapter(listAdapter);

        refreshListView(false);

        /*
        cellsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cellName.setText(cellsList.get(position).name);
            }

        });
        */

        RelativeLayout cellsFilterValueRoot = layout.findViewById(R.id.mobile_cells_pref_dlg_cells_filter_root);
        mMobileCellsFilterDialog = new MobileCellNamesDialog(context, this, true);
        cellsFilterValueRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMobileCellsFilterDialog.show();
            }
        });

        RelativeLayout cellNamesValueRoot = layout.findViewById(R.id.mobile_cells_pref_dlg_cells_name_root);
        mMobileCellNamesDialog = new MobileCellNamesDialog(context, this, false);
        cellNamesValueRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMobileCellNamesDialog.show();
            }
        });

        final ImageView editIcon = layout.findViewById(R.id.mobile_cells_pref_dlg_rename);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRenameDialog = new MaterialDialog.Builder(context)
                        .title(R.string.mobile_cells_pref_dlg_cell_rename_title)
                        .items(R.array.mobileCellsRenameArray)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                final DatabaseHandler db = DatabaseHandler.getInstance(context);
                                switch (which) {
                                    case 0:
                                    case 1:
                                        db.renameMobileCellsList(filteredCellsList, cellName.getText().toString(), which == 0, value);
                                        break;
                                    case 2:
                                        db.renameMobileCellsList(filteredCellsList, cellName.getText().toString(), false, value);
                                        break;
                                }
                                refreshListView(false);
                                return true;
                            }
                        })
                        .positiveText(R.string.mobile_cells_pref_dlg_cell_rename_button)
                        .negativeText(getNegativeButtonText())
                        .show();
            }
        });
        ImageView changeSelectionIcon = layout.findViewById(R.id.mobile_cells_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new MaterialDialog.Builder(context)
                        .title(R.string.pref_dlg_change_selection_title)
                        .items(R.array.mobileCellsChangeSelectionArray)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
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
                                refreshListView(false);
                                return true;
                            }
                        })
                        .positiveText(R.string.pref_dlg_change_selection_button)
                        .negativeText(getNegativeButtonText())
                        .show();
            }
        });

        final TextView helpText = layout.findViewById(R.id.mobile_cells_pref_dlg_helpText);

        final ImageView helpIcon = layout.findViewById(R.id.mobile_cells_pref_dlg_helpIcon);
        ApplicationPreferences.getSharedPreferences(context);
        if (ApplicationPreferences.preferences.getBoolean(PREF_SHOW_HELP, true)) {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
            helpText.setVisibility(View.VISIBLE);
        }
        else {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help);
            helpText.setVisibility(View.GONE);
        }
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE) {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help);
                    visibility = View.GONE;
                    editor.putBoolean(PREF_SHOW_HELP, false);
                }
                else {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
                    visibility = View.VISIBLE;
                    editor.putBoolean(PREF_SHOW_HELP, true);
                }
                helpText.setVisibility(visibility);
                editor.apply();
            }
        });

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);

        if (phoneStateChangedBroadcastReceiver != null) {
            //getActivity().unregisterReceiver(phoneStateChangedBroadcastReceiver);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(phoneStateChangedBroadcastReceiver);
            phoneStateChangedBroadcastReceiver = null;
        }

        forceStart = false;
        PPApplication.restartPhoneStateScanner(context, false);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mRenameDialog != null && mRenameDialog.isShowing())
            mRenameDialog.dismiss();
        if (mSelectorDialog != null && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
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
    public void refreshListView(final boolean forRescan)
    {
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
                        if (PhoneProfilesService.isPhoneStateScannerStarted()) {
                            PhoneProfilesService.phoneStateScanner.getRegisteredCell();

                            //try { Thread.sleep(200); } catch (InterruptedException e) { }
                            //SystemClock.sleep(200);
                            //PPApplication.sleep(200);
                        }
                    }

                    // add all from table
                    DatabaseHandler db = DatabaseHandler.getInstance(context);
                    db.addMobileCellsToList(_cellsList);

                    boolean found = false;
                    if (PhoneProfilesService.isPhoneStateScannerStarted()) {
                        // add registered cell
                        for (MobileCellsData cell : _cellsList) {
                            if (cell.cellId == PhoneProfilesService.phoneStateScanner.registeredCell) {
                                cell.connected = true;
                                found = true;
                                break;
                            }
                        }
                        if (!found && (PhoneProfilesService.phoneStateScanner.registeredCell != Integer.MAX_VALUE))
                            _cellsList.add(new MobileCellsData(PhoneProfilesService.phoneStateScanner.registeredCell,
                                    _cellName, true, true, PhoneProfilesService.phoneStateScanner.lastConnectedTime));
                    }

                    // add all from value
                    String[] splits = value.split("\\|");
                    for (String cell : splits) {
                        found = false;
                        for (MobileCellsData mCell : _cellsList) {
                            if (cell.equals(Integer.toString(mCell.cellId))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            try {
                                int iCell = Integer.parseInt(cell);
                                _cellsList.add(new MobileCellsData(iCell, _cellName, false, false, 0));
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    db.saveMobileCellsList(_cellsList, true, false);

                    Collections.sort(_cellsList, new SortList());

                    _filteredCellsList.clear();
                    splits = _value.split("\\|");
                    for (MobileCellsData cellData : _cellsList) {
                        if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_selected))) {
                            for (String cell : splits) {
                                if (cell.equals(Integer.toString(cellData.cellId))) {
                                    _filteredCellsList.add(cellData);
                                    break;
                                }
                            }
                        } else if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_without_name))) {
                            if (cellData.name.isEmpty())
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(context.getString(R.string.mobile_cell_names_dialog_item_show_without_name))) {
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
                            cellName.setText(cell.name);
                            found = true;
                        }
                    }
                    if (!found) {
                        SharedPreferences sharedPreferences = getSharedPreferences();
                        cellName.setText(sharedPreferences.getString(Event.PREF_EVENT_NAME, ""));
                    }
                }

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
        if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
        else
            popup = new PopupMenu(context, view);
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
                        refreshListView(false);
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    public class PhoneStateChangedBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsPreference preference;

        PhoneStateChangedBroadcastReceiver(MobileCellsPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            PPApplication.logE("MobileCellsPreference.PhoneStateChangedBroadcastReceiver", "xxx");
            if ((preference.mDialog != null) && preference.mDialog.isShowing()) {
                // save new registered cell
                synchronized (PPApplication.phoneStateScannerMutex) {
                    List<MobileCellsData> localCellsList = new ArrayList<>();
                    if (PhoneProfilesService.isPhoneStateScannerStarted()) {
                        if (PhoneProfilesService.phoneStateScanner.registeredCell != Integer.MAX_VALUE)
                            localCellsList.add(new MobileCellsData(PhoneProfilesService.phoneStateScanner.registeredCell,
                                    preference.cellName.getText().toString(), true, false,
                                    PhoneProfilesService.phoneStateScanner.lastConnectedTime));
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.saveMobileCellsList(localCellsList, true, false);
                        preference.refreshListView(false);
                    }
                }
            }
        }
    }

}