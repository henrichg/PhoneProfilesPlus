package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

class AddEventDialog
{
    EditorEventListFragment eventListFragment;

    MaterialDialog mDialog;

    AddEventDialog(Context context, EditorEventListFragment eventListFragment)
    {
        this.eventListFragment = eventListFragment;

        List<Event> eventList = new ArrayList<>();

        //boolean monochrome = false;
        //int monochromeValue = 0xFF;

        Event event;
        event = eventListFragment.dataWrapper.getNonInitializedEvent(context.getResources().getString(R.string.event_name_default), 0);
        //event.generatePreferencesIndicator(context, monochrome, monochromeValue);
        eventList.add(event);
        boolean profileNotExists = false;
        for (int index = 0; index < 6; index++) {
            event = eventListFragment.dataWrapper.getPredefinedEvent(index, false);
            //profile.generateIconBitmap(context, monochrome, monochromeValue);
            //profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            if (event._fkProfileStart == 0)
                profileNotExists = true;
            eventList.add(event);
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                           .title(R.string.new_event_predefined_events_dialog)
                            //.disableDefaultFonts()
                            .negativeText(android.R.string.cancel)
                            .autoDismiss(true)
                            .customView(R.layout.activity_event_pref_dialog, false);

        mDialog = dialogBuilder.build();

        ListView listView = mDialog.getCustomView().findViewById(R.id.event_pref_dlg_listview);
        TextView help = mDialog.getCustomView().findViewById(R.id.event_pref_dlg_help);
        if (!profileNotExists)
            help.setVisibility(View.GONE);

        AddEventAdapter addEventAdapter = new AddEventAdapter(this, context, eventList);
        listView.setAdapter(addEventAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

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
