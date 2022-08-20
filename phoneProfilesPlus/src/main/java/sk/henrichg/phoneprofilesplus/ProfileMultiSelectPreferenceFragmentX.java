package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class ProfileMultiSelectPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private LinearLayout linlaProgress;
    private ListView listView;
    private LinearLayout listViewRoot;
    private ProfileMultiSelectPreferenceAdapterX profilePreferenceAdapter;

    private Context prefContext;
    private ProfileMultiSelectPreferenceX preference;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ProfileMultiSelectPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_profile_multiselect_preferences, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.profile_multiselect_pref_dlg_linla_progress);

        listViewRoot = view.findViewById(R.id.profile_multiselect_pref_dlg_listview_root);
        listView = view.findViewById(R.id.profile_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            Profile profile = (Profile)profilePreferenceAdapter.getItem(position);
            profile._checked = !profile._checked;
            ProfilesViewHolder viewHolder = (ProfilesViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(profile._checked);
        });

        final Button unselectAllButton = view.findViewById(R.id.profile_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        refreshListView(true);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING)){
            asyncTask.cancel(true);
        }

        preference.fragment = null;
    }

    private static class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }

    private void getValuePMSDP(boolean notForUnselect)
    {
        synchronized (preference.dataWrapper.profileList) {
            for (Profile profile : preference.dataWrapper.profileList)
                profile._checked = false;

            if (notForUnselect) {
                if (!preference.value.isEmpty()) {
                    String[] splits = preference.value.split("\\|");
                    for (String split : splits) {
                        Profile profile = preference.dataWrapper.getProfileById(Long.parseLong(split), false, false, false);
                        if (profile != null)
                            profile._checked = true;
                    }
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

    void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect,
                preference, this, prefContext) ;
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean notForUnselect;

        private final WeakReference<ProfileMultiSelectPreferenceX> preferenceWeakRef;
        private final WeakReference<ProfileMultiSelectPreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ProfileMultiSelectPreferenceX preference,
                                        ProfileMultiSelectPreferenceFragmentX fragment,
                                        Context prefContext) {
            this.notForUnselect = notForUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            ProfileMultiSelectPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.listViewRoot.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ProfileMultiSelectPreferenceFragmentX fragment = fragmentWeakRef.get();
            ProfileMultiSelectPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
                synchronized (preference.dataWrapper.profileList) {
                    preference.dataWrapper.profileList.sort(new AlphabeticallyComparator());
                }

                fragment.getValuePMSDP(notForUnselect);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ProfileMultiSelectPreferenceFragmentX fragment = fragmentWeakRef.get();
            ProfileMultiSelectPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (notForUnselect) {
                    fragment.listViewRoot.setVisibility(View.VISIBLE);
                    fragment.linlaProgress.setVisibility(View.GONE);
                }

                fragment.profilePreferenceAdapter = new ProfileMultiSelectPreferenceAdapterX(fragment, prefContext, preference.dataWrapper.profileList);
                fragment.listView.setAdapter(fragment.profilePreferenceAdapter);
            }
        }

    }

}
