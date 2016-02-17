package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationGeofenceEditorActivity extends AppCompatActivity
                                     implements GoogleApiClient.ConnectionCallbacks,
                                                GoogleApiClient.OnConnectionFailedListener,
                                                LocationListener,
                                                OnMapReadyCallback
{
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Marker editedMarker;
    private Circle editedRadius;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplus";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String UPDATE_NAME_EXTRA = PACKAGE_NAME + ".UPDATE_NAME_EXTRA";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private Location mLastLocation;
    private Location mLocation;
    protected LocationRequest mLocationRequest;

    private long geofenceId;
    private Geofence geofence;

    private AddressResultReceiver mResultReceiver;
    private boolean mAddressRequested = false;

    DataWrapper dataWrapper;

    EditText geofenceNameEditText;
    AppCompatImageButton addressButton;
    TextView addressText;
    Button okButton;
    private SeekBar radiusSeekBar;
    private TextView radiusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            GUIData.setTheme(this, false, true);
        else
            GUIData.setTheme(this, false, false);
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_geofence_editor);

        mResultReceiver = new AddressResultReceiver(new Handler());

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        Intent intent = getIntent();
        geofenceId = intent.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);

        dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);

        if (geofenceId > 0) {
            geofence = dataWrapper.getDatabaseHandler().getGeofence(geofenceId);
            mLocation = new Location("LOC");
            mLocation.setLatitude(geofence._latitude);
            mLocation.setLongitude(geofence._longitude);
        }
        if (geofence == null) {
            geofenceId = 0;
            geofence = new Geofence();
            geofence._name = getString(R.string.event_preferences_location_new_location_name) + "_" +
                                String.valueOf(dataWrapper.getDatabaseHandler().getGeofenceCount()+1);
            geofence._radius = 100;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_editor_map);
        mapFragment.getMapAsync(this);

        radiusLabel = (TextView)findViewById(R.id.location_pref_dlg_radius_seekbar_label);
        radiusSeekBar = (SeekBar)findViewById(R.id.location_pref_dlg_radius_seekbar);
        radiusSeekBar.setProgress(Math.round(geofence._radius / (float)20.0)-1);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                geofence._radius = (progress+1) * 20;
                updateEditedMarker(false);
                //Log.d("LocationGeofenceEditorActivity.onProgressChanged", "radius="+geofence._radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        geofenceNameEditText = (EditText)findViewById(R.id.location_editor_geofence_name);
        geofenceNameEditText.setText(geofence._name);

        addressText = (TextView)findViewById(R.id.location_editor_address_text);

        okButton = (Button)findViewById(R.id.location_editor_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = geofenceNameEditText.getText().toString();
                if ((!name.isEmpty()) && (mLocation != null)) {
                    geofence._name = name;
                    geofence._latitude = mLocation.getLatitude();
                    geofence._longitude = mLocation.getLongitude();

                    if (geofenceId > 0) {
                        dataWrapper.getDatabaseHandler().updateGeofence(geofence);
                    } else {
                        dataWrapper.getDatabaseHandler().addGeofence(geofence);
                        // start location updates
                        GlobalData.geofencesScanner.connectForResolve();
                    }

                    dataWrapper.getDatabaseHandler().checkGeofence(geofence._id);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, geofence._id);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
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

        AppCompatImageButton myLocationButton = (AppCompatImageButton)findViewById(R.id.location_editor_my_location);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null)
                    mLocation = new Location(mLastLocation);
                refreshActivity(true);
            }
        });

        addressButton = (AppCompatImageButton)findViewById(R.id.location_editor_address_btn);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGeofenceAddress(true);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        refreshActivity(true);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Log.i("LocationGeofenceEditorActivity", "Connection suspended");
        //mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        //Log.d("LocationGeofenceEditorActivity.onLocationChanged", "latitude=" + String.valueOf(location.getLatitude()));
        //Log.d("LocationGeofenceEditorActivity.onLocationChanged", "longitude=" + String.valueOf(location.getLongitude()));

        if (mLocation == null) {
            mLocation = new Location(mLastLocation);
            refreshActivity(true);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        updateEditedMarker(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //Log.d("Map", "Map clicked");
                if (mLocation == null)
                    mLocation = new Location("LOC");
                mLocation.setLatitude(point.latitude);
                mLocation.setLongitude(point.longitude);
                refreshActivity(false);
            }
        });
    }

    private void updateEditedMarker(boolean setMapCamera) {
        if ((mMap != null) && (mLocation != null)) {

            LatLng editedGeofence = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

            if (editedMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(editedGeofence);
                editedMarker = mMap.addMarker(markerOptions);
            }
            else
                editedMarker.setPosition(editedGeofence);
            editedMarker.setTitle(geofenceNameEditText.getText().toString());

            if (editedRadius == null) {
                editedRadius = mMap.addCircle(new CircleOptions()
                        .center(editedGeofence)
                        .radius(geofence._radius)
                        .strokeColor(ContextCompat.getColor(this, R.color.map_marker_stroke))
                        .fillColor(ContextCompat.getColor(this, R.color.map_marker_fill))
                        .strokeWidth(5));
            }
            else {
                editedRadius.setRadius(geofence._radius);
                editedRadius.setCenter(editedGeofence);
            }
            radiusLabel.setText(String.valueOf(Math.round(geofence._radius)));

            if (setMapCamera)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(editedGeofence));
        }
    }

    //----------------------------------------------------

    public void refreshActivity(boolean setMapCamera) {
        //Log.d("LocationGeofenceEditorActivity.refreshActivity", "xxx");
        getLastLocation();
        boolean enableAddressButton = false;
        if (mLocation != null) {
            //Log.d("LocationGeofenceEditorActivity.refreshActivity", "latitude=" + String.valueOf(mLocation.getLatitude()));
            //Log.d("LocationGeofenceEditorActivity.refreshActivity", "longitude=" + String.valueOf(mLocation.getLongitude()));

            // Determine whether a Geocoder is available.
            if (Geocoder.isPresent()) {
                startIntentService(false);
                enableAddressButton = true;
            }
        }
        if (addressButton.isEnabled())
            GUIData.setImageButtonEnabled(enableAddressButton, addressButton, R.drawable.ic_action_location_address, getApplicationContext());
        String name = geofenceNameEditText.getText().toString();

        updateEditedMarker(setMapCamera);

        okButton.setEnabled((!name.isEmpty()) && (mLocation != null));
    }

    private void getLastLocation() {
        if (Permissions.grantLocationGeofenceEditorPermissions(getApplicationContext(), this)) {
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                return;
            }

            //Log.d("LocationGeofenceEditorActivity.getLastLocation", "mLastLocation="+mLastLocation);

            if (mLastLocation == null)
                startLocationUpdates();
            else if (mLocation == null)
                mLocation = new Location(mLastLocation);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (Permissions.grantLocationGeofenceEditorPermissions(getApplicationContext(), this)) {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                return;
            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private  void getGeofenceAddress(boolean updateName) {
        // Only start the service to fetch the address if GoogleApiClient is
        // connected.
        if (mGoogleApiClient.isConnected() && mLocation != null) {
            startIntentService(true);
        }
        // If GoogleApiClient isn't connected, process the user's request by
        // setting mAddressRequested to true. Later, when GoogleApiClient connects,
        // launch the service to fetch the address. As far as the user is
        // concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
    }

    protected void startIntentService(boolean updateName) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, mResultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, mLocation);
        intent.putExtra(UPDATE_NAME_EXTRA, updateName);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            boolean enableAddressButton = false;
            if (resultCode == LocationGeofenceEditorActivity.SUCCESS_RESULT) {
                // Display the address string
                // or an error message sent from the intent service.
                String addressOutput = resultData.getString(RESULT_DATA_KEY);
                addressText.setText(addressOutput);

                if (resultData.getBoolean(UPDATE_NAME_EXTRA, false))
                    geofenceNameEditText.setText(addressOutput);

                updateEditedMarker(false);

                enableAddressButton = true;
            }

            GUIData.setImageButtonEnabled(enableAddressButton, addressButton, R.drawable.ic_action_location_address, getApplicationContext());

            mAddressRequested = false;
        }
    }


    //------------------------------------------

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((LocationGeofenceEditorActivity) getActivity()).onDialogDismissed();
        }
    }

}

