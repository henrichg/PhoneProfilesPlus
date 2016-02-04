package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class LocationGeofencePreference extends DialogPreference {

    Context context;

    private MaterialDialog mDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    private TextView geofenceName;
    private ListView geofencesListView;
    private LocationGeofencesPreferenceAdapter listAdapter;

    public DataWrapper dataWrapper;

    public static final String EXTRA_GEOFENCE_ID = "geofence_id";
    public static final int RESULT_GEOFENCE_EDITOR = 2100;

    public LocationGeofencePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, false, 0);
    }

    @Override
    protected void showDialog(Bundle state) {

        long value = 0;
        value = getPersistedLong(value);
        dataWrapper.getDatabaseHandler().checkGeofence(value);

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .autoDismiss(false)
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        persistGeofence(false);
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_location_pref_dialog, null);
        onBindDialogView(layout);

        //progressLinearLayout = (LinearLayout) layout.findViewById(R.id.location_pref_dlg_linla_progress);
        //dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.location_pref_dlg_rella_data);

        geofenceName = (TextView) layout.findViewById(R.id.location_pref_dlg_geofence_name);
        updateGUIWithGeofence(dataWrapper.getDatabaseHandler().getCheckedGeofence());

        AppCompatImageButton addButton = (AppCompatImageButton)layout.findViewById(R.id.location_pref_dlg_add);

        geofencesListView = (ListView) layout.findViewById(R.id.location_pref_dlg_listview);

        listAdapter = new LocationGeofencesPreferenceAdapter(context, dataWrapper.getDatabaseHandler().getGeofencesCursor(), this);
        geofencesListView.setAdapter(listAdapter);

        refreshListView();

        geofencesListView.setOnItemClickListener(new OnItemClickListener() {
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

                long gid = (long) viewHolder.radioButton.getTag();
                dataWrapper.getDatabaseHandler().checkGeofence(gid);

                //viewHolder.radioButton.setChecked(true);
                updateGUIWithGeofence(gid);

                refreshListView();
            }

        });

        mBuilder.customView(layout, false);

        /*
        final TextView helpText = (TextView)layout.findViewById(R.id.wifi_ssid_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

        ImageView helpIcon = (ImageView)layout.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
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

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        dataWrapper.getDatabaseHandler().checkGeofence(0);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if(restoreValue)
        {
            long value = 0;
            value = getPersistedLong(value);
            dataWrapper.getDatabaseHandler().checkGeofence(value);
        }
        else
        {
            long value = (long)defaultValue;
            persistLong(value);
            dataWrapper.getDatabaseHandler().checkGeofence(value);
        }
        
    }    

    private void persistGeofence(boolean reset) {
        if (shouldPersist()) {
            long value = dataWrapper.getDatabaseHandler().getCheckedGeofence();
            if (callChangeListener(value)) {
                if (reset)
                    persistLong(0);
                persistLong(value);
            }
        }
    }

    public void updateGUIWithGeofence(long geofenceId)
    {
        String name = dataWrapper.getDatabaseHandler().getGeofenceName(geofenceId);
        if (name.isEmpty())
            name = "["+context.getString(R.string.event_preferences_locations_location_not_selected)+"]";

        this.geofenceName.setText(name);
    }
    
    public void refreshListView()
    {
        long value = dataWrapper.getDatabaseHandler().getCheckedGeofence();
        int position = dataWrapper.getDatabaseHandler().getGeofencePosition(value);
        listAdapter.reload(dataWrapper);
        if (position > -1)
            position = 0;
        geofencesListView.setSelection(position);
    }

    private void startEditor(long geofenceId) {
        Intent intent = new Intent(context, LocationGeofenceEditorActivity.class);
        intent.putExtra(EXTRA_GEOFENCE_ID, geofenceId);

        // hm, neda sa ziskat aktivita z preference, tak vyuzivam static metodu
        EventPreferencesFragment.setChangedLocationGeofencePreference(this);
        EventPreferencesFragment.getPreferencesActivity().startActivityForResult(intent, RESULT_GEOFENCE_EDITOR);
    }

    public void setGeofenceFromEditor(long geofenceId) {
        Log.d("LocationGeofencePreference.setGeofenceFromEditor", "geofenceId=" + geofenceId);
        persistGeofence(true);
        refreshListView();
        updateGUIWithGeofence(geofenceId);
    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.location_geofence_pref_item_edit, popup.getMenu());

        final long geofenceId = (long)view.getTag();
        final Context _context = context;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.location_geofence_pref_item_menu_edit:
                        startEditor(geofenceId);
                        return true;
                    case R.id.location_geofence_pref_item_menu_delete:
                        if (geofenceId > 0) {
                            if (!dataWrapper.getDatabaseHandler().isGeofenceUsed(geofenceId, false)) {
                                dataWrapper.getDatabaseHandler().deleteGeofence(geofenceId);
                                refreshListView();
                                updateGUIWithGeofence(0);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
                                dialogBuilder.setTitle(R.string.event_preferences_locations_cant_delete_location_title);
                                dialogBuilder.setMessage(R.string.event_preferences_locations_cant_delete_location_text);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                dialogBuilder.show();
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