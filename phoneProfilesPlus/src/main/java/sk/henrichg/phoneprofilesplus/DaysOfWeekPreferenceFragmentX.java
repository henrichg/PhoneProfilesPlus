package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class DaysOfWeekPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    DaysOfWeekPreferenceX preference;

    private DaysOfWeekPreferenceAdapterX listAdapter;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (DaysOfWeekPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_days_of_week_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.days_of_week_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                DayOfWeek dayOfWeek = (DayOfWeek)listAdapter.getItem(position);
                dayOfWeek.toggleChecked();
                DayOfWeekViewHolder viewHolder = (DayOfWeekViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(dayOfWeek.checked);
            }
        });

        listAdapter = new DaysOfWeekPreferenceAdapterX(prefContext, preference.daysOfWeekList);
        listView.setAdapter(listAdapter);
    }


        @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        preference.fragment = null;
    }

}
