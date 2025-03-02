package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class ActivityLogEventsFilterDialog extends DialogFragment
{
    DataWrapper dataWrapper;

    EditorEventListFragment eventListFragment;

    private AlertDialog mDialog;
    private ActivityLogActivity activity;

    private LinearLayout linlaProgress;
    private LinearLayout rellaData;
    private ListView listView;

    private GetEventsAsyncTask getEventsAsyncTask = null;

    //private int selectedFilter = PPApplication.ALFILTER_EVENTS_LIFECYCLE;
    private long mEventFilter = 0;
    private boolean eventSet = false;

    public ActivityLogEventsFilterDialog()
    {
    }

    public ActivityLogEventsFilterDialog(ActivityLogActivity activity)
    {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (ActivityLogActivity) getActivity();
        if (this.activity != null) {
            dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            GlobalGUIRoutines.lockScreenOrientation(activity);

            /*
            Bundle arguments = getArguments();
            if (arguments != null) {
                mEventFilter = arguments.getLong(ActivityLogActivity.EXTRA_EVENT_FILTER, 0);
                //selectedFilter = arguments.getInt(ActivityLogActivity.EXTRA_SELECTED_FILTER, PPApplication.ALFILTER_EVENTS_LIFECYCLE);
                selectedFilter = activity.mSelectedFilter;
            }
            Log.e("ActivityLogEventsFilterDialog.onCreateDialog", "mEventFilter="+mEventFilter);
            Log.e("ActivityLogEventsFilterDialog.onCreateDialog", "selectedFilter="+selectedFilter);
            */
            mEventFilter = activity.mEventFilter;

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
            GlobalGUIRoutines.unlockScreenOrientation(activity);
            if (!eventSet)
                activity.setEventFilter(activity.mSelectedFilter/*selectedFilter*/);
        }
    }

    private void doShow() {
        getEventsAsyncTask = new GetEventsAsyncTask(mEventFilter, this, activity);
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
        activity.setEventFilter(activity.mSelectedFilter/*selectedFilter*/);

        dismiss();
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            //mDialog.show();
            show(activity.getSupportFragmentManager(), "ACTIVITY_LOG_EVENT_FILTER_DIALOG");
    }

    private static class AlphabeticallyComparator implements Comparator<Event> {

        public int compare(Event lhs, Event rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }


    private static class GetEventsAsyncTask extends AsyncTask<Void, Integer, Void> {
        final long selectedEvent;

        private final WeakReference<ActivityLogEventsFilterDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakRef;

        public GetEventsAsyncTask(final long selectedEvent,
                                  final ActivityLogEventsFilterDialog dialog,
                                  final Activity activity) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakRef = new WeakReference<>(activity);
            this.selectedEvent = selectedEvent;
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
                synchronized (dialog.dataWrapper.eventList) {
                    dialog.dataWrapper.eventList.sort(new ActivityLogEventsFilterDialog.AlphabeticallyComparator());
                }
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

                ActivityLogEventsFilterAdapter eventAdapter = new ActivityLogEventsFilterAdapter(
                        dialog, activity, selectedEvent, dialog.dataWrapper.eventList);
                dialog.listView.setAdapter(eventAdapter);

                //noinspection ExtractMethodRecommender
                int position;
                long iEventId;
                iEventId = selectedEvent;
                if (iEventId == 0)
                    position = 0;
                else {
                    boolean found = false;
                    position = 0;
                    for (Event event : dialog.dataWrapper.eventList) {
                        if (event._id == iEventId) {
                            found = true;
                            break;
                        }
                        position++;
                    }
                    if (found) {
                        position++;
                    } else
                        position = 0;
                }
                dialog.listView.setSelection(position);
            }
        }

    }

}
