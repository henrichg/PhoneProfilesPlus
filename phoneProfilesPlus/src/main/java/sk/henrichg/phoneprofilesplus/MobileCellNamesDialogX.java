package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

class MobileCellNamesDialogX {

    List<String> cellNamesList;

    private final Activity activity;
    private final boolean showFilterItems;
    private final DialogPreference preference;

    private final AlertDialog mDialog;
    private final EditText cellName;

    private final LinearLayout linlaProgress;
    private final RelativeLayout rellaDialog;

    private final MobileCellNamesDialogAdapterX listAdapter;

    @SuppressWarnings("rawtypes")
    private AsyncTask asyncTask = null;

    MobileCellNamesDialogX(final Activity activity, final DialogPreference preference, final boolean showFilterItems) {

        this.activity = activity;
        this.showFilterItems = showFilterItems;
        this.preference = preference;

        cellNamesList = new ArrayList<>();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle((showFilterItems) ? R.string.mobile_cell_names_dialog_filter_title : R.string.mobile_cell_names_dialog_title);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        if (!showFilterItems) {
            dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (preference == null) {
                        if (activity instanceof NotUsedMobileCellsDetectedActivity) {
                            ((NotUsedMobileCellsDetectedActivity)activity).cellNameTextView.setText(cellName.getText());
                        }
                    }
                    else
                    if (preference instanceof MobileCellsRegistrationDialogPreferenceX) {
                        ((MobileCellsRegistrationDialogPreferenceX) preference).setCellNameText(cellName.getText().toString());
                    }
                    else
                    if (preference instanceof MobileCellsPreferenceX) {
                        ((MobileCellsPreferenceX) preference).setCellNameText(cellName.getText().toString());
                    }
                }
            });
        }

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
                    asyncTask.cancel(true);
                }
            }
        });

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_mobile_cell_names, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

//        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

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
                    Button button = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null)
                        button.setEnabled(!value.isEmpty());
                }
            });
        }
        else
            cellName.setVisibility(View.GONE);

        linlaProgress = layout.findViewById(R.id.mobile_cell_names_dlg_linla_progress);
        rellaDialog = layout.findViewById(R.id.mobile_cell_names_dlg_rella_dialog);

        listAdapter = new MobileCellNamesDialogAdapterX(activity, this);
        cellNamesListView.setAdapter(listAdapter);

        cellNamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (showFilterItems) {
                    ((MobileCellsPreferenceX) preference).setCellFilterText(cellNamesList.get(position));
                    mDialog.dismiss();
                }
                else
                    cellName.setText(cellNamesList.get(position));
            }

        });

    }


    @SuppressLint("StaticFieldLeak")
    public void show() {
        if (!activity.isFinishing()) {
            mDialog.show();

            asyncTask = new AsyncTask<Void, Integer, Void>() {

                final List<String> _cellNamesList = new ArrayList<>();

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    rellaDialog.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    if (showFilterItems) {
                        _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_selected));
                        _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_without_name));
                        _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_new));
                        _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_all));
                    }
                    DatabaseHandler.getInstance(activity.getApplicationContext()).addMobileCellNamesToList(_cellNamesList);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    rellaDialog.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);

                    cellNamesList = new ArrayList<>(_cellNamesList);

                    if (preference == null) {
                        if (activity instanceof NotUsedMobileCellsDetectedActivity) {
                            cellName.setText(((NotUsedMobileCellsDetectedActivity)activity).cellNameTextView.getText().toString());
                        }
                    } else if (preference instanceof MobileCellsRegistrationDialogPreferenceX) {
                        cellName.setText(((MobileCellsRegistrationDialogPreferenceX) preference).getCellNameText());
                    } else if (preference instanceof MobileCellsPreferenceX) {
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
                            cellName.setText(((MobileCellsPreferenceX) preference).getCellNameText());
                    }

                    listAdapter.notifyDataSetChanged();

                    if (!showFilterItems) {
                        cellName.setFocusable(true);
                        cellName.requestFocus();
                    }

                }

            }.execute();
        }
    }

}
