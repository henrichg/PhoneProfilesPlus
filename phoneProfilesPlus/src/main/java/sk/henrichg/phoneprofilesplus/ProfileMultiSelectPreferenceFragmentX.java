package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;

import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class ProfileMultiSelectPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private LinearLayout linlaProgress;
    private ListView listView;
    private ProfileMultiSelectPreferenceAdapterX profilePreferenceAdapter;

    private Context prefContext;
    private ProfileMultiSelectPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ProfileMultiSelectPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_profile_multiselect_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.profile_multiselect_pref_dlg_linla_progress);

        listView = view.findViewById(R.id.profile_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                Profile profile = (Profile)profilePreferenceAdapter.getItem(position);
                profile._checked = !profile._checked;
                ProfilesViewHolder viewHolder = (ProfilesViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(profile._checked);
            }
        });

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {

                preference.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator(preference.dataWrapper.context));
                Collections.sort(preference.dataWrapper.profileList, new ProfileMultiSelectPreferenceFragmentX.AlphabeticallyComparator());

                getValuePMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                profilePreferenceAdapter = new ProfileMultiSelectPreferenceAdapterX(prefContext, preference.dataWrapper.profileList);
                listView.setAdapter(profilePreferenceAdapter);
            }

        }.execute();

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

    private class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }

    private void getValuePMSDP()
    {
        //PPApplication.logE("ProfileMultiSelectPreferenceX.getValueAMSDP","value="+preference.value);

        for (Profile profile : preference.dataWrapper.profileList)
            profile._checked = false;

        if (!preference.value.isEmpty()) {
            String[] splits = preference.value.split("\\|");
            for (String split : splits) {
                Profile profile = preference.dataWrapper.getProfileById(Long.parseLong(split), false, false, false);
                if (profile != null)
                    profile._checked = true;
            }
        }

        // move checked on top
        int i = 0;
        int ich = 0;
        while (i < preference.dataWrapper.profileList.size()) {
            Profile profile = preference.dataWrapper.profileList.get(i);
            if (profile._checked) {
                preference.dataWrapper.profileList.remove(i);
                preference.dataWrapper.profileList.add(ich, profile);
                ich++;
            }
            i++;
        }

    }

}
