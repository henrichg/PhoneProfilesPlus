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

public class ProfilePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private LinearLayout linlaProgress;
    private ListView listView;
    private ProfilePreferenceAdapterX profilePreferenceAdapter;

    private Context prefContext;
    ProfilePreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ProfilePreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_profile_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.profile_pref_dlg_linla_progress);

        listView = view.findViewById(R.id.profile_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                doOnItemSelected(position);
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
                Collections.sort(preference.dataWrapper.profileList, new AlphabeticallyComparator());

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                profilePreferenceAdapter = new ProfilePreferenceAdapterX(ProfilePreferenceFragmentX.this, prefContext, preference.profileId, preference.dataWrapper.profileList);
                listView.setAdapter(profilePreferenceAdapter);

                int position;
                long iProfileId;
                if (preference.profileId.isEmpty())
                    iProfileId = 0;
                else
                    iProfileId = Long.valueOf(preference.profileId);
                if ((preference.addNoActivateItem == 1) && (iProfileId == Profile.PROFILE_NO_ACTIVATE))
                    position = 0;
                else
                {
                    boolean found = false;
                    position = 0;
                    for (Profile profile : preference.dataWrapper.profileList)
                    {
                        if (profile._id == iProfileId)
                        {
                            found = true;
                            break;
                        }
                        position++;
                    }
                    if (found)
                    {
                        if (preference.addNoActivateItem == 1)
                            position++;
                    }
                    else
                        position = 0;
                }
                listView.setSelection(position);
            }

        }.execute();

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

    void doOnItemSelected(int position)
    {
        if (preference.addNoActivateItem == 1)
        {
            long profileId;
            if (position == 0)
                profileId = Profile.PROFILE_NO_ACTIVATE;
            else
                profileId = profilePreferenceAdapter.profileList.get(position-1)._id;
            preference.setProfileId(profileId);
        }
        else
            preference.setProfileId(profilePreferenceAdapter.profileList.get(position)._id);
        dismiss();
    }

    private class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }

}
