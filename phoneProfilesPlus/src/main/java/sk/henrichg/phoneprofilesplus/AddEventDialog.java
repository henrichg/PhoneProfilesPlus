package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
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
    final EditorEventListFragment eventListFragment;

    final AlertDialog mDialog;
    private final Activity activity;

    final LinearLayout linlaProgress;
    final LinearLayout rellaData;
    final ListView listView;
    final TextView help;

    AddEventDialog(Activity activity, EditorEventListFragment eventListFragment)
    {
        this.eventListFragment = eventListFragment;
        this.activity = activity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.new_event_predefined_events_dialog);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_event_preference, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

            AddEventDialog.this.onShow();
        });

        linlaProgress = layout.findViewById(R.id.event_pref_dlg_linla_progress);
        rellaData = layout.findViewById(R.id.event_pref_dlg_rella_data);

        listView = layout.findViewById(R.id.event_pref_dlg_listview);
        help = layout.findViewById(R.id.event_pref_dlg_help);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            AddEventAdapter.ViewHolder viewHolder = (AddEventAdapter.ViewHolder) item.getTag();
            if (viewHolder != null)
                viewHolder.radioButton.setChecked(true);
            Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> doOnItemSelected(position), 200);
        });

    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {
         GetEventsAsyncTask asyncTask = new GetEventsAsyncTask(this, activity, eventListFragment.activityDataWrapper);
         asyncTask.execute();

/*        new AsyncTask<Void, Integer, Void>() {

            final List<Event> eventList = new ArrayList<>();
            boolean profileNotExists = false;

            //@Override
            //protected void onPreExecute()
            //{
            //    super.onPreExecute();
                //rellaData.setVisibility(View.GONE);
                //linlaProgress.setVisibility(View.VISIBLE);
            //}

            @Override
            protected Void doInBackground(Void... params) {
                Event event;
                event = DataWrapper.getNonInitializedEvent(activity.getString(R.string.event_name_default), 0);
                eventList.add(event);
                for (int index = 0; index < 6; index++) {
                    event = eventListFragment.activityDataWrapper.getPredefinedEvent(index, false, activity);
                    if (event._fkProfileStart == 0)
                        profileNotExists = true;
                    if (event._fkProfileEnd == 0)
                        profileNotExists = true;
                    eventList.add(event);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                linlaProgress.setVisibility(View.GONE);
                rellaData.setVisibility(View.VISIBLE);

                if (profileNotExists)
                    help.setVisibility(View.VISIBLE);

                AddEventAdapter addEventAdapter = new AddEventAdapter(AddEventDialog.this, activity, eventList);
                listView.setAdapter(addEventAdapter);
            }

        }.execute(); */
    }

    void doOnItemSelected(int position)
    {
        eventListFragment.startEventPreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

    private static class GetEventsAsyncTask extends AsyncTask<Void, Integer, Void> {

        final List<Event> eventList = new ArrayList<>();
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
                event = DataWrapper.getNonInitializedEvent(activity.getString(R.string.event_name_default), 0);
                eventList.add(event);
                for (int index = 0; index < 6; index++) {
                    event = dataWrapper.getPredefinedEvent(index, false, activity);
                    if (event._fkProfileStart == 0)
                        profileNotExists = true;
                    if (event._fkProfileEnd == 0)
                        profileNotExists = true;
                    eventList.add(event);
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

                AddEventAdapter addEventAdapter = new AddEventAdapter(dialog, activity, eventList);
                dialog.listView.setAdapter(addEventAdapter);
            }
        }

    }

}
