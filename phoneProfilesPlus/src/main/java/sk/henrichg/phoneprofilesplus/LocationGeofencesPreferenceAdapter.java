package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class LocationGeofencesPreferenceAdapter extends CursorAdapter {

    private final int KEY_G_ID;
    //private final int KEY_G_LATITUDE;
    //private final int KEY_G_LONGITUDE;
    //private final int KEY_G_RADIUS;
    private final int KEY_G_NAME;
    private final int KEY_G_CHECKED;

    //public RadioButton selectedRB;
    private LocationGeofencePreference preference;

    public LocationGeofencesPreferenceAdapter(Context context, Cursor cursor, LocationGeofencePreference preference) {
        super(context, cursor, 0);

        this.preference = preference;

        KEY_G_ID = cursor.getColumnIndex(DatabaseHandler.KEY_G_ID);
        //KEY_G_LATITUDE = cursor.getColumnIndex(DatabaseHandler.KEY_G_LATITUDE);
        //KEY_G_LONGITUDE = cursor.getColumnIndex(DatabaseHandler.KEY_G_LONGITUDE);
        //KEY_G_RADIUS = cursor.getColumnIndex(DatabaseHandler.KEY_G_RADIUS);
        KEY_G_NAME = cursor.getColumnIndex(DatabaseHandler.KEY_G_NAME);
        KEY_G_CHECKED = cursor.getColumnIndex(DatabaseHandler.KEY_G_CHECKED);

        //selectedRB = null;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.location_preference_list_item, parent, false);

        ViewHolder rowData  = new ViewHolder();

        rowData.radioButton = (RadioButton) view.findViewById(R.id.location_pref_dlg_item_radiobtn);
        rowData.name  = (TextView) view.findViewById(R.id.location_pref_dlg_item_name);
        rowData.itemEditMenu = (ImageView) view.findViewById(R.id.location_pref_dlg_item_edit_menu);

        getView(rowData, context, cursor, true);

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder rowData = (ViewHolder) view.getTag();
        getView(rowData, context, cursor, false);
    }

    private void getView(final ViewHolder rowData, Context context, Cursor cursor, boolean newView) {
        boolean checked = cursor.getInt(KEY_G_CHECKED) == 1;
        long id = cursor.getLong(KEY_G_ID);
        rowData.radioButton.setChecked(checked);
        rowData.radioButton.setTag(id);
        if (preference.dataWrapper.getDatabaseHandler().isGeofenceUsed(id, false))
            rowData.name.setTypeface(null, Typeface.BOLD);
        else
            rowData.name.setTypeface(null, Typeface.NORMAL);
        rowData.name.setText(cursor.getString(KEY_G_NAME));
        //if (checked) {
        //    selectedRB = rowData.radioButton;
        //    Log.d("LocationGeofencesPreferenceAdapter.getView", "checked id=" + id);
        //}

        rowData.radioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;

                /*
                if (selectedRB != null) {
                    selectedRB.setChecked(false);
                }
                selectedRB = rb;
                */

                long id = (long) rb.getTag();
                preference.dataWrapper.getDatabaseHandler().checkGeofence(id);

                //rowData.radioButton.setChecked(true);
                preference.setGeofenceId(id);

                preference.refreshListView();
            }
        });

        rowData.itemEditMenu.setTag(id);
        final ImageView itemEditMenu = rowData.itemEditMenu;
        rowData.itemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                preference.showEditMenu(itemEditMenu);
            }
        });

    }

    public static class ViewHolder {
        RadioButton radioButton;
        TextView name;
        ImageView itemEditMenu;
    }

    public void reload(DataWrapper dataWrapper) {
        //selectedRB = null;
        changeCursor(dataWrapper.getDatabaseHandler().getGeofencesCursor());
    }

}
