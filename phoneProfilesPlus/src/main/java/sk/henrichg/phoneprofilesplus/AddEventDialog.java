package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AddEventDialog extends DialogFragment
{
    EditorEventListFragment eventListFragment;

    private AlertDialog mDialog;
    private EditorActivity activity;

    private LinearLayout linlaProgress;
    private LinearLayout rellaData;
    private ListView listView;
    private TextView help;
    SwitchCompat hideEventDetailsSwitch;
    boolean hideEventDetailsValue;

    final List<Event> eventList = new ArrayList<>();

    private GetEventsAsyncTask getEventsAsyncTask = null;

    public AddEventDialog()
    {
    }

    public AddEventDialog(EditorActivity activity/*, EditorEventListFragment eventListFragment*/)
    {
        //this.eventListFragment = eventListFragment;
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (EditorActivity) getActivity();
        if (this.activity != null) {
            this.eventListFragment = (EditorEventListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.new_event_predefined_events_dialog), null);
            //dialogBuilder.setTitle(R.string.new_event_predefined_events_dialog);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_add_event, null);
            dialogBuilder.setView(layout);

            mDialog = dialogBuilder.create();

            mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

                doShow();
            });

            linlaProgress = layout.findViewById(R.id.event_pref_dlg_linla_progress);
            rellaData = layout.findViewById(R.id.event_pref_dlg_rella_data);

            listView = layout.findViewById(R.id.event_pref_dlg_listview);
            help = layout.findViewById(R.id.event_pref_dlg_help);
            hideEventDetailsSwitch = layout.findViewById(R.id.event_hide_event_details);
            if (hideEventDetailsSwitch != null)
                hideEventDetailsSwitch.setChecked(ApplicationPreferences.applicationEditorHideEventDetails);
            hideEventDetailsValue = ApplicationPreferences.applicationEditorHideEventDetails;

            //noinspection DataFlowIssue
            listView.setOnItemClickListener((parent, item, position, id) -> {
                EventListViewHolder viewHolder = (EventListViewHolder) item.getTag();
                if (viewHolder != null)
                    viewHolder.radioButton.setChecked(true);
                final Handler handler = new Handler(activity.getMainLooper());
                final WeakReference<AddEventDialog> dialogWeakRef = new WeakReference<>(this);
                handler.postDelayed(() -> {
                    AddEventDialog dialog1 = dialogWeakRef.get();
                    if (dialog1 != null)
                        dialog1.doOnItemSelected(position);
                }, 200);
            });

            if (hideEventDetailsSwitch != null) {
                hideEventDetailsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!activity.isFinishing()) {
                        hideEventDetailsValue = isChecked;
                        getEventsAsyncTask = new GetEventsAsyncTask(this, activity, eventListFragment.activityDataWrapper);
                        getEventsAsyncTask.execute();
                        //((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        //listView.invalidate();
                    }
                });
            }

        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if ((getEventsAsyncTask != null) &&
                getEventsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            getEventsAsyncTask.cancel(true);
        }
        getEventsAsyncTask = null;
        this.eventListFragment = null;
    }

    private void doShow() {
        getEventsAsyncTask = new GetEventsAsyncTask(this, activity, eventListFragment.activityDataWrapper);
        getEventsAsyncTask.execute();
    }

    void doOnItemSelected(int position)
    {
        if (eventListFragment != null)
            eventListFragment.startEventPreferencesActivity(null, position);
        dismiss();
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            //mDialog.show();
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "ADD_EVENT_DIALOG");
        }
    }

    private static class GetEventsAsyncTask extends AsyncTask<Void, Integer, Void> {

        final List<Event> _eventList = new ArrayList<>();
        boolean profileNotExists = false;

        private final WeakReference<AddEventDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakRef;
        final DataWrapper dataWrapper;

        public GetEventsAsyncTask(final AddEventDialog dialog,
                                  final Activity activity,
                                  final DataWrapper dataWrapper) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakRef = new WeakReference<>(activity);
            this.dataWrapper = dataWrapper.copyDataWrapper();
        }

        /*@Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //rellaData.setVisibility(View.GONE);
            //linlaProgress.setVisibility(View.VISIBLE);
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            Activity activity = activityWeakRef.get();
            if (activity != null) {
                Event event;
                event = DataWrapperStatic.getNonInitializedEvent(activity.getString(R.string.event_name_default), 0);
                _eventList.add(event);
                for (int index = 0; index < 6; index++) {
                    event = dataWrapper.getPredefinedEvent(index, false, activity);
                    if (event._fkProfileStart == 0)
                        profileNotExists = true;
                    if (event._fkProfileEnd == 0)
                        profileNotExists = true;
                    event._peferencesDecription = StringFormatUtils.fromHtml(
                            event.getPreferencesDescription(activity, null, false),
                            true, false, 0, 0, true);
                    _eventList.add(event);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            AddEventDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakRef.get();
            if ((dialog != null) && (activity != null)) {
                dialog.linlaProgress.setVisibility(View.GONE);
                dialog.rellaData.setVisibility(View.VISIBLE);

                if (profileNotExists)
                    dialog.help.setVisibility(View.VISIBLE);

                dialog.eventList.clear();
                dialog.eventList.addAll(_eventList);

                AddEventAdapter addEventAdapter = new AddEventAdapter(dialog, activity, dialog.eventList);
                dialog.listView.setAdapter(addEventAdapter);
            }
        }

    }

}
