package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class LocationGeofencePreferenceFragment extends PreferenceDialogFragmentCompat {

    LocationGeofencePreference preference;

    private RelativeLayout locationSystemSettingsRelLa;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;

    private Context prefContext;

    private LocationGeofencesPreferenceAdapter listAdapter;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (LocationGeofencePreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_location_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        /*if (preference.onlyEdit == 0) {
            String value = preference.getPersistedGeofence();
            DatabaseHandler.getInstance(prefContext.getApplicationContext()).checkGeofence(value, 1);
        }
        else
            preference.setNegativeButtonText(null);*/

        AppCompatImageButton addButton = view.findViewById(R.id.location_pref_dlg_add);
        TooltipCompat.setTooltipText(addButton, getString(R.string.location_pref_dlg_add_button_tooltip));

        ListView geofencesListView = view.findViewById(R.id.location_pref_dlg_listview);

        listAdapter = new LocationGeofencesPreferenceAdapter(prefContext, DatabaseHandler.getInstance(prefContext.getApplicationContext()).getGeofencesCursor(), this);
        geofencesListView.setAdapter(listAdapter);

        geofencesListView.setOnItemClickListener((parent, v, position, id) -> {
            LocationGeofencesPreferenceAdapter.ViewHolder viewHolder =
                    (LocationGeofencesPreferenceAdapter.ViewHolder) v.getTag();

            /*
            if (listAdapter.selectedRB != null){
                long gid = (long)listAdapter.selectedRB.getTag();
                Log.d("LocationGeofencePreference.onItemClick", "checked id="+gid);
                listAdapter.selectedRB.setChecked(false);
            }
            */
            //listAdapter.selectedRB = viewHolder.radioButton;

            long gid = viewHolder.geofenceId;
            if (preference.onlyEdit == 0) {
                DatabaseHandler.getInstance(prefContext.getApplicationContext()).checkGeofence(String.valueOf(gid), 2);
                //viewHolder.radioButton.setChecked(true);
                //updateGUIWithGeofence(gid);
                preference.refreshListView();
            }
            else {
                //startEditor(gid);
                startEditorOSM(gid);
            }

        });

        /*
        final TextView helpText = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

        ImageView helpIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE)
                    visibility = View.GONE;
                else
                    visibility = View.VISIBLE;
                helpText.setVisibility(visibility);
            }
        });
        */

        //addButton.setOnClickListener(v -> startEditor(0));
        addButton.setOnClickListener(v -> startEditorOSM(0));

        final Button unselectAllButton = view.findViewById(R.id.location_pref_dlg_unselectAll);
        if (preference.onlyEdit == 0) {
            unselectAllButton.setOnClickListener(v -> {
                // clear all checks
                DatabaseHandler.getInstance(prefContext.getApplicationContext()).checkGeofence("", 0);
                preference.refreshListView();
            });
        }
        else {
            unselectAllButton.setVisibility(View.GONE);
        }

        locationSystemSettingsRelLa = view.findViewById(R.id.location_pref_dlg_locationSystemSettingsRelLa);
        locationEnabledStatusTextView = view.findViewById(R.id.location_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.location_pref_dlg_locationSystemSettingsButton);
        TooltipCompat.setTooltipText(locationSystemSettingsButton, getString(R.string.location_settings_button_tooltip));

        setLocationEnableStatus();

        preference.resetSummary();
        preference.refreshListView();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (preference.onlyEdit == 0) {
            if (positiveResult)
                preference.persistGeofence(false);
            else
                preference.resetSummary();
        }

        Cursor cursor = listAdapter.getCursor();
        if (cursor != null)
            cursor.close();

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        String statusText;
        if (!GlobalUtils.isLocationEnabled(prefContext)) {
            statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    "* " + getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";

            locationEnabledStatusTextView.setText(statusText);

            locationSystemSettingsButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            getActivity().startActivityForResult(intent, EventsPrefsFragment.RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.location_settings_button_tooltip),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (getActivity() != null)
                            if (!getActivity().isFinishing())
                                dialog.show();
                    }
                }
            });

            locationSystemSettingsRelLa.setVisibility(View.VISIBLE);
            //locationEnabledStatusTextView.setVisibility(View.VISIBLE);
            //locationSystemSettingsButton.setVisibility(View.VISIBLE);
        } else {
            locationSystemSettingsRelLa.setVisibility(View.GONE);
            //locationEnabledStatusTextView.setVisibility(View.GONE);
            //locationSystemSettingsButton.setVisibility(View.GONE);
        }
    }

    /*
    private void startEditor(long geofenceId) {
        if (getActivity() != null) {
            Intent intent = new Intent(prefContext, LocationGeofenceEditorActivity.class);
            intent.putExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, geofenceId);

            // is not possible to get activity from preference, used is static method
            //if (preference.onlyEdit == 0) {
            //    //EventPrefsFragment.setChangedLocationGeofencePreference(this);
            //    getActivity().startActivityForResult(intent, LocationGeofencePreference.RESULT_GEOFENCE_EDITOR);
            //} else {
            //    //PhoneProfilesPrefsFragment.setChangedLocationGeofencePreference(this);
            //    getActivity().startActivityForResult(intent, LocationGeofencePreference.RESULT_GEOFENCE_EDITOR);
            //}
            getActivity().startActivityForResult(intent, LocationGeofencePreference.RESULT_GEOFENCE_EDITOR);
        }
    }
    */

    private void startEditorOSM(long geofenceId) {
        if (getActivity() != null) {
            Intent intent = new Intent(prefContext, LocationGeofenceEditorActivityOSM.class);
            intent.putExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, geofenceId);

            // is not possible to get activity from preference, used is static method
            /*if (preference.onlyEdit == 0) {
                //EventPrefsFragment.setChangedLocationGeofencePreference(this);
                getActivity().startActivityForResult(intent, LocationGeofencePreference.RESULT_GEOFENCE_EDITOR);
            } else {
                //PhoneProfilesPrefsFragment.setChangedLocationGeofencePreference(this);
                getActivity().startActivityForResult(intent, LocationGeofencePreference.RESULT_GEOFENCE_EDITOR);
            }*/
            //noinspection deprecation
            getActivity().startActivityForResult(intent, LocationGeofencePreference.RESULT_GEOFENCE_EDITOR);
        }
    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        final Context context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.location_geofence_pref_item_edit, popup.getMenu());

        final long geofenceId = (long)view.getTag();

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.location_geofence_pref_item_menu_edit) {
                //startEditor(geofenceId);
                startEditorOSM(geofenceId);
                return true;
            }
            else
            if (itemId == R.id.location_geofence_pref_item_menu_delete) {
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.event_preferences_locations_cant_delete_location_title),
                            getString(R.string.delete_geofence_name_alert_message),
                            getString(R.string.alert_button_yes),
                            getString(R.string.alert_button_no),
                            null, null,
                            (dialog1, which) -> {
                                if (geofenceId > 0) {
                                    if (!DatabaseHandler.getInstance(context.getApplicationContext()).isGeofenceUsed(geofenceId)) {
                                        DatabaseHandler.getInstance(context.getApplicationContext()).deleteGeofence(geofenceId);
                                        preference.refreshListView();
                                        //updateGUIWithGeofence(0);
                                        /*if (dataWrapper.getDatabaseHandler().getGeofenceCount() == 0) {
                                            // stop location updates
                                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.isLocationScannerStarted())
                                                PhoneProfilesService.getGeofencesScanner().disconnect();
                                        }*/
                                    } else {
                                        PPAlertDialog _dialog = new PPAlertDialog(
                                                getString(R.string.event_preferences_locations_cant_delete_location_title),
                                                getString(R.string.event_preferences_locations_cant_delete_location_text),
                                                getString(android.R.string.ok),
                                                null,
                                                null, null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                true, true,
                                                false, false,
                                                false,
                                                getActivity()
                                        );

                                        if (getActivity() != null)
                                            if (!getActivity().isFinishing())
                                                _dialog.show();
                                    }
                                }
                            },
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            getActivity()
                    );

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            }
            else {
                return false;
            }
        });

        if (getActivity() != null)
            if (!getActivity().isFinishing())
                popup.show();
    }

    void refreshListView()
    {
        if (listAdapter != null)
            listAdapter.reload(preference.dataWrapper);
    }

}
