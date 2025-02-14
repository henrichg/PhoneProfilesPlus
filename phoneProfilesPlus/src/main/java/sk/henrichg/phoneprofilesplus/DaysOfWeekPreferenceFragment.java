package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class DaysOfWeekPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private DaysOfWeekPreference preference;

    private DaysOfWeekPreferenceAdapter listAdapter;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (DaysOfWeekPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_days_of_week_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.days_of_week_pref_dlg_listview);

        //noinspection DataFlowIssue
        listView.setOnItemClickListener((parent, item, position, id) -> {
            DayOfWeek dayOfWeek = (DayOfWeek)listAdapter.getItem(position);
            dayOfWeek.toggleChecked();
            DayOfWeekViewHolder viewHolder = (DayOfWeekViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(dayOfWeek.checked);
        });

        listAdapter = new DaysOfWeekPreferenceAdapter(prefContext, preference.daysOfWeekList);
        listView.setAdapter(listAdapter);

        preference.getValueDOWMDP();

        final Button allNothingButton = view.findViewById(R.id.days_of_week_pref_dlg_button_all_nothing);
        //noinspection DataFlowIssue
        allNothingButton.setOnClickListener(v -> {
            boolean allIsConfigured = false;
            boolean[] daySet = new boolean[7];

            preference.getValue();
            String[] splits = preference.value.split(StringConstants.STR_SPLIT_REGEX);
            if (!preference.value.isEmpty()) {
                for (String split : splits) {
                    if (split.equals(DaysOfWeekPreference.allValue)) {
                        for (int i = 0; i < 7; i++)
                            daySet[i] = true;
                        allIsConfigured = true;
                        break;
                    }
                    daySet[Integer.parseInt(split)] = true;
                }
            }
            if (!allIsConfigured) {
                allIsConfigured = true;
                for (int i = 0; i < 7; i++)
                    allIsConfigured = allIsConfigured && daySet[i];
            }

            if (allIsConfigured)
                preference.value="";
            else
                preference.value="0|1|2|3|4|5|6";

            preference.getValueDOWMDP();
            listAdapter.notifyDataSetChanged();
        });

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
