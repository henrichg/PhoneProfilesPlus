package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class MultiSelectListDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private AppCompatActivity activity;
    private DialogInterface.OnClickListener positiveButtonClick;

    private int titleRes;
    private int itemsRes;
    boolean[] itemValues;

    public MultiSelectListDialog() {
    }

    public MultiSelectListDialog(int _titleRes, int _itemsRes, boolean[] _itemValues,
                          DialogInterface.OnClickListener _positiveButtonClick,
                          AppCompatActivity _activity)
    {
        this.activity = _activity;
        this.titleRes = _titleRes;
        this.itemsRes = _itemsRes;
        this.itemValues = _itemValues;
        this.positiveButtonClick = _positiveButtonClick;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(titleRes), null);
            //dialogBuilder.setTitle(activity.getString(_titleRes));
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
            dialogBuilder.setView(layout);

            dialogBuilder.setPositiveButton(android.R.string.ok, positiveButtonClick);

            mDialog = dialogBuilder.create();

            //mDialog.setOnShowListener(dialog -> doShow());

            ListView listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);
            // moved from doShow(), better for dialog animation and
            // also correct the displacement of the dialog
            if (listView != null) {
                MultiSelectListDialogAdapter listAdapter = new MultiSelectListDialogAdapter(itemsRes, this);
                listView.setAdapter(listAdapter);
            }

            //noinspection DataFlowIssue
            listView.setOnItemClickListener((parent, item, position, id) -> {
                CheckBox chb = item.findViewById(R.id.pp_multiselect_list_pref_dlg_item_checkbox);
                itemValues[position] = !itemValues[position];
                //noinspection DataFlowIssue
                chb.setChecked(itemValues[position]);
            });
        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

//    private void doShow() {
//        MultiSelectListDialogAdapter listAdapter = new MultiSelectListDialogAdapter(itemsRes, this);
//        listView.setAdapter(listAdapter);
//    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "MULTI_CHOICE_DIALOG");
        }
    }

}
