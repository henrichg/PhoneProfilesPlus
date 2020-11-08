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
                    //Log.e("----------- ConfiguredProfilePreferencesDialogPreferenceFragmentX.refreshListView", "profile._name="+profile._name);

                    ProfilePreferencesIndicator.fillArrays(profile, false, false, true, prefContext);
                    ProfilePreferencesIndicator.fillArrays(profile, true, false, true, prefContext);

                    //Log.e("----------- ConfiguredProfilePreferencesDialogPreferenceFragmentX.refreshListView", "ProfilePreferencesIndicator.countDrawables="+ProfilePreferencesIndicator.countDrawables);

                    int idDrawable = 0;
                    for (int i = 0; i < ProfilePreferencesIndicator.countPreferences; i++) {
                        ConfiguredProfilePreferencesData configuredPreferences;
                        if (ProfilePreferencesIndicator.countItems[i] == 2) {
                            configuredPreferences = new ConfiguredProfilePreferencesData(
                                    ProfilePreferencesIndicator.drawables[idDrawable],
                                    ProfilePreferencesIndicator.drawables[idDrawable+1],
                                    ProfilePreferencesIndicator.strings[idDrawable] + " " + ProfilePreferencesIndicator.strings[idDrawable+1],
                                    ProfilePreferencesIndicator.preferences[i]
                            );
                        }
                        else {
                            configuredPreferences = new ConfiguredProfilePreferencesData(
                                    ProfilePreferencesIndicator.drawables[idDrawable],
                                    0,
                                    ProfilePreferencesIndicator.strings[idDrawable],
                                    ProfilePreferencesIndicator.preferences[i]
                            );
                        }
                        _preferencesList.add(configuredPreferences);
                        idDrawable += ProfilePreferencesIndicator.countItems[i];
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
