package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class ProfileMultiSelectPreferenceFragment extends PreferenceDialogFragmentCompat {

    private LinearLayout linlaProgress;
    private ListView listView;
    private LinearLayout listViewRoot;
    RelativeLayout emptyList;
    private Button unselectAllButton;

    private ProfileMultiSelectPreferenceAdapter profilePreferenceAdapter;

    private Context prefContext;
    private ProfileMultiSelectPreference preference;

    private RefreshListViewAsyncTask asyncTask = null;

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
        preference = (ProfileMultiSelectPreference) getPreference();
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
        emptyList = view.findViewById(R.id.profile_multiselect_pref_dlg_empty);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            Profile profile = (Profile)profilePreferenceAdapter.getItem(position);
            profile._checked = !profile._checked;
            ProfilesViewHolder viewHolder = (ProfilesViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(profile._checked);
        });

        unselectAllButton = view.findViewById(R.id.profile_multiselect_pref_dlg_unselect_all);
        //noinspection DataFlowIssue
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        if (preference.dataWrapper != null)
            preference.dataWrapper.invalidateProfileList();
        if (profilePreferenceAdapter != null)
            profilePreferenceAdapter.notifyDataSetChanged();
        final Handler handler = new Handler(prefContext.getMainLooper());
        final WeakReference<ProfileMultiSelectPreferenceFragment> fragmentWeakRef
                = new WeakReference<>(this);
        handler.postDelayed(() -> {
            ProfileMultiSelectPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null)
                fragment.refreshListView(true);
        }, 200);

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
        asyncTask = null;

        if (preference.dataWrapper != null)
            preference.dataWrapper.invalidateProfileList();

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
//        PPApplicationStatic.logE("[SYNCHRONIZED] ProfileMultiSelectPreferenceFragment.getValuePMSDP", "DataWrapper.profileList");
        synchronized (preference.dataWrapper.profileList) {
            for (Profile profile : preference.dataWrapper.profileList)
                profile._checked = false;

            if (notForUnselect) {
                if (!preference.value.isEmpty()) {
                    String[] splits = preference.value.split(StringConstants.STR_SPLIT_REGEX);
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

        private final WeakReference<ProfileMultiSelectPreference> preferenceWeakRef;
        private final WeakReference<ProfileMultiSelectPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ProfileMultiSelectPreference preference,
                                        ProfileMultiSelectPreferenceFragment fragment,
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
            ProfileMultiSelectPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.listViewRoot.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                    fragment.unselectAllButton.setEnabled(false);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ProfileMultiSelectPreferenceFragment fragment = fragmentWeakRef.get();
            ProfileMultiSelectPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
//                PPApplicationStatic.logE("[SYNCHRONIZED] ProfileMultiSelectPreferenceFragment.RefreshListViewAsyncTask", "DataWrapper.profileList");
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

            ProfileMultiSelectPreferenceFragment fragment = fragmentWeakRef.get();
            ProfileMultiSelectPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    if (notForUnselect) {
                        fragment.listViewRoot.setVisibility(View.VISIBLE);
                    }

                    if (preference.dataWrapper.profileList.isEmpty()) {
                        fragment.listView.setVisibility(View.GONE);
                        fragment.emptyList.setVisibility(View.VISIBLE);
                    } else {
                        fragment.emptyList.setVisibility(View.GONE);
                        fragment.listView.setVisibility(View.VISIBLE);
                    }

                    fragment.profilePreferenceAdapter = new ProfileMultiSelectPreferenceAdapter(fragment, prefContext, preference.dataWrapper.profileList);
                    fragment.listView.setAdapter(fragment.profilePreferenceAdapter);

                    fragment.unselectAllButton.setEnabled(true);
                });
            }
        }

    }

}
