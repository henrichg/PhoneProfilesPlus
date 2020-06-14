package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.math.BigDecimal;

public class LocationGeofenceEditorActivity extends AppCompatActivity
                                     implements GoogleApiClient.ConnectionCallbacks,
                                                GoogleApiClient.OnConnectionFailedListener,
                                                OnMapReadyCallback
{
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private GoogleMap mMap;
    private Marker editedMarker;
    private Circle editedRadius;
    private Circle lastLocationRadius;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    static final int SUCCESS_RESULT = 0;
    static final int FAILURE_RESULT = 1;
    static final String RESULT_CODE = PPApplication.PACKAGE_NAME + ".RESULT_CODE";
    static final String RESULT_DATA_KEY = PPApplication.PACKAGE_NAME + ".RESULT_DATA_KEY";
    static final String LATITUDE_EXTRA = PPApplication.PACKAGE_NAME + ".LATITUDE_EXTRA";
    static final String LONGITUDE_EXTRA = PPApplication.PACKAGE_NAME + ".LONGITUDE_EXTRA";
    static final String UPDATE_NAME_EXTRA = PPApplication.PACKAGE_NAME + ".UPDATE_NAME_EXTRA";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private Location mLastLocation;
    private Location mLocation;
    private LocationRequest mLocationRequest;

    private long geofenceId;
    private Geofence geofence;

    //private AddressResultReceiver mResultReceiver;
    //private boolean mAddressRequested = false;

    private EditText geofenceNameEditText;
    private AppCompatImageButton addressButton;
    private TextView addressText;
    private Button okButton;
    //private TextView radiusLabel;
    private TextView radiusValue;
    private PPNumberPicker numberPicker;

    private AlertDialog valueDialog;

    private static final int MIN_RADIUS = 20;
    private static final int MAX_RADIUS = 500 * 1000;

    static final String FETCH_ADDRESS_WORK_TAG = "fetchAddressWork";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false);
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_geofence_editor);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.location_editor_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        //mResultReceiver = new AddressResultReceiver(new Handler(getMainLooper()));

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //PPApplication.logE("LocationGeofenceEditorActivity.LocationCallback","xxx");
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                    //PPApplication.logE("LocationGeofenceEditorActivity.LocationCallback","location="+location);

                    if (mLocation == null) {
                        mLocation = new Location(mLastLocation);
                        refreshActivity(true);
                    }
                    else
                        updateEditedMarker(false);
                }
            }
        };

        createLocationRequest();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        Intent intent = getIntent();
        geofenceId = intent.getLongExtra(LocationGeofencePreferenceX.EXTRA_GEOFENCE_ID, 0);

        if (geofenceId > 0) {
            geofence = DatabaseHandler.getInstance(getApplicationContext()).getGeofence(geofenceId);
            mLocation = new Location("LOC");
            mLocation.setLatitude(geofence._latitude);
            mLocation.setLongitude(geofence._longitude);
        }
        if (geofence == null) {
            geofenceId = 0;
            geofence = new Geofence();
            int _count = DatabaseHandler.getInstance(getApplicationContext()).getGeofenceCount()+1;
            geofence._name = getString(R.string.event_preferences_location_new_location_name) + "_" + _count;
            geofence._radius = 100;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_editor_map);
        //noinspection ConstantConditions
        mapFragment.getMapAsync(this);

        //radiusLabel = findViewById(R.id.location_pref_dlg_radius_seekbar_label);

        TextView radiusLabel = findViewById(R.id.location_pref_dlg_radius_label);
        radiusLabel.setText(getString(R.string.event_preferences_location_radius_label) + ":");

        radiusValue = findViewById(R.id.location_pref_dlg_radius_value);
        TooltipCompat.setTooltipText(radiusValue, getString(R.string.location_pref_dlg_edit_radius_tooltip));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.event_preferences_location_radius_label);
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean persist = true;
                BigDecimal number = numberPicker.getEnteredNumber();
                if (isSmaller(number) || isBigger(number)) {
                /*String errorText = context.getString(R.string.number_picker_min_max_error, String.valueOf(preference.mMin), String.valueOf(preference.mMax));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();*/
                    persist = false;
                } else if (isSmaller(number)) {
                /*String errorText = context.getString(R.string.number_picker_min_error, String.valueOf(preference.mMin));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();*/
                    persist = false;
                } else if (isBigger(number)) {
                /*String errorText = context.getString(R.string.number_picker_max_error, String.valueOf(preference.mMax));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();*/
                    persist = false;
                }

                if (persist) {
                    geofence._radius = numberPicker.getNumber().floatValue();
                    updateEditedMarker(true);
                }
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_better_number_preference, null);
        dialogBuilder.setView(layout);

        numberPicker = layout.findViewById(R.id.better_number_picker);
        // Initialize state
        numberPicker.setMin(BigDecimal.valueOf(MIN_RADIUS));
        numberPicker.setMax(BigDecimal.valueOf(MAX_RADIUS));
        numberPicker.setPlusMinusVisibility(View.INVISIBLE);
        numberPicker.setDecimalVisibility(View.INVISIBLE);
        //mNumberPicker.setLabelText(getContext().getString(R.string.minutes_label_description));
        numberPicker.setNumber(Math.round(geofence._radius), null, null);
        if (ApplicationPreferences.applicationTheme(this, true).equals("dark"))
            numberPicker.setTheme(R.style.BetterPickersDialogFragment);
        else
            numberPicker.setTheme(R.style.BetterPickersDialogFragment_Light);

        valueDialog = dialogBuilder.create();

//        valueDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        radiusValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(isFinishing()))
                    valueDialog.show();
            }
        });


        /*
        // seekBar is logarithmic
        SeekBar radiusSeekBar = findViewById(R.id.location_pref_dlg_radius_seekbar);
        final float minRadius = 20;
        final float maxRadius = 1020;
        int value = (int)(Math.sqrt(((geofence._radius - minRadius) / (maxRadius - minRadius)) * 1000.0f * 1000.0f));
        radiusSeekBar.setProgress(value);
        //Log.e("LocationGeofenceEditorActivity.onCreate", "geofence._radius="+geofence._radius);
        //Log.e("LocationGeofenceEditorActivity.onCreate", "seekBar value="+value);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Approximate an exponential curve with x^2.
                geofence._radius = ((progress * progress) / (1000.0f * 1000.0f)) * (maxRadius - minRadius) + minRadius;
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
        */

        geofenceNameEditText = findViewById(R.id.location_editor_geofence_name);
        geofenceNameEditText.setText(geofence._name);

        addressText = findViewById(R.id.location_editor_address_text);

        okButton = findViewById(R.id.location_editor_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = geofenceNameEditText.getText().toString();
                if ((!name.isEmpty()) && (mLocation != null)) {
                    geofence._name = name;
                    geofence._latitude = mLocation.getLatitude();
                    geofence._longitude = mLocation.getLongitude();

                    if (geofenceId > 0) {
                        DatabaseHandler.getInstance(getApplicationContext()).updateGeofence(geofence);
                    } else {
                        DatabaseHandler.getInstance(getApplicationContext()).addGeofence(geofence);
                        /*synchronized (PPApplication.geofenceScannerMutex) {
                            // start location updates
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.isGeofenceScannerStarted())
                                PhoneProfilesService.getGeofencesScanner().connectForResolve();
                        }*/
                    }

                    DatabaseHandler.getInstance(getApplicationContext()).checkGeofence(String.valueOf(geofence._id), 1);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(LocationGeofencePreferenceX.EXTRA_GEOFENCE_ID, geofence._id);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });

        Button cancelButton = findViewById(R.id.location_editor_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        AppCompatImageButton myLocationButton = findViewById(R.id.location_editor_my_location);
        TooltipCompat.setTooltipText(myLocationButton, getString(R.string.location_editor_set_to_my_location_button_tooltip));
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null)
                    mLocation = new Location(mLastLocation);
                refreshActivity(true);
            }
        });

        addressButton = findViewById(R.id.location_editor_address_btn);
        TooltipCompat.setTooltipText(addressButton, getString(R.string.location_editor_rename_with_address_button_tooltip));
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGeofenceAddress(/*true*/);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        GlobalGUIRoutines.lockScreenOrientation(this, true);
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        try {
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        try {
            if (mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                try {
                    if (!mGoogleApiClient.isConnecting() &&
                            !mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY) {
            getLastLocation();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            int version = GoogleApiAvailability.getInstance().getApkVersion(this.getApplicationContext());
            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION, version);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        //Log.i("LocationGeofenceEditorActivity", "Connection suspended");
        try {
            int version = GoogleApiAvailability.getInstance().getApkVersion(this.getApplicationContext());
            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION, version);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        //mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        try {
            int version = GoogleApiAvailability.getInstance().getApkVersion(this.getApplicationContext());
            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION, version);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        //noinspection StatementWithEmptyBody
        if (mResolvingError) {
            // Already attempting to resolve an error.
            //return;
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
        if (mMap != null) {
            int nightModeFlags =
                    this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_dark));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    mMap.setMapStyle(null);
                    break;
            }

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
        //else {
        //    Log.e("LocationGeofenceEditorActivity.onMapReady", "mMap==null");
        //}
    }

    private void updateEditedMarker(boolean setMapCamera) {
        if (mMap != null) {

            if (mLastLocation != null) {
                LatLng lastLocationGeofence = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (lastLocationRadius == null) {
                    lastLocationRadius = mMap.addCircle(new CircleOptions()
                            .center(lastLocationGeofence)
                            .radius(mLastLocation.getAccuracy())
                            .strokeColor(ContextCompat.getColor(this, R.color.map_last_location_marker_stroke))
                            .fillColor(ContextCompat.getColor(this, R.color.map_last_location_marker_fill))
                            .strokeWidth(5)
                            .zIndex(1));
                } else {
                    lastLocationRadius.setRadius(mLastLocation.getAccuracy());
                    lastLocationRadius.setCenter(lastLocationGeofence);
                }
            }

            if (mLocation != null) {
                LatLng editedGeofence = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                if (editedMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(editedGeofence);
                    editedMarker = mMap.addMarker(markerOptions);
                } else
                    editedMarker.setPosition(editedGeofence);
                editedMarker.setTitle(geofenceNameEditText.getText().toString());

                if (editedRadius == null) {
                    editedRadius = mMap.addCircle(new CircleOptions()
                            .center(editedGeofence)
                            .radius(geofence._radius)
                            .strokeColor(ContextCompat.getColor(this, R.color.map_edited_location_marker_stroke))
                            .fillColor(ContextCompat.getColor(this, R.color.map_edited_location_marker_fill))
                            .strokeWidth(5)
                            .zIndex(2));
                } else {
                    editedRadius.setRadius(geofence._radius);
                    editedRadius.setCenter(editedGeofence);
                }
                radiusValue.setText(String.valueOf(Math.round(geofence._radius)));

                if (setMapCamera) {
                    if (editedRadius != null) {
                        try {
                            float zoom = getCircleZoomValue(mLocation.getLatitude(), mLocation.getLongitude(), geofence._radius,
                                                                mMap.getMinZoomLevel(), mMap.getMaxZoomLevel());
                            //PPApplication.logE("LocationGeofenceEditorActivity.updateEditedMarker", "zoom=" + zoom);
                            if (zoom > 16)
                                zoom = 16;
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));//, 1000, null);
                        } catch (StackOverflowError e) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(editedGeofence));
                        }
                    }
                    else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(editedGeofence));
                    }
                }
            }
        }
        //else {
        //    Log.e("LocationGeofenceEditorActivity.updateEditedMarker", "mMap==null");
        //}
    }

    //----------------------------------------------------

    private void refreshActivity(boolean setMapCamera) {
        boolean enableAddressButton = false;
        if (mLocation != null) {
            // Determine whether a geo-coder is available.
            if (Geocoder.isPresent()) {
                startIntentService(false);
                enableAddressButton = true;
            }
        }
        if (addressButton.isEnabled())
            GlobalGUIRoutines.setImageButtonEnabled(enableAddressButton, addressButton, getApplicationContext());
        String name = geofenceNameEditText.getText().toString();

        updateEditedMarker(setMapCamera);

        okButton.setEnabled((!name.isEmpty()) && (mLocation != null));
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (Permissions.grantLocationGeofenceEditorPermissions(getApplicationContext(), this)) {
            try {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                //PPApplication.logE("LocationGeofenceEditorActivity.getLastLocation","location="+location);
                                if (location != null) {
                                    mLastLocation = location;
                                }
                                if (mLastLocation == null)
                                    startLocationUpdates();
                                else if (mLocation == null)
                                    mLocation = new Location(mLastLocation);
                                refreshActivity(true);
                            }
                        });
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (Permissions.grantLocationGeofenceEditorPermissions(getApplicationContext(), this)) {
            try {
                if (mFusedLocationClient != null)
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private  void getGeofenceAddress(/*boolean updateName*/) {
        try {
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
            //mAddressRequested = true;
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    private void startIntentService(boolean updateName) {
        /*Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, mResultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, mLocation);
        intent.putExtra(UPDATE_NAME_EXTRA, updateName);
        startService(intent);*/

        Data workData = new Data.Builder()
                .putDouble(LATITUDE_EXTRA, mLocation.getLatitude())
                .putDouble(LONGITUDE_EXTRA, mLocation.getLongitude())
                .putBoolean(UPDATE_NAME_EXTRA, updateName)
                .build();

        OneTimeWorkRequest fetchAddressWorker =
                new OneTimeWorkRequest.Builder(FetchAddressWorker.class)
                        .addTag(LocationGeofenceEditorActivity.FETCH_ADDRESS_WORK_TAG)
                        .setInputData(workData)
                        .build();

        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    workManager.enqueueUniqueWork(LocationGeofenceEditorActivity.FETCH_ADDRESS_WORK_TAG, ExistingWorkPolicy.REPLACE, fetchAddressWorker);

                    workManager.getWorkInfoByIdLiveData(fetchAddressWorker.getId())
                            .observe(this, new Observer<WorkInfo>() {
                                @Override
                                public void onChanged(@Nullable WorkInfo workInfo) {
                                    //PPApplication.logE("LocationGeofenceEditorActivity.getWorkInfoByIdLiveData", "xxx");

                                    if ((workInfo != null) && (workInfo.getState() == WorkInfo.State.SUCCEEDED)) {
                                        //PPApplication.logE("LocationGeofenceEditorActivity.getWorkInfoByIdLiveData", "WorkInfo.State.SUCCEEDED");

                                        Data outputData = workInfo.getOutputData();
                                        //PPApplication.logE("LocationGeofenceEditorActivity.getWorkInfoByIdLiveData", "outputData=" + outputData);

                                        int resultCode = outputData.getInt(RESULT_CODE, FAILURE_RESULT);
                                        //PPApplication.logE("LocationGeofenceEditorActivity.getWorkInfoByIdLiveData", "resultCode=" + resultCode);

                                        boolean enableAddressButton = false;
                                        if (resultCode == SUCCESS_RESULT) {
                                            //PPApplication.logE("LocationGeofenceEditorActivity.getWorkInfoByIdLiveData", "resultCode=" + resultCode);

                                            // Display the address string
                                            // or an error message sent from the intent service.
                                            String addressOutput = outputData.getString(RESULT_DATA_KEY);
                                            //PPApplication.logE("LocationGeofenceEditorActivity.getWorkInfoByIdLiveData", "addressOutput=" + addressOutput);

                                            addressText.setText(addressOutput);

                                            if (outputData.getBoolean(UPDATE_NAME_EXTRA, false))
                                                geofenceNameEditText.setText(addressOutput);

                                            updateEditedMarker(false);

                                            enableAddressButton = true;
                                        }

                                        GlobalGUIRoutines.setImageButtonEnabled(enableAddressButton, addressButton, getApplicationContext());
                                    }
                                }
                            });
                }
            }
        } catch (Exception e) {
            Log.e("LocationGeofenceEditorActivity.startIntentService", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

    }


    //------------------------------------------

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        if (!isFinishing()) {
            // Create a fragment for the error dialog
            ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
            // Pass the error that should be displayed
            Bundle args = new Bundle();
            args.putInt(DIALOG_ERROR, errorCode);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "errorDialog");
        }
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    private void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    @SuppressWarnings("WeakerAccess")
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = -999;
            if (this.getArguments() != null)
                errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            if (getActivity() != null)
                ((LocationGeofenceEditorActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (NullPointerException e) {
            // fixes Google Maps bug: http://stackoverflow.com/a/20905954/2075875
            String pkg = null;
            if (intent != null)
                pkg = intent.getPackage();
            if (intent == null || (pkg != null && pkg.equals("com.android.vending"))) {
                Log.e("LocationGeofenceEditorActivity", "ignoring startActivityForResult exception ", e);
                //PPApplication.recordException(e);
            }
            else {
                PPApplication.recordException(e);
                throw e;
            }
        }
    }

    private boolean isBigger(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(MAX_RADIUS)) > 0;
    }

    private boolean isSmaller(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(MIN_RADIUS)) < 0;
    }

    private float getCircleZoomValue(double latitude, double longitude, double radius,
                                   float minZoom, float maxZoom) {
        LatLng position = new LatLng(latitude, longitude);
        float currZoom = (minZoom + maxZoom) / 2;
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, currZoom);
        mMap.moveCamera(camera);
        float[] results = new float[1];
        LatLng topLeft = mMap.getProjection().getVisibleRegion().farLeft;
        LatLng topRight = mMap.getProjection().getVisibleRegion().farRight;
        Location.distanceBetween(topLeft.latitude, topLeft.longitude, topRight.latitude,
                                    topRight.longitude, results);
        // Difference between visible width in meters and 2.5 * radius.
        double delta = results[0] - 2.5 * radius;
        double accuracy = 10; // 10 meters.
        if (delta < -accuracy)
            return getCircleZoomValue(latitude, longitude, radius, minZoom, currZoom);
        else
        if (delta > accuracy)
            return getCircleZoomValue(latitude, longitude, radius, currZoom, maxZoom);
        else
            return currZoom;
    }

}

