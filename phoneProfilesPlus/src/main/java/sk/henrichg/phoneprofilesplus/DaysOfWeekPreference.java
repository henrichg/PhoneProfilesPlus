package sk.henrichg.phoneprofilesplus;


import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class DaysOfWeekPreference extends DialogPreference {

	public static final String allValue = "#ALL#";

    Context _context;
    String value = "";

    List<DayOfWeek> daysOfWeekList = null;

    // Layout widgets.
    private ListView listView = null;

    private DaysOfWeekPreferenceAdapter listAdapter;


    public DaysOfWeekPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        daysOfWeekList = new ArrayList<DayOfWeek>();

        CharSequence[] newEntries = new CharSequence[8];
        CharSequence[] newEntryValues = new CharSequence[8];

        /*
        String[] newEntries = _context.getResources().getStringArray(R.array.daysOfWeekArray);
        String[] newEntryValues = _context.getResources().getStringArray(R.array.daysOfWeekValues);
        */

        daysOfWeekList.clear();
        DayOfWeek dayOfWeek = new DayOfWeek();
        dayOfWeek.name = _context.getString(R.string.array_pref_event_all);
        dayOfWeek.value = allValue;
        daysOfWeekList.add(dayOfWeek);

        String[] namesOfDay = DateFormatSymbols.getInstance().getWeekdays();

        int _dayOfWeek;
        for (int i = 1; i < 8; i++)
        {
            _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);

            dayOfWeek = new DayOfWeek();
            dayOfWeek.name = namesOfDay[_dayOfWeek+1];
            dayOfWeek.value = String.valueOf(_dayOfWeek);
            daysOfWeekList.add(dayOfWeek);
        }

    }

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                .disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .callback(callback)
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_days_of_week_pref_dialog, null);
        onBindDialogView(layout);

        listView = (ListView)layout.findViewById(R.id.days_of_week_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                DayOfWeek dayOfWeek = (DayOfWeek)listAdapter.getItem(position);
                dayOfWeek.toggleChecked();
                DayOfWeekViewHolder viewHolder = (DayOfWeekViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(dayOfWeek.checked);
            }
        });

        listAdapter = new DaysOfWeekPreferenceAdapter(_context, daysOfWeekList);
        listView.setAdapter(listAdapter);

        mBuilder.customView(layout, false);

        /*
        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                DaysOfWeekMDPreference.this.onShow(dialog);
            }
        });
        */

        MaterialDialog mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    /*
    public void onShow(DialogInterface dialog) {

        CharSequence[] newEntries = new CharSequence[8];
        CharSequence[] newEntryValues = new CharSequence[8];

        daysOfWeekList.clear();
        DayOfWeek dayOfWeek = new DayOfWeek();
        dayOfWeek.name = _context.getString(R.string.array_pref_event_all);
        dayOfWeek.value = allValue;
        daysOfWeekList.add(dayOfWeek);

        String[] namesOfDay = DateFormatSymbols.getInstance().getWeekdays();

        int _dayOfWeek;
        for (int i = 1; i < 8; i++)
        {
            _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);

            dayOfWeek = new DayOfWeek();
            dayOfWeek.name = namesOfDay[_dayOfWeek+1];
            dayOfWeek.value = String.valueOf(_dayOfWeek);
            daysOfWeekList.add(dayOfWeek);
        }

        getValueDOWMDP();

        listAdapter = new DaysOfWeekPreferenceAdapter(_context, daysOfWeekList);
        listView.setAdapter(listAdapter);

    }
    */

    private final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog dialog) {
        if (shouldPersist())
        {
            // sem narvi stringy skupin kontatkov oddelenych |
            value = "";
            if (daysOfWeekList != null)
            {
                for (DayOfWeek dayOfWeek : daysOfWeekList)
                {
                    if (dayOfWeek.checked)
                    {
                        if (!value.isEmpty())
                            value = value + "|";
                        value = value + dayOfWeek.value;
                    }
                }
            }
            persistString(value);

            setSummaryDOWMDP();
        }
        }
    };

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            // restore state
            getValueDOWMDP();
        }
        else {
            // set state
            // sem narvi default string skupin kontaktov oddeleny |
            value = "";
            persistString("");
        }
        setSummaryDOWMDP();
    }

    private void getValueDOWMDP()
    {
        // Get the persistent value
        value = getPersistedString(value);

        // change checked state by value
        if (daysOfWeekList != null)
        {
            String[] splits = value.split("\\|");
            for (DayOfWeek dayOfWeek : daysOfWeekList)
            {
                dayOfWeek.checked = false;
                for (int i = 0; i < splits.length; i++)
                {
                    if (dayOfWeek.value.equals(splits[i]))
                        dayOfWeek.checked = true;
                }
            }
        }
    }

    private void setSummaryDOWMDP()
    {
        String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();

        String summary = "";

        if ((daysOfWeekList != null) && (daysOfWeekList.size() > 0))
        {
            if (daysOfWeekList.get(0).checked)
            {
                for ( int i = 1; i <= namesOfDay.length; i++ )
                    summary = summary + namesOfDay[EventPreferencesTime.getDayOfWeekByLocale(i-1)+1] + " ";
            }
            else
            {
                for ( int i = 1; i < daysOfWeekList.size(); i++ )
                {
                    DayOfWeek dayOfWeek = daysOfWeekList.get(i);
                    if (dayOfWeek.checked)
                        summary = summary + namesOfDay[EventPreferencesTime.getDayOfWeekByLocale(i-1)+1] + " ";
                }
            }
        }

        setSummary(summary);
    }

}
