package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class ProfilePreferenceFragment extends PreferenceDialogFragmentCompat {

    private LinearLayout linlaProgress;
    private ListView listView;
    RelativeLayout emptyList;

    private ProfilePreferenceAdapter profilePreferenceAdapter;

    private Context prefContext;
    ProfilePreference preference;

    private BindViewAsyncTask bindViewAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ProfilePreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_profile_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.profile_pref_dlg_linla_progress);

        listView = view.findViewById(R.id.profile_pref_dlg_listview);
        emptyList = view.findViewById(R.id.profile_pref_dlg_empty);

        listView.setOnItemClickListener((parent, item, position, id) -> doOnItemSelected(position));

        if (preference.dataWrapper != null)
            preference.dataWrapper.invalidateProfileList();
        if (profilePreferenceAdapter != null)
            profilePreferenceAdapter.notifyDataSetChanged();

        final Handler handler = new Handler(prefContext.getMainLooper());
        final WeakReference<ProfilePreference> preferenceWeakRef
                = new WeakReference<>(preference);
        final WeakReference<Context> prefContextWeakRef
                = new WeakReference<>(prefContext);
        handler.postDelayed(() -> {
            ProfilePreference _preference = preferenceWeakRef.get();
            Context _context = prefContextWeakRef.get();
            if ((_context != null) && (_preference != null)) {
                _preference.fragment.bindViewAsyncTask = new BindViewAsyncTask(_preference, _preference.fragment, _context);
                _preference.fragment.bindViewAsyncTask.execute();
            }
        }, 200);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((bindViewAsyncTask != null) &&
                bindViewAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            bindViewAsyncTask.cancel(true);
        bindViewAsyncTask = null;

        if (preference.dataWrapper != null)
            preference.dataWrapper.invalidateProfileList();

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

        private final WeakReference<ProfilePreference> preferenceWeakRef;
        private final WeakReference<ProfilePreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public BindViewAsyncTask(ProfilePreference preference,
                                 ProfilePreferenceFragment fragment,
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
            ProfilePreference preference = preferenceWeakRef.get();
            if (preference != null) {
                preference.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
//                PPApplicationStatic.logE("[SYNCHRONIZED] ProfilePreferenceFragment.BindViewAsyncTask", "DataWrapper.profileList");
                synchronized (preference.dataWrapper.profileList) {
                    preference.dataWrapper.profileList.sort(new AlphabeticallyComparator());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ProfilePreferenceFragment fragment = fragmentWeakRef.get();
            ProfilePreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.listView.setVisibility(View.VISIBLE);

                    if ((preference.addNoActivateItem != 1) && (preference.dataWrapper.profileList.isEmpty())) {
                        fragment.listView.setVisibility(View.GONE);
                        fragment.emptyList.setVisibility(View.VISIBLE);
                    } else {
                        fragment.emptyList.setVisibility(View.GONE);
                        fragment.listView.setVisibility(View.VISIBLE);
                    }

                    fragment.profilePreferenceAdapter = new ProfilePreferenceAdapter(fragment, prefContext, preference.profileId, preference.dataWrapper.profileList);
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
                });
            }
        }

    }

}
