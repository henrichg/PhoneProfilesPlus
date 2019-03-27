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
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

class LocationGeofencesPreferenceAdapterX extends CursorAdapter {

    private final int KEY_G_ID;
    //private final int KEY_G_LATITUDE;
    //private final int KEY_G_LONGITUDE;
    //private final int KEY_G_RADIUS;
    private final int KEY_G_NAME;
    private final int KEY_G_CHECKED;

    //public RadioButton selectedRB;
    private final LocationGeofencePreferenceFragmentX preferenceFragment;

    LocationGeofencesPreferenceAdapterX(Context context, Cursor cursor, LocationGeofencePreferenceFragmentX preferenceFragment) {
        super(context, cursor, 0);

        this.preferenceFragment = preferenceFragment;

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
        View view;

        if (preferenceFragment.preference.onlyEdit == 0)
            view = inflater.inflate(R.layout.location_preference_list_item, parent, false);
        else
            view = inflater.inflate(R.layout.location_preference_list_item_no_rb, parent, false);

        ViewHolder rowData  = new ViewHolder();

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

        ViewHolder rowData = (ViewHolder) view.getTag();
        getView(rowData, cursor/*, false*/);
    }

    private void getView(final ViewHolder rowData, /*Context context,*/ Cursor cursor/*, boolean newView*/) {
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
            rowData.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox chb = (CheckBox) v;

                    long id = (long) chb.getTag();
                    DatabaseHandler.getInstance(preferenceFragment.preference.dataWrapper.context).checkGeofence(String.valueOf(id), 2);

                    //preference.updateGUIWithGeofence(id);

                    preferenceFragment.preference.refreshListView();
                }
            });
        }

        rowData.itemEditMenu.setTag(id);
        final ImageView itemEditMenu = rowData.itemEditMenu;
        rowData.itemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                preferenceFragment.showEditMenu(itemEditMenu);
            }
        });

    }

    public static class ViewHolder {
        CheckBox checkBox;
        TextView name;
        AppCompatImageButton itemEditMenu;
        long geofenceId;
    }

    public void reload(DataWrapper dataWrapper) {
        //selectedRB = null;
        changeCursor(DatabaseHandler.getInstance(dataWrapper.context).getGeofencesCursor());
    }

}
