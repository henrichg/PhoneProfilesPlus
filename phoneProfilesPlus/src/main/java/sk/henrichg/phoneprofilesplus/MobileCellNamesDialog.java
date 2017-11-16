package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

class MobileCellNamesDialog {

    List<String> cellNamesList = new ArrayList<>();

    final MaterialDialog mDialog;
    final ListView cellNamesListView;
    final EditText cellName;

    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    private MobileCellNamesDialogAdapter listAdapter;

    final Context context;

    MobileCellNamesDialog(final Context context, final DialogPreference preference) {

        this.context = context;
        cellNamesList = new ArrayList<>();

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.mobile_cell_names_dialog_title)
                //.disableDefaultFonts()
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .customView(R.layout.activity_mobile_cell_names_dialog, false)
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

        mDialog = dialogBuilder.build();
        View layout = mDialog.getCustomView();

        cellNamesListView = layout.findViewById(R.id.mobile_cell_names_dlg_listview);
        cellName = layout.findViewById(R.id.mobile_cell_names_dlg_name);
        if (preference instanceof MobileCellsRegistrationDialogPreference) {
            cellName.setText(((MobileCellsRegistrationDialogPreference) preference).mCellsName.getText().toString());
        }
        else
        if (preference instanceof MobileCellsPreference) {
            cellName.setText(((MobileCellsPreference) preference).cellName.getText().toString());
        }

        linlaProgress = layout.findViewById(R.id.mobile_cell_names_dlg_linla_progress);
        rellaDialog = layout.findViewById(R.id.mobile_cell_names_dlg_rella_dialog);

        listAdapter = new MobileCellNamesDialogAdapter(context, this);
        cellNamesListView.setAdapter(listAdapter);

        cellNamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cellName.setText(cellNamesList.get(position));
            }

        });

    }

    @SuppressLint("StaticFieldLeak")
    public void show() {
        mDialog.show();

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                rellaDialog.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                cellNamesList.clear();
                DatabaseHandler.getInstance(context).addMobileCellNamesToList(cellNamesList);
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
                rellaDialog.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);
                listAdapter.notifyDataSetChanged();
            }

        }.execute();
    }

}
