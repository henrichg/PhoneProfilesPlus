package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class AddEventDialog
{
    EditorEventListFragment eventListFragment;

    final AlertDialog mDialog;
    final Activity activity;

    final LinearLayout linlaProgress;
    final LinearLayout rellaData;
    final ListView listView;
    final TextView help;

    final List<Event> eventList = new ArrayList<>();

    private GetEventsAsyncTask getEventsAsyncTask = null;

    AddEventDialog(Activity activity, EditorEventListFragment eventListFragment)
    {
        this.eventListFragment = eventListFragment;
        this.activity = activity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.new_event_predefined_events_dialog);
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
        mDialog.setOnDismissListener(dialog -> {
            if ((getEventsAsyncTask != null) &&
                    getEventsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                getEventsAsyncTask.cancel(true);
            }
            getEventsAsyncTask = null;
            this.eventListFragment = null;
        });

        linlaProgress = layout.findViewById(R.id.event_pref_dlg_linla_progress);
        rellaData = layout.findViewById(R.id.event_pref_dlg_rella_data);

        listView = layout.findViewById(R.id.event_pref_dlg_listview);
        help = layout.findViewById(R.id.event_pref_dlg_help);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            AddEventViewHolder viewHolder = (AddEventViewHolder) item.getTag();
            if (viewHolder != null)
                viewHolder.radioButton.setChecked(true);
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> doOnItemSelected(position), 200);
        });

    }

    private void doShow() {
        getEventsAsyncTask = new GetEventsAsyncTask(this, activity, eventListFragment.activityDataWrapper);
        getEventsAsyncTask.execute();
    }

    void doOnItemSelected(int position)
    {
        eventListFragment.startEventPreferencesActivity(null, position);
        mDialog.dismiss();
    }

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
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
