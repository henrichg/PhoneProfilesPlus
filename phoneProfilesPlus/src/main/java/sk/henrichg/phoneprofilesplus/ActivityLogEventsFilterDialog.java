package sk.henrichg.phoneprofilesplus;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogEventsFilterDialog extends DialogFragment
{
    private DataWrapper dataWrapper;

    EditorEventListFragment eventListFragment;

    private AlertDialog mDialog;
    private ActivityLogActivity activity;

    private LinearLayout linlaProgress;
    private LinearLayout rellaData;
    private ListView listView;
    private TextView help;
    SwitchCompat hideEventDetailsSwitch;
    boolean hideEventDetailsValue;

    final List<Event> eventList = new ArrayList<>();

    private GetEventsAsyncTask getEventsAsyncTask = null;

    private int selectedFilter = PPApplication.ALFILTER_EVENTS_LIFECYCLE;
    private long mEventFilter = 0;
    private boolean eventSet = false;

    public ActivityLogEventsFilterDialog()
    {
    }

    public ActivityLogEventsFilterDialog(ActivityLogActivity activity)
    {
        this.activity = activity;
        dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (ActivityLogActivity) getActivity();
        if (this.activity != null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mEventFilter = arguments.getLong(ActivityLogActivity.EXTRA_EVENT_FILTER, 0);
                selectedFilter = arguments.getInt(ActivityLogActivity.EXTRA_SELECTED_FILTER, PPApplication.ALFILTER_EVENTS_LIFECYCLE);
            }

            this.eventListFragment = (EditorEventListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.new_event_predefined_events_dialog), null);
            //dialogBuilder.setTitle(R.string.new_event_predefined_events_dialog);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_event_preference, null);
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
                final WeakReference<ActivityLogEventsFilterDialog> dialogWeakRef = new WeakReference<>(this);
                handler.postDelayed(() -> {
                    ActivityLogEventsFilterDialog dialog1 = dialogWeakRef.get();
                    if (dialog1 != null)
                        dialog1.doOnItemSelected(position);
                }, 200);
            });

            if (hideEventDetailsSwitch != null) {
                hideEventDetailsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!activity.isFinishing()) {
                        hideEventDetailsValue = isChecked;
                        getEventsAsyncTask = new GetEventsAsyncTask(this, activity);
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

        dataWrapper.invalidateDataWrapper();

        if (activity != null) {
            //GlobalGUIRoutines.unlockScreenOrientation(activity);
            if (!eventSet)
                activity.setEventFilter(selectedFilter);
        }

        this.eventListFragment = null;
    }

    private void doShow() {
        getEventsAsyncTask = new GetEventsAsyncTask(this, activity);
        getEventsAsyncTask.execute();
    }

    void doOnItemSelected(int position)
    {
        long eventId = 0;
        if (position > 0) {
//            PPApplicationStatic.logE("[SYNCHRONIZED] AskForDurationActivateProfileDialog.doOnItemSelected", "DataWrapper.profileList");
            synchronized (dataWrapper.eventList) {
                eventId = dataWrapper.eventList.get(position - 1)._id;
            }
        }
        mEventFilter = eventId;
        activity.mEventFilter = eventId;
        eventSet = true;
        activity.setEventFilter(selectedFilter);

        dismiss();
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            //mDialog.show();
            show(activity.getSupportFragmentManager(), "ADD_EVENT_DIALOG");
    }

    private static class GetEventsAsyncTask extends AsyncTask<Void, Integer, Void> {
        boolean profileNotExists = false;

        private final WeakReference<ActivityLogEventsFilterDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakRef;

        public GetEventsAsyncTask(final ActivityLogEventsFilterDialog dialog,
                                  final Activity activity) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakRef = new WeakReference<>(activity);
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
            ActivityLogEventsFilterDialog dialog = dialogWeakRef.get();
            if (dialog != null) {
                dialog.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
                dialog.dataWrapper.fillEventList();
//                PPApplicationStatic.logE("[SYNCHRONIZED] AskForDurationActivateProfileDialog.ShowDialogAsyncTask", "DataWrapper.profileList");
                //synchronized (dialog.dataWrapper.profileList) {
                //    dialog.dataWrapper.profileList.sort(new ActivityLogActivatedProfileFilterDialog.AlphabeticallyComparator());
                //}
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ActivityLogEventsFilterDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakRef.get();
            if ((dialog != null) && (activity != null)) {
                dialog.linlaProgress.setVisibility(View.GONE);
                dialog.rellaData.setVisibility(View.VISIBLE);

                if (profileNotExists)
                    dialog.help.setVisibility(View.VISIBLE);

                ActivityLogEventsFilterAdapter eventAdapter = new ActivityLogEventsFilterAdapter(dialog, activity, dialog.eventList);
                dialog.listView.setAdapter(eventAdapter);
            }
        }

    }

}
