package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LocationGeofenceEditor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            GUIData.setTheme(this, false, true);
        else
            GUIData.setTheme(this, false, false);
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_geogence_editor);

        Button okButton = (Button)findViewById(R.id.location_editor_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geofence geofence = new Geofence();
                geofence._name = "Pokus";
                geofence._latitude = 1;
                geofence._longitude = 1;
                geofence._radius = 100;

                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);

                dataWrapper.getDatabaseHandler().addGeofence(geofence, false);
                dataWrapper.getDatabaseHandler().checkGeofence(geofence._id);

                Intent returnIntent = new Intent();
                returnIntent.putExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, geofence._id);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        Button cancelButton = (Button)findViewById(R.id.location_editor_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

    }
}
