package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceScreen;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sk.henrichg.phoneprofilesplus.EditorEventListFragment.OnFinishEventPreferencesActionMode;
import sk.henrichg.phoneprofilesplus.EditorEventListFragment.OnStartEventPreferences;
import sk.henrichg.phoneprofilesplus.EditorProfileListFragment.OnFinishProfilePreferencesActionMode;
import sk.henrichg.phoneprofilesplus.EditorProfileListFragment.OnStartProfilePreferences;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnHideActionModeInEventPreferences;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnRedrawEventListFragment;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnRestartEventPreferences;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnShowActionModeInEventPreferences;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnHideActionModeInProfilePreferences;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnRedrawProfileListFragment;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnRestartProfilePreferences;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnShowActionModeInProfilePreferences;

public class EditorProfilesActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnRestartProfilePreferences,
                                               OnRedrawProfileListFragment,
                                               OnFinishProfilePreferencesActionMode,
                                               OnStartEventPreferences,
                                               OnRestartEventPreferences,
                                               OnRedrawEventListFragment,
                                               OnFinishEventPreferencesActionMode,
                                               OnShowActionModeInProfilePreferences,
                                               OnShowActionModeInEventPreferences,
                                               OnHideActionModeInProfilePreferences,
                                               OnHideActionModeInEventPreferences
{

    private static EditorProfilesActivity instance;

    private ImageView eventsRunStopIndicator;

    private static boolean savedInstanceStateChanged;

    private static ApplicationsCache applicationsCache;
    private static ContactsCache contactsCache;
    private static ContactGroupsCache contactGroupsCache;


    private int editModeProfile;
    private int editModeEvent;

    private static final String SP_RESET_PREFERENCES_FRAGMENT = "editor_restet_preferences_fragment";
    private static final String SP_RESET_PREFERENCES_FRAGMENT_DATA_ID = "editor_restet_preferences_fragment_data_id";
    private static final String SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE = "editor_restet_preferences_fragment_edit_mode";
    private static final int RESET_PREFERENCE_FRAGMENT_RESET_PROFILE = 1;
    private static final int RESET_PREFERENCE_FRAGMENT_RESET_EVENT = 2;
    private static final int RESET_PREFERENCE_FRAGMENT_REMOVE = 3;

    private static final String SP_EDITOR_DRAWER_SELECTED_ITEM = "editor_drawer_selected_item";
    private static final String SP_EDITOR_ORDER_SELECTED_ITEM = "editor_order_selected_item";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static boolean mTwoPane;

    DrawerLayout drawerLayout;
    ScrimInsetsFrameLayout drawerRoot;
    ListView drawerListView;
    ActionBarDrawerToggle drawerToggle;
    TextView filterStatusbarTitle;
    TextView orderLabel;
    Spinner orderSpinner;
    ImageView drawerHeaderFilterImage;
    TextView drawerHeaderFilterTitle;
    TextView drawerHeaderFilterSubtitle;

    String[] drawerItemsTitle;
    String[] drawerItemsSubtitle;
    Integer[] drawerItemsIcon;
    EditorDrawerListAdapter drawerAdapter;

    private int drawerSelectedItem = 2;
    private int orderSelectedItem = 2; // priority
    private int profilesFilterType = EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR;
    private int eventsFilterType = EditorEventListFragment.FILTER_TYPE_ALL;
    private int eventsOrderType = EditorEventListFragment.ORDER_TYPE_EVENT_NAME;

    private static final int COUNT_DRAWER_PROFILE_ITEMS = 3;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Log.e("$$$ PPP","EditorProfilesActivity.onCreate");

        GUIData.setTheme(this, false, true);
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        instance = this;

        savedInstanceStateChanged = (savedInstanceState != null);

        createApplicationsCache();
        createContactsCache();
        createContactGroupsCache();

        setContentView(R.layout.activity_editor_list_onepane);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //	getWindow().setNavigationBarColor(R.attr.colorPrimary);

        //setWindowContentOverlayCompat();

    /*	// add profile list into list container
        EditorProfileListFragment fragment = new EditorProfileListFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit(); */


        if (findViewById(R.id.editor_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null)
                onStartProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, profilesFilterType);
            else
            {
                // for 7 inch tablets lauout changed:
                //   - portrait - one pane
                //   - landscape - two pane
                // onRestartProfilePreferences is called, when user save/not save profile
                // preference changes (Back button, or Cancel in ActionMode)
                // In this method, editmode and profile_id is saved into shared preferences
                // And when orientaion changed into lanscape mode, profile preferences fragment
                // must by recreated due profile preference changes
                SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                int resetMode = preferences.getInt(SP_RESET_PREFERENCES_FRAGMENT, 0);
                if (resetMode == RESET_PREFERENCE_FRAGMENT_RESET_PROFILE)
                {
                    // restart profile preferences fragmentu
                    long profile_id = preferences.getLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, 0);
                    int editMode =  preferences.getInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                    Bundle arguments = new Bundle();
                    arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile_id);
                    arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
                    arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
                    ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.editor_detail_container, fragment, "ProfilePreferencesFragment").commit();
                }
                if (resetMode == RESET_PREFERENCE_FRAGMENT_RESET_EVENT)
                {
                    // restart event preferences fragmentu
                    long event_id = preferences.getLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, 0);
                    int editMode =  preferences.getInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                    Bundle arguments = new Bundle();
                    arguments.putLong(GlobalData.EXTRA_EVENT_ID, event_id);
                    arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, editMode);
                    arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
                    EventPreferencesFragment fragment = new EventPreferencesFragment();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.editor_detail_container, fragment, "EventPreferencesFragment").commit();
                }
                else
                if (resetMode == RESET_PREFERENCE_FRAGMENT_REMOVE)
                {
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
                    if (fragment != null)
                    {
                        getFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                    }
                }
                // remove preferences
                Editor editor = preferences.edit();
                editor.remove(SP_RESET_PREFERENCES_FRAGMENT);
                editor.remove(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID);
                editor.remove(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE);
                editor.commit();
            }
        }
        else
        {
            mTwoPane = false;
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("ProfilePreferencesFragment");
            if (fragment != null)
                fragmentManager.beginTransaction()
                .remove(fragment).commit();
            fragment = fragmentManager.findFragmentByTag("EventPreferencesFragment");
            if (fragment != null)
                fragmentManager.beginTransaction()
                .remove(fragment).commit();
            fragmentManager.executePendingTransactions();
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.editor_list_drawer_layout);
        drawerRoot = (ScrimInsetsFrameLayout) findViewById(R.id.editor_drawer_root);

        // set status bar background for Activity body layout
        if (GlobalData.applicationTheme.equals("material"))
            drawerLayout.setStatusBarBackground(R.color.profile_all_primaryDark);
        else
        if (GlobalData.applicationTheme.equals("dark"))
            drawerLayout.setStatusBarBackground(R.color.profile_all_primaryDark_dark);
        else
        if (GlobalData.applicationTheme.equals("dlight"))
            drawerLayout.setStatusBarBackground(R.color.profile_all_primaryDark_dark);

        drawerListView = (ListView) findViewById(R.id.editor_drawer_list);
        View headerView =  ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.editor_drawer_list_header, null, false);
        drawerListView.addHeaderView(headerView, null, false);
        drawerHeaderFilterImage = (ImageView) findViewById(R.id.editor_drawer_list_header_icon);
        drawerHeaderFilterTitle = (TextView) findViewById(R.id.editor_drawer_list_header_title);
        drawerHeaderFilterSubtitle = (TextView) findViewById(R.id.editor_drawer_list_header_subtitle);

        int drawerShadowId;
        if (GlobalData.applicationTheme.equals("dark"))
            drawerShadowId = R.drawable.drawer_shadow_dark;
        else
            drawerShadowId = R.drawable.drawer_shadow;
        drawerLayout.setDrawerShadow(drawerShadowId, GravityCompat.START);

        // actionbar titles
        drawerItemsTitle = new String[] {
                getResources().getString(R.string.editor_drawer_title_profiles),
                getResources().getString(R.string.editor_drawer_title_profiles),
                getResources().getString(R.string.editor_drawer_title_profiles),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events)
              };

        // drawer item titles
        drawerItemsSubtitle = new String[] {
                getResources().getString(R.string.editor_drawer_list_item_profiles_all),
                getResources().getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                getResources().getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                getResources().getString(R.string.editor_drawer_list_item_events_all),
                getResources().getString(R.string.editor_drawer_list_item_events_running),
                getResources().getString(R.string.editor_drawer_list_item_events_paused),
                getResources().getString(R.string.editor_drawer_list_item_events_stopped)
              };

        drawerItemsIcon = new Integer[] {
                R.drawable.ic_events_drawer_profile_filter_2,
                R.drawable.ic_events_drawer_profile_filter_0,
                R.drawable.ic_events_drawer_profile_filter_1,
                R.drawable.ic_events_drawer_event_filter_2,
                R.drawable.ic_events_drawer_event_filter_0,
                R.drawable.ic_events_drawer_event_filter_1,
                R.drawable.ic_events_drawer_event_filter_3,
              };


        // Pass string arrays to EditorDrawerListAdapter
        // use action bar themed context
        //drawerAdapter = new EditorDrawerListAdapter(drawerListView, getSupportActionBar().getThemedContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        drawerAdapter = new EditorDrawerListAdapter(drawerListView, getBaseContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        
        // Set the MenuListAdapter to the ListView
        drawerListView.setAdapter(drawerAdapter);
 
        // Capture listview menu item click
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());


        Toolbar toolbar = (Toolbar)findViewById(R.id.editor_tollbar);
        setSupportActionBar(toolbar);
        
        // Enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        /*int drawerIconId;
        if (GlobalData.applicationTheme.equals("light"))
            drawerIconId = R.drawable.ic_drawer;
        else
            drawerIconId = R.drawable.ic_drawer_dark;*/
        
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.editor_drawer_open, R.string.editor_drawer_open)
        {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
 
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            
            // this disable animation 
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) 
            {
                  if(drawerView!=null && drawerView == drawerRoot){
                        super.onDrawerSlide(drawerView, 0);
                  }else{
                        super.onDrawerSlide(drawerView, slideOffset);
                  }
            }            
        };
        drawerLayout.setDrawerListener(drawerToggle);
        
        filterStatusbarTitle = (TextView) findViewById(R.id.editor_filter_title);
       
        orderLabel = (TextView) findViewById(R.id.editor_drawer_order_title);
        
        orderSpinner = (Spinner) findViewById(R.id.editor_drawer_order);
        ArrayAdapter<CharSequence> orderSpinneAadapter = ArrayAdapter.createFromResource(
                                    //getSupportActionBar().getThemedContext(),
                                    getBaseContext(),
                                    R.array.drawerOrderEvents,
                                    //android.R.layout.simple_spinner_item);
                                    R.layout.editor_drawer_spinner);
        orderSpinneAadapter.setDropDownViewResource(android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item);
        orderSpinneAadapter.setDropDownViewResource(R.layout.editor_drawer_spinner_dropdown);
        orderSpinner.setAdapter(orderSpinneAadapter);
        orderSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != orderSelectedItem)
                    changeEventOrder(position, false);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle(R.string.title_activity_phone_profiles);

    /*
        // Create an array adapter to populate dropdownlist
        ArrayAdapter<CharSequence> navigationAdapter =
                ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(), R.array.phoneProfilesNavigator, R.layout.sherlock_spinner_item);

        // Enabling dropdown list navigation for the action bar
        getSupportActionBar().setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST);

        // Defining Navigation listener
        ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                switch(itemPosition) {
                case 0:
                    EditorProfileListFragment profileFragment = new EditorProfileListFragment();
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, profileFragment, "EditorProfileListFragment").commit();
                    onStartProfilePreferences(-1, false);
                    break;
                case 1:
                    EditorEventListFragment eventFragment = new EditorEventListFragment();
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, eventFragment, "EditorEventListFragment").commit();
                    onStartEventPreferences(-1, false);
                    break;
                }
                return false;
            }
        };

        // Setting dropdown items and item navigation listener for the actionbar
        getSupportActionBar().setListNavigationCallbacks(navigationAdapter, navigationListener);
        navigationAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
    */

        eventsRunStopIndicator = (ImageView)findViewById(R.id.editor_list_run_stop_indicator);
        
        // set drawer item and order
        if ((savedInstanceState != null) || (GlobalData.applicationEditorSaveEditorState))
        {
            SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            drawerSelectedItem = preferences.getInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 2);
            orderSelectedItem = preferences.getInt(SP_EDITOR_ORDER_SELECTED_ITEM, 2); // priority
        }

        // first must be set eventsOrderType
        changeEventOrder(orderSelectedItem, savedInstanceState != null);
        selectDrawerItem(drawerSelectedItem, false, savedInstanceState != null);

        refreshGUI();

    }

    public static EditorProfilesActivity getInstance()
    {
        return instance;
    }

    @Override
    protected void onStart()
    {
        //Log.e("$$$ PPP","EditorProfilesActivity.onStart");
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        //Log.e("$$$ PPP","EditorProfilesActivity.onStop");
        super.onStop();
        if (instance == this)
            instance = null;
    }

    @Override
    protected void onResume()
    {
        //Log.e("$$$ PPP","EditorProfilesActivity.onResume");
        //Debug.stopMethodTracing();
        super.onResume();

        if (instance == null)
        {
            instance = this;
            refreshGUI();
        }
    }


    @Override
    protected void onDestroy()
    {
        if (!savedInstanceStateChanged)
        {
            // no destroy caches on orientation change
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache = null;
            if (contactsCache != null)
                contactsCache.clearCache(true);
            contactsCache = null;
        }

        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_editor_profiles, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        /*MenuItem menuItem = menu.findItem(R.id.menu_settingsX);
        menuItem.setTitle(getResources().getString(R.string.menu_settings) + "  >");*/
        MenuItem menuItem = menu.findItem(R.id.menu_import_export);
        menuItem.setTitle(getResources().getString(R.string.menu_import_export) + "  >");

        // change global events run/stop menu item title
        menuItem = menu.findItem(R.id.menu_run_stop_events);
        if (menuItem != null)
        {
            if (GlobalData.getGlobalEventsRuning(getApplicationContext()))
            {
                menuItem.setTitle(R.string.menu_stop_events);
            }
            else
            {
                menuItem.setTitle(R.string.menu_run_events);
            }
        }

        boolean isPPHInstalled = PhoneProfilesHelper.isPPHelperInstalled(getApplicationContext(), PhoneProfilesHelper.PPHELPER_CURRENT_VERSION);

        menuItem = menu.findItem(R.id.menu_pphelper_install);
        if (menuItem != null)
        {
            //menuItem.setVisible(GlobalData.isRooted(true) && (!isPPHInstalled));
            menuItem.setVisible(!isPPHInstalled);

            if (PhoneProfilesHelper.PPHelperVersion != -1)
            {
                menuItem.setTitle(R.string.menu_phoneprofilehepler_upgrade);
            }
            else
            {
                menuItem.setTitle(R.string.menu_phoneprofilehepler_install);
            }
        }
        menuItem = menu.findItem(R.id.menu_pphelper_uninstall);
        if (menuItem != null)
        {
            //menuItem.setVisible(GlobalData.isRooted(true) && (PhoneProfilesHelper.PPHelperVersion != -1));
            menuItem.setVisible(PhoneProfilesHelper.PPHelperVersion != -1);
        }

        menuItem = menu.findItem(R.id.menu_restart_events);
        if (menuItem != null)
        {
            menuItem.setVisible(GlobalData.getGlobalEventsRuning(getApplicationContext()));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
        case android.R.id.home:
            if (drawerLayout.isDrawerOpen(drawerRoot)) {
                drawerLayout.closeDrawer(drawerRoot);
            } else {
                drawerLayout.openDrawer(drawerRoot);
            }	
            return super.onOptionsItemSelected(item);
        case R.id.menu_restart_events:
            getDataWrapper().getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

            // ignoruj manualnu aktivaciu profilu
            // a odblokuj forceRun eventy
            GlobalData.logE("$$$ restartEvents","from EditorProfilesActivity.onOptionsItemSelected menu_restart_events");
            getDataWrapper().restartEventsWithAlert(this);
            return true;
        case R.id.menu_run_stop_events:
            DataWrapper dataWrapper = getDataWrapper();
            if (GlobalData.getGlobalEventsRuning(getApplicationContext()))
            {
                dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_RUNEVENTS_DISABLE, null, null, null, 0);

                // no setup for next start
                dataWrapper.removeAllEventDelays(false);
                // no set system events, unblock all events, no activate return profile
                dataWrapper.pauseAllEvents(true, false/*, false*/);
                GlobalData.setGlobalEventsRuning(getApplicationContext(), false);
                // stop Wifi scanner
                WifiScanAlarmBroadcastReceiver.initialize(getApplicationContext());
                WifiScanAlarmBroadcastReceiver.removeAlarm(getApplicationContext(), false);
                // stop bluetooth scanner
                BluetoothScanAlarmBroadcastReceiver.initialize(getApplicationContext());
                BluetoothScanAlarmBroadcastReceiver.removeAlarm(getApplicationContext(), false);
            }
            else
            {
                dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_RUNEVENTS_ENABLE, null, null, null, 0);

                GlobalData.setGlobalEventsRuning(getApplicationContext(), true);

                // setup for next start
                dataWrapper.firstStartEvents(false);
            }
            invalidateOptionsMenu();
            refreshGUI();
            return true;
        case R.id.menu_activity_log:
            intent = new Intent(getBaseContext(), ActivityLogActivity.class);
            startActivity(intent);

            return true;
        case R.id.menu_settings:
            intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);

            startActivityForResult(intent, GlobalData.REQUEST_CODE_APPLICATION_PREFERENCES);

            return true;
        case R.id.menu_pphelper_install:
            PhoneProfilesHelper.installPPHelper(this, false);
            return true;
        case R.id.menu_pphelper_uninstall:
            PhoneProfilesHelper.uninstallPPHelper(this);
            return true;
        case R.id.menu_export:
            exportData();

            return true;
        case R.id.menu_import:
            importData();

            return true;
        /*case R.id.menu_help:
            try {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/henrichg/PhoneProfilesPlus/wiki"));
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                //Toast.makeText(this, "No application can handle this request."
                //    + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return true;*/
        case R.id.menu_exit:
            GlobalData.setApplicationStarted(getApplicationContext(), false);

            // stop all events
            getDataWrapper().stopAllEvents(false, false);

            // zrusenie notifikacie
            getDataWrapper().getActivateProfileHelper().removeNotification();

            SearchCalendarEventsBroadcastReceiver.removeAlarm(getApplicationContext());
            WifiScanAlarmBroadcastReceiver.removeAlarm(getApplicationContext(), false);

            // remove alarm for profile duration
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(getApplicationContext());
            GlobalData.setActivatedProfileForDuration(getApplicationContext(), 0);

            stopService(new Intent(getApplicationContext(), ReceiversService.class));
            stopService(new Intent(getApplicationContext(), KeyguardService.class));

            ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());
            ActivateProfileHelper.removeBrightnessView(getApplicationContext());

            getDataWrapper().getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONEXIT, null, null, null, 0);


            finish();

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    // fix for bug in LG stock ROM Android <= 4.1
    // https://code.google.com/p/android/issues/detail?id=78154
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         //Log.e("*** EditorPrActivity","keyCode="+keyCode);
         if ((keyCode == KeyEvent.KEYCODE_MENU) &&
              (Build.VERSION.SDK_INT <= 16) &&
              (Build.MANUFACTURER.compareTo("LGE") == 0)) {
           return true;
         }
         return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                 (Build.VERSION.SDK_INT <= 16) &&
                 (Build.MANUFACTURER.compareTo("LGE") == 0)) {
           openOptionsMenu();
         return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    /////

    // ListView click listener in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // header is position=0
            if (position > 0)
                selectDrawerItem(position, true, false);
        }
    }
 
    private void selectDrawerItem(int position, boolean removePreferences, boolean orientationChange) {

        Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_list_container);
        if (position == 0) position = 2;
        if ((position != drawerSelectedItem) || (fragment == null))
        {
            drawerSelectedItem = position;

            // save into shared preferences
            SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putInt(SP_EDITOR_DRAWER_SELECTED_ITEM, drawerSelectedItem);
            editor.commit();

            Bundle arguments;

            switch (drawerSelectedItem) {
            case 1:
                profilesFilterType = EditorProfileListFragment.FILTER_TYPE_ALL;
                fragment = new EditorProfileListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit();
                if (removePreferences)
                    onStartProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, profilesFilterType);
                break;
            case 2:
                profilesFilterType = EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR;
                fragment = new EditorProfileListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit();
                if (removePreferences)
                    onStartProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, profilesFilterType);
                break;
            case 3:
                profilesFilterType = EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR;
                fragment = new EditorProfileListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit();
                if (removePreferences)
                    onStartProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, profilesFilterType);
                break;
            case 4:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_ALL;
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorEventListFragment").commit();
                if (removePreferences)
                    onStartEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, eventsFilterType, eventsOrderType);
                break;
            case 5:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_RUNNING;
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorEventListFragment").commit();
                if (removePreferences)
                    onStartEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, eventsFilterType, eventsOrderType);
                break;
            case 6:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_PAUSED;
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorEventListFragment").commit();
                if (removePreferences)
                    onStartEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, eventsFilterType, eventsOrderType);
                break;
            case 7:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_STOPPED;
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorEventListFragment").commit();
                if (removePreferences)
                    onStartEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, eventsFilterType, eventsOrderType);
                break;
            }
        }

        // header is position=0
        drawerListView.setItemChecked(drawerSelectedItem, true);
        // Get the title and icon followed by the position
        setTitle(drawerItemsTitle[drawerSelectedItem-1]);
        //setIcon(drawerItemsIcon[drawerSelectedItem-1]);
        drawerHeaderFilterImage.setImageResource(drawerItemsIcon[drawerSelectedItem-1]);
        drawerHeaderFilterTitle.setText(drawerItemsTitle[drawerSelectedItem-1]);

        // show/hide order
        if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
        {
            orderLabel.setVisibility(View.GONE);
            orderSpinner.setVisibility(View.GONE);
        }
        else
        {
            orderLabel.setVisibility(View.VISIBLE);
            orderSpinner.setVisibility(View.VISIBLE);
        }

        // set filter statusbar title
        setStatusBarTitle();
        
        
        // Close drawer
        if (GlobalData.applicationEditorAutoCloseDrawer && (!orientationChange))
            drawerLayout.closeDrawer(drawerRoot);
    }
    
    private void changeEventOrder(int position, boolean orientationChange)
    {
        orderSelectedItem = position;

        // save into shared preferences
        SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(SP_EDITOR_ORDER_SELECTED_ITEM, orderSelectedItem);
        editor.commit();

        eventsOrderType = EditorEventListFragment.ORDER_TYPE_EVENT_NAME;
        switch (position)
        {
            case 0: eventsOrderType = EditorEventListFragment.ORDER_TYPE_EVENT_NAME; break;
            case 1: eventsOrderType = EditorEventListFragment.ORDER_TYPE_PROFILE_NAME; break;
            case 2: eventsOrderType = EditorEventListFragment.ORDER_TYPE_PRIORITY; break;
        }
        setStatusBarTitle();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_list_container);
        if ((fragment != null) && (fragment instanceof EditorEventListFragment))
        {
            ((EditorEventListFragment)fragment).changeListOrder(eventsOrderType);
        }

        orderSpinner.setSelection(orderSelectedItem);

        // Close drawer
        if (GlobalData.applicationEditorAutoCloseDrawer && (!orientationChange))
            drawerLayout.closeDrawer(drawerRoot);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == GlobalData.REQUEST_CODE_ACTIVATE_PROFILE)
        {
            EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null)
                fragment.doOnActivityResult(requestCode, resultCode, data);
        }
        else
        if (requestCode == GlobalData.REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                // redraw list fragment after finish ProfilePreferencesFragmentActivity
                long profile_id = data.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
                int newProfileMode = data.getIntExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);

                if (profile_id > 0)
                {
                    Profile profile = getDataWrapper().getDatabaseHandler().getProfile(profile_id, false);
                    // generate bitmaps
                    profile.generateIconBitmap(getBaseContext(), false, 0);
                    profile.generatePreferencesIndicator(getBaseContext(), false, 0);

                    // redraw list fragment , notifications, widgets after finish ProfilePreferencesFragmentActivity
                    onRedrawProfileListFragment(profile, newProfileMode);
                }
                else
                if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
                {
                    // refresh activity for changes of default profile
                    GUIData.reloadActivity(this, false);
                }
            }
        }
        else
        if (requestCode == GlobalData.REQUEST_CODE_EVENT_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                // redraw list fragment after finish EventPreferencesFragmentActivity
                long event_id = data.getLongExtra(GlobalData.EXTRA_EVENT_ID, 0L);
                int newEventMode = data.getIntExtra(GlobalData.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);

                if (event_id > 0)
                {
                    Event event = getDataWrapper().getDatabaseHandler().getEvent(event_id);

                    // redraw list fragment , notifications, widgets after finish ProfilePreferencesFragmentActivity
                    onRedrawEventListFragment(event, newEventMode);
                }
            }
        }
        else
        if (requestCode == GlobalData.REQUEST_CODE_APPLICATION_PREFERENCES)
        {
            if (resultCode == RESULT_OK)
            {
                boolean restart = data.getBooleanExtra(GlobalData.EXTRA_RESET_EDITOR, false);

                if (restart)
                {
                    // refresh activity for special changes
                    GUIData.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == GlobalData.REQUEST_CODE_REMOTE_EXPORT)
        {
            if (resultCode == RESULT_OK)
            {
                doImportData(GUIData.REMOTE_EXPORT_PATH);
            }
        }
        else
        {
            // send other activity results into preference fragment
            if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
            {
                ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                    fragment.doOnActivityResult(requestCode, resultCode, data);
            }
            else
            {
                EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                    fragment.doOnActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // handle your back button code here
            if (mTwoPane) {
                if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
                {
                    ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_detail_container);
                    if ((fragment != null) && (fragment.isActionModeActive()))
                    {
                        fragment.finishActionMode(ProfilePreferencesFragment.BUTTON_CANCEL);
                        return true; // consumes the back key event - ActionMode is not finished
                    }
                    else
                        return super.dispatchKeyEvent(event);
                }
                else
                {
                    EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_detail_container);
                    if ((fragment != null) && (fragment.isActionModeActive()))
                    {
                        fragment.finishActionMode(EventPreferencesFragment.BUTTON_CANCEL);
                        return true; // consumes the back key event - ActionMode is not finished
                    }
                    else
                        return super.dispatchKeyEvent(event);
                }
            }
            else
                return super.dispatchKeyEvent(event);
        }
        /*else
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    openOptionsMenu();
                }
            }, 1000);
            return true;
        }*/

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(drawerRoot))
            drawerLayout.closeDrawer(drawerRoot);
        else
            super.onBackPressed();
    }

    /*
    @Override
    public void openOptionsMenu() {
        Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;
        } else {
            super.openOptionsMenu();
        }
    }
    */

    private void importExportErrorDialog(int importExport)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        int resString;
        if (importExport == 1)
            resString = R.string.import_profiles_alert_title;
        else
            resString = R.string.export_profiles_alert_title;
        dialogBuilder.setTitle(resString);
        if (importExport == 1)
            resString = R.string.import_profiles_alert_error;
        else
            resString = R.string.export_profiles_alert_error;
        dialogBuilder.setMessage(resString);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(android.R.string.ok, null);
        dialogBuilder.show();
    }

    @SuppressWarnings({ "unchecked" })
    private boolean importApplicationPreferences(File src, int what) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
                input = new ObjectInputStream(new FileInputStream(src));
                Editor prefEdit;
                if (what == 1)
                    prefEdit = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                else
                    prefEdit = getSharedPreferences(GlobalData.DEFAULT_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                prefEdit.clear();
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();

                    if (v instanceof Boolean)
                        prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                    else if (v instanceof Float)
                        prefEdit.putFloat(key, ((Float) v).floatValue());
                    else if (v instanceof Integer)
                        prefEdit.putInt(key, ((Integer) v).intValue());
                    else if (v instanceof Long)
                        prefEdit.putLong(key, ((Long) v).longValue());
                    else if (v instanceof String)
                        prefEdit.putString(key, ((String) v));

                    if (what == 1)
                    {
                        if (key.equals(GlobalData.PREF_APPLICATION_THEME))
                        {
                            if (((String)v).equals("light"))
                                prefEdit.putString(key, "material");
                        }
                    }
                }
                prefEdit.commit();
            res = true;
        } catch (FileNotFoundException e) {
            // no error, this is OK
            //e.printStackTrace();
            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    private void doImportData(String applicationDataPath)
    {
        final Activity activity = this;
        final String _applicationDataPath = applicationDataPath;

        class ImportAsyncTask extends AsyncTask<Void, Integer, Integer>
        {
            private MaterialDialog dialog;
            private DataWrapper dataWrapper;

            ImportAsyncTask()
            {
                 this.dialog = new MaterialDialog.Builder(activity)
                                     .content(R.string.import_profiles_alert_title)
                                     //.disableDefaultFonts()
                                     .progress(true, 0)
                                     .build();
                 this.dataWrapper = getDataWrapper();
            }

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                lockScreenOrientation();
                this.dialog.setCancelable(false);
                this.dialog.setCanceledOnTouchOutside(false);
                this.dialog.show();

                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null)
                {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment)fragment).removeAdapter();
                    else
                        ((EditorEventListFragment)fragment).removeAdapter();
                }

                // check root, this set GlobalData.rooted for doInBackgroud()
                GlobalData.isRooted(false);
            }

            @Override
            protected Integer doInBackground(Void... params) {

                this.dataWrapper.stopAllEvents(true, false);

                int ret = this.dataWrapper.getDatabaseHandler().importDB(_applicationDataPath);

                if (ret == 1)
                {
                    // check for hardware capability and update data
                    ret = this.dataWrapper.getDatabaseHandler().updateForHardware(getApplicationContext());
                }
                if (ret == 1)
                {
                    File sd = Environment.getExternalStorageDirectory();
                    File exportFile = new File(sd, _applicationDataPath + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
                    if (!importApplicationPreferences(exportFile, 1))
                        ret = 0;
                    else
                    {
                        exportFile = new File(sd, _applicationDataPath + "/" + GUIData.EXPORT_DEF_PROFILE_PREF_FILENAME);
                        if (!importApplicationPreferences(exportFile, 2))
                            ret = 0;
                    }
                }

                return ret;
            }

            @Override
            protected void onPostExecute(Integer result)
            {
                super.onPostExecute(result);

                if (this.dialog.isShowing())
                    this.dialog.dismiss();
                unlockScreenOrientation();

                if (result == 1)
                {
                    GlobalData.loadPreferences(getApplicationContext());

                    dataWrapper.invalidateProfileList();
                    dataWrapper.invalidateEventList();

                    dataWrapper.updateNotificationAndWidgets(null, "");
                    //dataWrapper.getActivateProfileHelper().showNotification(null, "");
                    //dataWrapper.getActivateProfileHelper().updateWidget();

                    GlobalData.logE("$$$ setEventsBlocked", "EditorProfilesActivity.doImportData.onPostExecute, false");
                    GlobalData.setEventsBlocked(getApplicationContext(), false);
                    dataWrapper.getDatabaseHandler().unblockAllEvents();
                    GlobalData.setForceRunEventRunning(getApplicationContext(), false);

                    SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                    Editor editor = preferences.edit();
                    editor.putInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 1);
                    editor.putInt(SP_EDITOR_ORDER_SELECTED_ITEM, 0);
                    editor.commit();

                    // restart events
                    // startneme eventy
                    if (GlobalData.getGlobalEventsRuning(getApplicationContext()))
                    {
                        /*
                        Intent intent = new Intent();
                        intent.setAction(RestartEventsBroadcastReceiver.INTENT_RESTART_EVENTS);
                        getBaseContext().sendBroadcast(intent);
                        */
                        GlobalData.logE("$$$ restartEvents", "from EditorProfilesActivity.doImportData.onPostExecute");
                        dataWrapper.restartEventsWithDelay(1, false);
                    }

                    dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_DATAIMPORT, null, null, null, 0);

                    // toast notification
                    Toast msg = Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_import_ok),
                            Toast.LENGTH_SHORT);
                    msg.show();

                    // refresh activity
                    GUIData.reloadActivity(activity, true);
                }
                else
                {
                    importExportErrorDialog(1);
                }

            }

            private void lockScreenOrientation() {
                int currentOrientation = activity.getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            }

            private void unlockScreenOrientation() {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }

        }

        new ImportAsyncTask().execute();
    }

    private void importDataAlert(boolean remoteExport)
    {
        final boolean _remoteExport = remoteExport;

        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        if (remoteExport)
        {
            dialogBuilder2.setTitle(R.string.import_profiles_from_phoneprofiles_alert_title2);
            dialogBuilder2.setMessage(R.string.import_profiles_alert_message);
            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
        }
        else
        {
            dialogBuilder2.setTitle(R.string.import_profiles_alert_title);
            dialogBuilder2.setMessage(R.string.import_profiles_alert_message);
            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
        }

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (_remoteExport)
                {
                    // start RemoteExportDataActivity
                    Intent intent = new Intent("phoneprofiles.intent.action.EXPORTDATA");

                    final PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0)
                        startActivityForResult(intent, GlobalData.REQUEST_CODE_REMOTE_EXPORT);
                    else
                        importExportErrorDialog(1);
                }
                else
                    doImportData(GlobalData.EXPORT_PATH);
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder2.show();
    }

    private void importData()
    {
        /*// test whether the PhoneProfile is installed
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent phoneProfiles = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofiles");
        if (phoneProfiles != null)
        {
            // PhoneProfiles is istalled

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.import_profiles_from_phoneprofiles_alert_title);
            dialogBuilder.setMessage(R.string.import_profiles_from_phoneprofiles_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    importDataAlert(true);
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    importDataAlert(false);
                }
            });
            dialogBuilder.show();
        }
        else*/
            importDataAlert(false);
    }

    private boolean exportApplicationPreferences(File dst, int what) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref;
            if (what == 1)
                pref = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            else
                pref = getSharedPreferences(GlobalData.DEFAULT_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE);
            output.writeObject(pref.getAll());

            res = true;
        } catch (FileNotFoundException e) {
            // this is OK
            //e.printStackTrace();
            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    private void exportData()
    {
        final Activity activity = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.export_profiles_alert_title);
        dialogBuilder.setMessage(R.string.export_profiles_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                class ExportAsyncTask extends AsyncTask<Void, Integer, Integer>
                {
                    private MaterialDialog dialog;
                    private DataWrapper dataWrapper;

                    ExportAsyncTask()
                    {
                        this.dialog = new MaterialDialog.Builder(activity)
                                .content(R.string.export_profiles_alert_title)
                                //.disableDefaultFonts()
                                .progress(true, 0)
                                .build();
                         this.dataWrapper = getDataWrapper();
                    }

                    @Override
                    protected void onPreExecute()
                    {
                        super.onPreExecute();

                        lockScreenOrientation();
                        this.dialog.setCancelable(false);
                        this.dialog.setCanceledOnTouchOutside(false);
                        this.dialog.show();
                    }

                    @Override
                    protected Integer doInBackground(Void... params) {

                        int ret = dataWrapper.getDatabaseHandler().exportDB();
                        if (ret == 1)
                        {
                            File sd = Environment.getExternalStorageDirectory();
                            File exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 1))
                                ret = 0;
                            else
                            {
                                exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_DEF_PROFILE_PREF_FILENAME);
                                if (!exportApplicationPreferences(exportFile, 2))
                                    ret = 0;
                            }
                        }

                        return ret;
                    }

                    @Override
                    protected void onPostExecute(Integer result)
                    {
                        super.onPostExecute(result);

                        if (dialog.isShowing())
                            dialog.dismiss();
                        unlockScreenOrientation();

                        if (result == 1)
                        {

                            // toast notification
                            Toast msg = Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.toast_export_ok),
                                    Toast.LENGTH_SHORT);
                            msg.show();

                        }
                        else
                        {
                            importExportErrorDialog(2);
                        }
                    }

                    private void lockScreenOrientation() {
                        int currentOrientation = activity.getResources().getConfiguration().orientation;
                        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        } else {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        }
                    }

                    private void unlockScreenOrientation() {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }

                }

                new ExportAsyncTask().execute();

            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }
 
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        savedInstanceStateChanged = true;

        if (mTwoPane) {
            SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);

            if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
            {
                if ((editModeProfile != EditorProfileListFragment.EDIT_MODE_INSERT) &&
                    (editModeProfile != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag("ProfilePreferencesFragment");
                    if (fragment != null)
                    {
                        Editor editor = preferences.edit();
                        editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_RESET_PROFILE);
                        editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, ((ProfilePreferencesFragment)fragment).profile_id);
                        editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeProfile);
                        editor.commit();
                    }
                }
                else
                {
                    Editor editor = preferences.edit();
                    editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_REMOVE);
                    editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, 0);
                    editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeProfile);
                    editor.commit();
                }
            }
            else
            {
                if ((editModeEvent != EditorProfileListFragment.EDIT_MODE_INSERT) &&
                    (editModeEvent != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag("EventPreferencesFragment");
                    if (fragment != null)
                    {
                        Editor editor = preferences.edit();
                        editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_RESET_EVENT);
                        editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, ((EventPreferencesFragment)fragment).event_id);
                        editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeEvent);
                        editor.commit();
                    }
                }
                else
                {
                    Editor editor = preferences.edit();
                    editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_REMOVE);
                    editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, 0);
                    editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeEvent);
                    editor.commit();
                }
            }
        }

    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        /*
        // activity will restarted
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig); */

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GUIData.reloadActivity(this, false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    /*    drawerSelectedItem = savedInstanceState.getInt("editor_drawer_selected_item", -1);
        selectDrawerItem(drawerSelectedItem, false);
        orderSelectedItem = savedInstanceState.getInt("editor_order_selected_item", -1);
        changeEventOrder(orderSelectedItem);  */
    }

     @Override
     public void setTitle(CharSequence title) {
         getSupportActionBar().setTitle(title);
     }

     /*
     public void setIcon(int iconRes) {
         getSupportActionBar().setIcon(iconRes);
     }
     */

     private void setStatusBarTitle()
     {
        // set filter statusbar title
        String text = "";
        if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
        {
            text = drawerItemsSubtitle[drawerSelectedItem-1];
        }
        else
        {
            String[] orderItems = getResources().getStringArray(R.array.drawerOrderEvents);
            text = drawerItemsSubtitle[drawerSelectedItem-1] +
                    "; " +
                    orderItems[orderSelectedItem];
        }
        filterStatusbarTitle.setText(text);
        drawerHeaderFilterSubtitle.setText(text);
     }

    public void onStartProfilePreferences(Profile profile, int editMode, int filterType) {

        editModeProfile = editMode;

        onFinishProfilePreferencesActionMode();

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            if ((profile != null) ||
                (editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            {
                Bundle arguments = new Bundle();
                if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
                    arguments.putLong(GlobalData.EXTRA_PROFILE_ID, 0);
                else
                    arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
                arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
                ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_detail_container, fragment, "ProfilePreferencesFragment").commit();
            }
            else
            {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }

        } else {
            // In single-pane mode, simply start the profile preferences activity
            // for the profile position.
            if (((profile != null) ||
                 (editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                 (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
                && (editMode != EditorProfileListFragment.EDIT_MODE_DELETE))
            {
                Intent intent = new Intent(getBaseContext(), ProfilePreferencesFragmentActivity.class);
                if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
                    intent.putExtra(GlobalData.EXTRA_PROFILE_ID, 0);
                else
                    intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
                startActivityForResult(intent, GlobalData.REQUEST_CODE_PROFILE_PREFERENCES);
            }
        }
    }

    public void onRestartProfilePreferences(Profile profile, int newProfileMode) {
        if (mTwoPane) {
            if ((newProfileMode != EditorProfileListFragment.EDIT_MODE_INSERT) &&
                (newProfileMode != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            {
                // restart profile preferences fragmentu
                Bundle arguments = new Bundle();
                arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editModeProfile);
                arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
                ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.editor_detail_container, fragment, "ProfilePreferencesFragment").commit();
            }
            else
            {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }
        }
        else
        {
            SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);

            if ((newProfileMode != EditorProfileListFragment.EDIT_MODE_INSERT) &&
                (newProfileMode != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            {
                Editor editor = preferences.edit();
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_RESET_PROFILE);
                editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, profile._id);
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeProfile);
                editor.commit();
            }
            else
            {
                Editor editor = preferences.edit();
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_REMOVE);
                editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, profile._id);
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeProfile);
                editor.commit();
            }
        }
    }

    public void onRedrawProfileListFragment(Profile profile, int newProfileMode) {
        // redraw headeru list fragmentu, notifikacie a widgetov

        EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null)
        {
            // update profile, this rewrite profile in profileList
            fragment.dataWrapper.updateProfile(profile);

            boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                                  (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
            fragment.updateListView(profile, newProfile);

            Profile activeProfile = fragment.dataWrapper.getActivatedProfile();
            fragment.updateHeader(activeProfile);
            fragment.dataWrapper.getActivateProfileHelper().showNotification(activeProfile, "");
            fragment.dataWrapper.getActivateProfileHelper().updateWidget();

        }
        onRestartProfilePreferences(profile, newProfileMode);
    }

    public void onFinishProfilePreferencesActionMode() {
        //if (mTwoPane) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
            if (fragment != null)
            {
                if (fragment instanceof ProfilePreferencesFragment)
                {
                    ((ProfilePreferencesFragment)fragment).finishActionMode(EventPreferencesFragment.BUTTON_CANCEL);
                }
                else
                {
                    ((EventPreferencesFragment)fragment).finishActionMode(EventPreferencesFragment.BUTTON_CANCEL);
                }
            }
        //}
    }

    public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
        return;
    }

    public void onFinishEventPreferencesActionMode() {
        //if (mTwoPane) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
            if (fragment != null)
            {
                if (fragment instanceof ProfilePreferencesFragment)
                    ((ProfilePreferencesFragment)fragment).finishActionMode(EventPreferencesFragment.BUTTON_CANCEL);
                else
                    ((EventPreferencesFragment)fragment).finishActionMode(EventPreferencesFragment.BUTTON_CANCEL);
            }
        //}
    }

    public void onStartEventPreferences(Event event, int editMode, int filterType, int orderType) {

        editModeEvent = editMode;

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            onFinishEventPreferencesActionMode();

            if ((event != null) ||
                (editMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
            {
                Bundle arguments = new Bundle();
                if (editMode == EditorEventListFragment.EDIT_MODE_INSERT)
                    arguments.putLong(GlobalData.EXTRA_EVENT_ID, 0L);
                else
                    arguments.putLong(GlobalData.EXTRA_EVENT_ID, event._id);
                arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, editMode);
                arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
                EventPreferencesFragment fragment = new EventPreferencesFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                    .replace(R.id.editor_detail_container, fragment, "EventPreferencesFragment").commit();
            }
            else
            {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }

        } else {
            // In single-pane mode, simply start the profile preferences activity
            // for the event id.
            if (((event != null) ||
                 (editMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                 (editMode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
                && (editMode != EditorEventListFragment.EDIT_MODE_DELETE))
            {
                Intent intent = new Intent(getBaseContext(), EventPreferencesFragmentActivity.class);
                if (editMode == EditorEventListFragment.EDIT_MODE_INSERT)
                    intent.putExtra(GlobalData.EXTRA_EVENT_ID, 0L);
                else
                    intent.putExtra(GlobalData.EXTRA_EVENT_ID, event._id);
                intent.putExtra(GlobalData.EXTRA_NEW_EVENT_MODE, editMode);
                startActivityForResult(intent, GlobalData.REQUEST_CODE_EVENT_PREFERENCES);
            }
        }
    }

    public void onRedrawEventListFragment(Event event, int newEventMode) {
        // redraw headeru list fragmentu, notifikacie a widgetov
        EditorEventListFragment fragment = (EditorEventListFragment)getFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null)
        {
            // update event, this rewrite event in eventList
            fragment.dataWrapper.updateEvent(event);

            boolean newEvent = ((newEventMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                                (newEventMode == EditorEventListFragment.EDIT_MODE_DUPLICATE));
            fragment.updateListView(event, newEvent);
        }
        onRestartEventPreferences(event, newEventMode);
    }

    public void onRestartEventPreferences(Event event, int newEventMode) {
        if (mTwoPane) {
            if ((newEventMode != EditorEventListFragment.EDIT_MODE_INSERT) &&
                (newEventMode != EditorEventListFragment.EDIT_MODE_DUPLICATE))
            {
                // restart event preferences fragmentu
                Bundle arguments = new Bundle();
                arguments.putLong(GlobalData.EXTRA_EVENT_ID, event._id);
                arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, editModeEvent);
                arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
                EventPreferencesFragment fragment = new EventPreferencesFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.editor_detail_container, fragment, "EventPreferencesFragment").commit();
            }
            else
            {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }
        }
        else
        {
            SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);

            if ((newEventMode != EditorEventListFragment.EDIT_MODE_INSERT) &&
                (newEventMode != EditorEventListFragment.EDIT_MODE_DUPLICATE))
            {
                Editor editor = preferences.edit();
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_RESET_EVENT);
                editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, event._id);
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeEvent);
                editor.commit();
            }
            else
            {
                Editor editor = preferences.edit();
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_REMOVE);
                editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_DATA_ID, event._id);
                editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editModeEvent);
                editor.commit();
            }
        }
    }

    @Override
    public void onShowActionModeInEventPreferences() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onShowActionModeInProfilePreferences() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onHideActionModeInEventPreferences() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onHideActionModeInProfilePreferences() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }


    public static ApplicationsCache getApplicationsCache()
    {
        return applicationsCache;
    }

    public static void createApplicationsCache()
    {
        if ((!savedInstanceStateChanged) || (applicationsCache == null))
        {
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache =  new ApplicationsCache();
        }
    }

    public static void createContactsCache()
    {
        if ((!savedInstanceStateChanged) || (contactsCache == null))
        {
            if (contactsCache != null)
                contactsCache.clearCache(true);
            contactsCache =  new ContactsCache();
        }
    }

    public static ContactsCache getContactsCache()
    {
        return contactsCache;
    }

    public static void createContactGroupsCache()
    {
        if ((!savedInstanceStateChanged) || (contactGroupsCache == null))
        {
            if (contactGroupsCache != null)
                contactGroupsCache.clearCache(true);
            contactGroupsCache =  new ContactGroupsCache();
        }
    }

    public static ContactGroupsCache getContactGroupsCache()
    {
        return contactGroupsCache;
    }

    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null)
        {
            if (fragment instanceof EditorProfileListFragment)
                return ((EditorProfileListFragment)fragment).dataWrapper;
            else
                return ((EditorEventListFragment)fragment).dataWrapper;
        }
        else
            return null;
    }

    public void setEventsRunStopIndicator()
    {
        if (GlobalData.getGlobalEventsRuning(getApplicationContext()))
        {
            if (GlobalData.getEventsBlocked(getApplicationContext()))
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation);
            else
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running);
        }
        else
            eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stoppped);
    }

    public void refreshGUI()
    {
        setEventsRunStopIndicator();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null)
        {
            if (fragment instanceof EditorProfileListFragment)
                ((EditorProfileListFragment)fragment).refreshGUI();
            else
                ((EditorEventListFragment)fragment).refreshGUI();
        }
    }

    /*
    private void setWindowContentOverlayCompat() {
        if (android.os.Build.VERSION.SDK_INT >= 20) {
            // Get the content view
            View contentView = findViewById(android.R.id.content);

            // Make sure it's a valid instance of a FrameLayout
            if (contentView instanceof FrameLayout) {
                TypedValue tv = new TypedValue();

                // Get the windowContentOverlay value of the current theme
                if (getTheme().resolveAttribute(
                        android.R.attr.windowContentOverlay, tv, true)) {

                    // If it's a valid resource, set it as the foreground drawable
                    // for the content view
                    if (tv.resourceId != 0) {
                        ((FrameLayout) contentView).setForeground(
                                getResources().getDrawable(tv.resourceId));
                    }
                }
            }
        }
    }
    */
}
