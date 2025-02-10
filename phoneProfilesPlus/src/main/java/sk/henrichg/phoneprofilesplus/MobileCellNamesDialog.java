package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MobileCellNamesDialog extends DialogFragment {

    List<String> cellNamesList;

    private AppCompatActivity activity;
    private boolean showFilterItems;
    private DialogPreference preference;

    private AlertDialog mDialog;
    private EditText cellName;
    private ListView cellNamesListView;
    private RelativeLayout emptyList;

    private LinearLayout linlaProgress;
    private LinearLayout rellaDialog;

    private DialogInterface.OnClickListener positiveClick;
    private MobileCellNamesDialog fragment;

    private MobileCellNamesDialogAdapter listAdapter;

    private ShowDialogAsyncTask asyncTask = null;

    public MobileCellNamesDialog() {
    }

    public MobileCellNamesDialog(final AppCompatActivity activity,
                          final DialogPreference preference,
                          final boolean showFilterItems,
                          final DialogInterface.OnClickListener _positiveClick) {
        this.activity = activity;
        this.showFilterItems = showFilterItems;
        this.preference = preference;
        this.positiveClick = _positiveClick;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            fragment = this;

            cellNamesList = new ArrayList<>();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    (showFilterItems) ?
                            activity.getString(R.string.mobile_cell_names_dialog_filter_title) :
                            activity.getString(R.string.mobile_cell_names_dialog_title),
                    null);
            //dialogBuilder.setTitle((showFilterItems) ? R.string.mobile_cell_names_dialog_filter_title : R.string.mobile_cell_names_dialog_title);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            if (!showFilterItems) {
                //noinspection ReplaceNullCheck
                if (positiveClick != null)
                    dialogBuilder.setPositiveButton(android.R.string.ok, positiveClick);
                else {
                    dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //if (preference == null) {
                            //if (activity instanceof NotUsedMobileCellsDetectedActivity) {
                            //    ((NotUsedMobileCellsDetectedActivity) activity).cellNameTextView.setText(cellName.getText());
                            //}
                            //} else
                            if (preference instanceof MobileCellsRegistrationDialogPreference) {
                                //noinspection DataFlowIssue
                                ((MobileCellsRegistrationDialogPreference) preference).setCellNameText(cellName.getText().toString());
                            } else if (preference instanceof MobileCellsEditorPreference) {
                                //noinspection DataFlowIssue
                                ((MobileCellsEditorPreference) preference).setCellNameText(cellName.getText().toString());
                            }
                        }
                    });
                }
            }

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_mobile_cell_names, null);
            dialogBuilder.setView(layout);

            mDialog = dialogBuilder.create();

            mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    asyncTask = new ShowDialogAsyncTask(fragment, activity);
                    asyncTask.execute();
                }
            });

            cellNamesListView = layout.findViewById(R.id.mobile_cell_names_dlg_listview);
            emptyList = layout.findViewById(R.id.mobile_cell_names_dlg_empty);
            cellName = layout.findViewById(R.id.mobile_cell_names_dlg_name);
            //noinspection DataFlowIssue
            cellName.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.edit_text_color));
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
            else {
                cellName.setVisibility(View.GONE);
                RelativeLayout cellNameRelLa = layout.findViewById(R.id.mobile_cell_names_dlg_name_rella);
                //noinspection DataFlowIssue
                cellNameRelLa.setVisibility(View.GONE);
            }

            linlaProgress = layout.findViewById(R.id.mobile_cell_names_dlg_linla_progress);
            rellaDialog = layout.findViewById(R.id.mobile_cell_names_dlg_rella_dialog);

            listAdapter = new MobileCellNamesDialogAdapter(activity, this);
            //noinspection DataFlowIssue
            cellNamesListView.setAdapter(listAdapter);

            cellNamesListView.setOnItemClickListener((parent, v, position, id) -> {
                if (showFilterItems) {
                    ((MobileCellsEditorPreference) preference).setCellFilterText(cellNamesList.get(position));
                    mDialog.dismiss();
                }
                else
                    cellName.setText(cellNamesList.get(position));
            });

        }

        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
        asyncTask = null;

        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            show(activity.getSupportFragmentManager(), "MOBILE_CELL_NAMES_DIALOG");
    }

    private static class ShowDialogAsyncTask extends AsyncTask<Void, Integer, Void> {
        final List<String> _cellNamesList = new ArrayList<>();

        private final WeakReference<MobileCellNamesDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakReference;

        public ShowDialogAsyncTask(MobileCellNamesDialog dialog,
                                   Activity activity) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MobileCellNamesDialog dialog = dialogWeakRef.get();
            if (dialog != null) {
                dialog.rellaDialog.setVisibility(View.GONE);
                dialog.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            MobileCellNamesDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakReference.get();
            if ((dialog != null) && (activity != null)) {
                if (dialog.showFilterItems) {
                    _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_new));
                    _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_without_name));
                    _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_selected));
                    _cellNamesList.add(activity.getString(R.string.mobile_cell_names_dialog_item_show_all));
                }
                DatabaseHandler.getInstance(activity.getApplicationContext()).addMobileCellNamesToList(_cellNamesList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            MobileCellNamesDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakReference.get();
            if ((dialog != null) && (activity != null)) {
                dialog.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(activity.getMainLooper());
                final WeakReference<List<String>> cellNamesListWeakRef = new WeakReference<>(_cellNamesList);
                handler.post(() -> {
                    dialog.rellaDialog.setVisibility(View.VISIBLE);

                    List<String> __cellNamesList = cellNamesListWeakRef.get();
                    if (__cellNamesList != null) {
                        dialog.cellNamesList = new ArrayList<>(__cellNamesList);

                        if (dialog.cellNamesList.isEmpty()) {
                            dialog.cellNamesListView.setVisibility(View.GONE);
                            dialog.emptyList.setVisibility(View.VISIBLE);
                        } else {
                            dialog.emptyList.setVisibility(View.GONE);
                            dialog.cellNamesListView.setVisibility(View.VISIBLE);
                        }

                        //if (dialog.preference == null) {
                        //    if (activity instanceof NotUsedMobileCellsDetectedActivity) {
                        //        dialog.cellName.setText(((NotUsedMobileCellsDetectedActivity) activity).cellNameTextView.getText().toString());
                        //    }
                        //} else
                        if (dialog.preference instanceof MobileCellsRegistrationDialogPreference) {
                            dialog.cellName.setText(((MobileCellsRegistrationDialogPreference) dialog.preference).getCellNameText());
                        } else if (dialog.preference instanceof MobileCellsEditorPreference) {
                            //if (showFilterItems) {
                            //    cellName.setText(((MobileCellsEditorPreference) preference).cellFilter.getText().toString());
                            //    cellName.setInputType(InputType.TYPE_NULL);
                            //    cellName.setTextIsSelectable(false);
                            //    cellName.setOnKeyListener(new View.OnKeyListener() {
                            //        @Override
                            //        public boolean onKey(View v,int keyCode, KeyEvent event) {
                            //            return true;  // Blocks input from hardware keyboards.
                            //        }
                            //    });
                            //}
                            //else
                            if (!dialog.showFilterItems)
                                dialog.cellName.setText(((MobileCellsEditorPreference) dialog.preference).getCellNameText());
                        }

                        dialog.listAdapter.notifyDataSetChanged();

                        if (!dialog.showFilterItems) {
                            dialog.cellName.setFocusable(true);
                            dialog.cellName.requestFocus();
                        }
                    }
                });
            }
        }
    }

}
