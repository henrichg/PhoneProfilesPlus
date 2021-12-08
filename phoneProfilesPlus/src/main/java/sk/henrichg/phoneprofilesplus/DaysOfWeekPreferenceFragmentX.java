package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class DaysOfWeekPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private DaysOfWeekPreferenceX preference;

    private DaysOfWeekPreferenceAdapterX listAdapter;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (DaysOfWeekPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_days_of_week_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.days_of_week_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            DayOfWeek dayOfWeek = (DayOfWeek)listAdapter.getItem(position);
            dayOfWeek.toggleChecked();
            DayOfWeekViewHolder viewHolder = (DayOfWeekViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(dayOfWeek.checked);
        });

        listAdapter = new DaysOfWeekPreferenceAdapterX(prefContext, preference.daysOfWeekList);
        listView.setAdapter(listAdapter);

        preference.getValueDOWMDP();
    }


        @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

}
