package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EditorEventListItemMenuDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private EditorActivity activity;
    EditorEventListFragment fragment;

    DialogInterface.OnClickListener itemClick;
    private DialogInterface.OnCancelListener cancelListener;

    int itemValue;

    long event_id = 0;
    Event event;

    static final int NOT_USE_RADIO_BUTTONS = -10;

    public EditorEventListItemMenuDialog() {
    }

    public EditorEventListItemMenuDialog(EditorActivity _activity) {
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
                event_id = arguments.getLong(PPApplication.EXTRA_EVENT_ID, 0);
            }

            if (event_id != 0) {
                fragment = (EditorEventListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    //if (fragment instanceof EditorProfileListFragment)
                    //    ((EditorProfileListFragment) fragment).showHeaderAndBottomToolbar();

                    event = fragment.activityDataWrapper.getEventById(event_id);

                    int itemsRes;
                    if (event.getStatusFromDB(fragment.activityDataWrapper.context) == Event.ESTATUS_STOP)
                        itemsRes = R.array.eventListItemEditEnableRunArray;
                    else
                        itemsRes = R.array.eventListItemEditStopArray;
                    this.itemValue = SingleSelectListDialog.NOT_USE_RADIO_BUTTONS;
                    this.itemClick =
                            (dialog1, which) -> {
                                switch (which) {
                                    case 0:
                                        //runStopEvent(event);
                                        EventStatic.runStopEvent(fragment.activityDataWrapper, event, (EditorActivity) getActivity());
                                        break;
                                    case 1:
                                        fragment.duplicateEvent(event);
                                        break;
                                    case 2:
                                        fragment.deleteEventWithAlert(event);
                                        break;
                                    default:
                                }
                            };
                    this.cancelListener = null;
                    boolean showSubtitle = true;
                    String title = getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + event._name;
                    String subtitle = getString(R.string.tooltip_options_menu);

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
                        EditorEventListItemMenuDialogAdapter listAdapter = new EditorEventListItemMenuDialogAdapter(itemsRes, this);
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
//        EditorEventListItemMenuDialogAdapter listAdapter = new EditorEventListItemMenuDialogAdapter(itemsRes, this);
//        listView.setAdapter(listAdapter);
//    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            show(activity.getSupportFragmentManager(), "SINGLE_CHOICE_DIALOG");
    }

}
