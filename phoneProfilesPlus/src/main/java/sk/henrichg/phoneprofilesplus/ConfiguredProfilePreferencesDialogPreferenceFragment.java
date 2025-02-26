package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
public class ConfiguredProfilePreferencesDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ConfiguredProfilePreferencesDialogPreference preference;

    private ListView listView;
    //private LinearLayout linlaProgress;
    //private ProgressBar progressBar;

    private ConfiguredProfilePreferencesAdapter listAdapter;

    private View buttonsDivider;

    private RefreshListViewAsyncTask asyncTask = null;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        prefContext = context;
        preference = (ConfiguredProfilePreferencesDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_configured_profile_preferences, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        buttonsDivider = view.findViewById(R.id.profile_pref_dlg_listview_buttonBarDivider);
        listView = view.findViewById(R.id.configured_profile_preferences_pref_dlg_listview);
        //linlaProgress = view.findViewById(R.id.configured_profile_preferences_pref_dlg_linla_progress);
        //progressBar = view.findViewById(R.id.configured_profile_preferences_pref_dlg_progress);

        listAdapter = new ConfiguredProfilePreferencesAdapter(prefContext, preference);

        refreshListView();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING)){
            asyncTask.cancel(true);
        }
        asyncTask = null;

        preference.fragment = null;
    }

    void refreshListView() {
        asyncTask = new RefreshListViewAsyncTask(preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        List<ConfiguredProfilePreferencesData> _preferencesList = null;

        private final WeakReference<ConfiguredProfilePreferencesDialogPreference> preferenceWeakRef;
        private final WeakReference<ConfiguredProfilePreferencesDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(ConfiguredProfilePreferencesDialogPreference preference,
                                        ConfiguredProfilePreferencesDialogPreferenceFragment fragment,
                                        Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //ConfiguredProfilePreferencesDialogPreferenceFragment fragment = fragmentWeakRef.get();
            //if (fragment != null) {
                //fragment.listView.setVisibility(View.GONE);
                //fragment.linlaProgress.setVisibility(View.VISIBLE);
                //GlobalGUIRoutines.setProgressBarVisible(fragment.linlaProgress, fragment.progressBar);
            //}

            _preferencesList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConfiguredProfilePreferencesDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ConfiguredProfilePreferencesDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                try {

                    DataWrapper dataWrapper = new DataWrapper(prefContext.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                    Profile profile = dataWrapper.getProfileById(preference.profile_id, false, false, false);
                    dataWrapper.invalidateDataWrapper();
                    if (profile != null) {
                        ProfilePreferencesIndicator indicators = new ProfilePreferencesIndicator();
                        indicators.fillArrays(profile, false, /*false,*/ true, /*DataWrapper.IT_FOR_EDITOR,*/ prefContext);
                        indicators.fillArrays(profile, true, /*false,*/ true, /*DataWrapper.IT_FOR_EDITOR,*/ prefContext);

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
                    if (_preferencesList.isEmpty()) {
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
                    PPApplicationStatic.recordException(e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ConfiguredProfilePreferencesDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ConfiguredProfilePreferencesDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.preferencesList = new ArrayList<>(_preferencesList);
                fragment.listView.setAdapter(fragment.listAdapter);

                //fragment.linlaProgress.setVisibility(View.GONE);
                //fragment.listView.setVisibility(View.VISIBLE);

                if (preference.preferencesList.size() <= 1)
                    fragment.buttonsDivider.setVisibility(View.GONE);
                else
                    fragment.buttonsDivider.setVisibility(View.VISIBLE);
            }
        }

    }

}
