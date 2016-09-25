package sk.henrichg.phoneprofilesplus;

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
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
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
    public String persistedValue;
    public List<MobileCellsData> cellsList = null;

    Context context;

    private MaterialDialog mDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    private ListView cellsListView;
    private EditText cellName;
    private MobileCellsPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    public MobileCellsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        cellsList = new ArrayList<MobileCellsData>();

        if (!PhoneProfilesService.instance.isPhoneStateStarted()) {
            //Log.d("MobileCellsPreference","no scanner started");
            GlobalData.startPhoneStateScanner(context);
        }
        //else
        //    Log.d("MobileCellsPreference","scanner started");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PhoneStateScanner.ACTION_PHONE_STATE_CHANGED);
        EventPreferencesNestedFragment.phoneStateChangedBroadcastReceiver = new PhoneStateChangedBroadcastReceiver(this);
        context.registerReceiver(EventPreferencesNestedFragment.phoneStateChangedBroadcastReceiver, intentFilter);

    }

    @Override
    protected void showDialog(Bundle state) {

        persistedValue = getPersistedString("");
        value = persistedValue;

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
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (shouldPersist()) {
                            //Log.d("MobileCellsPreference.onPositive", "1");
                            if (callChangeListener(value))
                            {
                                //Log.d("MobileCellsPreference.onPositive", "2");
                                DatabaseHandler db = DatabaseHandler.getInstance(context);
                                db.saveMobileCellsList(cellsList, false);
                                persistString(value);
                            }
                        }
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (Permissions.grantMobileCellsDialogPermissions(context, MobileCellsPreference.this))
                            refreshListView(true);
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_mobile_cells_pref_dialog, null);
        onBindDialogView(layout);

        //progressLinearLayout = (LinearLayout) layout.findViewById(R.id.mobile_cells_pref_dlg_linla_progress);
        //dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.mobile_cells_pref_dlg_rella_data);

        cellName = (EditText) layout.findViewById(R.id.mobile_cells_pref_dlg_cells_name);
        SharedPreferences sharedPreferences = context.getSharedPreferences(EventPreferencesFragment.getPreferenceName(), Context.MODE_PRIVATE);
        cellName.setText(sharedPreferences.getString(Event.PREF_EVENT_NAME, ""));

        cellsListView = (ListView) layout.findViewById(R.id.mobile_cells_pref_dlg_listview);
        listAdapter = new MobileCellsPreferenceAdapter(context, this);
        cellsListView.setAdapter(listAdapter);

        refreshListView(false);

        cellsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                MobileCellsPreferenceAdapter.ViewHolder viewHolder =
                        (MobileCellsPreferenceAdapter.ViewHolder) v.getTag();

                /*viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());

                if (viewHolder.checkBox.isChecked()) {
                    addCellId(cellsList.get(position).cellId);
                }
                else {
                    removeCellId(cellsList.get(position).cellId);
                }*/

                cellName.setText(cellsList.get(position).name);
            }

        });

        ImageView editIcon = (ImageView)layout.findViewById(R.id.mobile_cells_pref_dlg_rename);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title(R.string.mobile_cells_pref_dlg_cell_rename_title)
                        .items(R.array.materislCellsRenameArray)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                DatabaseHandler db = DatabaseHandler.getInstance(context);
                                db.renameMobileCellsList(cellsList, cellName.getText().toString(), which==0, value);
                                refreshListView(false);
                                return true;
                            }
                        })
                        .positiveText(R.string.mobile_cells_pref_dlg_cell_rename_button)
                        .show();
            }
        });

        mBuilder.customView(layout, false);

        final TextView helpText = (TextView)layout.findViewById(R.id.mobile_cells_pref_dlg_helpText);

        ImageView helpIcon = (ImageView)layout.findViewById(R.id.mobile_cells_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE)
                    visibility = View.GONE;
                else
                    visibility = View.VISIBLE;
                helpText.setVisibility(visibility);
            }
        });

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if ((rescanAsyncTask != null) && (!rescanAsyncTask.isCancelled()))
            rescanAsyncTask.cancel(true);
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

    public String getCells() {
        return value;
    }

    public void addCellId(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        value = "";
        boolean found = false;
        for (String cell : splits) {
            if (!cell.equals(sCellId)) {
                if (!value.isEmpty())
                    value = value + "|";
                value = value + cell;
            }
            else
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + sCellId;
        }
    }

    public void removeCellId(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        value = "";
        for (String cell : splits) {
            if (!cell.equals(sCellId)) {
                if (!value.isEmpty())
                    value = value + "|";
                value = value + cell;
            }
        }
    }

    public boolean isCellSelected(int cellId) {
        String[] splits = value.split("\\|");
        String sCellId = Integer.toString(cellId);
        for (String cell : splits) {
            if (cell.equals(sCellId))
                return true;
        }
        return false;
    }

    public void refreshListView(final boolean forRescan)
    {
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            String _cellName;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                _cellName = cellName.getText().toString();
                //dataRelativeLayout.setVisibility(View.GONE);
                //progressLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                cellsList.clear();

                if (_forRescan) {
                    PhoneProfilesService.instance.phoneStateScanner.getRegisteredCell();

                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    //GlobalData.sleep(200);
                }

                // add all from table
                DatabaseHandler db = DatabaseHandler.getInstance(context);
                db.addMobileCellsToList(cellsList);

                // add registered cell
                boolean found = false;
                for (MobileCellsData cell : cellsList) {
                    if (cell.cellId == PhoneProfilesService.instance.phoneStateScanner.registeredCell) {
                        cell.connected = true;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    cellsList.add(new MobileCellsData(PhoneProfilesService.instance.phoneStateScanner.registeredCell, _cellName, true, true));

                // add all from value
                String[] splits = value.split("\\|");
                for (String cell : splits) {
                    found = false;
                    for (MobileCellsData mCell : cellsList) {
                        if (cell.equals(Integer.toString(mCell.cellId))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        try {
                            int iCell = Integer.parseInt(cell);
                            cellsList.add(new MobileCellsData(iCell, _cellName, false, false));
                        }
                        catch (Exception e) { }
                    }
                }

                db.saveMobileCellsList(cellsList, true);

                Collections.sort(cellsList, new SortList());

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                listAdapter.notifyDataSetChanged();
                //progressLinearLayout.setVisibility(View.GONE);
                //dataRelativeLayout.setVisibility(View.VISIBLE);

                /*
                for (int position = 0; position < cellsList.size() - 1; position++) {
                    if (Integer.toString(cellsList.get(position).cellId).equals(value)) {
                        cellsListView.setSelection(position);
                        cellsListView.setItemChecked(position, true);
                        cellsListView.smoothScrollToPosition(position);
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

            return GUIData.collator.compare(_lhs, _rhs);
        }

    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
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

        MobileCellsPreference preference;

        PhoneStateChangedBroadcastReceiver(MobileCellsPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("MobileCellsPreference.PhoneStateChangedBroadcastReceiver", "xxx");
            if ((preference.mDialog != null) && preference.mDialog.isShowing()) {
                // save new registered cell
                List<MobileCellsData> localCellsList = new ArrayList<MobileCellsData>();
                localCellsList.add(new MobileCellsData(PhoneProfilesService.instance.phoneStateScanner.registeredCell,
                        preference.cellName.getText().toString(), true, false));
                DatabaseHandler db = DatabaseHandler.getInstance(context);
                db.saveMobileCellsList(localCellsList, true);

                preference.refreshListView(false);
            }
        }
    }

}