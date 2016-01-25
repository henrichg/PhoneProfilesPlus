package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class LocationPrefDialogGeofencesAdapter extends CursorAdapter {

    private final int KEY_G_ID;
    //private final int KEY_G_LATITUDE;
    //private final int KEY_G_LONGITUDE;
    //private final int KEY_G_RADIUS;
    private final int KEY_G_NAME;

    public LocationPrefDialogGeofencesAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        KEY_G_ID = cursor.getColumnIndex(DatabaseHandler.KEY_G_ID);
        //KEY_G_LATITUDE = cursor.getColumnIndex(DatabaseHandler.KEY_G_LATITUDE);
        //KEY_G_LONGITUDE = cursor.getColumnIndex(DatabaseHandler.KEY_G_LONGITUDE);
        //KEY_G_RADIUS = cursor.getColumnIndex(DatabaseHandler.KEY_G_RADIUS);
        KEY_G_NAME = cursor.getColumnIndex(DatabaseHandler.KEY_G_NAME);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.location_preference_list_item, parent, false);

        MyRowViewHolder rowData  = new MyRowViewHolder();

        rowData.radioButton = (RadioButton) view.findViewById(R.id.location_pref_dlg_item_radiobtn);
        rowData.name  = (TextView) view.findViewById(R.id.location_pref_dlg_item_name);

        rowData.radioButton.setChecked(false);
        rowData.name.setText(cursor.getString(KEY_G_NAME));
        rowData._id = cursor.getInt(KEY_G_ID);

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        MyRowViewHolder rowData = (MyRowViewHolder) view.getTag();

        rowData.radioButton.setChecked(false);
        rowData.name.setText(cursor.getString(KEY_G_NAME));
        rowData._id = cursor.getInt(KEY_G_ID);
    }

    public static class MyRowViewHolder {
        RadioButton radioButton;
        TextView name;
        int _id;
    }

    public void reload(DataWrapper dataWrapper) {
        changeCursor(dataWrapper.getDatabaseHandler().getGeofencesCursor());
    }

}
