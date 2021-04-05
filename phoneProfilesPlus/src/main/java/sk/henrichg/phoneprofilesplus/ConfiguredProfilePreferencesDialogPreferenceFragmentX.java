package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ConfiguredProfilePreferencesDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ConfiguredProfilePreferencesDialogPreferenceX preference;

    private ListView listView;
    private LinearLayout linlaProgress;

    private ConfiguredProfilePreferencesAdapterX listAdapter;

    @SuppressWarnings("rawtypes")
    private AsyncTask asyncTask = null;

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
        asyncTask = new AsyncTask<Void, Integer, Void>() {

            List<ConfiguredProfilePreferencesData> _preferencesList = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);

                _preferencesList = new ArrayList<>();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    DataWrapper dataWrapper = new DataWrapper(prefContext.getApplicationContext(), false, 0, false);
                    Profile profile = dataWrapper.getProfileById(preference.profile_id, false, false, false);
                    if (profile != null) {
                        //Log.e("----------- ConfiguredProfilePreferencesDialogPreferenceFragmentX.refreshListView", "profile._name="+profile._name);

                        ProfilePreferencesIndicator indicators = new ProfilePreferencesIndicator();
                        indicators.fillArrays(profile, false, false, true, prefContext);
                        indicators.fillArrays(profile, true, false, true, prefContext);

                        //Log.e("----------- ConfiguredProfilePreferencesDialogPreferenceFragmentX.refreshListView", "ProfilePreferencesIndicator.countDrawables="+ProfilePreferencesIndicator.countDrawables);

                        int idDrawable = 0;
                        for (int i = 0; i < indicators.countPreferences; i++) {
                            ConfiguredProfilePreferencesData configuredPreferences;
                            if (indicators.countItems[i] == 2) {
                                configuredPreferences = new ConfiguredProfilePreferencesData(
                                        indicators.drawables[idDrawable],
                                        indicators.drawables[idDrawable + 1],
                                        indicators.strings[idDrawable] + " " + indicators.strings[idDrawable + 1],
                                        indicators.preferences[i]
                                );
                            } else {
                                configuredPreferences = new ConfiguredProfilePreferencesData(
                                        indicators.drawables[idDrawable],
                                        0,
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
                                getString(R.string.profile_preferences_savedProfilePreferences_notConfiguredAnyPreference),
                               ""
                        );
                        _preferencesList.add(configuredPreferences);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                preference.preferencesList = new ArrayList<>(_preferencesList);
                listView.setAdapter(listAdapter);

                linlaProgress.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }

        }.execute();
    }

}
