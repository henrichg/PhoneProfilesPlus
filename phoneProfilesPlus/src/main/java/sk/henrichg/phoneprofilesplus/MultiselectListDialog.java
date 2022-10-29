package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

class MultiselectListDialog
{
    final AlertDialog mDialog;
    final Activity activity;

    private final ListView listView;
    private final DialogInterface.OnClickListener positiveButtonClick;

    int itemsRes;
    boolean[] itemValues;

    MultiselectListDialog(int _titleRes, int _itemsRes, boolean[] _itemValues,
                          DialogInterface.OnClickListener _positiveButtonClick,
                          Activity _activity)
    {
        this.activity = _activity;
        this.itemsRes = _itemsRes;
        this.itemValues = _itemValues;
        this.positiveButtonClick = _positiveButtonClick;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getString(_titleRes));
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
        dialogBuilder.setView(layout);

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positiveButtonClick.onClick(dialog, which);
            }
        });

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> doShow());

        listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            CheckBox chb = item.findViewById(R.id.pp_multiselect_list_pref_dlg_item_checkbox);
            itemValues[position] = !itemValues[position];
            chb.setChecked(itemValues[position]);
        });
    }

    private void doShow() {
        MultiselectListDialogAdapter listAdapter = new MultiselectListDialogAdapter(itemsRes, this);
        listView.setAdapter(listAdapter);
    }

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
