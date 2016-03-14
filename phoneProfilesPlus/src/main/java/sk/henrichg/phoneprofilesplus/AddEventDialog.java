package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AddEventDialog
{
    private AddEventAdapter addEventAdapter;

    public List<Event> eventList;

    private Context _context;
    public EditorEventListFragment eventListFragment;

    private AppCompatDialog mDialog;
    private ListView listView;

    public AddEventDialog(Context context, EditorEventListFragment eventListFragment)
    {
        _context = context;
        this.eventListFragment = eventListFragment;

        eventList = new ArrayList<Event>();

        boolean monochrome = false;
        int monochromeValue = 0xFF;

        Event event;
        event = eventListFragment.dataWrapper.getNoinitializedEvent(context.getResources().getString(R.string.event_name_default));
        //event.generatePreferencesIndicator(context, monochrome, monochromeValue);
        eventList.add(event);
        boolean profileNotExists = false;
        for (int index = 0; index < 5; index++) {
            event = eventListFragment.dataWrapper.getDefaultEvent(index, false);
            //profile.generateIconBitmap(context, monochrome, monochromeValue);
            //profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            if (event._fkProfileStart == 0)
                profileNotExists = true;
            eventList.add(event);
        }

        mDialog = new AppCompatDialog(context);
        mDialog.setTitle(R.string.new_event_predefined_events_dialog);
        mDialog.setContentView(R.layout.activity_event_pref_dialog);

        listView = (ListView)mDialog.findViewById(R.id.event_pref_dlg_listview);
        TextView help = (TextView)mDialog.findViewById(R.id.event_pref_dlg_help);
        if (!profileNotExists)
            help.setVisibility(View.GONE);

        addEventAdapter = new AddEventAdapter(this, _context, eventList);
        listView.setAdapter(addEventAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    public void doOnItemSelected(int position)
    {
        eventListFragment.startEventPreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
