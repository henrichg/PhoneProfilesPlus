package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;

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
        return inflater.inflate(R.layout.dialog_profile_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.profile_pref_dlg_linla_progress);

        listView = view.findViewById(R.id.profile_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> doOnItemSelected(position));

        new BindViewAsyncTask(preference, this, prefContext).execute();

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

    private static class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }

    private static class BindViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<ProfilePreferenceX> preferenceWeakRef;
        private final WeakReference<ProfilePreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public BindViewAsyncTask(ProfilePreferenceX preference,
                                 ProfilePreferenceFragmentX fragment,
                                 Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        /*@Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //listView.setVisibility(View.GONE);
            //linlaProgress.setVisibility(View.VISIBLE);
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            ProfilePreferenceX preference = preferenceWeakRef.get();
            if (preference != null) {
                preference.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
                synchronized (preference.dataWrapper.profileList) {
                    //noinspection Java8ListSort
                    Collections.sort(preference.dataWrapper.profileList, new AlphabeticallyComparator());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ProfilePreferenceFragmentX fragment = fragmentWeakRef.get();
            ProfilePreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //listView.setVisibility(View.VISIBLE);
                fragment.linlaProgress.setVisibility(View.GONE);

                fragment.profilePreferenceAdapter = new ProfilePreferenceAdapterX(fragment, prefContext, preference.profileId, preference.dataWrapper.profileList);
                fragment.listView.setAdapter(fragment.profilePreferenceAdapter);

                int position;
                long iProfileId;
                if (preference.profileId.isEmpty())
                    iProfileId = 0;
                else
                    iProfileId = Long.parseLong(preference.profileId);
                if ((preference.addNoActivateItem == 1) && (iProfileId == Profile.PROFILE_NO_ACTIVATE))
                    position = 0;
                else {
                    boolean found = false;
                    position = 0;
                    for (Profile profile : preference.dataWrapper.profileList) {
                        if (profile._id == iProfileId) {
                            found = true;
                            break;
                        }
                        position++;
                    }
                    if (found) {
                        if (preference.addNoActivateItem == 1)
                            position++;
                    } else
                        position = 0;
                }
                fragment.listView.setSelection(position);
            }
        }

    }

}
