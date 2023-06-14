package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import androidx.appcompat.widget.TooltipCompat;

class LocationGeofencesPreferenceAdapter extends CursorAdapter {

    private final int KEY_G_ID;
    //private final int KEY_G_LATITUDE;
    //private final int KEY_G_LONGITUDE;
    //private final int KEY_G_RADIUS;
    private final int KEY_G_NAME;
    private final int KEY_G_CHECKED;

    //public RadioButton selectedRB;
    private final LocationGeofencePreferenceFragment preferenceFragment;

    final private Context context;

    LocationGeofencesPreferenceAdapter(Context context, Cursor cursor, LocationGeofencePreferenceFragment preferenceFragment) {
        super(context, cursor, 0);

        this.context = context;
        this.preferenceFragment = preferenceFragment;

        KEY_G_ID = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID);
        //KEY_G_LATITUDE = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LATITUDE);
        //KEY_G_LONGITUDE = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LONGITUDE);
        //KEY_G_RADIUS = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_RADIUS);
        KEY_G_NAME = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_NAME);
        KEY_G_CHECKED = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_CHECKED);

        //selectedRB = null;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;

        if (preferenceFragment.preference.onlyEdit == 0)
            view = inflater.inflate(R.layout.listitem_location_preference, parent, false);
        else
            view = inflater.inflate(R.layout.listitem_location_preference_no_chb, parent, false);

        LocationGeofencesPreferenceViewHolder rowData  = new LocationGeofencesPreferenceViewHolder();

        if (preferenceFragment.preference.onlyEdit == 0)
            rowData.checkBox = view.findViewById(R.id.location_pref_dlg_item_checkBox);
        else
            rowData.checkBox = null;
        rowData.name  = view.findViewById(R.id.location_pref_dlg_item_name);
        rowData.itemEditMenu = view.findViewById(R.id.location_pref_dlg_item_edit_menu);

        getView(rowData, cursor/*, true*/);

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        LocationGeofencesPreferenceViewHolder rowData = (LocationGeofencesPreferenceViewHolder) view.getTag();

        // must be set, without this not working long click
        if (preferenceFragment.preference.onlyEdit == 0) {
            rowData.checkBox.setFocusable(false);
            rowData.checkBox.setFocusableInTouchMode(false);
        }
        rowData.itemEditMenu.setFocusable(false);
        rowData.itemEditMenu.setFocusableInTouchMode(false);

        getView(rowData, cursor/*, false*/);
    }

    private void getView(final LocationGeofencesPreferenceViewHolder rowData, /*Context context,*/
                         Cursor cursor/*, boolean newView*/) {
        boolean checked = cursor.getInt(KEY_G_CHECKED) == 1;
        long id = cursor.getLong(KEY_G_ID);

        rowData.geofenceId = id;

        if (preferenceFragment.preference.onlyEdit == 0) {
            rowData.checkBox.setChecked(checked);
            rowData.checkBox.setTag(id);
        }
        if (DatabaseHandler.getInstance(preferenceFragment.preference.dataWrapper.context).isGeofenceUsed(id))
            rowData.name.setTypeface(null, Typeface.BOLD);
        else
            rowData.name.setTypeface(null, Typeface.NORMAL);
        rowData.name.setText(cursor.getString(KEY_G_NAME));

        if (preferenceFragment.preference.onlyEdit == 0) {
            rowData.checkBox.setOnClickListener(v -> {
                CheckBox chb = (CheckBox) v;

                long id1 = (long) chb.getTag();
                DatabaseHandler.getInstance(preferenceFragment.preference.dataWrapper.context).checkGeofence(String.valueOf(id1), 2);

                //preference.updateGUIWithGeofence(id);

                preferenceFragment.preference.refreshListView();
            });
        }

        TooltipCompat.setTooltipText(rowData.itemEditMenu, context.getString(R.string.tooltip_options_menu));
        rowData.itemEditMenu.setTag(id);
        final ImageView itemEditMenu = rowData.itemEditMenu;
        rowData.itemEditMenu.setOnClickListener(v -> preferenceFragment.showEditMenu(itemEditMenu));

    }

    public void reload(DataWrapper dataWrapper) {
        //selectedRB = null;
        changeCursor(DatabaseHandler.getInstance(dataWrapper.context).getGeofencesCursor());
    }

}
