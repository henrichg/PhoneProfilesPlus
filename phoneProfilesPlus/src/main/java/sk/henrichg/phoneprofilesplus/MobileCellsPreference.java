package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    public List<MobileCellsData> cellsList = null;

    Context context;

    private MaterialDialog mDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView cellsListView;
    private MobileCellsPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    public MobileCellsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        cellsList = new ArrayList<MobileCellsData>();
    }

    @Override
    protected void showDialog(Bundle state) {
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

                            if (callChangeListener(value))
                            {

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

        progressLinearLayout = (LinearLayout) layout.findViewById(R.id.mobile_cells_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.mobile_cells_pref_dlg_rella_data);

        cellsListView = (ListView) layout.findViewById(R.id.mobile_cells_pref_dlg_listview);
        listAdapter = new MobileCellsPreferenceAdapter(context, this);
        cellsListView.setAdapter(listAdapter);

        refreshListView(false);

        cellsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                MobileCellsPreferenceAdapter.ViewHolder viewHolder =
                        (MobileCellsPreferenceAdapter.ViewHolder) v.getTag();

                viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());

                if (viewHolder.checkBox.isChecked())
                    addCellId(cellsList.get(position).cellId);
                else
                    removeCellId(cellsList.get(position).cellId);
            }

        });

        mBuilder.customView(layout, false);

        /*
        final TextView helpText = (TextView)layout.findViewById(R.id.mobile_cells_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

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
        */

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (!rescanAsyncTask.isCancelled())
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

    public void refreshListView(boolean forRescan)
    {
        final boolean _forRescan = forRescan;

        if (GlobalData.phoneProfilesService.isPhoneStateStarted()) {

            rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    //dataRelativeLayout.setVisibility(View.GONE);
                    //progressLinearLayout.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    cellsList.clear();

                    if (_forRescan) {
                        GlobalData.phoneProfilesService.phoneStateScanner.getRegisteredCell();

                        //try { Thread.sleep(200); } catch (InterruptedException e) { }
                        //SystemClock.sleep(200);
                        //GlobalData.sleep(200);
                    }

                    cellsList.add(new MobileCellsData(GlobalData.phoneProfilesService.phoneStateScanner.registeredCell, "", true));

                    String[] splits = value.split("\\|");
                    String sRegisteredCell = Integer.toString(GlobalData.phoneProfilesService.phoneStateScanner.registeredCell);
                    for (String cell : splits) {
                        if (!cell.equals(sRegisteredCell)) {
                            try {
                                int iCell = Integer.parseInt(cell);
                                cellsList.add(new MobileCellsData(iCell, "", false));
                            }
                            catch (Exception e) { }
                        }
                    }

                    DatabaseHandler db = DatabaseHandler.getInstance(context);
                    db.addMobileCellsToList(cellsList);

                    Collections.sort(cellsList, new SortList());

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    listAdapter.notifyDataSetChanged();
                    //progressLinearLayout.setVisibility(View.GONE);
                    //dataRelativeLayout.setVisibility(View.VISIBLE);

                    for (int position = 0; position < cellsList.size() - 1; position++) {
                        if (Integer.toString(cellsList.get(position).cellId).equals(value)) {
                            cellsListView.setSelection(position);
                            cellsListView.setItemChecked(position, true);
                            cellsListView.smoothScrollToPosition(position);
                            break;
                        }
                    }
                }

            };

            rescanAsyncTask.execute();
        }

    }

    private class SortList implements Comparator<MobileCellsData> {

        public int compare(MobileCellsData lhs, MobileCellsData rhs) {
            String _lhs = lhs.name;
            if (_lhs.isEmpty())
                _lhs = "\uFFFF";
            _lhs = _lhs + lhs.cellId;
            String _rhs = rhs.name;
            if (_rhs.isEmpty())
                _rhs = "\uFFFF";
            _rhs = _rhs + rhs.cellId;
            return GUIData.collator.compare(_lhs, _rhs);
        }

    }

}