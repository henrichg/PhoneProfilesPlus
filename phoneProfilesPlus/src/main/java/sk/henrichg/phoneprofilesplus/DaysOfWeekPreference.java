package sk.henrichg.phoneprofilesplus;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import androidx.appcompat.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class DaysOfWeekPreference extends DialogPreference {

    static final String allValue = "#ALL#";

    private final Context _context;
    private String value = "";

    private final List<DayOfWeek> daysOfWeekList;

    private AlertDialog mDialog;
    private DaysOfWeekPreferenceAdapter listAdapter;


    public DaysOfWeekPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        daysOfWeekList = new ArrayList<>();

        //CharSequence[] newEntries = new CharSequence[8];
        //CharSequence[] newEntryValues = new CharSequence[8];

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
        getValueDOWMDP();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist())
                {
                    // fill with days of week separated with |
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
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_days_of_week_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        //noinspection ConstantConditions
        ListView listView = layout.findViewById(R.id.days_of_week_pref_dlg_listview);

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

        /*
        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                DaysOfWeekMDPreference.this.onShow(dialog);
            }
        });
        */

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)_context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            // restore state
            getValueDOWMDP();
        }
        else {
            // set state
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
                for (String split : splits) {
                    if (dayOfWeek.value.equals(split))
                        dayOfWeek.checked = true;
                }
            }
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
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
