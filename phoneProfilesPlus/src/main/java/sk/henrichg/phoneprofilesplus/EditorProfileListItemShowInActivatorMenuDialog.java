package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class EditorProfileListItemShowInActivatorMenuDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private EditorActivity activity;
    EditorProfileListFragment fragment;

    DialogInterface.OnClickListener itemClick;
    private DialogInterface.OnCancelListener cancelListener;

    int itemValue;

    long profile_id = 0;
    Profile profile;

    static final int NOT_USE_RADIO_BUTTONS = -10;

    public EditorProfileListItemShowInActivatorMenuDialog() {
    }

    public EditorProfileListItemShowInActivatorMenuDialog(EditorActivity _activity) {
        this.activity = _activity;
        //this.itemsRes = _itemsRes;
        //this.itemValue = _itemValue;
        //this.itemClick = _itemClick;
        //this.cancelListener = _cancelListener;
        //this.showSubtitle = _showSubtitle;
        //this.title = _title;
        //this.subtitle = _subtitle;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (EditorActivity) getActivity();
        if (activity != null) {
            //GlobalGUIRoutines.lockScreenOrientation(activity);

            Bundle arguments = getArguments();
            if (arguments != null) {
                profile_id = arguments.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
            }

            if (profile_id != 0) {
                fragment = (EditorProfileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    //if (fragment instanceof EditorProfileListFragment)
                    //    ((EditorProfileListFragment) fragment).showHeaderAndBottomToolbar();

                    profile = fragment.activityDataWrapper.getProfileById(profile_id, false, false, false);

                    int itemsRes = R.array.profileListItemShowInActivatorArray;
                    int value;
                    if (profile._showInActivator)
                        value = 1;
                    else
                        value = 0;
                    this.itemValue = value;
                    this.itemClick =
                            (dialog1, which) -> {
                                switch (which) {
                                    case 0:
                                        profile._showInActivator = false;
                                        DatabaseHandler.getInstance(fragment.activityDataWrapper.context).updateProfileShowInActivator(profile);
                                        //profileListAdapter.notifyDataSetChanged();
                                        ((EditorActivity) getActivity()).redrawProfileListFragment(profile, PPApplication.EDIT_MODE_EDIT);

                                        PPApplication.showToast(fragment.activityDataWrapper.context.getApplicationContext(),
                                                getString(R.string.show_profile_in_activator_not_show_toast),
                                                Toast.LENGTH_LONG);
                                        break;
                                    case 1:
                                        profile._showInActivator = true;
                                        DatabaseHandler.getInstance(fragment.activityDataWrapper.context).updateProfileShowInActivator(profile);
                                        //profileListAdapter.notifyDataSetChanged();
                                        ((EditorActivity) getActivity()).redrawProfileListFragment(profile, PPApplication.EDIT_MODE_EDIT);

                                        PPApplication.showToast(fragment.activityDataWrapper.context.getApplicationContext(),
                                                getString(R.string.show_profile_in_activator_show_toast),
                                                Toast.LENGTH_LONG);
                                        break;
                                    default:
                                }
                            };
                    this.cancelListener = null;
                    boolean showSubtitle = true;
                    String title = getString(R.string.profile_string_0) + StringConstants.STR_COLON_WITH_SPACE + profile._name;
                    String subtitle = getString(R.string.profile_preferences_showInActivator);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, showSubtitle, title, subtitle);

                    dialogBuilder.setCancelable(true);
                    dialogBuilder.setNegativeButton(android.R.string.cancel, null);

                    LayoutInflater inflater = activity.getLayoutInflater();
                    View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
                    dialogBuilder.setView(layout);

                    /*
                    if (cancelListener != null)
                        dialogBuilder.setOnCancelListener(cancelListener);
                    */

                    View buttonsDivider = layout.findViewById(R.id.pp_list_pref_dlg_buttonBarDivider);
                    //if (hideButtonsDivider)
                    //noinspection DataFlowIssue
                    //    buttonsDivider.setVisibility(View.GONE);
                    //else
                    //noinspection DataFlowIssue
                    buttonsDivider.setVisibility(View.VISIBLE);

                    mDialog = dialogBuilder.create();

                    //mDialog.setOnShowListener(dialog -> doShow());

                    ListView listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);
                    // moved from doShow(), better for dialog animation and
                    // also correct the displacement of the dialog
                    if (listView != null) {
                        EditorProfileListItemShowInActivatorMenuDialogAdapter listAdapter = new EditorProfileListItemShowInActivatorMenuDialogAdapter(itemsRes, this);
                        listView.setAdapter(listAdapter);
                    }

                    //noinspection DataFlowIssue
                    listView.setOnItemClickListener((parent, item, position, id) -> {
                        if (itemValue != NOT_USE_RADIO_BUTTONS) {
                            RadioButton rb = item.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
                            itemValue = position;
                            //noinspection DataFlowIssue
                            rb.setChecked(true);
                        }
                        itemClick.onClick(mDialog, position);
                        dismiss();
                    });
                }
            }
        }
        return mDialog;
    }

    public void onCancel (@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (cancelListener != null)
            cancelListener.onCancel(dialog);
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        //if (activity != null)
        //    GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

//    private void doShow() {
//        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
//        EditorProfileListItemMenuDialogAdapter listAdapter = new EditorProfileListItemMenuDialogAdapter(itemsRes, this);
//        listView.setAdapter(listAdapter);
//    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "SINGLE_CHOICE_DIALOG");
        }
    }

}
