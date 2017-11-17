package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;

import java.util.ArrayList;
import java.util.List;

class MobileCellNamesDialog {

    List<String> cellNamesList = new ArrayList<>();

    private final Context context;
    private final boolean showFilterItems;
    private final DialogPreference preference;

    final MaterialDialog mDialog;
    private final EditText cellName;

    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    private MobileCellNamesDialogAdapter listAdapter;

    private AsyncTask asyncTask = null;

    MobileCellNamesDialog(final Context context, final DialogPreference preference, final boolean showFilterItems) {

        this.context = context;
        this.showFilterItems = showFilterItems;
        this.preference = preference;

        cellNamesList = new ArrayList<>();

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title((showFilterItems) ? R.string.mobile_cell_names_dialog_filter_title : R.string.mobile_cell_names_dialog_title)
                //.disableDefaultFonts()
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .customView(R.layout.activity_mobile_cell_names_dialog, false);
        if (!showFilterItems) {
            dialogBuilder.positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (preference instanceof MobileCellsRegistrationDialogPreference) {
                                ((MobileCellsRegistrationDialogPreference) preference).mCellsName.setText(cellName.getText().toString());
                            }
                            else
                            if (preference instanceof MobileCellsPreference) {
                                ((MobileCellsPreference) preference).cellName.setText(cellName.getText().toString());
                            }
                        }
                    });
        }
        dialogBuilder.dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
                    asyncTask.cancel(true);
                }
            }
        });

        mDialog = dialogBuilder.build();
        View layout = mDialog.getCustomView();

        ListView cellNamesListView = layout.findViewById(R.id.mobile_cell_names_dlg_listview);
        cellName = layout.findViewById(R.id.mobile_cell_names_dlg_name);
        if (!showFilterItems) {
            cellName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String value = cellName.getText().toString();
                    MDButton button = mDialog.getActionButton(DialogAction.POSITIVE);
                    if (button != null)
                        button.setEnabled(!value.isEmpty());
                }
            });
        }
        else
            cellName.setVisibility(View.GONE);

        linlaProgress = layout.findViewById(R.id.mobile_cell_names_dlg_linla_progress);
        rellaDialog = layout.findViewById(R.id.mobile_cell_names_dlg_rella_dialog);

        listAdapter = new MobileCellNamesDialogAdapter(context, this);
        cellNamesListView.setAdapter(listAdapter);

        cellNamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (showFilterItems) {
                    ((MobileCellsPreference) preference).cellFilter.setText(cellNamesList.get(position));
                    mDialog.dismiss();
                }
                else
                    cellName.setText(cellNamesList.get(position));
            }

        });

    }


    @SuppressLint("StaticFieldLeak")
    public void show() {
        mDialog.show();

        asyncTask = new AsyncTask<Void, Integer, Void>() {

            List<String> _cellNamesList = new ArrayList<>();

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                rellaDialog.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (showFilterItems) {
                    _cellNamesList.add(context.getString(R.string.mobile_cell_names_dialog_item_show_selected));
                    _cellNamesList.add(context.getString(R.string.mobile_cell_names_dialog_item_show_without_name));
                    _cellNamesList.add(context.getString(R.string.mobile_cell_names_dialog_item_show_new));
                    _cellNamesList.add(context.getString(R.string.mobile_cell_names_dialog_item_show_all));
                }
                DatabaseHandler.getInstance(context).addMobileCellNamesToList(_cellNamesList);
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
                rellaDialog.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                cellNamesList = new ArrayList<>(_cellNamesList);

                if (preference instanceof MobileCellsRegistrationDialogPreference) {
                    cellName.setText(((MobileCellsRegistrationDialogPreference) preference).mCellsName.getText().toString());
                }
                else
                if (preference instanceof MobileCellsPreference) {
                    /*if (showFilterItems) {
                        cellName.setText(((MobileCellsPreference) preference).cellFilter.getText().toString());
                        cellName.setInputType(InputType.TYPE_NULL);
                        cellName.setTextIsSelectable(false);
                        cellName.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v,int keyCode, KeyEvent event) {
                                return true;  // Blocks input from hardware keyboards.
                            }
                        });
                    }
                    else*/
                    if (!showFilterItems)
                        cellName.setText(((MobileCellsPreference) preference).cellName.getText().toString());
                }

                listAdapter.notifyDataSetChanged();
            }

        }.execute();
    }

}
