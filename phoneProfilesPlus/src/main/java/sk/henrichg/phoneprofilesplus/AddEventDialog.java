package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

class AddEventDialog
{
    final EditorEventListFragment eventListFragment;

    final MaterialDialog mDialog;
    final Context context;

    private final LinearLayout linlaProgress;
    private final RelativeLayout rellaData;
    private final ListView listView;
    private final TextView help;

    AddEventDialog(Context context, EditorEventListFragment eventListFragment)
    {
        this.eventListFragment = eventListFragment;
        this.context = context;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                           .title(R.string.new_event_predefined_events_dialog)
                            //.disableDefaultFonts()
                            .negativeText(android.R.string.cancel)
                            .autoDismiss(true)
                            .customView(R.layout.activity_event_pref_dialog, false);

        dialogBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AddEventDialog.this.onShow(/*dialog*/);
            }
        });

        mDialog = dialogBuilder.build();
        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.event_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        rellaData = layout.findViewById(R.id.event_pref_dlg_rella_data);

        //noinspection ConstantConditions
        listView = layout.findViewById(R.id.event_pref_dlg_listview);
        //noinspection ConstantConditions
        help = layout.findViewById(R.id.event_pref_dlg_help);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {
        new AsyncTask<Void, Integer, Void>() {

            List<Event> eventList = new ArrayList<>();
            boolean profileNotExists = false;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                rellaData.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Event event;
                event = eventListFragment.activityDataWrapper.getNonInitializedEvent(context.getResources().getString(R.string.event_name_default), 0);
                eventList.add(event);
                for (int index = 0; index < 6; index++) {
                    event = eventListFragment.activityDataWrapper.getPredefinedEvent(index, false);
                    if (event._fkProfileStart == 0)
                        profileNotExists = true;
                    eventList.add(event);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                rellaData.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                if (!profileNotExists)
                    help.setVisibility(View.GONE);

                AddEventAdapter addEventAdapter = new AddEventAdapter(AddEventDialog.this, context, eventList);
                listView.setAdapter(addEventAdapter);
            }

        }.execute();
    }

    private void doOnItemSelected(int position)
    {
        eventListFragment.startEventPreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
