package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class AddEventDialog
{
    private AddEventAdapter addEventAdapter;

    public List<Event> eventList;

    private Context _context;
    public EditorEventListFragment eventListFragment;

    private MaterialDialog mDialog;
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
        for (int index = 0; index < 5; index++) {
            event = eventListFragment.dataWrapper.getDefaultEvent(index, false);
            //profile.generateIconBitmap(context, monochrome, monochromeValue);
            //profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            eventList.add(event);
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.new_event_predefined_events_dialog)
                //.disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.activity_event_pref_dialog, false);

        mDialog = dialogBuilder.build();

        listView = (ListView)mDialog.getCustomView().findViewById(R.id.event_pref_dlg_listview);

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
