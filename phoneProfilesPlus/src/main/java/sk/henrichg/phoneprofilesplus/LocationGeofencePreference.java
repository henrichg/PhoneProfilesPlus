package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class LocationGeofencePreference extends DialogPreference {

    Context context;

    private MaterialDialog mDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    private TextView geofenceName;
    private ListView geofencesListView;
    private LocationGeofencesPreferenceAdapter listAdapter;

    private LocationGeofencesPreferenceAdapter.ViewHolder selectedItem = null;
    private int selectedPosition = -1;

    DataWrapper dataWrapper;

    public LocationGeofencePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, false, 0);
        dataWrapper.getDatabaseHandler().checkGeofence(0);
    }

    @Override
    protected void showDialog(Bundle state) {
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
                        if (shouldPersist()) {
                            //SSIDName.clearFocus();
                            int value = dataWrapper.getDatabaseHandler().getCheckedGeofence();

                            if (callChangeListener(value)) {
                                persistInt(value);
                            }
                        }
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
        setGeofenceId(dataWrapper.getDatabaseHandler().getCheckedGeofence());

        geofencesListView = (ListView) layout.findViewById(R.id.location_pref_dlg_listview);

        listAdapter = new LocationGeofencesPreferenceAdapter(context, dataWrapper.getDatabaseHandler().getGeofencesCursor());
        geofencesListView.setAdapter(listAdapter);

        refreshListView();

        geofencesListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                LocationGeofencesPreferenceAdapter.ViewHolder viewHolder =
                        (LocationGeofencesPreferenceAdapter.ViewHolder) v.getTag();

                if(position != selectedPosition && selectedItem != null){
                    selectedItem.radioButton.setChecked(false);
                }
                selectedPosition = position;
                selectedItem = viewHolder;

                dataWrapper.getDatabaseHandler().checkGeofence(viewHolder._id);

                viewHolder.radioButton.setChecked(true);
                setGeofenceId(viewHolder._id);
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

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
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
            int value = 0;
            value = getPersistedInt(value);
            dataWrapper.getDatabaseHandler().checkGeofence(value);
        }
        else
        {
            int value = (int)defaultValue;
            persistInt(value);
            dataWrapper.getDatabaseHandler().checkGeofence(value);
        }
        
    }    

    public void setGeofenceId(int geofenceId)
    {
        this.geofenceName.setText(dataWrapper.getDatabaseHandler().getGeofenceName(geofenceId));
    }
    
    public void refreshListView()
    {
        int value = dataWrapper.getDatabaseHandler().getCheckedGeofence();
        int position = dataWrapper.getDatabaseHandler().getGeofencePosition(value);
        listAdapter.reload(dataWrapper);
        geofencesListView.setSelection(position);

        /*
        //listAdapter.notifyDataSetChanged();
        progressLinearLayout.setVisibility(View.GONE);
        dataRelativeLayout.setVisibility(View.VISIBLE);

        for (int position = 0; position < SSIDList.size()-1; position++)
        {
            if (SSIDList.get(position).ssid.equals(value))
            {
                SSIDListView.setSelection(position);
                SSIDListView.setItemChecked(position, true);
                SSIDListView.smoothScrollToPosition(position);
                break;
            }
        }
        */
    }
    
}