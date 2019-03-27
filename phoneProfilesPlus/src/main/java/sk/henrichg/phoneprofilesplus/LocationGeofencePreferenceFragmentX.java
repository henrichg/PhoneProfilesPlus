package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.preference.PreferenceDialogFragmentCompat;

public class LocationGeofencePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    LocationGeofencePreferenceX preference;

    private Context prefContext;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (LocationGeofencePreferenceX) getPreference();

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_location_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        /*if (preference.onlyEdit == 0) {
            String value = preference.getPersistedGeofence();
            DatabaseHandler.getInstance(prefContext.getApplicationContext()).checkGeofence(value, 1);
        }
        else
            preference.setNegativeButtonText(null);*/

        AppCompatImageButton addButton = view.findViewById(R.id.location_pref_dlg_add);

        //noinspection ConstantConditions
        ListView geofencesListView = view.findViewById(R.id.location_pref_dlg_listview);

        preference.listAdapter = new LocationGeofencesPreferenceAdapterX(prefContext, DatabaseHandler.getInstance(prefContext.getApplicationContext()).getGeofencesCursor(), this);
        geofencesListView.setAdapter(preference.listAdapter);

        preference.refreshListView();

        geofencesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
                    startEditor(gid);
                }

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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditor(0);
            }
        });

        final Button unselectAllButton = view.findViewById(R.id.location_pref_dlg_unselectAll);
        if (preference.onlyEdit == 0) {
            //unselectAllButton.setAllCaps(false);
            unselectAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHandler.getInstance(prefContext.getApplicationContext()).checkGeofence("", 0);
                    preference.refreshListView();
                }
            });
        }
        else {
            unselectAllButton.setVisibility(View.GONE);
        }

        final TextView locationEnabledStatusTextView = view.findViewById(R.id.location_pref_dlg_locationEnableStatus);
        String statusText;
        if (!PhoneProfilesService.isLocationEnabled(prefContext)) {
            statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    "* " + getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";
        } else {
            statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
        }
        locationEnabledStatusTextView.setText(statusText);

        AppCompatImageButton locationSystemSettingsButton = view.findViewById(R.id.location_pref_dlg_locationSystemSettingsButton);
        locationSystemSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
                else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                    if (!((Activity)prefContext).isFinishing())
                        dialog.show();
                }
            }
        });

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        DatabaseHandler.getInstance(prefContext.getApplicationContext()).checkGeofence("", 0);
        Cursor cursor = preference.listAdapter.getCursor();
        if (cursor != null)
            cursor.close();

        if (positiveResult && (preference.onlyEdit != 0))
            preference.persistGeofence(false);
    }

    private void startEditor(long geofenceId) {
        if (getActivity() != null) {
            Intent intent = new Intent(prefContext, LocationGeofenceEditorActivity.class);
            intent.putExtra(LocationGeofencePreferenceX.EXTRA_GEOFENCE_ID, geofenceId);

            // is not possible to get activity from preference, used is static method
            if (preference.onlyEdit == 0) {
                //EventPreferencesFragment.setChangedLocationGeofencePreference(this);
                getActivity().startActivityForResult(intent, LocationGeofencePreferenceX.RESULT_GEOFENCE_EDITOR);
            } else {
                //PhoneProfilesPreferencesFragment.setChangedLocationGeofencePreference(this);
                getActivity().startActivityForResult(intent, LocationGeofencePreferenceX.RESULT_GEOFENCE_EDITOR);
            }
        }
    }

    public void showEditMenu(View view)
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

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.location_geofence_pref_item_menu_edit:
                        startEditor(geofenceId);
                        return true;
                    case R.id.location_geofence_pref_item_menu_delete:
                        if (geofenceId > 0) {
                            if (!DatabaseHandler.getInstance(context.getApplicationContext()).isGeofenceUsed(geofenceId)) {
                                DatabaseHandler.getInstance(context.getApplicationContext()).deleteGeofence(geofenceId);
                                preference.refreshListView();
                                //updateGUIWithGeofence(0);
                                /*if (dataWrapper.getDatabaseHandler().getGeofenceCount() == 0) {
                                    // stop location updates
                                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.isGeofenceScannerStarted())
                                        PhoneProfilesService.getGeofencesScanner().disconnect();
                                }*/
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                                dialogBuilder.setTitle(R.string.event_preferences_locations_cant_delete_location_title);
                                dialogBuilder.setMessage(R.string.event_preferences_locations_cant_delete_location_text);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                                if (!((Activity)context).isFinishing())
                                    dialog.show();
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

}
