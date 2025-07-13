package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

/** @noinspection ExtractMethodRecommender*/
public class LocationGeofenceEditorActivityOSM extends AppCompatActivity
                        implements LocationGeofenceEditorActivityOSMNetworkOfflineListener{
    private LocationManager mLocationManager;
    private boolean mListenerEnabled = false;

    private MapView mMap = null;
    private LocationGeofenceEditorCurrentLocationOverlayOSM currentLocationOverlay = null;
    private LocationGeofenceEditorGeofenceOverlayOSM geofenceOverlay = null;
    private Marker editedMarker = null;

    private Handler errorLocationHandler = null;
    private Runnable errorLocationRunnable = null;
    private boolean errorLocationDisplayed = false;

    static final int WORKRES_SUCCESS_RESULT = 0;
    static final int WORKRES_FAILURE_RESULT = 1;
    static final String WORKRES_RESULT_CODE = PPApplication.PACKAGE_NAME + ".RESULT_CODE";
    static final String WORKRES_RESULT_DATA_KEY = PPApplication.PACKAGE_NAME + ".RESULT_DATA_KEY";
    static final String WORKRES_LATITUDE_EXTRA = PPApplication.PACKAGE_NAME + ".LATITUDE_EXTRA";
    static final String WORKRES_LONGITUDE_EXTRA = PPApplication.PACKAGE_NAME + ".LONGITUDE_EXTRA";
    static final String WORKRES_UPDATE_NAME_EXTRA = PPApplication.PACKAGE_NAME + ".UPDATE_NAME_EXTRA";
    private static final int RESULT_LOCATION_SETTINGS = 2992;

    static final String ACTION_LOCATION_GEOFENCE_EDITOR_ONLINE_STATUS_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".LocationGeofenceEditorOnlineStatusBroadcastReceiver";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 500;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    //private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private Location mActualLocation = null;
    private Location mGeofenceLocation = null;

    private long geofenceId;
    Geofence geofence;

    //private AddressResultReceiver mResultReceiver;
    //private boolean mAddressRequested = false;

    private EditText geofenceNameEditText;
    private AppCompatImageButton addressButton;
    private TextView addressText;
    private Button okButton;
    //private TextView radiusLabel;
    TextView radiusValue;
    private LinearLayout mapIsLoading;

    private LocationGeofenceEditorOnlineStatusBroadcastReceiver checkOnlineStatusBroadcatReceiver = null;

    static final int MIN_RADIUS = 20;
    static final int MAX_RADIUS = 500 * 1000;

    // must be less then 20 because in 20+ map tiles are not loaded :-(
    private static final double MAX_ZOOM_LEVEL = 19.99999d;

    static final String FETCH_ADDRESS_WORK_TAG_OSM = "fetchAddressWorkOSM";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, false, false, false, false, true, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        IConfigurationProvider osmDroidConfigurationProvider = org.osmdroid.config.Configuration.getInstance();
        osmDroidConfigurationProvider.load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        osmDroidConfigurationProvider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        osmDroidConfigurationProvider.setOsmdroidTileCache(getApplicationContext().getExternalFilesDir(null));
        osmDroidConfigurationProvider.setOsmdroidBasePath(getApplicationContext().getExternalFilesDir(null));

        setContentView(R.layout.activity_location_geofence_editor_osm);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        /*
        Toolbar toolbar = findViewById(R.id.location_editor_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.location_editor_title);
            getSupportActionBar().setElevation(0);
        }
        */
        setTitle(R.string.location_editor_title);

        //mResultReceiver = new AddressResultReceiver(new Handler(getMainLooper()));

        Intent intent = getIntent();
        geofenceId = intent.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);

        if (geofenceId > 0) {
            geofence = DatabaseHandler.getInstance(getApplicationContext()).getGeofence(geofenceId);
            mGeofenceLocation = new Location("LOC");
            mGeofenceLocation.setLatitude(geofence._latitude);
            mGeofenceLocation.setLongitude(geofence._longitude);
        }
        if (geofence == null) {
            geofenceId = 0;
            geofence = new Geofence();
            int _count = DatabaseHandler.getInstance(getApplicationContext()).getGeofenceCount() + 1;
            geofence._name = getString(R.string.event_preferences_location_new_location_name) + "_" + _count;
            geofence._radius = 100;
        }

        mapIsLoading = findViewById(R.id.location_editor_map_loading);

        mMap = findViewById(R.id.location_editor_map);
        //noinspection DataFlowIssue
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        //mMap.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mMap.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mMap.setMaxZoomLevel(MAX_ZOOM_LEVEL);
        mMap.setMultiTouchControls(true);
        //mMap.setTilesScaledToDpi(true);
        //mMap.getTileProvider().clearTileCache();

        //nightModeOn = GlobalGUIRoutines.isNightModeEnabled(getApplicationContext());
//                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
//                                    == Configuration.UI_MODE_NIGHT_YES;
        String applicationTheme = ApplicationPreferences.applicationTheme(this, true);
        boolean nightModeOn = !applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE);

        /*boolean isNightMode;
        String applicationThene = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
        switch (applicationThene) {
            case "white":
                isNightMode = false;
                break;
            case "dark":
                isNightMode= true;
                break;
            case "night_mode":
                int nightModeFlags =
                        getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                //if (notificationNightMode) {
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        isNightMode = true;
                        // this is possible only when device has option for set background color
                        //if ((Build.VERSION.SDK_INT < 29) && notificationNightMode)
                        //    notificationTextColor = "2";
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        isNightMode = false;
                        // this is possible only when device has option for set background color
                        //if ((Build.VERSION.SDK_INT < 29) && notificationNightMode)
                        //    notificationTextColor = "1";
                        break;
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        isNightMode = false;
                        break;
                    default:
                        isNightMode = false;
                }
                break;
            default:
                isNightMode = false;
        }*/
        if (/*isNightMode*/nightModeOn)
            mMap.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        else
            mMap.getOverlayManager().getTilesOverlay().setColorFilter(null);
        //mMap.getOverlayManager().getTilesOverlay().setLoadingBackgroundColor(GlobalGUIRoutines.getThemeDialogBackgroundColor(this));
        //mMap.getOverlayManager().getTilesOverlay().setLoadingLineColor(GlobalGUIRoutines.getThemeNormalTextColor(this));

        IMapController mapController = mMap.getController();
        //mapController.setZoom(15f);

        /*
        mMap.getOverlayManager().getTilesOverlay().getTileStates().getRunAfters().add(() -> {
            if (mMap.getOverlayManager().getTilesOverlay().getTileStates().isDone()) {
                if (mapIsLoading.getVisibility() != View.GONE)
                    mapIsLoading.setVisibility(View.GONE);
                if (mMap.getVisibility() != View.VISIBLE) {
                    mMap.setVisibility(View.VISIBLE);
                    addressText.setVisibility(View.VISIBLE);
                }
            }
        });
        */

        mMap.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint point) {
                if (mGeofenceLocation == null)
                    mGeofenceLocation = new Location("LOC");
                mGeofenceLocation.setLatitude(point.getLatitude());
                mGeofenceLocation.setLongitude(point.getLongitude());
                refreshActivity(true, false);

                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));

        //radiusLabel = findViewById(R.id.location_pref_dlg_radius_seekbar_label);

        TextView radiusLabel = findViewById(R.id.location_pref_dlg_radius_label);
        //noinspection DataFlowIssue
        radiusLabel.setText(getString(R.string.event_preferences_location_radius_label) + ":");

        radiusValue = findViewById(R.id.location_pref_dlg_radius_value);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(radiusValue, getString(R.string.location_pref_dlg_edit_radius_tooltip));
        radiusValue.setText(String.valueOf(Math.round(geofence._radius)));

        radiusValue.setOnClickListener(view -> {
            if (!(isFinishing())) {
                BetterNumberPickerDialog dialog = new BetterNumberPickerDialog(this);
                dialog.showDialog();
                //valueDialog.show();
            }
        });


        /*
        // seekBar is logarithmic
        SeekBar radiusSeekBar = findViewById(R.id.location_pref_dlg_radius_seekbar);
        final float minRadius = 20;
        final float maxRadius = 1020;
        int value = (int)(Math.sqrt(((geofence._radius - minRadius) / (maxRadius - minRadius)) * 1000.0f * 1000.0f));
        radiusSeekBar.setProgress(value);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Approximate an exponential curve with x^2.
                geofence._radius = ((progress * progress) / (1000.0f * 1000.0f)) * (maxRadius - minRadius) + minRadius;
                updateEditedMarker(false);
                //Log.d("LocationGeofenceEditorActivityOSM.onProgressChanged", "radius="+geofence._radius);
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
        //noinspection DataFlowIssue
        geofenceNameEditText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.edit_text_color));
        geofenceNameEditText.setText(geofence._name);

        addressText = findViewById(R.id.location_editor_address_text);

        okButton = findViewById(R.id.location_editor_ok);
        //noinspection DataFlowIssue
        okButton.setOnClickListener(v -> {
            String name = geofenceNameEditText.getText().toString();
            if ((!name.isEmpty()) && (mGeofenceLocation != null)) {
                geofence._name = name;
                geofence._latitude = mGeofenceLocation.getLatitude();
                geofence._longitude = mGeofenceLocation.getLongitude();

                if (geofenceId > 0) {
                    DatabaseHandler.getInstance(getApplicationContext()).updateGeofence(geofence);
                } else {
                    DatabaseHandler.getInstance(getApplicationContext()).addGeofence(geofence);
                    /*synchronized (PPApplication.locationScannerMutex) {
                        // start location updates
                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.isLocationScannerStarted())
                            PhoneProfilesService.getGeofencesScanner().connectForResolve();
                    }*/
                }

                // check edited geofence in table
                DatabaseHandler.getInstance(getApplicationContext()).checkGeofence(String.valueOf(geofence._id), 1, false);

                Intent returnIntent = new Intent();
                returnIntent.putExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, geofence._id);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        Button cancelButton = findViewById(R.id.location_editor_cancel);
        //noinspection DataFlowIssue
        cancelButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        });

        AppCompatImageButton myLocationButton = findViewById(R.id.location_editor_my_location);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(myLocationButton, getString(R.string.location_editor_change_location_dialog_title));
        myLocationButton.setOnClickListener(v -> {
            SingleSelectListDialog changeLocationDialog = new SingleSelectListDialog(
                    false,
                    getString(R.string.location_editor_change_location_dialog_title),
                    null,
                    R.array.locationEditorChangeLocationArray,
                    SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                    (dialog, which) -> {
                        IMapController _mapController = mMap.getController();
                        switch (which) {
                            case 0:
                                if (mGeofenceLocation != null)
                                    _mapController.setCenter(new GeoPoint(mGeofenceLocation));
                                break;
                            case 1:
                                if (mActualLocation != null)
                                    _mapController.setCenter(new GeoPoint(mActualLocation));
                                else {
                                    PPAlertDialog alert = new PPAlertDialog(
                                            getString(R.string.location_editor_title),
                                            getString(R.string.location_editor_title_not_actual_location),
                                            getString(android.R.string.ok),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            true, true,
                                            false, false,
                                            true,
                                            false,
                                            this
                                    );
                                    if (!isFinishing())
                                        alert.showDialog();
                                }
                                break;
                            case 2:
                                //getLastLocation();
                                if (mActualLocation != null) {
                                    mGeofenceLocation = new Location(mActualLocation);
                                    refreshActivity(true, true);
                                } else {
                                    PPAlertDialog alert = new PPAlertDialog(
                                            getString(R.string.location_editor_title),
                                            getString(R.string.location_editor_title_not_actual_location),
                                            getString(android.R.string.ok),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            true, true,
                                            false, false,
                                            true,
                                            false,
                                            this
                                    );
                                    if (!isFinishing())
                                        alert.showDialog();
                                }
                                break;
                            default:
                        }
                    },
                    null,
                    //false,
                    this);

            changeLocationDialog.showDialog();
        });

        addressButton = findViewById(R.id.location_editor_address_btn);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(addressButton, getString(R.string.location_editor_rename_with_address_button_tooltip));
        addressButton.setOnClickListener(v -> {
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.location_editor_rename_with_address_button_tooltip),
                    getString(R.string.location_editor_rename_with_address_message),
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> getGeofenceAddress(/*true*/),
                    (dialog2, which) -> finish(),
                    null,
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    false,
                    this
            );

            if (!isFinishing())
                dialog.showDialog();
        });

        if (geofence != null)
            mapController.setCenter(new GeoPoint(geofence._latitude, geofence._longitude));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //GlobalGUIRoutines.lockScreenOrientation(this, true);

        if (checkOnlineStatusBroadcatReceiver == null) {
            checkOnlineStatusBroadcatReceiver = new LocationGeofenceEditorOnlineStatusBroadcastReceiver(this);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(checkOnlineStatusBroadcatReceiver,
                    new IntentFilter(ACTION_LOCATION_GEOFENCE_EDITOR_ONLINE_STATUS_BROADCAST_RECEIVER));
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Permissions.grantLocationGeofenceEditorPermissionsOSM(getApplicationContext(), this)) {
            // update map with location, if is set from geofence
            Handler handler = new Handler(getMainLooper());
            final WeakReference<LocationGeofenceEditorActivityOSM> activityWeakRef = new WeakReference<>(this);
            Runnable runnable = () -> {
                //PPApplicationStatic.logE("[HANDLER] PPApplication.startHandlerThread", "START run - from=LocationGeofenceEditorActivityOSM.onStart");
                LocationGeofenceEditorActivityOSM activity = activityWeakRef.get();
                if ((activity != null) && !activity.isFinishing() && !activity.isDestroyed())
                    activity.doUpdatedLocation(null);
            };
            handler.postDelayed(runnable, 500);

            startLocationUpdates(true);
            //
            refreshActivity(false, false);
            showDialogIfIsNotOnline(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(checkOnlineStatusBroadcatReceiver);
        } catch (Exception ignored) {}
        checkOnlineStatusBroadcatReceiver = null;

        //GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!mListenerEnabled)
                startLocationUpdates(false);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        mMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
        try {
            stopLocationUpdates();
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EditorActivity.itemDragPerformed = false;

        mMap.onPause();
        try {
            stopLocationUpdates();
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(checkOnlineStatusBroadcatReceiver);
        } catch (Exception ignored) {}
        checkOnlineStatusBroadcatReceiver = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY)) {
            stopLocationUpdates();
            GlobalUtils.sleep(1000);
            startLocationUpdates(true);
            refreshActivity(false, false);
        }

        if (requestCode == RESULT_LOCATION_SETTINGS) {
            stopLocationUpdates();
            GlobalUtils.sleep(1000);
            startLocationUpdates(true);
        }
    }

    private double calcZoom() {
        double zoom;
        if (mGeofenceLocation == null)
            zoom = 15f;
        else {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int mapWidth = Math.round(mMap.getWidth() / metrics.scaledDensity);

//            int mapHeight = Math.round(mMap.getHeight() / metrics.scaledDensity);

            zoom = calcZoom(geofence._radius * 2, mapWidth, mGeofenceLocation.getLatitude());

            if (zoom > MAX_ZOOM_LEVEL)
                zoom = MAX_ZOOM_LEVEL;
        }

        return zoom;
    }

    void updateEditedMarker(boolean setMapCamera) {
        if (mMap != null) {

//            Log.e("LocationGeofenceEditorActivityOSM.updateEditedMarker", "setMapCamera="+setMapCamera);
//            Log.e("LocationGeofenceEditorActivityOSM.updateEditedMarker", "zoom level="+mMap.getZoomLevelDouble());

            if (mActualLocation != null) {
                if (currentLocationOverlay != null) {
                    mMap.getOverlays().remove(currentLocationOverlay);
                    //mMap.invalidate();
                }
                currentLocationOverlay = new LocationGeofenceEditorCurrentLocationOverlayOSM(new GeoPoint(mActualLocation), mActualLocation.getAccuracy(),
                        ContextCompat.getColor(this, R.color.mapLastLocationMarkerFillColor),
                        ContextCompat.getColor(this, R.color.mapLastLocationMarkerStrokeColor));
                mMap.getOverlays().add(currentLocationOverlay);
                mMap.invalidate();
            }

            if (mGeofenceLocation != null) {
                if (geofenceOverlay != null) {
                    mMap.getOverlays().remove(geofenceOverlay);
                    //mMap.invalidate();
                }
                geofenceOverlay = new LocationGeofenceEditorGeofenceOverlayOSM(new GeoPoint(mGeofenceLocation), geofence._radius,
                        ContextCompat.getColor(this, R.color.mapEditedLocationMarkerFillColor),
                        ContextCompat.getColor(this, R.color.mapEditedLocationMarkerStrokeColor));
                mMap.getOverlays().add(geofenceOverlay);

                if (editedMarker != null) {
                    mMap.getOverlays().remove(editedMarker);
                    //mMap.invalidate();
                }
                editedMarker = new Marker(mMap);
                editedMarker.setPosition(new GeoPoint(mGeofenceLocation));
                editedMarker.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_edited_location_marker));
                //editedMarker.setDefaultIcon();
                editedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                //editedMarker.setTitle(geofenceNameEditText.getText().toString());
                editedMarker.setOnMarkerClickListener((marker, mapView) -> false);
                mMap.getOverlays().add(editedMarker);

                if (setMapCamera) {
                    IMapController mapController = mMap.getController();
                    try {
                        mapController.setZoom(calcZoom());

                        GeoPoint startPoint = new GeoPoint(mGeofenceLocation.getLatitude(), mGeofenceLocation.getLongitude());
                        mapController.setCenter(startPoint);
                    } catch (StackOverflowError e) {
                        GeoPoint startPoint = new GeoPoint(mGeofenceLocation.getLatitude(), mGeofenceLocation.getLongitude());
                        mapController.setCenter(startPoint);
                    }
                }

                mMap.invalidate();
                mMap.postInvalidate();
            }
        }
    }
    //----------------------------------------------------

    private void refreshActivity(boolean updateEditedMarker, boolean setMapCamera) {
        if (CheckOnlineStatusBroadcastReceiver.isOnline(getApplicationContext())) {
            boolean enableAddressButton = false;
            //if (mLocation != null) {
                // Determine whether a geo-coder is available.
                if (Geocoder.isPresent()) {
                    if (mGeofenceLocation != null) {
                        startWorkerForFetchAddress(false);
                        enableAddressButton = true;
                    }
                }
            //}
            if (addressButton.isEnabled())
                GlobalGUIRoutines.setImageButtonEnabled(enableAddressButton, addressButton, this);

            if (updateEditedMarker) {
                radiusValue.setText(String.valueOf(Math.round(geofence._radius)));
                updateEditedMarker(setMapCamera);
            }

            String name = geofenceNameEditText.getText().toString();
            okButton.setEnabled((!name.isEmpty()) && (mGeofenceLocation != null));
        }
        else {
            okButton.setEnabled(false);
        }
    }

    private void showErrorLocationDialog() {
        if (!errorLocationDisplayed) {
            errorLocationDisplayed = true;

            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.location_editor_title),
                    getString(R.string.location_editor_enable_location_summary),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    null, null,
                    (dialog1, which) -> {
                        boolean ok = false;
                        //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_LOCATION_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        }
                        if (!ok) {
                            PPAlertDialog _dialog = new PPAlertDialog(
                                    getString(R.string.location_settings_button_tooltip),
                                    getString(R.string.setting_screen_not_found_alert),
                                    getString(android.R.string.ok),
                                    null,
                                    null, null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true, true,
                                    false, false,
                                    true,
                                    false,
                                    this
                            );

                            if (!isFinishing())
                                _dialog.showDialog();
                        }

                        errorLocationDisplayed = false;
                    },
                    (dialog12, which) -> errorLocationDisplayed = false,
                    null,
                    dialog13 -> errorLocationDisplayed = false,
                    null,
                    null,
                    true, true,
                    false, false,
                    false,
                    false,
                    this
            );

            if (!isFinishing())
                dialog.showDialog();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(boolean showErrorDialog) {

        if (showErrorDialog) {
            errorLocationHandler = new Handler(getMainLooper());
            final WeakReference<LocationGeofenceEditorActivityOSM> activityWeakRef = new WeakReference<>(this);
            errorLocationRunnable = () -> {
//            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=LocationGeofenceEditorActivityOSM.startLocationUpdates");
                LocationGeofenceEditorActivityOSM activity = activityWeakRef.get();
                if ((activity != null) && !activity.isFinishing() && !activity.isDestroyed()) {
                    if (activity.mActualLocation == null) {
                        activity.showErrorLocationDialog();
                    }
                }
            };
            errorLocationHandler.postDelayed(errorLocationRunnable, 30000);
        }

        boolean locationEnabled = false;
        String provider; // = "";
        /*try {
            locationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (locationEnabled)
                provider = LocationManager.GPS_PROVIDER;
        } catch (Exception e) {
            // we may get IllegalArgumentException if gps location provider
            // does not exist or is not yet installed.
            locationEnabled = false;
        }
        if (!locationEnabled) {
            try {
                locationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (locationEnabled)
                    provider = LocationManager.NETWORK_PROVIDER;
            } catch (Exception e) {
                // we may get IllegalArgumentException if network location provider
                // does not exist or is not yet installed.
                //locationEnabled = false;
            }
        }*/

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        provider = mLocationManager.getBestProvider(criteria, false);
        if ((provider != null) && (!provider.isEmpty()))
            locationEnabled = true;
        else
            provider = "";

        if (!mListenerEnabled && locationEnabled) {
            if (Permissions.checkLocation(getApplicationContext())) {
                try {
                    mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 0, mLocationListener);

//                    if (Build.VERSION.SDK_INT >= 30) {
//                        mLocationManager.getCurrentLocation(
//                                LocationManager.GPS_PROVIDER,
//                                null,
//                                ContextCompat.getMainExecutor(this),
//                                new Consumer<Location>() {
//                                    @Override
//                                    public void accept(Location location) {
//                                        PPApplicationStatic.logE("******* LocationGeofenceEditorActivityOSM.startLocationUpdates", "location="+location);
//                                    }
//                                }
//                        );
//                    }
                    mListenerEnabled = true;
                } catch (Exception e) {
                    mListenerEnabled = false;
                    PPApplicationStatic.recordException(e);
                }
            }
        }

        if (((!mListenerEnabled) || (!locationEnabled)) && showErrorDialog) {
            showErrorLocationDialog();
        }
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        if (mListenerEnabled) {
            if (mLocationManager != null)
                mLocationManager.removeUpdates(mLocationListener);
            mListenerEnabled = false;
        }
        if ((errorLocationHandler != null) && (errorLocationRunnable != null)) {
            errorLocationHandler.removeCallbacks(errorLocationRunnable);
            errorLocationHandler = null;
            errorLocationRunnable = null;
        }
    }

    private void getGeofenceAddress(/*boolean updateName*/) {
        try {
            if (mGeofenceLocation != null) {
                startWorkerForFetchAddress(true);
            }
            //mAddressRequested = true;
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    private void startWorkerForFetchAddress(boolean updateName) {
        /*Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, mResultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, mLocation);
        intent.putExtra(UPDATE_NAME_EXTRA, updateName);
        startService(intent);*/

        Data workData = new Data.Builder()
                .putDouble(WORKRES_LATITUDE_EXTRA, mGeofenceLocation.getLatitude())
                .putDouble(WORKRES_LONGITUDE_EXTRA, mGeofenceLocation.getLongitude())
                .putBoolean(WORKRES_UPDATE_NAME_EXTRA, updateName)
                .build();

        OneTimeWorkRequest fetchAddressWorkerOSM =
                new OneTimeWorkRequest.Builder(FetchAddressWorkerOSM.class)
                        .addTag(FETCH_ADDRESS_WORK_TAG_OSM)
                        .setInputData(workData)
                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                        .build();

        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                    //if (PPApplicationStatic.logEnabled()) {
//                    ListenableFuture<List<WorkInfo>> statuses;
//                    statuses = workManager.getWorkInfosForUniqueWork(LocationGeofenceEditorActivityOSM.FETCH_ADDRESS_WORK_TAG);
//                    try {
//                        List<WorkInfo> workInfoList = statuses.get();
//                    } catch (Exception ignored) {
//                    }
//                    //}

//                    PPApplicationStatic.logE("[WORKER_CALL] LocationGeofenceEditorActivityOSM.startWorkerForFetchAddress", "xxx");
                    //workManager.enqueue(fetchAddressWorkerOSM);
                    workManager.enqueueUniqueWork(FETCH_ADDRESS_WORK_TAG_OSM, ExistingWorkPolicy./*APPEND_OR_*/REPLACE, fetchAddressWorkerOSM);

                    workManager.getWorkInfoByIdLiveData(fetchAddressWorkerOSM.getId())
                            .observe(this, workInfo -> {

                                if ((workInfo != null) && (workInfo.getState() == WorkInfo.State.SUCCEEDED)) {

                                    Data outputData = workInfo.getOutputData();

                                    int resultCode = outputData.getInt(WORKRES_RESULT_CODE, WORKRES_FAILURE_RESULT);

                                    boolean enableAddressButton = false;
                                    if (resultCode == WORKRES_SUCCESS_RESULT) {

                                        // Display the address string
                                        // or an error message sent from the intent service.
                                        String addressOutput = outputData.getString(WORKRES_RESULT_DATA_KEY);

                                        addressText.setText(addressOutput);

                                        if (outputData.getBoolean(WORKRES_UPDATE_NAME_EXTRA, false))
                                            geofenceNameEditText.setText(addressOutput);

                                        //updateEditedMarker(false);

                                        enableAddressButton = true;
                                    }

                                    GlobalGUIRoutines.setImageButtonEnabled(enableAddressButton, addressButton, this);
                                }
                            });
                }
            }
        } catch (Exception e) {
            //Log.e("LocationGeofenceEditorActivityOSM.startWorkerForFetchAddress", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }

    }


    //------------------------------------------

    @SuppressWarnings("deprecation")
    @Override
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (NullPointerException e) {
            // fixes Google Maps bug: http://stackoverflow.com/a/20905954/2075875
            String pkg;// = null;
            //if (intent != null)
                pkg = intent.getPackage();
            //noinspection StatementWithEmptyBody
            if (/*intent == null ||*/ ((pkg != null) && (pkg.equals("com.android.vending")))) {
                //PPApplicationStatic.recordException(e);
            } else {
                PPApplicationStatic.recordException(e);
                throw e;
            }
        }
    }

    boolean isBigger(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(MAX_RADIUS)) > 0;
    }

    boolean isSmaller(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(MIN_RADIUS)) < 0;
    }

    private int getParallelLength(double atLatitude)
    {

        int FR_LAT = 0; // from latitude
        int TO_LAT = 1; // to latidude
        int PA_LEN = 2; // parallel length in meters)
        //int PC_ERR = 3; // percentage error

        //  fr_lat| to_lat            |  par_len| perc_err
        double[][] tbl = {
                { 0.00, 12.656250000000000, 40075016, 2.410},
                {12.66, 17.402343750000000, 39107539, 2.180},
                {17.40, 22.148437500000000, 38252117, 2.910},
                {22.15, 25.708007812500000, 37135495, 2.700},
                {25.71, 28.377685546875000, 36130924, 2.330},
                {28.38, 31.047363281250000, 35285940, 2.610},
                {31.05, 33.717041015625000, 34364413, 2.890},
                {33.72, 35.719299316406250, 33368262, 2.380},
                {35.72, 37.721557617187500, 32573423, 2.560},
                {37.72, 39.723815917968750, 31738714, 2.750},
                {39.72, 41.726074218750000, 30865121, 2.950},
                {41.73, 43.227767944335938, 29953681, 2.360},
                {43.23, 44.729461669921875, 29245913, 2.480},
                {44.73, 46.231155395507812, 28517939, 2.620},
                {46.23, 47.732849121093750, 27770248, 2.760},
                {47.73, 49.234542846679688, 27003344, 2.900},
                {49.23, 50.360813140869141, 26217745, 2.290},
                {50.36, 51.487083435058594, 25616595, 2.380},
                {51.49, 52.613353729248047, 25005457, 2.480},
                {52.61, 53.739624023437500, 24384564, 2.580},
                {53.74, 54.865894317626953, 23754152, 2.690},
                {54.87, 55.992164611816406, 23114464, 2.800},
                {55.99, 57.118434906005859, 22465745, 2.920},
                {57.12, 57.963137626647949, 21808245, 2.280},
                {57.96, 58.807840347290039, 21309508, 2.360},
                {58.81, 59.652543067932129, 20806081, 2.440},
                {59.65, 60.497245788574219, 20298074, 2.520},
                {60.50, 61.341948509216309, 19785597, 2.610},
                {61.34, 62.186651229858398, 19268762, 2.700},
                {62.19, 63.031353950500488, 18747680, 2.800},
                {63.03, 63.876056671142578, 18222465, 2.900},
                {63.88, 64.509583711624146, 17693232, 2.250},
                {64.51, 65.143110752105713, 17293739, 2.320},
                {65.14, 65.776637792587280, 16892100, 2.390},
                {65.78, 66.410164833068848, 16488364, 2.460},
                {66.41, 67.043691873550415, 16082582, 2.530},
                {67.04, 67.677218914031982, 15674801, 2.610},
                {67.68, 68.310745954513550, 15265074, 2.690},
                {68.31, 68.944272994995117, 14853450, 2.780},
                {68.94, 69.577800035476685, 14439980, 2.870},
                {69.58, 70.211327075958252, 14024715, 2.970},
                {70.21, 70.686472356319427, 13607707, 2.300},
                {70.69, 71.161617636680603, 13293838, 2.360},
                {71.16, 71.636762917041779, 12979039, 2.430},
                {71.64, 72.111908197402954, 12663331, 2.500},
                {72.11, 72.587053477764130, 12346738, 2.570},
                {72.59, 73.062198758125305, 12029281, 2.640},
                {73.06, 73.537344038486481, 11710981, 2.720},
                {73.54, 74.012489318847656, 11391862, 2.800},
                {74.01, 74.487634599208832, 11071946, 2.890},
                {74.49, 74.962779879570007, 10751254, 2.980},
                {74.96, 75.319138839840889, 10429810, 2.310},
                {75.32, 75.675497800111771, 10188246, 2.370},
                {75.68, 76.031856760382652,  9946280, 2.430},
                {76.03, 76.388215720653534,  9703923, 2.500},
                {76.39, 76.744574680924416,  9461183, 2.560},
                {76.74, 77.100933641195297,  9218071, 2.640},
                {77.10, 77.457292601466179,  8974595, 2.710},
                {77.46, 77.813651561737061,  8730766, 2.790},
                {77.81, 78.170010522007942,  8486593, 2.880},
                {78.17, 78.526369482278824,  8242085, 2.970},
                {78.53, 78.793638702481985,  7997252, 2.290},
                {78.79, 79.060907922685146,  7813420, 2.350},
                {79.06, 79.328177142888308,  7629414, 2.410},
                {79.33, 79.595446363091469,  7445240, 2.470},
                {79.60, 79.862715583294630,  7260900, 2.540},
                {79.86, 80.129984803497791,  7076399, 2.600},
                {80.13, 80.397254023700953,  6891742, 2.680},
                {80.40, 80.664523243904114,  6706931, 2.750},
                {80.66, 80.931792464107275,  6521972, 2.830},
                {80.93, 81.199061684310436,  6336868, 2.920},
                {81.20, 81.399513599462807,  6151624, 2.250},
                {81.40, 81.599965514615178,  6012600, 2.310},
                {81.60, 81.800417429767549,  5873502, 2.360},
                {81.80, 82.000869344919920,  5734331, 2.420},
                {82.00, 82.201321260072291,  5595088, 2.480},
                {82.20, 82.401773175224662,  5455775, 2.550},
                {82.40, 82.602225090377033,  5316394, 2.620},
                {82.60, 82.802677005529404,  5176947, 2.690},
                {82.80, 83.003128920681775,  5037435, 2.770},
                {83.00, 83.203580835834146,  4897860, 2.850},
                {83.20, 83.404032750986516,  4758224, 2.930},
                {83.40, 83.554371687350795,  4618528, 2.260},
                {83.55, 83.704710623715073,  4513719, 2.320},
                {83.70, 83.855049560079351,  4408878, 2.370},
                {83.86, 84.005388496443629,  4304006, 2.430},
                {84.01, 84.155727432807907,  4199104, 2.490},
                {84.16, 84.306066369172186,  4094172, 2.560},
                {84.31, 84.456405305536464,  3989211, 2.630},
                {84.46, 84.606744241900742,  3884223, 2.700},
                {84.61, 84.757083178265020,  3779207, 2.770},
                {84.76, 84.907422114629298,  3674165, 2.850},
                {84.91, 85.057761050993577,  3569096, 2.940},
                {85.06, 85.170515253266785,  3464003, 2.270},
                {85.17, 85.283269455539994,  3385167, 2.320},
                {85.28, 85.396023657813203,  3306318, 2.380},
                {85.40, 85.508777860086411,  3227456, 2.440},
                {85.51, 85.621532062359620,  3148581, 2.500},
                {85.62, 85.734286264632829,  3069693, 2.570},
                {85.73, 85.847040466906037,  2990793, 2.630},
                {85.85, 85.959794669179246,  2911882, 2.710},
                {85.96, 86.072548871452454,  2832959, 2.780},
                {86.07, 86.185303073725663,  2754025, 2.860},
                {86.19, 86.298057275998872,  2675080, 2.950},
                {86.30, 86.382622927703778,  2596124, 2.280},
                {86.38, 86.467188579408685,  2536901, 2.330},
                {86.47, 86.551754231113591,  2477672, 2.390},
                {86.55, 86.636319882818498,  2418437, 2.440},
                {86.64, 86.720885534523404,  2359197, 2.510},
                {86.72, 86.805451186228311,  2299952, 2.570},
                {86.81, 86.890016837933217,  2240701, 2.640},
                {86.89, 86.974582489638124,  2181446, 2.710},
                {86.97, 87.059148141343030,  2122186, 2.790},
                {87.06, 87.143713793047937,  2062921, 2.870},
                {87.14, 87.228279444752843,  2003652, 2.950},
                {87.23, 87.291703683531523,  1944378, 2.280},
                {87.29, 87.355127922310203,  1899919, 2.340},
                {87.36, 87.418552161088883,  1855459, 2.390},
                {87.42, 87.481976399867563,  1810996, 2.450},
                {87.48, 87.545400638646242,  1766531, 2.510},
                {87.55, 87.608824877424922,  1722063, 2.580},
                {87.61, 87.672249116203602,  1677594, 2.650},
                {87.67, 87.735673354982282,  1633122, 2.720},
                {87.74, 87.799097593760962,  1588648, 2.790},
                {87.80, 87.862521832539642,  1544172, 2.880},
                {87.86, 87.925946071318322,  1499695, 2.960},
                {87.93, 87.973514250402332,  1455215, 2.290},
                {87.97, 88.021082429486341,  1421854, 2.340},
                {88.02, 88.068650608570351,  1388493, 2.400},
                {88.07, 88.116218787654361,  1355130, 2.460},
                {88.12, 88.163786966738371,  1321766, 2.520},
                {88.16, 88.211355145822381,  1288401, 2.580},
                {88.21, 88.258923324906391,  1255036, 2.650},
                {88.26, 88.306491503990401,  1221669, 2.730},
                {88.31, 88.354059683074411,  1188302, 2.800},
                {88.35, 88.401627862158421,  1154934, 2.880},
                {88.40, 88.449196041242431,  1121565, 2.970},
                {88.45, 88.484872175555438,  1088195, 2.290},
                {88.48, 88.520548309868445,  1063167, 2.350},
                {88.52, 88.556224444181453,  1038139, 2.410},
                {88.56, 88.591900578494460,  1013110, 2.470},
                {88.59, 88.627576712807468,   988081, 2.530},
                {88.63, 88.663252847120475,   963052, 2.590},
                {88.66, 88.698928981433482,   938022, 2.660},
                {88.70, 88.734605115746490,   912992, 2.740},
                {88.73, 88.770281250059497,   887961, 2.810},
                {88.77, 88.805957384372505,   862930, 2.900},
                {88.81, 88.841633518685512,   837899, 2.980},
                {88.84, 88.868390619420268,   812867, 2.300},
                {88.87, 88.895147720155023,   794093, 2.360},
                {88.90, 88.921904820889779,   775319, 2.420},
                {88.92, 88.948661921624534,   756545, 2.480},
                {88.95, 88.975419022359290,   737771, 2.540},
                {88.98, 89.002176123094046,   718996, 2.610},
                {89.00, 89.028933223828801,   700221, 2.680},
                {89.03, 89.055690324563557,   681446, 2.750},
                {89.06, 89.082447425298312,   662671, 2.830},
                {89.08, 89.109204526033068,   643896, 2.910},
                {89.11, 89.129272351584135,   625121, 2.250},
                {89.13, 89.149340177135201,   611039, 2.300},
                {89.15, 89.169408002686268,   596957, 2.350},
                {89.17, 89.189475828237335,   582876, 2.410},
                {89.19, 89.209543653788401,   568794, 2.470},
                {89.21, 89.229611479339468,   554712, 2.530},
                {89.23, 89.249679304890535,   540630, 2.600},
                {89.25, 89.269747130441601,   526548, 2.670},
                {89.27, 89.289814955992668,   512466, 2.740},
                {89.29, 89.309882781543735,   498384, 2.820},
                {89.31, 89.329950607094801,   484302, 2.900},
                {89.33, 89.350018432645868,   470219, 2.990},
                {89.35, 89.365069301809172,   456137, 2.310},
                {89.37, 89.380120170972475,   445575, 2.370},
                {89.38, 89.395171040135779,   435013, 2.420},
                {89.40, 89.410221909299082,   424451, 2.480},
                {89.41, 89.425272778462386,   413889, 2.550},
                {89.43, 89.440323647625689,   403328, 2.610},
                {89.44, 89.455374516788993,   392766, 2.680},
                {89.46, 89.470425385952296,   382204, 2.760},
                {89.47, 89.485476255115600,   371642, 2.840},
                {89.49, 89.500527124278904,   361080, 2.920},
                {89.50, 89.511815276151381,   350518, 2.260},
                {89.51, 89.523103428023859,   342596, 2.310},
                {89.52, 89.534391579896337,   334674, 2.360},
                {89.53, 89.545679731768814,   326753, 2.420},
                {89.55, 89.556967883641292,   318831, 2.480},
                {89.56, 89.568256035513770,   310910, 2.540},
                {89.57, 89.579544187386247,   302988, 2.610},
                {89.58, 89.590832339258725,   295066, 2.680},
                {89.59, 89.602120491131203,   287145, 2.750},
                {89.60, 89.613408643003680,   279223, 2.830},
                {89.61, 89.624696794876158,   271301, 2.910},
                {89.62, 89.633162908780520,   263380, 2.250},
                {89.63, 89.641629022684882,   257438, 2.300},
                {89.64, 89.650095136589243,   251497, 2.360},
                {89.65, 89.658561250493605,   245556, 2.410},
                {89.66, 89.667027364397967,   239615, 2.470},
                {89.67, 89.675493478302329,   233673, 2.540},
                {89.68, 89.683959592206691,   227732, 2.600},
                {89.68, 89.692425706111052,   221791, 2.670},
                {89.69, 89.700891820015414,   215849, 2.750},
                {89.70, 89.709357933919776,   209908, 2.830},
                {89.71, 89.717824047824138,   203967, 2.910},
                {89.72, 89.724173633252406,   198026, 2.250},
                {89.72, 89.730523218680673,   193570, 2.300},
                {89.73, 89.736872804108941,   189114, 2.350},
                {89.74, 89.743222389537209,   184658, 2.410},
                {89.74, 89.749571974965477,   180202, 2.470},
                {89.75, 89.755921560393745,   175746, 2.530},
                {89.76, 89.762271145822012,   171290, 2.600},
                {89.76, 89.768620731250280,   166834, 2.670},
                {89.77, 89.774970316678548,   162378, 2.740},
                {89.77, 89.781319902106816,   157922, 2.820},
                {89.78, 89.787669487535084,   153466, 2.900},
                {89.79, 89.794019072963351,   149010, 2.990},
                {89.79, 89.798781262034552,   144554, 2.310},
                {89.80, 89.803543451105753,   141212, 2.360},
                {89.80, 89.808305640176954,   137869, 2.420},
                {89.81, 89.813067829248155,   134527, 2.480},
                {89.81, 89.817830018319356,   131185, 2.540},
                {89.82, 89.822592207390556,   127843, 2.610},
                {89.82, 89.827354396461757,   124501, 2.680},
                {89.83, 89.832116585532958,   121159, 2.750},
                {89.83, 89.836878774604159,   117817, 2.830},
                {89.84, 89.841640963675360,   114475, 2.910},
                {89.84, 89.845212605478764,   111133, 2.250},
                {89.85, 89.848784247282168,   108627, 2.300},
                {89.85, 89.852355889085572,   106120, 2.360},
                {89.85, 89.855927530888977,   103614, 2.410},
                {89.86, 89.859499172692381,   101107, 2.470},
                {89.86, 89.863070814495785,    98601, 2.540},
                {89.86, 89.866642456299189,    96094, 2.600},
                {89.87, 89.870214098102593,    93588, 2.670},
                {89.87, 89.873785739905998,    91081, 2.750},
                {89.87, 89.877357381709402,    88575, 2.830},
                {89.88, 89.880929023512806,    86068, 2.910},
                {89.88, 89.883607754865352,    83562, 2.240},
                {89.88, 89.886286486217898,    81682, 2.300},
                {89.89, 89.888965217570444,    79802, 2.350},
                {89.89, 89.891643948922990,    77922, 2.410},
                {89.89, 89.894322680275536,    76042, 2.470},
                {89.89, 89.897001411628082,    74162, 2.530},
                {89.90, 89.899680142980628,    72282, 2.600},
                {89.90, 89.902358874333174,    70402, 2.660},
                {89.90, 89.905037605685720,    68523, 2.740},
                {89.91, 89.907716337038266,    66643, 2.820},
                {89.91, 89.910395068390812,    64763, 2.900},
                {89.91, 89.913073799743358,    62883, 2.980},
                {89.91, 89.915082848257768,    61003, 2.310},
                {89.92, 89.917091896772178,    59593, 2.360},
                {89.92, 89.919100945286587,    58183, 2.420},
                {89.92, 89.921109993800997,    56773, 2.480},
                {89.92, 89.923119042315406,    55363, 2.540},
                {89.92, 89.925128090829816,    53953, 2.610},
                {89.93, 89.927137139344225,    52543, 2.680},
                {89.93, 89.929146187858635,    51134, 2.750},
                {89.93, 89.931155236373044,    49724, 2.830},
                {89.93, 89.933164284887454,    48314, 2.910},
                {89.93, 89.934671071273257,    46904, 2.250},
                {89.93, 89.936177857659061,    45846, 2.300},
                {89.94, 89.937684644044865,    44789, 2.360},
                {89.94, 89.939191430430668,    43731, 2.410},
                {89.94, 89.940698216816472,    42674, 2.470},
                {89.94, 89.942205003202275,    41617, 2.540},
                {89.94, 89.943711789588079,    40559, 2.600},
                {89.94, 89.945218575973882,    39502, 2.670},
                {89.95, 89.946725362359686,    38444, 2.740},
                {89.95, 89.948232148745490,    37387, 2.820},
                {89.95, 89.949738935131293,    36329, 2.900}
        };

        //noinspection ForLoopReplaceableByForEach
        for(int r=0; r < tbl.length; r++)
        {
            double fromLat = tbl[r][FR_LAT];
            double toLat = tbl[r][TO_LAT];
            //double atLat = atLatitude;

            if(fromLat <= atLatitude && atLatitude < toLat)
            {
                double parallelLength = tbl[r][PA_LEN];
                return (int)parallelLength;
            }
        }

        return 0;
    }

    private double calcZoom(float visible_distance, int img_width, double atLat)
    {
        // visible_distance -> in meters
        // img_width -> in pixels
        // atLat -> the latitude you want the zoom level

        visible_distance = Math.abs(visible_distance);
        double parallel_length = getParallelLength(atLat); // in meters

        // for an immage of 256 pixel
        double zoom256 = Math.log(parallel_length/visible_distance)/Math.log(2);

        // adapt the zoom to the image size
        int x = (int) (Math.log((img_width*4)/256f)/Math.log(2));
        return zoom256 + x;
    }

    @SuppressLint("MissingPermission")
    private void doUpdatedLocation(Location oldLastLocation) {
        if ((oldLastLocation == null) && (mActualLocation == null)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            //criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
            String provider = mLocationManager.getBestProvider(criteria, false);
            if ((provider != null) && (!provider.isEmpty())) {
                if (Permissions.checkLocation(getApplicationContext()))
                    mActualLocation = mLocationManager.getLastKnownLocation(provider);
            }
        }
        if (mGeofenceLocation == null) {
            if (mActualLocation != null) {
                mGeofenceLocation = new Location(mActualLocation);
                if (mapIsLoading.getVisibility() != View.GONE)
                    mapIsLoading.setVisibility(View.GONE);
                if (mMap.getVisibility() != View.VISIBLE) {
                    mMap.setVisibility(View.VISIBLE);
                    addressText.setVisibility(View.VISIBLE);
                }
                refreshActivity(true, true);
            }
        } else {
            if (mapIsLoading.getVisibility() != View.GONE)
                mapIsLoading.setVisibility(View.GONE);
            if (mMap.getVisibility() != View.VISIBLE) {
                mMap.setVisibility(View.VISIBLE);
                addressText.setVisibility(View.VISIBLE);
            }

            String name = geofenceNameEditText.getText().toString();
            okButton.setEnabled((!name.isEmpty()) && (mGeofenceLocation != null));

            radiusValue.setText(String.valueOf(Math.round(geofence._radius)));
            updateEditedMarker(oldLastLocation == null);
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(@NonNull Location location) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationGeofenceEditorActivityOSM.mLocationListener.onLocationChanged", "xxx");

            //if (location == null)
            //    return;

            final Location oldLastLocation = mActualLocation;
            mActualLocation = location;
            doUpdatedLocation(oldLastLocation);
        }

        public void onProviderDisabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationGeofenceEditorActivityOSM.mLocationListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationGeofenceEditorActivityOSM.mLocationListener.onProviderEnabled", "888888888888888");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationGeofenceEditorActivityOSM.mLocationListener.onStatusChanged", "xxx");
        }
    };

    private void showDialogIfIsNotOnline(Context context) {
        if (!CheckOnlineStatusBroadcastReceiver.isOnline(context.getApplicationContext())) {
            if (!isFinishing()) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.location_editor_title),
                        getString(R.string.location_editor_connection_is_offline),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        false,
                        false,
                        this
                );

                if (!isFinishing())
                    dialog.showDialog();
            }
        }
    }

    @Override
    public void showDialogAndRefreshFromListener(Context context) {
        showDialogIfIsNotOnline(context);
        if (!isFinishing())
            refreshActivity(true, CheckOnlineStatusBroadcastReceiver.isOnline(context.getApplicationContext()));
    }

    static private class LocationGeofenceEditorOnlineStatusBroadcastReceiver extends BroadcastReceiver
    {

        private final LocationGeofenceEditorActivityOSMNetworkOfflineListener listener;

        public LocationGeofenceEditorOnlineStatusBroadcastReceiver(LocationGeofenceEditorActivityOSMNetworkOfflineListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            listener.showDialogAndRefreshFromListener(context);
        }
    }

}
