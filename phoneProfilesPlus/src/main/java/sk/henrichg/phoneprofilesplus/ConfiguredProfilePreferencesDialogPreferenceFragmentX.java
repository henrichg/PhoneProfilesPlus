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
import java.util.ArrayList;
import java.util.List;

public class ConfiguredProfilePreferencesDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ConfiguredProfilePreferencesDialogPreferenceX preference;

    private ListView listView;
    private LinearLayout linlaProgress;

    private ConfiguredProfilePreferencesAdapterX listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ConfiguredProfilePreferencesDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_configured_profile_preferences, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.configured_profile_preferences_pref_dlg_listview);
        linlaProgress = view.findViewById(R.id.configured_profile_preferences_pref_dlg_linla_progress);

        listAdapter = new ConfiguredProfilePreferencesAdapterX(prefContext, preference);

        refreshListView();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        preference.fragment = null;
    }

    void refreshListView() {
        asyncTask = new RefreshListViewAsyncTask(preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        List<ConfiguredProfilePreferencesData> _preferencesList = null;

        private final WeakReference<ConfiguredProfilePreferencesDialogPreferenceX> preferenceWeakRef;
        private final WeakReference<ConfiguredProfilePreferencesDialogPreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(ConfiguredProfilePreferencesDialogPreferenceX preference,
                                        ConfiguredProfilePreferencesDialogPreferenceFragmentX fragment,
                                        Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ConfiguredProfilePreferencesDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                fragment.listView.setVisibility(View.GONE);
                fragment.linlaProgress.setVisibility(View.VISIBLE);
            }

            _preferencesList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConfiguredProfilePreferencesDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ConfiguredProfilePreferencesDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                try {

                    DataWrapper dataWrapper = new DataWrapper(prefContext.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                    Profile profile = dataWrapper.getProfileById(preference.profile_id, false, false, false);
                    if (profile != null) {
                        //Log.e("----------- ConfiguredProfilePreferencesDialogPreferenceFragmentX.refreshListView", "profile._name="+profile._name);

                        ProfilePreferencesIndicator indicators = new ProfilePreferencesIndicator();
                        indicators.fillArrays(profile, false, /*false,*/ true, /*DataWrapper.IT_FOR_EDITOR,*/ prefContext);
                        indicators.fillArrays(profile, true, /*false,*/ true, /*DataWrapper.IT_FOR_EDITOR,*/ prefContext);

                        //Log.e("----------- ConfiguredProfilePreferencesDialogPreferenceFragmentX.refreshListView", "ProfilePreferencesIndicator.countDrawables="+ProfilePreferencesIndicator.countDrawables);

                        int idDrawable = 0;
                        for (int i = 0; i < indicators.countPreferences; i++) {
                            ConfiguredProfilePreferencesData configuredPreferences;
                            if (indicators.countItems[i] == 2) {
                                configuredPreferences = new ConfiguredProfilePreferencesData(
                                        indicators.drawables[idDrawable],
                                        indicators.drawables[idDrawable + 1],
                                        indicators.disabled[idDrawable],
                                        indicators.disabled[idDrawable + 1],
                                        indicators.strings[idDrawable] + " " + indicators.strings[idDrawable + 1],
                                        indicators.preferences[i]
                                );
                            } else {
                                configuredPreferences = new ConfiguredProfilePreferencesData(
                                        indicators.drawables[idDrawable],
                                        0,
                                        indicators.disabled[idDrawable],
                                        false,
                                        indicators.strings[idDrawable],
                                        indicators.preferences[i]
                                );
                            }
                            _preferencesList.add(configuredPreferences);
                            idDrawable += indicators.countItems[i];
                        }
                    }
                    if (_preferencesList.size() == 0) {
                        ConfiguredProfilePreferencesData configuredPreferences = new ConfiguredProfilePreferencesData(
                                0,
                                0,
                                false,
                                false,
                                "",
                                prefContext.getString(R.string.profile_preferences_savedProfilePreferences_notConfiguredAnyParameter)
                        );
                        _preferencesList.add(configuredPreferences);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ConfiguredProfilePreferencesDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ConfiguredProfilePreferencesDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.preferencesList = new ArrayList<>(_preferencesList);
                fragment.listView.setAdapter(fragment.listAdapter);

                fragment.linlaProgress.setVisibility(View.GONE);
                fragment.listView.setVisibility(View.VISIBLE);
            }
        }

    }

}
